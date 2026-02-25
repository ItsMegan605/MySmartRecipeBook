package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.service.FoodieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Order(1)
@Component
public class FavouritesPopulator implements CommandLineRunner {

    @Value("${app.recipe.do-population:false}")
    private boolean doPopulation;

    private final RecipeMongoRepository recipeRepository;
    private final FoodieRepository foodieRepository;
    private final FoodieService foodieService;


    public FavouritesPopulator(RecipeMongoRepository recipeRepository, FoodieRepository foodieRepository,
                               FoodieService foodieService) {
        this.recipeRepository = recipeRepository;
        this.foodieRepository = foodieRepository;
        this.foodieService = foodieService;
    }

    @Override
    public void run(String... args) throws Exception {

        if(!doPopulation){
            return;
        }

        System.out.println("Populating mongo DB");

        List<RecipeMongo> recipes = recipeRepository.findAll();
        List<Foodie> foodies = foodieRepository.findAll();

        Random random = new Random();
        for(Foodie foodie : foodies){

            // Scelgo un numero casuale di ricette da salvare tra i preferiti
            int numRecipes = random.nextInt(200);

            // Mescolo le ricette o prenderei sempre le stesse in ordine
            Collections.shuffle(recipes);

            int addedRecipes = 0;

            for(RecipeMongo recipe : recipes){

                // Controlliamo se abbiamo raggiunto il numero di ricette che dobbiamo aggiungere per quall'utente
                if(addedRecipes >= numRecipes){
                    break;
                }

                try{
                    foodieService.saveRecipe(foodie.getId(), recipe.getId());
                    addedRecipes++;
                }
                catch(Exception e){
                    // Non facciamo nulla, semplicemente proviamo con la ricetta successiva
                    System.out.println("Failed while saving recipe " + recipe.getTitle() + " (id: " + recipe.getId() + ")");
                }
            }

            System.out.println("Foodie '" + foodie.getUsername() + "' population completed");
        }

        System.out.println("Population completed");
    }
}
