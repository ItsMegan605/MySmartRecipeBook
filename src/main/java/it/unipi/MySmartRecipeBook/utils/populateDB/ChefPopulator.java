package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.ChefUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Order(2)
@Component
public class ChefPopulator implements CommandLineRunner{

    @Value("${app.recipe.do-chef-recipes:false}")
    private boolean doChefRecipes;

    private final ChefRepository chefRepository;
    private final RecipeMongoRepository recipeRepository;
    private final ChefUtilityFunctions chefUtils;

    public ChefPopulator(ChefRepository chefRepository, RecipeMongoRepository recipeRepository,
                         ChefUtilityFunctions chefUtils) {
        this.chefRepository = chefRepository;
        this.recipeRepository = recipeRepository;
        this.chefUtils = chefUtils;
    }

    @Override
    public  void run(String... args){
        if(!doChefRecipes){
            return;
        }

        System.out.println("Starting Chef population");
        List<Chef> chefs = chefRepository.findAll();

        for(Chef chef : chefs){
            if(chef.getUsername().equals("admin")){
                continue;
            }
            int totalRecipes = recipeRepository.countByChef(chef.getId());

            Pageable pageable = PageRequest.of(0, 5, Sort.by("creation_date").descending());
            Slice<RecipeMongo> sliceMatchedRecipes = recipeRepository.findByChef_Id(chef.getId(), pageable);
            List<RecipeMongo> recipesList = sliceMatchedRecipes.getContent();
            List<ChefRecipeSummary> recipes = chefUtils.MongoListToChefListSummary(recipesList);

            Integer totSaves = recipeRepository.getTotalSaves(chef.getId());
            chefRepository.addChefNewSaved(chef.getId(), totalRecipes, totSaves, recipes);
            System.out.println("Finished Chef " + chef.getUsername() + " population");
        }

        System.out.println("Finished Chefs population");
    }
}
