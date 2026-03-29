package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Categories.*;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.TopChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.*;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
import it.unipi.MySmartRecipeBook.utils.parameters.Task;
import it.unipi.MySmartRecipeBook.repository.Mongo.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;

import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.ChefUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.unipi.MySmartRecipeBook.dto.ChefRankAnalyticsDTO;


import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

@Service
public class ChefService {


    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;


    private final ChefRepository chefRepository;
    private final ChefNeo4jRepository chefNeo4jRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChefUtilityFunctions chefConvertions;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeMongoRepository;
    private final LowLoadManager lowLoadManager;
    private final IngredientService ingredientService;
    private final MongoTemplate mongoTemplate;

    public ChefService(ChefRepository chefRepository, ChefUtilityFunctions chefConvertions,
                       PasswordEncoder passwordEncoder, AdminRepository adminRepository,
                       RecipeMongoRepository recipeMongoRepository, LowLoadManager lowLoadManager,
                       IngredientService ingredientService, MongoTemplate mongoTemplate, ChefNeo4jRepository chefNeo4jRepository) {
        this.chefRepository = chefRepository;
        this.chefConvertions = chefConvertions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.recipeMongoRepository = recipeMongoRepository;
        this.lowLoadManager = lowLoadManager;
        this.ingredientService = ingredientService;
        this.mongoTemplate = mongoTemplate;
        this.chefNeo4jRepository = chefNeo4jRepository;
    }




    /*--------------- Retrieve chef's informations ----------------*/

    public RegistedUserInfoDTO getByUsername(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        return chefConvertions.chefToChefInfo(chef);
    }


    /*--------------- Change chef's informations ----------------*/
    /* This function allows a chef to change its personal information, in particolar one or more among the following
    fields:
        - Email
        - Password
        - Birthday

     We don't allow a foodie to change his/her username, name and surname for security reasons */

    public RegistedUserInfoDTO updateChef(UpdateChefDTO dto) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if(!chefRepository.existsById(authChef.getId())){
            throw new RuntimeException("Chef not found");
        }

        // Vado a modificare solo le informazioni personali, il resto lo lascio invariato
        Query query = new Query(Criteria.where("id").is(authChef.getId()));

        Update update = new Update();
        if (dto.getEmail() != null && StringUtils.hasText(dto.getEmail()))
            update.set("email", dto.getEmail());

        if (dto.getPassword() != null && StringUtils.hasText(dto.getPassword()))
            update.set("password", passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            update.set("birthdate", dto.getBirthdate());

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        Chef chef = mongoTemplate.findAndModify(query, update, options, Chef.class);

        // Ritorniamo le informazioni aggiornate che verranno mostrate nell'area personale
        return chefConvertions.chefToChefInfo(chef);
    }


    /*----------------- Delete chef's profile ----------------*/

    @Transactional
    public void deleteChef(String chefId) {

        Chef chef = chefRepository.findByUsername(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        // Vengono eliminate tutte le ricette di quello chef (è stato definito un indice sullo chef - compound o
        // semplice non mi ricordo) dalla collection "recipes"
        recipeMongoRepository.deleteAllByChefId(chefId);

        // Viene eliminato lo chef dalla collection degli chef
        chefRepository.delete(chef);

        // Viene aggiunto un task alla coda degli eventi che dovranno essere gestiti successivamente: in particolare
        // la rimozione delle ricette dello chef dai preferiti degli utenti e viene rimossa la ricetta da neo4j
        lowLoadManager.addTask(Task.TaskType.DELETE_CHEF_RECIPE, chefId);

    }


    /*------------------- Add new recipe --------------------*/

    /**
     * Funzione invocata dallo chef per scrivere una nuova ricetta: la ricetta è provvisoria e viene aggiunta alla lista
     * di quelle in attesa di conferma da parte dell'admin
     * @param recipeDTO DTO che si compone di tutti i cambi (tutti obbligatori) inseriti dallo chef al momento della scrittura
     *            della ricetta
     * @return DTO che contiene una preview della ricetta appena inserita per mostrarla allo chef come conferma dell'operazione
     *            correttamente eseguita
     */
    @Transactional
    public ChefPreviewRecipeDTO createRecipe(CreateRecipeDTO recipeDTO) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if(!CATEGORIES.contains(recipeDTO.getCategory())){
            throw new RuntimeException("'" + recipeDTO.getCategory() + "': invalid category");
        }

        if(!DIFFICULTIES.contains(recipeDTO.getDifficulty())){
            throw new RuntimeException("'" + recipeDTO.getDifficulty() + "': invalid difficulty");
        }

        // Controlliamo che gli ingredienti siano presenti nel formato richiesto
        List<IngredientDTO> ingredients = recipeDTO.getIngredients();
        for(IngredientDTO ingredient : ingredients) {
            String ingredientName = ingredient.getName();
            if(!ingredientService.isValidIngredient(ingredientName)){
                throw new RuntimeException("'" + ingredientName + "': invalid ingredient");
            }
        }

        Admin admin = adminRepository.findByUsername("admin");

        if (admin == null) {
            throw new RuntimeException("Admin not found");
        }

        ChefInfoDTO chefDTO = new ChefInfoDTO(chef.getId(), chef.getName(), chef.getSurname());
        // A partire dal DTO creiamo un'istanza dell'entità PendingRecipe per poterla salvare embedded dentro il documento
        // dell'admin
        PendingRecipe savedRecipe = chefConvertions.createBaseRecipe(recipeDTO, chefDTO);

        // Controlliamo che la ricetta non sia già stata inserita tra quella in attesa di approvazione
        if(admin.getRecipesToApprove() != null){
            for(PendingRecipe recipe : admin.getRecipesToApprove()){
                if(recipe.getTitle().equals(recipeDTO.getTitle())){
                    throw new RuntimeException("Recipe already waiting to be approved");
                }
            }
        }

        // Aggiungiamo la ricetta a quelle in attesa di approvazione dell'admin
        adminRepository.addRecipeToApprovals(admin.getId(), savedRecipe);

        // Dobbiamo convertire la ricetta nel formato in cui viene salvata all'interno della collezione degli chef (con
        // il campo numSaves inzializzato a 0
        ChefPendingRecipe chefRecipe = chefConvertions.recipeToChefRecipe(savedRecipe);
        chefRepository.addRecipeToWaiting(chef.getId(), chefRecipe);

        // Allo chef viene mostrata un'anteprima della ricetta inserita nella sezione "in attesa di approvazione"
        return chefConvertions.baseToChefDTO(savedRecipe);

    }


    /*--------------- Delete a recipe  ----------------*/

    @Transactional
    public void deleteRecipe(String recipeId) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        List<ChefRecipeSummary> newRecipes = chef.getNewRecipes();

        if (newRecipes == null) {
            throw new RuntimeException("No recipes found");
        }

        // Rimuoviamo la ricetta dalla collezione "recipes" da Mongo
        if(recipeMongoRepository.deleteRecipeById(recipeId) == 0){
            throw new RuntimeException("Recipe not found");
        }

        // Questa parte non è atomica ma per renderla tale dobbiamo necessariamente usare version o lock
        boolean findRecipe = false;
        for (ChefRecipeSummary recipe : newRecipes) {
            if (recipe.getId().equals(recipeId)) {

                chef.setTotalSaves(chef.getTotalSaves() - recipe.getNumSaves());
                newRecipes.remove(recipe);

                if(chef.getOldRecipes() != null){
                    OldRecipe oldRecipe = chef.getOldRecipes().remove(0);
                    Optional<RecipeMongo> recipeMongo = recipeMongoRepository.findById(oldRecipe.getId());
                    ChefRecipeSummary reducedRecipe = chefConvertions.recipeToChefRecipe(recipeMongo.get());
                    chef.getNewRecipes().add(reducedRecipe);
                }

                findRecipe = true;
                break;
            }
        }

        if(findRecipe == false){
            for(OldRecipe oldRecipe : chef.getOldRecipes()){
                if(oldRecipe.getId().equals(recipeId)){
                    chef.setTotalSaves(chef.getTotalSaves() - oldRecipe.getNumSaves());
                    chef.getOldRecipes().remove(oldRecipe);
                    break;
                }
            }
        }

        for(ChefRecipeSummary popularRecipe : chef.getPopularRecipes()){
            if(popularRecipe.getId().equals(recipeId)){
                chef.getPopularRecipes().remove(popularRecipe);
                break;
            }
        }

        chef.setTotalRecipes(chef.getTotalRecipes() - 1);
        chefRepository.save(chef);

        lowLoadManager.addTask(Task.TaskType.DELETE_RECIPE, recipeId, chef.getId());
    }


    /*---------- Remove a recipe from the list of recipes waiting to be confirmed ------------*/

    @Transactional
    public void removeRecipe(String recipeId) {

        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        // Controlliamo che lo chef abbia delle ricette che sono in attesa di essere confermate
        if(chef.getRecipesToConfirm() == null){
            throw new RuntimeException("No recipes waiting to be confirmed");
        }

        ObjectId chefObjectId = new ObjectId(chef.getId());
        // Troviamo la ricetta da rimuovere tra quelle in attesa di conferma
        boolean recipeFound = chefRepository.removeRecipeFromWaiting(chefObjectId, recipeId) > 0;



        // Se la ricetta è stata trovata va rimossa anche dalla lista delle ricette in attesa di conferma dell'admin
        if(recipeFound){

            Admin admin = adminRepository.findByUsername("admin");
            if(admin == null){
                throw new RuntimeException("Admin not found");
            }

            if(admin.getRecipesToApprove() == null){
                throw new RuntimeException("No recipes waiting to be approved");
            }

            adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId);
        }
    }



    /*------------------- Show recipe --------------------*/
    public SliceRecipeDTO showRecipes (int pageNumber){

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if(pageNumber <= 0){
            throw new RuntimeException("Invalid parameters");
        }


        int start = (pageNumber-1)*pageSizeChef;
        int end = pageNumber*pageSizeChef;
        List<ChefPreviewRecipeDTO> content;
        boolean hasPrevious = true;
        // Se richiediamo la prima pagina ordinata per data (quella che viene mostrata di default) non è necessario
            // fare un altro accesso al DB, abbiamo già tutte le informazioni memorizzate dentro il documento dello chef
        if(pageNumber <= 3){

            if (chef.getNewRecipes() == null || chef.getNewRecipes().isEmpty()) {
                return new SliceRecipeDTO(null, false, false);
            }

            content = chefConvertions.ChefListToSummaryList(chef.getNewRecipes().subList(start, end));
            hasPrevious = pageNumber == 1 ? false :  true;
        }
            // Se la pagina non è la prima o il filtro non è quello per data, dobbiamo accedere direttamente al DB, che
            // sfrutta l'indice secondario definito sull'id dello chef della collection "recipes"
        else{

            List<OldRecipe> oldRecipes = chef.getOldRecipes().subList(start, end);
            List<String> ids = oldRecipes.stream().map(OldRecipe::getId).toList();
            List<RecipeMongo> recipes = recipeMongoRepository.findByIdIn(ids);
            content = chefConvertions.MongoListToChefPreview(recipes);
        }

        boolean hasNext = (chef.getTotalRecipes() > end) ? true : false;
        return  new SliceRecipeDTO(content, hasNext, hasPrevious);
    }

    public SliceRecipeDTO showPopularRecipes(int pageNumber){

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if(pageNumber <= 0){
            throw new RuntimeException("Invalid parameters");
        }

        if(chef.getPopularRecipes() == null || chef.getPopularRecipes().isEmpty()) {
            throw new RuntimeException("No popular recipes");
        }

        int start = (pageNumber-1)*pageSizeChef;
        int end = pageNumber*pageSizeChef;

        boolean hasPrevious = pageNumber == 1 ? false : true;
        boolean hasNext = (chef.getPopularRecipes().size() > end) ? true : false;

        List<ChefRecipeSummary> chefList = chef.getPopularRecipes().subList(start, end);
        return  new SliceRecipeDTO(chefList, hasNext, hasPrevious);
    }

    /* Get top 3 chefs per category */

    public List<TopChefDTO> getTopChef() {
        return chefNeo4jRepository.findTop3ChefsByCategory(CATEGORIES);
    }

    /* --------- Bayesian Chef Ranking-------- */
    public List<ChefRankAnalyticsDTO> getChefRankingForFoodie() {
        return chefRepository.ChefBayesianRanking();
    }

}
