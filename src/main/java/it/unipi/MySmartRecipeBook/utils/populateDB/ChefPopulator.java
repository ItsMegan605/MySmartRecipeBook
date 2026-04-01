package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
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

import java.util.ArrayList;
import java.util.List;

@Order(3)
@Component
public class ChefPopulator implements CommandLineRunner {

    @Value("${app.recipe.do-chef-recipes:false}")
    private boolean doChefRecipes;

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;

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
    public void run(String... args) {
        if (!doChefRecipes) return;

        System.out.println("Starting Chef population");
        List<Chef> chefs = chefRepository.findAll();

        for (Chef chef : chefs) {
            if (chef.getUsername().equals("admin")) continue;

            // Query 1 - totalRecipes
            int totalRecipes = recipeRepository.countByChef(chef.getId());

            // Query 2 - totalSaves
            Integer totalSaves = recipeRepository.getTotalSaves(chef.getId());
            //if (totalSaves == null) totalSaves = 0;

            // Query 3 - newRecipes: le prime pageSizeChef*3 per data decrescente
            Pageable newPageable = PageRequest.of(0, pageSizeChef * 3,
                    Sort.by("creation_date").descending());
            Slice<RecipeMongo> newSlice = recipeRepository.findByChef_Id(chef.getId(), newPageable);
            List<ChefRecipeSummary> newRecipes = chefUtils.MongoListToChefListSummary(newSlice.getContent());

            // Query 4 - popularRecipes: top popularSize per num_saves, filtrate in memoria >= 40
            Pageable popularPageable = PageRequest.of(0, Integer.MAX_VALUE,
                    Sort.by("num_saves").descending());
            Slice<RecipeMongo> popularSlice = recipeRepository.findByChef_Id(chef.getId(), popularPageable);
            List<ChefRecipeSummary> popularRecipes = chefUtils.MongoListToChefListSummary(
                    popularSlice.getContent().stream()
                            .filter(r -> r.getNumSaves() != null && r.getNumSaves() >= 40)
                            .toList()
            );

            // Query 5 - oldRecipes: dalla pageSizeChef*3+1 in poi, solo id + num_saves
            List<OldRecipe> oldRecipes = new ArrayList<>();
            if (totalRecipes > pageSizeChef * 3) {
                Pageable oldPageable = PageRequest.of(1, pageSizeChef * 3,
                        Sort.by("creation_date").descending());
                Slice<RecipeMongo> oldSlice = recipeRepository.findByChef_Id(chef.getId(), oldPageable);
                oldRecipes = oldSlice.getContent().stream()
                        .map(r -> new OldRecipe(r.getId(), r.getNumSaves()))
                        .toList();
            }

            chefRepository.addChefNewSaved(chef.getId(), totalRecipes, totalSaves,
                    newRecipes, oldRecipes, popularRecipes);

            System.out.println("Finished Chef " + chef.getUsername() + " population");
        }

        System.out.println("Finished Chefs population");
    }
}