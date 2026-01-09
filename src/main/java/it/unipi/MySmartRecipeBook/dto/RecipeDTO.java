package it.unipi.MySmartRecipeBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class RecipeDTO {

    //forse serve l'id

    private String title;
    private String description;
    private String category;
    private Double prepTime;
    private String difficulty;
    private String imageURL;
    private String preparation;
    private ArrayList<String> ingredients;
    private String chefName;

    public RecipeDTO() {} //costruttore vuoto


    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("prepTime")
    public Double getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(Double prepTime) {
        this.prepTime = prepTime;
    }

    @JsonProperty("difficulty")
    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @JsonProperty("imageURL")
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @JsonProperty("preparation")
    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    @JsonProperty("ingredients")
    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    @JsonProperty("chefName")
    public String getChefName() {
        return chefName;
    }

    public void setChefName(String chefName) {
        this.chefName = chefName;
    }

    //non so se va bene e manca qualcosa sicuro
}