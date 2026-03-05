package it.unipi.MySmartRecipeBook.dto;

public class PopularIngredientsDTO {

    public String chefName;
    public String chefSurname;
    public String ingredientName;
    public Integer usageCount;

    public PopularIngredientsDTO(String chefName, String chefSurname, String ingredientName, Integer usageCount) {
        this.chefName = chefName;
        this.chefSurname = chefSurname;
        this.ingredientName = ingredientName;
        this.usageCount = usageCount;
    }
}