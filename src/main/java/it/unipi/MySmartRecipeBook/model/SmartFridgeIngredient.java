package it.unipi.MySmartRecipeBook.model;

public class SmartFridgeIngredient {

        private Integer ingredientId;
        private String ingredientName;

        public SmartFridgeIngredient(Integer ingredientId, String ingredientName) {
            this.ingredientId = ingredientId;
            this.ingredientName = ingredientName;
        }

        public Integer getIngredientId() {
            return ingredientId;
        }

        public void setIngredientId(Integer ingredientId) {
            this.ingredientId = ingredientId;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public void setIngredientName(String ingredientName) {
            this.ingredientName = ingredientName;
        }

        @Override
        public String toString() {
            return "SmartFridgeIngredient{" +
                    "ingredientId=" + ingredientId +
                    ", ingredientName='" + ingredientName +
                    '}';

        }
}

