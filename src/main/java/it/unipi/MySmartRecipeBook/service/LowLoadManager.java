package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.event.TaskToDo;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.utils.parameters.Task;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
@Service
public class LowLoadManager {

    /* Tipo di coda che è utile per evitare problemi di sincronizzazione con i thread */
    private static final Queue<TaskToDo> taskQueue = new ConcurrentLinkedQueue<>();
    private final RecipeMongoRepository recipeMongoRepository;
    private final ChefRepository chefRepository;
    private final RecipeNeo4jRepository recipeNeo4jRepository;

    public LowLoadManager(RecipeMongoRepository recipeMongoRepository, ChefRepository chefRepository,
                          RecipeNeo4jRepository recipeNeo4jRepository) {
        this.recipeMongoRepository = recipeMongoRepository;
        this.chefRepository = chefRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    /**
     *
     * @param type
     * @param recipe
     * @param chefId
     */
    public void addTask (Task.TaskType type, ChefRecipeSummary recipe, String chefId){
        TaskToDo task = new TaskToDo(type, recipe, chefId);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    /**
     *
     * @param type
     * @param recipeId
     * @param chefId
     */
    public void addTask (Task.TaskType type, String recipeId, String chefId){
        TaskToDo task = new TaskToDo(type, recipeId, chefId);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    /**
     *
     * @param type
     * @param infoToDelete
     */
    public void addTask (Task.TaskType type, InfoToDeleteDTO infoToDelete){
        TaskToDo task = new TaskToDo(type, infoToDelete);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    /**
     *
     * @param type
     * @param recipeId
     */
    public void addTask (Task.TaskType type, String recipeId){
        TaskToDo task = new TaskToDo(type, recipeId);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    /**
     *
     * @param type
     * @param recipe
     */
    public void addTask (Task.TaskType type, GraphRecipeDTO recipe){
        TaskToDo task = new TaskToDo(type, recipe);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    /**
     *
     */
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

    /**
     *
     * @param task
     */
    private void executeTask(TaskToDo task){

        try{
            switch (task.getType()){
                case SET_COUNTERS_FOODIE_DELETE:
                    decrementSavesCounters(task);
                    break;

                case SET_COUNTERS_ADD_FAVOURITE:
                    updateChefCountersSaves(task);
                    break;

                case SET_COUNTERS_REMOVE_FAVOURITE:
                    updateChefCounters(task);
                    break;

                case CREATE_RECIPE_NEO4J:
                    createNeo4jRecipe(task);
                    break;

                case DELETE_CHEF_RECIPE:
                    deleteChefRecipes(task.getChefId());
                    break;

                case DELETE_RECIPE:
                    deleteRecipe(task.getRecipeId());
                    break;

                default:
                    System.out.println("Invalid task type");
            }
        }
        catch (Exception e){
            System.err.println("Error occurred while executing the task");
        }
    }

    /**
     *
     * @param task
     */
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

    /**
     *
     * @param task
     */
    private void decrementSavesCounters(TaskToDo task){

        System.out.println("Decrement Saves Counters");
        List<String> recipesId = task.getInfoToDelete().getRecipeIds();

        if(recipesId != null) {
            for (String recipeId : recipesId) {
                recipeMongoRepository.updateSavesCounter(recipeId, -1);
            }
        }

        Map<String, List<String>> recipesByChefId = task.getInfoToDelete().getChefRecipeList();

        recipesByChefId.forEach((chefId, chefRecipes) -> {
            Chef targetChef = chefRepository.findById(chefId).get();

            for (String recipeId : chefRecipes) {

                Integer numSaves = null;
                for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
                    if(recipe.getId().equals(recipeId)){
                        recipe.setNumSaves(recipe.getNumSaves()-1);
                        targetChef.setTotalSaves(targetChef.getTotalSaves()-1);
                        numSaves = recipe.getNumSaves();
                        break;
                    }
                }
                if(numSaves == null){
                    for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
                        if(recipe.getId().equals(recipeId)){
                            recipe.setNumSaves(recipe.getNumSaves()-1);
                            targetChef.setTotalSaves(targetChef.getTotalSaves()-1);
                            numSaves = recipe.getNumSaves();
                            break;
                        }
                    }
                }

                if(numSaves > 40){
                    for(ChefRecipeSummary recipe : targetChef.getPopularRecipes()){
                        if(recipe.getId().equals(recipeId)){
                            recipe.setNumSaves(recipe.getNumSaves()-1);
                            targetChef.getPopularRecipes().sort(Comparator.comparing(ChefRecipeSummary::getNumSaves).reversed());
                            break;
                        }
                    }
                }
                else if(numSaves == 40){
                    for(ChefRecipeSummary recipe : targetChef.getPopularRecipes()){
                        if(recipe.getId().equals(recipeId)){
                            targetChef.getPopularRecipes().remove(recipe.getId());
                            break;
                        }
                    }
                }
            }
            chefRepository.save(targetChef);
        });

    }

    /**
     *
     * @param task
     */
    private void updateChefCounters(TaskToDo task) {

        System.out.println("Update Chef Counters");

        Chef targetChef = chefRepository.findById(task.getChefId()).get();
        targetChef.setTotalSaves(targetChef.getTotalSaves()-1);

        Integer numSaves = null;
        for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
            if(recipe.getId().equals(task.getRecipeId())){
                numSaves = recipe.getNumSaves();
                recipe.setNumSaves(recipe.getNumSaves()-1);
                break;
            }
        }

        if(numSaves == null){
            for(OldRecipe recipe : targetChef.getOldRecipes()){
                if(recipe.getId().equals(task.getRecipeId())){
                    numSaves = recipe.getNumSaves();
                    recipe.setNumSaves(recipe.getNumSaves()-1);
                    break;
                }
            }
        }

        if(numSaves > 40){
            targetChef.getPopularRecipes().sort(Comparator.comparing(ChefRecipeSummary::getNumSaves).reversed());
        }
        else if(numSaves == 40) {
            targetChef.getPopularRecipes().remove(task.getRecipeMongo());
        }


        /* Aggiorno il numero totale di saves nella collezione delle recipes */
        recipeMongoRepository.updateSavesCounter(task.getRecipeId(), -1);
    }

    /**
     *
     * @param task
     */
    private void updateChefCountersSaves(TaskToDo task) {

        System.out.println("Update Chef Counters");

        Chef targetChef = chefRepository.findById(task.getChefId()).get();
        targetChef.setTotalSaves(targetChef.getTotalSaves()+1);

        Integer numSaves = null;
        for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
            if(recipe.getId().equals(task.getRecipeId())){
                recipe.setNumSaves(recipe.getNumSaves()+1);
                numSaves = recipe.getNumSaves();
                break;
            }
        }

        if(numSaves == null){
            for(OldRecipe recipe : targetChef.getOldRecipes()){
                if(recipe.getId().equals(task.getRecipeId())){
                    recipe.setNumSaves(recipe.getNumSaves()+1);
                    numSaves = recipe.getNumSaves();
                    break;
                }
            }
        }

        if(numSaves > 40){
            targetChef.getPopularRecipes().sort(Comparator.comparing(ChefRecipeSummary::getNumSaves).reversed());
        }
        else if(numSaves == 40) {
            targetChef.getPopularRecipes().add(targetChef.getPopularRecipes().size()-1, task.getRecipeMongo());
        }

        /* Aggiorno il numero totale di saves nella collezione delle recipes */
        recipeMongoRepository.updateSavesCounter(task.getRecipeId(), 1);
    }

    /**
     *
     * @param chefId
     */
    /* In questa funzione c'è la Risk Acceptancec */
    private void deleteChefRecipes(String chefId){
        System.out.println("Delete Chef Recipes");

        /* Pulizia su Neo4j*/
        recipeNeo4jRepository.deleteChef(chefId);

        /* Pulizia su Redis - non viene fatta quando sbattiamo sulla ricetta che non c'è più facciamo l'eliminazione */
    }

    /**
     *
     * @param recipeId
     */
    private void deleteRecipe(String recipeId){

        System.out.println("Delete Recipe");
        recipeNeo4jRepository.deleteRecipeById(recipeId);

    }
}
