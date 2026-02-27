package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.enums.Categories.*;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Ingredient;
import it.unipi.MySmartRecipeBook.model.Mongo.*;
import it.unipi.MySmartRecipeBook.utils.enums.Task;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;

import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.ChefUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Service
public class ChefService {

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;


    private final ChefRepository chefRepository;
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
                       IngredientService ingredientService, MongoTemplate mongoTemplate) {
        this.chefRepository = chefRepository;
        this.chefConvertions = chefConvertions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.recipeMongoRepository = recipeMongoRepository;
        this.lowLoadManager = lowLoadManager;
        this.ingredientService = ingredientService;
        this.mongoTemplate = mongoTemplate;
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
        if (dto.getEmail() != null)
            update.set("email", dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank())
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

        Chef chef = chefRepository.findById(chefId)
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

    @Transactional
    public ChefPreviewRecipeDTO createRecipe(CreateRecipeDTO dto) {

        // Controlliamo che gli ingredienti siano presenti nel formato richiesto
        List<Ingredient> ingredients = dto.getIngredients();
        for(Ingredient ingredient : ingredients) {
            String ingredientName = ingredient.getName();
            if(!ingredientService.isValidIngredient(ingredientName)){
                throw new RuntimeException("'" + ingredientName + "': invalid ingredient");
            }
        }

        /* We add the entire recipe to the admin list of recipes waiting to be approved */
        Admin admin = adminRepository.findByUsername("admin");

        if (admin == null) {
            throw new RuntimeException("Admin not found");
        }

        // A partire dal DTO creiamo un'istanza dell'entità BaseRecipe per poterla salvare embedded dentro il documento
        // dell'admin
        BaseRecipe savedRecipe = chefConvertions.createBaseRecipe(dto);

        // Controlliamo che la ricetta non sia già stata inserita tra quella in attesa di approvazione
        if(admin.getRecipesToApprove() != null){
            for(BaseRecipe recipe : admin.getRecipesToApprove()){
                if(recipe.getTitle().equals(dto.getTitle())){
                    throw new RuntimeException("Recipe already waiting to be approved");
                }
            }
        }

        // Aggiungiamo la ricetta a quelle in attesa di approvazione dell'admin
        adminRepository.addRecipeToApprovals(admin.getId(), savedRecipe);

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        // Dobbiamo convertire la ricetta nel formato in cui viene salvata all'interno della collezione degli chef (con
        // il campo numSaves inzializzato a 0
        ChefRecipe chefRecipe = chefConvertions.recipeToChefRecipe(savedRecipe);
        chefRepository.addRecipeToWaiting(chef.getId(), chefRecipe);

        // Allo chef viene mostrata un'anteprima della ricetta inserita nella sezione "in attesa di approvazione"
        return chefConvertions.baseToChefDTO(savedRecipe);

    }


    /*--------------- Delete a recipe  ----------------*/

    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public void deleteRecipe(String recipeId) {

        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
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
        for (ChefRecipeSummary recipe : newRecipes) {
            if (recipe.getId().equals(recipeId)) {

                Pageable pageable = PageRequest.of(0, pageSizeChef, Sort.by("creationDate").descending());
                Slice<RecipeMongo> matchSlice = recipeMongoRepository.findByChef_Id(chef1.getId(), pageable);
                List<RecipeMongo> matchRecipes = matchSlice.getContent();

                List<ChefRecipeSummary> recipesToSave = chefConvertions.MongoListToChefListSummary(matchRecipes);
                chef.setNewRecipes(recipesToSave);
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


        // Troviamo la ricetta da rimuovere tra quelle in attesa di conferma
        boolean recipeFound = chefRepository.removeRecipeFromWaiting(chef.getId(), recipeId) > 0;



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
    @Transactional
    public Slice<ChefPreviewRecipeDTO> showRecipes (String filter, int pageNumber){

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if(pageNumber <= 0 || !CHEF_FILTERS.contains(filter)){
            throw new RuntimeException("Invalid parameters");
        }


        Pageable pageable = null;
        if(filter.equals("date")){

            // Se richiediamo la prima pagina ordinata per data (quella che viene mostrata di default) non è necessario
            // fare un altro accesso al DB, abbiamo già tutte le informazioni memorizzate dentro il documento dello chef
            if(pageNumber == 1){

                if (chef.getNewRecipes() == null || chef.getNewRecipes().isEmpty()) {
                    return new SliceImpl<>(new ArrayList<>(), PageRequest.of(pageNumber - 1, pageSizeChef), false);
                }

                List<ChefPreviewRecipeDTO> content = chefConvertions.ChefListToSummaryList(chef.getNewRecipes());
                pageable = PageRequest.of(pageNumber - 1, pageSizeChef);
                boolean hasNext = (chef.getTotalRecipes() > pageSizeChef) ? true : false;

                return  new SliceImpl<>(content, pageable, hasNext);
            }

            // Se la pagina non è la prima o il filtro non è quello per data, dobbiamo accedere direttamente al DB, che
            // sfrutta l'indice secondario definito sull'id dello chef della collection "recipes"
            else{
                pageable = PageRequest.of(pageNumber - 1, pageSizeChef,
                        Sort.by("creationDate").descending());
            }
        }
        else if(filter.equals("popularity")){
            pageable = PageRequest.of(pageNumber - 1, pageSizeChef,
                    Sort.by("numSaves").descending());
        }
        else{
            throw new RuntimeException("Invalid filter");
        }

        // Recuperiamo le ricette di interesse (le convertiamo nel formato ridotto dell'anteprima)
        Slice<RecipeMongo> recipesPage = recipeMongoRepository.findByChef_Id(chef.getId(), pageable);
        List<ChefPreviewRecipeDTO> content = chefConvertions.MongoListToChefPreview(recipesPage.getContent());
        boolean hasNext = (chef.getTotalRecipes() > pageSizeChef*pageNumber) ? true : false;

        return  new SliceImpl<>(content, pageable, hasNext);
    }

}
