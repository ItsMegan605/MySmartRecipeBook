package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.recipe.StandardRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.*;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import org.springframework.beans.factory.annotation.Value;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.RecipeConvertions;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.model.ReducedChef;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    @Value("${app.recipe.pag-size-title:5}")
    private Integer pageSizeTitle;

    @Value("${app.recipe.pag-size-category:10}")
    private Integer pageSizeCategory;

    @Value("${app.recipe.pag-size-home:10}")
    private Integer pageSizeHome;

    @Value("${app.recipe.pag-size-chef:10}")
    private Integer pageSizeChef;

    private static final List<String> VALID_CATEGORIES = List.of(
            "vegan",
            "dairy-free",
            "gluten-free",
            "main-course",
            "second-course",
            "dessert"
    );

    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeRepository;
    private final RecipeConvertions convertions;
    private final ChefRepository chefRepository;
    public RecipeService(RecipeMongoRepository recipeRepository, RecipeConvertions convertions,
                         ChefRepository chefRepository, AdminRepository adminRepository) {
        this.recipeRepository = recipeRepository;
        this.convertions = convertions;
        this.chefRepository = chefRepository;
        this.adminRepository = adminRepository;
    }


    public ChefPreviewRecipeDTO createRecipe(CreateRecipeDTO dto) {

        Admin admin = adminRepository.findFirstBy();

        if (admin == null) {
            throw new RuntimeException("Admin not found");
        }

        RecipeMongo savedRecipe = createRecipeMongo(dto);

        if(admin.getRecipesToApprove() == null){
            admin.setRecipesToApprove(new ArrayList<>());
        }

        admin.getRecipesToApprove().add(savedRecipe);
        adminRepository.save(admin);

        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        ChefRecipe chefRecipe = convertions.RecipeMongoToChefRecipe(savedRecipe);

        if(chef.getRecipesToConfirm() == null){
            chef.setRecipesToConfirm(new ArrayList<>());
        }

        chef.getRecipesToConfirm().add(chefRecipe);
        chefRepository.save(chef);

        return convertions.EntityToChefDto(savedRecipe);

    }

    private RecipeMongo createRecipeMongo(CreateRecipeDTO dto){

        RecipeMongo recipe = new RecipeMongo();
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setPresentation(dto.getPresentation());
        recipe.setImageURL(dto.getImageURL());
        recipe.setIngredients(dto.getIngredients());
        recipe.setCreationDate(LocalDateTime.now());

        ReducedChef chef = new ReducedChef();
        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        chef.setMongoId(chef1.getId());
        chef.setName(chef1.getName());
        chef.setSurname(chef1.getSurname());

        recipe.setChef(chef);
        return recipeRepository.save(recipe);
    }



    public StandardRecipeDTO getRecipeById(String id){

        RecipeMongo full_recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        StandardRecipeDTO standardRecipeDTO = convertions.EntityToDto(full_recipe);
        return standardRecipeDTO;
    }

    public void deleteRecipe(String recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }
        recipeRepository.deleteById(recipeId);
        /* Manca l'eliminazione da Neo4j e bisogna vedere se anche da Redis*/
    }

    public List<UserPreviewRecipeDTO> getRecipeByTitle(String title, Integer pageNumber){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeTitle);
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByTitleContainingIgnoreCase(title, pageable);
        if (matching_recipes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        List<UserPreviewRecipeDTO> recipes = new ArrayList<>();
        for (RecipeMongo full_recipe : matching_recipes){
            UserPreviewRecipeDTO recipeDTO = convertions.EntityToUserDto(full_recipe);
            recipes.add(recipeDTO);
        }
        return recipes;
    }

    public List<UserPreviewRecipeDTO> getNewestRecipe (Integer pageNumber){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeHome, Sort.by("creationDate").descending());
        Page<RecipeMongo> pageResult = recipeRepository.findAll(pageable);

        List<UserPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: pageResult.getContent()){
            UserPreviewRecipeDTO recipeDTO = convertions.EntityToUserDto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }

    public List<UserPreviewRecipeDTO> getByCategory (Integer pageNumber, String filter){

        if(pageNumber <= 0 || !VALID_CATEGORIES.contains(filter)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeCategory);
        Slice<RecipeMongo> matching_list = recipeRepository.findByCategory(filter, pageable);


        List<UserPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: matching_list){
            UserPreviewRecipeDTO recipeDTO = convertions.EntityToUserDto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }

    /* Per ora sono stati ordinati per data ma andrebbero ordinate per popolarità*/
    public List<ChefPreviewRecipeDTO> getChefRecipePage(Integer pageNumber, String chefName){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeChef, Sort.by("creationDate").descending());
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByChefName(chefName, pageable);

        if (matching_recipes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        List<ChefPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: matching_recipes){
            ChefPreviewRecipeDTO recipeDTO = convertions.EntityToChefDto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }
}

