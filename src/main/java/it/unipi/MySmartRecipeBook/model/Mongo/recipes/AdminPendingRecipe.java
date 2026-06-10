package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a lightweight, embedded sub-document stored within the Admin entity.
 * It tracks the essential details of a recipe (along with its author) that has been submitted
 * by a chef and is currently awaiting review and approval by the admin.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminPendingRecipe extends PendingRecipe {

    private ReducedChef chef;

}

