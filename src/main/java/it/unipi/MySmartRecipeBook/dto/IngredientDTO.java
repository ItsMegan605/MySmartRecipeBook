package it.unipi.MySmartRecipeBook.dto;

public class IngredientDTO {
    private String name;

    // Costruttore vuoto (obbligatorio per Jackson/Spring)
    public IngredientDTO() {}

    public IngredientDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}