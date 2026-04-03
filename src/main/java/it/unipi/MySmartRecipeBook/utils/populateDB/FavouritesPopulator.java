package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.FoodieUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 *
 */
@Order(2)
@Component
public class FavouritesPopulator implements CommandLineRunner {

    @Value("${app.recipe.do-population:false}")
    private boolean doPopulation;

    private final RecipeMongoRepository recipeRepository;
    private final FoodieRepository foodieRepository;
    private final FoodieUtilityFunctions foodieUtils;


    public FavouritesPopulator(RecipeMongoRepository recipeRepository, FoodieRepository foodieRepository,
                               FoodieUtilityFunctions foodieUtils) {
        this.recipeRepository = recipeRepository;
        this.foodieRepository = foodieRepository;
        this.foodieUtils = foodieUtils;
    }

    /**
     *
     * @param args
     */
    // Devo salvare le ricette preferite (in numero compreso tra 0 e 200) per ciascun foodie
    @Override
    public void run(String... args){

        if(!doPopulation){
            return;
        }

        System.out.println("Populating mongo DB");

        List<RecipeMongo> recipes = recipeRepository.findAll();
        List<Foodie> foodies = foodieRepository.findAll();

        if(recipes.isEmpty() || foodies.isEmpty()){
            return;
        }

        int bound = recipes.size() < 200 ?  recipes.size() : 200;
        Random random = new Random();
        for(Foodie foodie : foodies){

            // Scelgo un numero casuale di ricette da salvare tra i preferiti
            int numRecipes = random.nextInt(bound);

            // Mescolo le ricette o prenderei sempre le stesse in ordine

            int addedRecipes = 0;

            Set<Integer> chosenIndices = new HashSet<>();
            List<FoodieRecipeSummary> foodieRecipes = new ArrayList<>();
            List<String> recipesId = new ArrayList<>();

            // Fino a quando non abbiamo raggiunto il numero di preferiti che abbiamo casualmente estratto
            while(addedRecipes < numRecipes){

                // estraggo un numero casuale da 0 al numero di ricette
                int randomIndex = random.nextInt(recipes.size());

                // se quell'indice non è ancora uscito faccio tutte le operazioni del caso
                if(chosenIndices.add(randomIndex)) {

                    RecipeMongo recipe = recipes.get(randomIndex);
                    FoodieRecipeSummary fullRecipe = foodieUtils.entityToReducedRecipe(recipe);
                    foodieRecipes.add(fullRecipe);
                    recipesId.add(recipe.getId());

                    /* Aggiorno il numero totale di saves nella collezione delle recipes */
                    recipeRepository.updateSavesCounter(recipe.getId(), 1);

                    addedRecipes++;
                }

            }
            foodieRepository.addRecipesToFavourites(foodie.getId(), recipesId, foodieRecipes);
            System.out.println("Foodie '" + foodie.getUsername() + "' population completed");
        }

        System.out.println("Population completed");
    }
}
