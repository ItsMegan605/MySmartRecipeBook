package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.event.TaskToDo;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.event.Task;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Service for managing and executing background tasks when the CPU load is low.
 * It utilizes a thread-safe queue to hold tasks and periodically checks the CPU load
 * before processing them to ensure main application performance is not impacted.
 */
@Service
public class LowLoadManager {
    @Lazy
    private static final Queue<TaskToDo> taskQueue = new ConcurrentLinkedQueue<>();
    private final RecipeMongoRepository recipeMongoRepository;
    private final ChefRepository chefRepository;
    private final RecipeNeo4jRepository recipeNeo4jRepository;

    @Autowired
    @Lazy
    private LowLoadManager lowLoadManager;

    private final RecipeUtilityFunctions recipeUtilityFunctions;

    public LowLoadManager(RecipeMongoRepository recipeMongoRepository, ChefRepository chefRepository,
                          RecipeNeo4jRepository recipeNeo4jRepository, RecipeUtilityFunctions recipeUtilityFunctions) {
        this.recipeMongoRepository = recipeMongoRepository;
        this.chefRepository = chefRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
        this.recipeUtilityFunctions = recipeUtilityFunctions;
    }

    /**
     * Adds a task involving a ChefRecipeSummary to the low-load processing queue.
     * @param type - task type
     * @param recipe - the recipe 
     * @param chefId - id of the chef
     */
    public void addTask (Task.TaskType type, ChefRecipeSummary recipe, String chefId){
        TaskToDo task = new TaskToDo(type, recipe, chefId);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue");
    }

    /**
     * Task linked to specific Chef and Recipe identifiers.
     * @param type - task type
     * @param recipeId - id of the recipe
     * @param chefId - id for the chef
     */
    public void addTask (Task.TaskType type, String recipeId, String chefId){
        TaskToDo task = new TaskToDo(type, recipeId, chefId);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue");
    }

    /**
     * Task to delete and clean DB records with info to delete
     * @param type - task type
     * @param infoToDelete - information to delete
     */
    public void addTask (Task.TaskType type, InfoToDeleteDTO infoToDelete){
        TaskToDo task = new TaskToDo(type, infoToDelete);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue: managing of information to delete");
    }

    /**
     * Adds a generic task linked to a specific recipe identifier to the queue.
     * @param type - task type
     * @param recipeId - id for the recipe
     */
    public void addTask (Task.TaskType type, String recipeId){
        TaskToDo task = new TaskToDo(type, recipeId);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue: a new recipe will be added");
    }

    /**
     * Graph synchronization with Neo4j
     * @param type - task type
     * @param recipe - the recipe
     */
    public void addTask (Task.TaskType type, GraphRecipeDTO recipe){
        TaskToDo task = new TaskToDo(type, recipe);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue: a new node will be created");
    }

    /**
     * Scheduled consumer that evaluates system health
     * and processes the task queue.
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
     * Task dispatcher. Routes the dequeued task to
     * the appropriate internal method based on its TaskType enum.
     * @param task - the task to execute
     */
    private void executeTask(TaskToDo task){

        try{
            switch (task.getType()){
                case SET_COUNTERS_FOODIE_DELETE:
                    lowLoadManager.decrementSavesCounters(task);
                    break;

                case SET_COUNTERS_ADD_FAVOURITE:
                    lowLoadManager.updateChefCountersSaves(task);
                    break;

                case SET_COUNTERS_REMOVE_FAVOURITE:
                    lowLoadManager.updateChefCounters(task);
                    break;

                case CREATE_RECIPE_NEO4J:
                    lowLoadManager.createNeo4jRecipe(task);
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
     * Eventual Consistency operations so that the graph db is updated.
     * This method creates the new Neo4j nodes.
     * @param task - the task to execute
     */
    public void createNeo4jRecipe(TaskToDo task) {

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
     * Batch decrement operation for recipe "saves" counters, when a foodie deltes its profile .
     * @param task - the task to execute
     */
    @Transactional
    public void decrementSavesCounters(TaskToDo task){

        System.out.println("Decrementing Saves Counters");
        List<String> recipesId = task.getInfoToDelete().getRecipeIds();

        if(recipesId != null) {
            for (String recipeId : recipesId) {
                recipeMongoRepository.updateSavesCounter(recipeId, -1);
            }
        }

        Map<String, List<String>> recipesByChefId = task.getInfoToDelete().getChefRecipeList();

        recipesByChefId.forEach((chefId, chefRecipes) -> {
            Chef targetChef = chefRepository.findById(chefId)
                    .orElseThrow(() -> new NoSuchElementException("Chef not found"));

            for (String recipeId : chefRecipes) {

                boolean found = false;

                for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
                    if(recipe.getId().equals(recipeId)){
                        found = true;
                        recipe.setNumSaves(recipe.getNumSaves()-1);
                        targetChef.setTotalSaves(targetChef.getTotalSaves()-1);
                        break;
                    }
                }

                if(!found){ //if not in the new recipes, look in the old ones
                    for(String oldRecipeId : targetChef.getOldRecipes()){
                        if(oldRecipeId.equals(recipeId)){
                            found = true;
                            targetChef.setTotalSaves(targetChef.getTotalSaves()-1);
                            break;
                        }
                    }
                }

                if(!found){
                    throw new NoSuchElementException("No recipe found");
                }


                for(ChefRecipeSummary recipe : targetChef.getPopularRecipes()){
                    if(recipe.getId().equals(recipeId)){
                        recipe.setNumSaves(recipe.getNumSaves()-1);

                        if(recipe.getNumSaves() < 40){
                            targetChef.getPopularRecipes().remove(recipe);
                            break;
                        }
                    }
                }
            }
            chefRepository.save(targetChef);
        });

    }

    /**
     * Decrements the popularity metrics when a foodie
     * removes a recipe from their favorites.
     * @param task - the task to execute
     */
    @Transactional
    public void updateChefCounters(TaskToDo task) {

        System.out.println("Update Chef Counters: removing from favorites");

        Chef targetChef = chefRepository.findById(task.getChefId())
                        .orElseThrow(() -> new NoSuchElementException("Chef not found"));
        targetChef.setTotalSaves(targetChef.getTotalSaves()-1);

        boolean found = false;
        for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
            if(recipe.getId().equals(task.getRecipeId())){
                found = true;
                recipe.setNumSaves(recipe.getNumSaves()-1);
                break;
            }
        }

        if(!found){
            for(String oldRecipeId : targetChef.getOldRecipes()){
                if(oldRecipeId.equals(task.getRecipeId())){
                    found = true;
                    break;
                }
            }
        }

        if(!found){
            throw new NoSuchElementException("No recipe found");
        }

        for(ChefRecipeSummary recipe : targetChef.getPopularRecipes()){
            if(recipe.getId().equals(task.getRecipeId())){
                recipe.setNumSaves(recipe.getNumSaves()-1);

                if(recipe.getNumSaves() < 40){
                    targetChef.getPopularRecipes().remove(recipe);
                    break;
                }
            }
        }

        chefRepository.save(targetChef);
        //Updating the total saves in recipe's collection
        recipeMongoRepository.updateSavesCounter(task.getRecipeId(), -1);
    }

    /**
     * Increase popularity counter (num_saves)
     * for the chef when a foodie adds a recipe to its favorites
     * @param task - the task to execute
     */
    @Transactional
    public void updateChefCountersSaves(TaskToDo task) {

        System.out.println("Update Chef Counters: increasing saves numbers");

        Chef targetChef = chefRepository.findById(task.getChefId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));
        int totSaves = targetChef.getTotalSaves() == null ? 0 : targetChef.getTotalSaves();
        targetChef.setTotalSaves(totSaves+1);

        boolean found = false;
        for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
            if(recipe.getId().equals(task.getRecipeId())){
                int oldNumSaves = recipe.getNumSaves() == null ? 0 : recipe.getNumSaves();
                recipe.setNumSaves(oldNumSaves+1);
                found = true;
                break;
            }
        }

        if(!found){
            for(String recipeId : targetChef.getOldRecipes()){
                if(recipeId.equals(task.getRecipeId())){
                    found = true;
                    break;
                }
            }
        }

        if(!found){
            throw new NoSuchElementException("No recipe found");
        }

        RecipeMongo recipe = recipeMongoRepository.findById(task.getRecipeId())
                        .orElseThrow(() -> new NoSuchElementException("No recipe found"));
        int numSaves = recipe.getNumSaves() == null ? 0 : recipe.getNumSaves();
        recipe.setNumSaves(numSaves+1);
        recipeMongoRepository.save(recipe);


        if(recipe.getNumSaves() == 40){
            ChefRecipeSummary recipeToAdd = recipeUtilityFunctions.recipeToChefRecipe(recipe);
            targetChef.getPopularRecipes().add(recipeToAdd);
            targetChef.getPopularRecipes().sort(Comparator.comparing(ChefRecipeSummary::getNumSaves).reversed());
        }
        else if (recipe.getNumSaves() > 40){
            for (ChefRecipeSummary popularRecipe : targetChef.getPopularRecipes()) {
                if (popularRecipe.getId().equals(recipe.getId())) {
                    popularRecipe.setNumSaves(popularRecipe.getNumSaves()+1);
                    break;
                }
            }
            targetChef.getPopularRecipes().sort(Comparator.comparing(ChefRecipeSummary::getNumSaves).reversed());
        }
        chefRepository.save(targetChef);
    }

    /**
     * Deletes all recipes associated with a specific chef from Neo4j.
     * @param chefId - chef id
     */
    public void deleteChefRecipes(String chefId){
        System.out.println("Deleting Chef Recipes");

        recipeNeo4jRepository.deleteChef(chefId); //neo4j cleaning

        /* Cleanup on Redis - this is not done eagerly; deletion occurs lazily when a cache miss/invalid state is encountered */    }

    /**
     * Removes the recipe node and its corresponding
     * edges/relationships from Neo4j.
     * @param recipeId - id of the recipe
     */
    public void deleteRecipe(String recipeId){

        System.out.println("Deleting Recipe: " + recipeId);
        recipeNeo4jRepository.deleteRecipeById(recipeId);

    }
}
