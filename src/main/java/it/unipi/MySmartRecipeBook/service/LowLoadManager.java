package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.event.TaskToDo;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.utils.parameters.Task;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class LowLoadManager {

    /* Tipo di coda che è utile per evitare problemi di sincronizzazione con i thread */
    private static final Queue<TaskToDo> taskQueue = new ConcurrentLinkedQueue<>();
    private final RecipeMongoRepository recipeMongoRepository;
    private final ChefRepository chefRepository;
    private final FoodieRepository foodieRepository;
    private final RecipeNeo4jRepository recipeNeo4jRepository;

    public LowLoadManager(RecipeMongoRepository recipeMongoRepository, ChefRepository chefRepository,
                          FoodieRepository foodieRepository, RecipeNeo4jRepository recipeNeo4jRepository) {
        this.recipeMongoRepository = recipeMongoRepository;
        this.chefRepository = chefRepository;
        this.foodieRepository = foodieRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    public void addTask (Task.TaskType type, String recipeId, String chefId){
        TaskToDo task = new TaskToDo(type, recipeId,chefId);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    public void addTask (Task.TaskType type, InfoToDeleteDTO infoToDelete){
        TaskToDo task = new TaskToDo(type, infoToDelete);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    public void addTask (Task.TaskType type, String recipeId){
        TaskToDo task = new TaskToDo(type, recipeId);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    public void addTask (Task.TaskType type, GraphRecipeDTO recipe){
        TaskToDo task = new TaskToDo(type, recipe);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    @Scheduled(fixedDelay = 10000)
    public void taskHandler(){
        if(taskQueue.isEmpty()){
            System.out.println("No task in the queue");
            return;
        }

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuLoad = osBean.getCpuLoad();
        System.out.println("cpuLoad = " + cpuLoad);

        if(cpuLoad < 0.3){

            int processedTasks = 0;
            while(processedTasks < 10 && !taskQueue.isEmpty()){
                TaskToDo task = taskQueue.poll();
                executeTask(task);
                processedTasks++;
            }
        }
    }

    private void executeTask(TaskToDo task){

        try{
            switch (task.getType()){
                case SET_COUNTERS_FOODIE_DELETE:
                    decrementSavesCounters(task);
                    break;

                case SET_COUNTERS_ADD_FAVOURITE:
                    updateChefCounters(task, 1);
                    break;

                case SET_COUNTERS_REMOVE_FAVOURITE:
                    updateChefCounters(task, -1);
                    break;

                case CREATE_RECIPE_NEO4J:
                    createNeo4jRecipe(task);
                    break;

                case DELETE_CHEF_RECIPE:
                    deleteChefRecipes(task.getChefId());
                    break;

                case DELETE_RECIPE:
                    deleteRecipe(task.getRecipeId(), task.getChefId());
                    break;

                default:
                    System.out.println("Invalid task type");
            }
        }
        catch (Exception e){
            System.err.println("Error occurred while executing the task");
        }
    }

    private void createNeo4jRecipe(TaskToDo task) {

        System.out.println("Creating Neo4j recipe");
        List<String> ingredientNames = new ArrayList<>();
        List<IngredientDTO> ingredients = task.getRecipe().getIngredients();

        for(IngredientDTO ingredient : ingredients){
            ingredientNames.add(ingredient.getName());
        }

        recipeNeo4jRepository.createRecipe(
                task.getRecipe().getId(),
                task.getRecipe().getTitle(),
                task.getRecipe().getImgURL(),
                task.getRecipe().getCategory(),
                task.getRecipe().getChefId(),
                ingredientNames
        );

    }


    private void decrementSavesCounters(TaskToDo task){

        System.out.println("Decrement Saves Counters");
        List<String> recipesId = task.getInfoToDelete().getRecipeIds();
        Map<String, Long> chefDecrements = task.getInfoToDelete().getChefDecrements();

        if(recipesId != null) {
            for (String recipeId : recipesId) {
                recipeMongoRepository.updateSavesCounter(recipeId, -1);
            }
        }

        if(chefDecrements != null) {
            for (Map.Entry<String, Long> element : chefDecrements.entrySet()) {
                String chefId = element.getKey();
                int savesToRemove = element.getValue().intValue();

                chefRepository.updateTotalSaves(chefId, -savesToRemove);
            }
        }
    }

    private void updateChefCounters(TaskToDo task, int increment) {

        System.out.println("Update Chef Counters");
        /* Aggiorno il numero totale di ricette salvate dello chef */
        chefRepository.updateTotalSaves(task.getChefId(), increment);

        /* Se la ricette è tra le ultime 5 dello chef aggiorno la copia embedded */
        chefRepository.updateChefCounters(task.getChefId(), task.getRecipeId(), increment);

        /* Aggiorno il numero totale di saves nella collezione delle recipes */
        recipeMongoRepository.updateSavesCounter(task.getRecipeId(), increment);
    }

    /* In questa funzione c'è la Risk Acceptancec */
    private void deleteChefRecipes(String chefId){
        System.out.println("Delete Chef Recipes");
        /* Pulizia su mongo delle ricette salvate dei foodie */
        List<Foodie> foodieList = foodieRepository.findFoodiesWithChefRecipes(chefId);

        for(Foodie foodie : foodieList) {

            /* In questo caso non possiamo usare la struttura con il for o ci verrebbe dato errore se eliminiamo un
            elemento e continuiamo a scorrere la lista */

            if (foodie.getSavedRecipes() != null) {
                foodie.getSavedRecipes().removeIf(r -> r.getChef().getId().equals(chefId));
            }
        }

        if(!foodieList.isEmpty()){
            foodieRepository.saveAll(foodieList);
        }

        /* Pulizia su Neo4j*/
        recipeNeo4jRepository.deleteChef(chefId);

        /* Pulizia su Redis - non viene fatta quando sbattiamo sulla ricetta che non c'è più facciamo l'eliminazione */
    }

    private void deleteRecipe(String recipeId, String chefId){

        System.out.println("Delete Recipe");
        foodieRepository.deleteRecipeFromFoodies(chefId, recipeId);
        recipeNeo4jRepository.deleteRecipeById(recipeId);

    }
}
