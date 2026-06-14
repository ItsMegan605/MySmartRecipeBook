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
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
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

    @Autowired
    @Lazy
    private LowLoadManager lowLoadManager;

    private static final Queue<TaskToDo> taskQueue = new ConcurrentLinkedQueue<>();

    private final RecipeMongoRepository recipeMongoRepository;
    private final ChefRepository chefRepository;
    private final RecipeNeo4jRepository recipeNeo4jRepository;
    private final RecipeUtilityFunctions recipeUtilityFunctions;
    private final ChefNeo4jRepository chefNeo4jRepository;

    public LowLoadManager(RecipeMongoRepository recipeMongoRepository, ChefRepository chefRepository,
                          RecipeNeo4jRepository recipeNeo4jRepository, RecipeUtilityFunctions recipeUtilityFunctions,
                          ChefNeo4jRepository chefNeo4jRepository) {
        this.recipeMongoRepository = recipeMongoRepository;
        this.chefRepository = chefRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
        this.recipeUtilityFunctions = recipeUtilityFunctions;
        this.chefNeo4jRepository = chefNeo4jRepository;
    }


    /**
     * Enqueues a background task associated with both a specific recipe and a chef.
     * This is used for operations such as updating popularity counters (both when a recipe is added or removed from favorites).
     * @param type the specific type of task to be executed
     * @param recipeId the unique identifier of the target recipe
     * @param chefId the unique identifier of the associated chef
     */
    public void addTask (Task.TaskType type, String recipeId, String chefId){
        TaskToDo task = new TaskToDo(type, recipeId, chefId);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue");
    }


    /**
     * Enqueues a background task associated with a list of recipe IDs to delete and the corresponding chefs.
     * This is used for the foodie's profile deletion, in order to update chefs' popularity counters.
     * @param type the specific type of task to be executed
     * @param infoToDelete a {@link InfoToDeleteDTO} containing the list of recipe IDs and a map associating each chef with their affected recipes
     */
    public void addTask (Task.TaskType type, InfoToDeleteDTO infoToDelete){
        TaskToDo task = new TaskToDo(type, infoToDelete);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue");
    }


    /**
     * Enqueues a background task associated with a specific recipe.
     * This is used for delete the specified recipe from neo4j when a chef deletes one of its recipes
     * @param type the specific type of task to be executed
     * @param recipeId the unique identifier of the target recipe
     */
    public void addTask (Task.TaskType type, String recipeId){
        TaskToDo task = new TaskToDo(type, recipeId);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue");
    }


    /**
     * Enqueues a background task associated with a specific recipe.
     * This is used to add the specified recipe to neo4j when the admin approves a chef's recipe
     * @param type the specific type of task to be executed
     * @param recipe a {@link GraphRecipeDTO} containing all the information required to add a new recipe on the graph DB
     */
    public void addTask (Task.TaskType type, GraphRecipeDTO recipe){
        TaskToDo task = new TaskToDo(type, recipe);
        taskQueue.add(task);
        System.out.println("Task successfully added to the queue");
    }


    /**
     * Scheduled method executed every 10 seconds to process the background task queue.
     * Tasks are executed only if the system CPU load is below 30%.
     * To keep the application running smoothly, a maximum of 10 tasks are executed
     * at a time. The rest are kept in the queue for the next run.
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
     * Reads the task from the queue and calls the correct method based on its {@link Task.TaskType}.
     * If an error occurs during execution, it is caught and logged to prevent the whole
     * background process from crashing.
     * @param task the {@link TaskToDo} object containing the operation type and the required elements to execute the operation
     */
    private void executeTask(TaskToDo task){

        try{
            switch (task.getType()){
                case SET_COUNTERS_FOODIE_DELETE:
                    lowLoadManager.decrementSavesCounters(task);
                    break;

                case SET_COUNTERS_NEW_FAVOURITE:
                    lowLoadManager.updateChefCountersSaves(task);
                    break;

                case SET_COUNTERS_REMOVE_FAVOURITE:
                    lowLoadManager.decrementChefCounters(task);
                    break;

                case CREATE_RECIPE_NEO4J:
                    lowLoadManager.createNeo4jRecipe(task);
                    break;

                case DELETE_CHEF_PROFILE_NEO4J:
                    deleteChefRecipes(task.getChefId());
                    break;

                case DELETE_RECIPE_NEO4J:
                    deleteRecipe(task.getRecipeId());
                    break;

                default:
                    System.out.println("Invalid task type");
            }
        }
        catch (Exception e){
            System.err.println("Error occurred while executing the task");
            taskQueue.add(task);
        }
    }


    /**
     * Executed when the {@link Task.TaskType} is "SET_COUNTERS_FOODIE_DELETE".
     * This method updates the popularity counters of the affected chefs according to the foodie's profile deletion.
     * @param task a {@link TaskToDo} containing the list of recipes saved by the foodie and a map
     * associating each chef with their corresponding list of saved recipes
     */
    @Transactional
    public void decrementSavesCounters(TaskToDo task){

        System.out.println("Decrementing chef saves counters according to foodie's profile deletion");
        if (task.getInfoToDelete() == null) {
            return;
        }

        List<String> recipesId = task.getInfoToDelete().getRecipeIds();
        if(recipesId != null) {
            for (String recipeId : recipesId) {
                recipeMongoRepository.updateSavesCounter(recipeId, -1);
            }
        }

        Map<String, List<String>> recipesByChefId = task.getInfoToDelete().getChefRecipeList();
        if (recipesByChefId == null) {
            return;
        }

        recipesByChefId.forEach((chefId, chefRecipes) -> {
            Optional<Chef> optTargetChef = chefRepository.findApprovedById(chefId);
            if(optTargetChef.isEmpty()){
                return;
            }

            Chef targetChef = optTargetChef.get();
            for (String recipeId : chefRecipes) {

                boolean found = false;
                int currentTotalSaves = targetChef.getTotalSaves() == null ? 0 : targetChef.getTotalSaves();

                if (targetChef.getNewRecipes() != null && !targetChef.getNewRecipes().isEmpty()) {

                    for (ChefRecipeSummary recipe : targetChef.getNewRecipes()) {
                        if (recipe.getId().equals(recipeId)) {
                            found = true;
                            int currentRecipeSaves = recipe.getNumSaves() == null ? 0 : recipe.getNumSaves();
                            recipe.setNumSaves(Math.max(0, currentRecipeSaves - 1));
                            targetChef.setTotalSaves(Math.max(0, currentTotalSaves - 1));
                            break;
                        }
                    }


                    if (!found && targetChef.getOldRecipes() != null) {
                        for (String oldRecipeId : targetChef.getOldRecipes()) {
                            if (oldRecipeId.equals(recipeId)) {
                                found = true;
                                targetChef.setTotalSaves(Math.max(0, currentTotalSaves - 1));
                                break;
                            }
                        }
                    }

                    if (found && targetChef.getPopularRecipes() != null) {
                        for (ChefRecipeSummary recipe : targetChef.getPopularRecipes()) {
                            if (recipe.getId().equals(recipeId)) {
                                int currentPopSaves = recipe.getNumSaves() == null ? 0 : recipe.getNumSaves();
                                recipe.setNumSaves(Math.max(0, currentPopSaves - 1));

                                if (recipe.getNumSaves() < 40) {
                                    targetChef.getPopularRecipes().remove(recipe);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            chefRepository.save(targetChef);
        });
    }


    /**
     * Executed when the {@link Task.TaskType} is "SET_COUNTERS_REMOVE_FAVOURITE".
     * This method updates the popularity counters of the affected chef when a foodie removes a recipe from its favorites.
     * In addition, updates the recipe's total saves in the recipe collection.
     * @param task a {@link TaskToDo} containing the id of the target recipe and the id of the corresponding chef
     */
    @Transactional
    public void decrementChefCounters(TaskToDo task) {

        System.out.println("Decrement chef saves counters when a foodie removes a recipe from its favourite");

        Optional<Chef> optTargetChef = chefRepository.findApprovedById(task.getChefId());
        if(optTargetChef.isEmpty()){
            return;
        }

        Chef targetChef =  optTargetChef.get();
        int totSaves = targetChef.getTotalSaves() == null ? 0 : targetChef.getTotalSaves();
        targetChef.setTotalSaves(Math.max(0, totSaves-1));

        boolean found = false;
        if(targetChef.getNewRecipes() != null && !targetChef.getNewRecipes().isEmpty()) {

            for (ChefRecipeSummary recipe : targetChef.getNewRecipes()) {
                if (recipe.getId().equals(task.getRecipeId())) {
                    found = true;
                    int oldSaves = recipe.getNumSaves() == null ? 0 : recipe.getNumSaves();
                    recipe.setNumSaves(Math.max(0, oldSaves - 1));
                    break;
                }
            }

            if (!found && targetChef.getOldRecipes() != null) {
                for (String oldRecipeId : targetChef.getOldRecipes()) {
                    if (oldRecipeId.equals(task.getRecipeId())) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                if (targetChef.getPopularRecipes() != null) {
                    for (ChefRecipeSummary recipe : targetChef.getPopularRecipes()) {
                        if (recipe.getId().equals(task.getRecipeId())) {
                            int numSaves = recipe.getNumSaves() == null ? 0 : recipe.getNumSaves();
                            recipe.setNumSaves(Math.max(0, numSaves - 1));

                            if (recipe.getNumSaves() < 40) {
                                targetChef.getPopularRecipes().remove(recipe);
                            }
                            break;
                        }
                    }
                }
            }
        }
        chefRepository.save(targetChef);
        recipeMongoRepository.decrementSavesCounter(task.getRecipeId());
    }


    /**
     * Executed when the {@link Task.TaskType} is "SET_COUNTERS_NEW_FAVOURITE".
     * This method updates the popularity counters of the affected chef when a foodie adds a recipe to its favorites.
     * @param task a {@link TaskToDo} containing the id of the target recipe and the id of the corresponding chef
     */
    @Transactional
    public void updateChefCountersSaves(TaskToDo task) {

        System.out.println("Incrementing chef counters when a foodie adds a recipe to its favourite");

        Optional <Chef> optTargetChef = chefRepository.findApprovedById(task.getChefId());
        if(optTargetChef.isEmpty()){
            return;
        }

        Chef targetChef =  optTargetChef.get();
        int totSaves = targetChef.getTotalSaves() == null ? 0 : targetChef.getTotalSaves();
        targetChef.setTotalSaves(totSaves + 1);

        if(targetChef.getNewRecipes() != null && !targetChef.getNewRecipes().isEmpty()) {
            for(ChefRecipeSummary recipe : targetChef.getNewRecipes()){
                if(recipe.getId().equals(task.getRecipeId())){
                    int oldNumSaves = recipe.getNumSaves() == null ? 0 : recipe.getNumSaves();
                    recipe.setNumSaves(oldNumSaves+1);
                    break;
                }
            }
        }

        Optional<RecipeMongo> optRecipe = recipeMongoRepository.findApprovedById(task.getRecipeId());
        if(optRecipe.isEmpty()){
            return;
        }
        RecipeMongo recipe = optRecipe.get();
        recipeMongoRepository.updateSavesCounter(task.getRecipeId(), 1);

        int newNumSaves = recipe.getNumSaves() == null? 0 : recipe.getNumSaves()+1;
        if(newNumSaves == 40){
            ChefRecipeSummary recipeToAdd = recipeUtilityFunctions.recipeToChefPopular(recipe);
            if(targetChef.getPopularRecipes() == null) {
                targetChef.setPopularRecipes(new ArrayList<>());
            }
            targetChef.getPopularRecipes().add(recipeToAdd);
            targetChef.getPopularRecipes().sort(Comparator.comparing(ChefRecipeSummary::getNumSaves).reversed());
        }
        else if (newNumSaves > 40 && targetChef.getPopularRecipes() != null){
            for (ChefRecipeSummary popularRecipe : targetChef.getPopularRecipes()) {
                if (popularRecipe.getId().equals(recipe.getId())) {
                    popularRecipe.setNumSaves(popularRecipe.getNumSaves() + 1);
                    break;
                }
            }
            targetChef.getPopularRecipes().sort(
                    Comparator.comparing((ChefRecipeSummary r) -> r.getNumSaves() == null ? 0 : r.getNumSaves()).reversed());
        }

        chefRepository.save(targetChef);
    }


    /**
     * Deletes the chef node and all its related recipes from Neo4j after the chef's profile deletion.
     * @param chefId the unique identifier of the chef to remove
     */
    public void deleteChefRecipes(String chefId){

        System.out.println("Deleting Chef and all its recipes from Neo4j");
        chefNeo4jRepository.deleteChef(chefId);
    }


    /**
     * Removes the recipe node and its corresponding relationships from Neo4j after the chef deletes one of its recipes.
     * @param recipeId unique identifier of the recipe to delete
     */
    public void deleteRecipe(String recipeId){

        System.out.println("Deleting recipe from Neo4j");
        recipeNeo4jRepository.deleteRecipeById(recipeId);
    }


    /**
     * Creates a new recipe node on Neo4j after the admin approval of a pending recipe.
     * @param task a {@link TaskToDo} containing the {@link GraphRecipeDTO} with all the details of the recipe to add
     */
    public void createNeo4jRecipe(TaskToDo task) {

        System.out.println("Creating Neo4j recipe after the admin approval");
        if (task.getRecipe() == null) {
            System.out.println("No recipe data found in task, skipping Neo4j creation...");
            return;
        }

        List<String> ingredientNames = new ArrayList<>();
        List<IngredientDTO> ingredients = task.getRecipe().getIngredients();

        if(ingredients != null){
            for(IngredientDTO ingredient : ingredients){
                ingredientNames.add(ingredient.getName());
            }
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
}
