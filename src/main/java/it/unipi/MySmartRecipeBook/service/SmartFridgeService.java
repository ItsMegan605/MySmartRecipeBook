package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.model.SmartFridgeIngredient;
import it.unipi.MySmartRecipeBook.repository.SmartFridgeRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmartFridgeService {
/*
    @Autowired
    private SmartFridgeRepository smartFridgeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void updateFridge(SmartFridge fridge) {
        try {
            String json = objectMapper.writeValueAsString(fridge);
            smartFridgeRepository.save(fridge.getId().toString(), json);
        } catch (JsonProcessingException e) {
            // Gestione errore serializzazione
        }
    }

    public SmartFridge getFridgeByUserId(Integer userId) {
        String json = smartFridgeRepository.findById(userId.toString());
        if (json != null) {
            try {
                return objectMapper.readValue(json, SmartFridge.class);
            } catch (JsonProcessingException e) {
                // Gestione errore deserializzazione
            }
        }
        return new SmartFridge(userId); // Ritorna un frigo vuoto se non trovato
    }
    */
}