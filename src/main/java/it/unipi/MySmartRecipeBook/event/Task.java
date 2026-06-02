package it.unipi.MySmartRecipeBook.event;

/**
 * Enumerate for task types
 */
public class Task {

    public enum TaskType{
        SET_COUNTERS_FOODIE_DELETE,
        SET_COUNTERS_NEW_FAVOURITE,
        SET_COUNTERS_REMOVE_FAVOURITE,
        CREATE_RECIPE_NEO4J,
        DELETE_CHEF_PROFILE_NEO4J,
        DELETE_RECIPE_NEO4J,
    }
}
