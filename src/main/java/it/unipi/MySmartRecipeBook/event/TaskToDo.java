package it.unipi.MySmartRecipeBook.event;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.utils.parameters.Task;
import lombok.Getter;

/**
 * Task manager
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
     * Task for info to delate
     * @param type
     * @param infoToDelete
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
     *
     * @param type
     * @param recipeMongo
     * @param chefId
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
     *
     * @param type
     * @param recipeId
     * @param chefId
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
     *
     * @param type
     * @param recipeId
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
     *
     * @param type
     * @param recipe
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
