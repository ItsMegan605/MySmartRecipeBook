package it.unipi.MySmartRecipeBook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.list;


@RestController
@RequestMapping("/api/chefs")
public class ChefController {
    @Autowired
    private ChefRepository chefRepository;

    @GetMapping
    public List<Chef> findAll(){ return chefRepository.findAll();}

}
