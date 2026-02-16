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

public class Neo4jDataLoader {

    // --- CONFIGURAZIONE ---
    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "12345678";

    // Percorsi ai file JSON (assicurati che partano dalla root del progetto)
    private static final String PATH_INGREDIENTS = "data/ingredients/eggfree_ingredients.json";
    private static final String PATH_RECIPES = "data/recipes/NEW_allrecipes_egg_free_CLEANED.json";

    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));

        try (Session session = driver.session()) {
            System.out.println("=== INIZIO CARICAMENTO NEO4J ===");

            // 1. Carichiamo prima tutti gli ingredienti puliti dal file specifico
            loadMasterIngredients(session);

            // 2. Carichiamo le ricette e le colleghiamo agli ingredienti
            loadRecipesAndConnect(session);

            System.out.println("=== CARICAMENTO COMPLETATO ===");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.close();
        }
    }

    // FASE 1: Carica la lista pura degli ingredienti
    private static void loadMasterIngredients(Session session) {
        JSONParser parser = new JSONParser();
        System.out.println("--> Caricamento Ingredienti da: " + PATH_INGREDIENTS);

        try {
            // Questo file è un array di stringhe: ["zucchero", "sale", ...]
            Object obj = parser.parse(new FileReader(PATH_INGREDIENTS));
            JSONArray ingredientList = (JSONArray) obj;

            int count = 0;
            for (Object o : ingredientList) {
                String ingName = (String) o; // Qui il cast a String è corretto
                if (ingName != null && !ingName.isEmpty()) {
                    // Creiamo il nodo
                    session.run("MERGE (i:Ingredient {name: $name})",
                            Values.parameters("name", ingName.trim().toLowerCase()));
                    count++;
                }
            }
            System.out.println("    Fatto! Caricati " + count + " nodi Ingrediente.");

        } catch (Exception e) {
            System.err.println("Errore in loadMasterIngredients: " + e.getMessage());
        }
    }

    // FASE 2: Carica Ricette e crea relazioni
    private static void loadRecipesAndConnect(Session session) {
        JSONParser parser = new JSONParser();
        System.out.println("--> Caricamento Ricette da: " + PATH_RECIPES);

        try {
            Object obj = parser.parse(new FileReader(PATH_RECIPES));
            JSONArray recipeList = (JSONArray) obj;

            int count = 0;
            for (Object o : recipeList) {
                JSONObject recipe = (JSONObject) o;

                // 1. Prendiamo SOLO title e url per il nodo Ricetta
                String title = (String) recipe.get("title");
                String image_url = (String) recipe.get("image_url");

                if (title != null && image_url != null) {
                    // Crea il nodo Ricetta (usiamo l'URL come ID univoco)
                    session.run("MERGE (r:Recipe {image_url: $image_url}) SET r.title = $title",
                            Values.parameters("image_url", image_url, "title", title));

                    // 2. Gestione Ingredienti dentro la ricetta
                    JSONArray ingredients = (JSONArray) recipe.get("ingredients");
                    if (ingredients != null) {
                        for (Object ingObj : ingredients) {
                            // *** QUI ERA L'ERRORE ***
                            // Nel file ricette, l'ingrediente è un OGGETTO: {"name": "sugar", ...}
                            if (ingObj instanceof JSONObject) {
                                JSONObject fullIng = (JSONObject) ingObj;
                                String ingName = (String) fullIng.get("name"); // Estraiamo solo il nome

                                if (ingName != null) {
                                    ingName = ingName.trim().toLowerCase();

                                    // Query: Collega la Ricetta all'Ingrediente
                                    // Usiamo MERGE su Ingredient per sicurezza (se mancava nel file master lo crea ora)
                                    String query =
                                            "MATCH (r:Recipe {image_url: $image_url}) " +
                                                    "MERGE (i:Ingredient {name: $ingName}) " +
                                                    "MERGE (r)-[:CONTAINS_INGREDIENT]->(i)";

                                    session.run(query, Values.parameters("image_url", image_url, "ingName", ingName));
                                }
                            }
                        }
                    }
                    count++;
                    if (count % 50 == 0) System.out.println("    Processate " + count + " ricette...");
                }
            }
            System.out.println("    Fatto! Totale ricette caricate: " + count);

        } catch (Exception e) {
            System.err.println("Errore in loadRecipesAndConnect: " + e.getMessage());
            e.printStackTrace(); // Utile per capire se il path è sbagliato
        }
    }
}