package it.unipi.MySmartRecipeBook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/api/foodies")
public class FoodieController {

        @Autowired
        private FoodieRepository foodieRepository;

        @GetMapping
        public List<Chef> findAll(){ return foodieRepository.findAll();}

    }
}
