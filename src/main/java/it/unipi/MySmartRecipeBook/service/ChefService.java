package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.AdminRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.ReducedChef;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;

import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.ChefConvertions;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class ChefService {

    private final ChefRepository chefRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChefConvertions chefConvertions;
    private final AdminRepository adminRepository;

    public ChefService(ChefRepository chefRepository, ChefConvertions chefConvertions,
                       PasswordEncoder passwordEncoder, AdminRepository adminRepository) {
        this.chefRepository = chefRepository;
        this.chefConvertions = chefConvertions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
    }


    /*--------------- Retrieve chef's informations ----------------*/

    public ChefInfoDTO getByUsername(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        return chefConvertions.chefToChefInfo(chef);
    }


    /*--------------- Change chef's informations ----------------*/

    public ChefInfoDTO updateChef(String username, UpdateChefDTO dto) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if (dto.getName() != null)
            chef.setName(dto.getName());

        if (dto.getSurname() != null)
            chef.setSurname(dto.getSurname());

        if (dto.getEmail() != null)
            chef.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            chef.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            chef.setBirthdate(dto.getBirthdate());

        chefRepository.save(chef);
        return chefConvertions.chefToChefInfo(chef);
    }


    /*----------------- Delete chef's profile ----------------*/

    public void deleteChef(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        chefRepository.delete(chef);
    }


    /*------------------- Add new recipe --------------------*/

    // @Transaction
    public ChefPreviewRecipeDTO createRecipe(CreateRecipeDTO dto) {

        /* We add the entire recipe to the admin list of recipes waiting to be approved */
        Admin admin = adminRepository.findByUsername("admin");

        if (admin == null) {
            throw new RuntimeException("Admin not found");
        }

        AdminRecipe savedRecipe = createAdminRecipe(dto);

        if(admin.getRecipesToApprove() == null){
            admin.setRecipesToApprove(new ArrayList<>());
        }

        admin.getRecipesToApprove().add(savedRecipe);
        adminRepository.save(admin);


        /* We add the entire recipe to the chef list of recipes waiting to be approved by the admin*/
        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        ChefRecipe chefRecipe = chefConvertions.adminToChefRecipe(savedRecipe);

        if(chef.getRecipesToConfirm() == null){
            chef.setRecipesToConfirm(new ArrayList<>());
        }

        chef.getRecipesToConfirm().add(chefRecipe);
        chefRepository.save(chef);

        return chefConvertions.adminToChefDTO(savedRecipe);

    }

    private AdminRecipe createAdminRecipe (CreateRecipeDTO dto){

        AdminRecipe recipe = new AdminRecipe();
        recipe.setId(java.util.UUID.randomUUID().toString());
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setPresentation(dto.getPresentation());
        recipe.setImageURL(dto.getImageURL());
        recipe.setIngredients(dto.getIngredients());
        recipe.setCreationDate(LocalDateTime.now());

        ReducedChef chef = new ReducedChef();
        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        chef.setMongoId(chef1.getId());
        chef.setName(chef1.getName());
        chef.setSurname(chef1.getSurname());

        recipe.setChef(chef);

        return recipe;
    }

}
