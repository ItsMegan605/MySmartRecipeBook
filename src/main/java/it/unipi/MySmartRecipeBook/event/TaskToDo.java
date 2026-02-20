package it.unipi.MySmartRecipeBook.event;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.model.enums.Task;
import lombok.Getter;

@Getter
public class TaskToDo {

    private final Task.TaskType type;
    private final String recipeId;
    private final String chefId;
    private final InfoToDeleteDTO infoToDelete;


    public TaskToDo(Task.TaskType type, InfoToDeleteDTO infoToDelete) {
        this.type = type;
        this.recipeId = null;
        this.chefId = null;
        this.infoToDelete = infoToDelete;
    }

    public TaskToDo(Task.TaskType type, String recipeId, String chefId) {
        this.type = type;
        this.recipeId = recipeId;
        this.chefId = chefId;
        this.infoToDelete = null;
    }

    public TaskToDo(Task.TaskType type, String recipeId) {
        this.type = type;
        this.recipeId = recipeId;
        this.chefId = null;
        this.infoToDelete = null;
    }
}
