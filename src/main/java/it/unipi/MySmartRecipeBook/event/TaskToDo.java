package it.unipi.MySmartRecipeBook.event;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import lombok.Getter;

/**
 * Task manager class representing a specific task to be executed.
 */
@Getter
public class TaskToDo {

    private final Task.TaskType type;
    private final String recipeId;
    private final String chefId;
    private final InfoToDeleteDTO infoToDelete;
    private final GraphRecipeDTO recipe;
    private final ChefRecipeSummary recipeMongo;

    /**
     * Constructor for tasks with bulk deletion of information.
     * @param type the specific type of task to execute
     * @param infoToDelete the DTO containing the data to be deleted
     */
    public TaskToDo(Task.TaskType type, InfoToDeleteDTO infoToDelete) {
        this.type = type;
        this.recipeId = null;
        this.chefId = null;
        this.infoToDelete = infoToDelete;
        this.recipe = null;
        this.recipeMongo = null;
    }

    /**
     * Constructor for tasks involving the creation or update of a Chef's recipe summary.
     * @param type type of action that must be executed
     * @param recipeMongo the summary of the recipe to be processed
     * @param chefId ID of the chef
     */
    public TaskToDo(Task.TaskType type, ChefRecipeSummary recipeMongo, String chefId) {
        this.type = type;
        this.recipeId = recipeMongo.getId();
        this.chefId = chefId;
        this.infoToDelete = null;
        this.recipe = null;
        this.recipeMongo = recipeMongo;
    }

    /**
     * Constructor for tasks requiring both a recipe ID and a chef ID.
     * @param type type of action that must be executed
     * @param recipeId id of the recipe
     * @param chefId ID of the chef
     */
    public TaskToDo(Task.TaskType type, String recipeId, String chefId) {
        this.type = type;
        this.recipeId = recipeId;
        this.chefId = chefId;
        this.infoToDelete = null;
        this.recipe = null;
        this.recipeMongo = null;
    }

    /**
     * Constructor for tasks requiring only a recipe ID.
     * @param type type of action that must be executed
     * @param recipeId id of the recipe
     */
    public TaskToDo(Task.TaskType type, String recipeId) {
        this.type = type;
        this.recipeId = recipeId;
        this.chefId = null;
        this.infoToDelete = null;
        this.recipe = null;
        this.recipeMongo = null;
    }

    /**
     * Constructor for tasks involving graph database operations for a specific recipe.
     * @param type type of action that must be executed
     * @param recipe the DTO containing the recipe details for the graph
     */
    public TaskToDo(Task.TaskType type, GraphRecipeDTO recipe) {
        this.type = type;
        this.recipeId = null;
        this.chefId = null;
        this.infoToDelete = null;
        this.recipe = recipe;
        this.recipeMongo = null;
    }
}
