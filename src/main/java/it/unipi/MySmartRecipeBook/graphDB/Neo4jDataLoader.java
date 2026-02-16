package it.unipi.MySmartRecipeBook.graphDB;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Neo4jDataLoader {

    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "12345678"; // <--- INSERISCI LA TUA PASSWORD QUI

    private static final String PATH_INGREDIENTS = "data/ingredients/eggfree_ingredients.json";
    private static final String PATH_RECIPES = "data/recipes/NEW_allrecipes_egg_free_CLEANED.json";

    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));

        try (Session session = driver.session()) {
            System.out.println("=== START LOADING NEO4J ===");
            loadIngredients(session, PATH_INGREDIENTS);
            loadRecipesAndConnect(session, PATH_RECIPES);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.close();
            System.out.println("=== END ===");
        }
    }

    private static void loadIngredients(Session session, String filePath) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(filePath)) {
            System.out.println("--- uploading ingredient ---");
            Object obj = parser.parse(reader);
            JSONArray ingredientsList = (JSONArray) obj;

            int count = 0;
            for (Object o : ingredientsList) {
                String ingName = (String) o;
                if (ingName != null) {
                    session.run("MERGE (i:Ingredient {name: $name})",
                            Values.parameters("name", ingName.trim().toLowerCase()));
                    count++;
                }
            }
            System.out.println(" Uploaded " + count + " ingredients.");

        } catch (Exception e) {
            System.err.println("Error loadIngredients: " + e.getMessage());
        }
    }

    private static void loadRecipesAndConnect(Session session, String filePath) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(filePath)) {
            System.out.println("--- Uploading of recipes ---");
            Object obj = parser.parse(reader);
            JSONArray recipesList = (JSONArray) obj;

            int count = 0;
            for (Object o : recipesList) {
                JSONObject recipe = (JSONObject) o;

                // CORREZIONE: Usiamo "url" come ID, perché "id" non esiste nel JSON
                String id = (String) recipe.get("url");
                String title = (String) recipe.get("title");
                String imageUrl = (String) recipe.get("image_url"); // Nel JSON è snake_case

                // Procediamo solo se abbiamo almeno URL e Titolo
                if (id != null && title != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", id);
                    params.put("title", title);
                    // Salviamo come imageURL (camelCase) per compatibilità con il tuo codice Java
                    params.put("imageURL", imageUrl != null ? imageUrl : "");

                    // 1. Creiamo il nodo Ricetta
                    String createRecipeQuery =
                            "MERGE (r:Recipe {id: $id}) " +
                                    "SET r.title = $title, r.imageURL = $imageURL";

                    session.run(createRecipeQuery, params);

                    // 2. Gestione Ingredienti e Relazione USED_IN
                    JSONArray ingredients = (JSONArray) recipe.get("ingredients");
                    if (ingredients != null) {
                        for (Object ingObj : ingredients) {
                            JSONObject fullIng = (JSONObject) ingObj;
                            String ingName = (String) fullIng.get("name");

                            if (ingName != null) {
                                ingName = ingName.trim().toLowerCase();

                                // Query: (Ingrediente)-[:USED_IN]->(Ricetta)
                                String relationQuery =
                                        "MATCH (r:Recipe {id: $id}) " +
                                                "MERGE (i:Ingredient {name: $ingName}) " +
                                                "MERGE (i)-[:USED_IN]->(r)";

                                session.run(relationQuery, Values.parameters("id", id, "ingName", ingName));
                            }
                        }
                    }
                    count++;
                    if (count % 100 == 0) System.out.println("    Processed " + count + " recipes...");
                }
            }
            System.out.println("    Done! Total recipes: " + count);

        } catch (Exception e) {
            System.err.println("Error loadRecipesAndConnect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}