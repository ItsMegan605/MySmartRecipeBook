package it.unipi.MySmartRecipeBook.event;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.utils.enums.Task;
import lombok.Getter;

@Getter
public class TaskToDo {

    private final Task.TaskType type;
    private final String recipeId;
    private final String chefId;
    private final InfoToDeleteDTO infoToDelete;
    private final GraphRecipeDTO recipe;



    public TaskToDo(Task.TaskType type, InfoToDeleteDTO infoToDelete) {
        this.type = type;
        this.recipeId = null;
        this.chefId = null;
        this.infoToDelete = infoToDelete;
        this.recipe = null;
    }

    public TaskToDo(Task.TaskType type, String recipeId, String chefId) {
        this.type = type;
        this.recipeId = recipeId;
        this.chefId = chefId;
        this.infoToDelete = null;
        this.recipe = null;
    }

    public TaskToDo(Task.TaskType type, String recipeId) {
        this.type = type;
        this.recipeId = recipeId;
        this.chefId = null;
        this.infoToDelete = null;
        this.recipe = null;
    }

    public TaskToDo(Task.TaskType type, GraphRecipeDTO recipe) {
        this.type = type;
        this.recipeId = null;
        this.chefId = null;
        this.infoToDelete = null;
        this.recipe = recipe;
    }
}
