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
    private static final String PASSWORD = "12345678";

    private static final String PATH_INGREDIENTS = "data/ingredients/eggfree_ingredients.json";
    private static final String PATH_RECIPES = "data/recipes/export_ricette_pretty.json";

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
                if (ingName != null) { //we wanted just the ingredients name
                    session.run("MERGE (i:Ingredient {name: $name})", //merge to check if the node already exixts
                            Values.parameters("name", ingName.trim().toLowerCase()));
                    count++; //gets the single ingredients and counts them
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
            System.out.println("--- Uploading Recipes and Connecting Ingredients ---");
            Object obj = parser.parse(reader);

            JSONArray recipesList = null;

            // --- FIX 1: GESTIONE FORMATO POWERSHELL ---
            if (obj instanceof JSONObject) {
                JSONObject jsonWrapper = (JSONObject) obj;
                // Se è il file "pretty" di PowerShell, la lista è dentro "value"
                if (jsonWrapper.containsKey("value")) {
                    recipesList = (JSONArray) jsonWrapper.get("value");
                    System.out.println("Rilevato formato PowerShell wrapper. Trovate " + recipesList.size() + " ricette.");
                } else {
                    System.err.println("Errore: Il file JSON è un oggetto ma non contiene la chiave 'value'.");
                    return;
                }
            } else if (obj instanceof JSONArray) {
                // Caso standard: il file è direttamente una lista
                recipesList = (JSONArray) obj;
                System.out.println("Rilevato formato Array standard. Trovate " + recipesList.size() + " ricette.");
            }

            if (recipesList == null) return;

            int count = 0;
            for (Object o : recipesList) {
                JSONObject recipe = (JSONObject) o;

                // --- FIX 2: GESTIONE ID MONGO ---
                Object idObj = recipe.get("_id");
                String id = null;

                if (idObj instanceof JSONObject) {
                    // Nel file export l'id è un oggetto {"$oid": "..."}
                    id = (String) ((JSONObject) idObj).get("$oid");
                } else if (idObj instanceof String) {
                    id = (String) idObj;
                }

                String title = (String) recipe.get("title");
                String imageUrl = (String) recipe.get("image_url");

                // Procediamo solo se ID e Titolo esistono
                if (id != null && title != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", id);
                    params.put("title", title);
                    params.put("imageURL", imageUrl != null ? imageUrl : "");

                    // Creiamo il nodo Ricetta
                    String createRecipeQuery =
                            "MERGE (r:Recipe {id: $id}) " +
                                    "SET r.title = $title, r.imageURL = $imageURL";

                    session.run(createRecipeQuery, params);

                    // --- FIX 3: GESTIONE INGREDIENTI ---
                    Object ingField = recipe.get("ingredients");

                    if (ingField instanceof JSONArray) {
                        JSONArray ingredients = (JSONArray) ingField;
                        for (Object ingObj : ingredients) {
                            // L'ingrediente è un oggetto { "name": "...", "quantity": "..." }
                            JSONObject fullIng = (JSONObject) ingObj;
                            String ingName = (String) fullIng.get("name");

                            if (ingName != null && !ingName.isBlank()) {
                                ingName = ingName.trim().toLowerCase();

                                // Creiamo il nodo Ingrediente e la relazione
                                String relationQuery =
                                        "MATCH (r:Recipe {id: $id}) " +
                                                "MERGE (i:Ingredient {name: $ingName}) " +
                                                "MERGE (i)-[:USED_IN]->(r)";

                                session.run(relationQuery, Values.parameters("id", id, "ingName", ingName));
                            }
                        }
                    }
                    count++;
                    if (count % 50 == 0) System.out.println("    Processed " + count + " recipes...");
                }
            }
            System.out.println("Done! Total recipes imported: " + count);

        } catch (Exception e) {
            System.err.println("Critical Error in loadRecipesAndConnect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}