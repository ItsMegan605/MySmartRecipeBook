package it.unipi.MySmartRecipeBook.utils.conversionFunctions;

import it.unipi.MySmartRecipeBook.dto.users.ChefPreviewDTO;
import it.unipi.MySmartRecipeBook.dto.users.PendingChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
/**
 * Utility class for Chef-related entity and DTO conversions.
 */
@Component
public class ChefUtilityFunctions {

    private final PasswordEncoder passwordEncoder;

    public ChefUtilityFunctions(PasswordEncoder passwordEncoder) {

        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Converts a registration DTO into a PendingChef to insert in the admin document among the chef waiting to be accepted.
     * @param newChef the entity data
     * @return the PendingChef entity
     */
    public PendingChef createPendingChef (Chef newChef){

        PendingChef chef = new PendingChef();
        chef.setId(newChef.getId());
        chef.setUsername(newChef.getUsername());
        chef.setName(newChef.getName());
        chef.setSurname(newChef.getSurname());

        chef.setEmail(newChef.getEmail());

        chef.setBirthdate(newChef.getBirthdate());

        return chef;
    }

    /**
     * Converts a registration DTO into a Chef entity to insert in the chef collection
     * @param dto the registration data
     * @return the PendingChef entity
     */
    public Chef createChefEntity (RegisteredUserDTO dto){

        Chef chef = new Chef();
        chef.setUsername(dto.getUsername());
        chef.setName(dto.getName());
        chef.setSurname(dto.getSurname());
        chef.setPassword(passwordEncoder.encode(dto.getPassword()));
        chef.setEmail(dto.getEmail());
        chef.setBirthdate(dto.getBirthdate());

        chef.setStatus("PENDING");

        return chef;
    }

    /**
     * Converts a Chef entity to a DTO to show in the chef's personal page.
     * Password is not shown for security
     * @param chef the Chef entity
     * @return the registered user info DTO
     */
    public RegisteredUserInfoDTO chefToChefInfo(Chef chef){

        return new RegisteredUserInfoDTO(
                chef.getUsername(),
                chef.getName(),
                chef.getSurname(),
                chef.getEmail(),
                chef.getBirthdate()
        );
    }

    /**
     * Checks if a chef registration request already exists based on personal data or username.
     * @param targetChef the existing pending chef
     * @param chef the new pending chef
     * @return true if a duplicate exists, false otherwise
     */
    public boolean chefAlreadyInserted(PendingChef targetChef, PendingChef chef) {

        boolean sameRequest = targetChef.getName().equals(chef.getName()) &&
                targetChef.getSurname().equals(chef.getSurname()) &&
                targetChef.getBirthdate().equals(chef.getBirthdate());

        boolean sameUsername = targetChef.getUsername().equals(chef.getUsername());
        return sameRequest || sameUsername;
    }

    /**
     * Converts an approved PendingChef into a final Chef entity.
     * @param chef the pending chef
     * @return the final Chef entity
     */
    public Chef pendingChefToChef (PendingChef chef){

        Chef chefMongo = new Chef();
        chefMongo.setUsername(chef.getUsername());
        chefMongo.setName(chef.getName());
        chefMongo.setSurname(chef.getSurname());
        chefMongo.setEmail(chef.getEmail());
        chefMongo.setBirthdate(chef.getBirthdate());
        return chefMongo;
    }


    /**
     * Converts a list of PendingChef entities into a list of PendingChefDTOs.
     * @param chefs the list of pending chefs
     * @return the list of pending chef DTOs
     */
    public List<PendingChefDTO> PendingChefListToDTO(List<PendingChef> chefs) {
        List<PendingChefDTO> result = new ArrayList<>();
        for (PendingChef chef : chefs) {
            result.add(new PendingChefDTO(chef.getUsername(), chef.getName(), chef.getSurname()));
        }
        return result;
    }

    /**
     * Converts a list of Chef entities into a list of ChefPreviewDTOs.
     * @param chefs the list of Chef entities to convert
     * @return a list of ChefPreviewDTO containing the chef preview details
     */
    public List<ChefPreviewDTO> chefModelToChefDTO(List<Chef> chefs) {
        List<ChefPreviewDTO> chefsDTO = new ArrayList<>();
        for(Chef chef: chefs){
            if(chef.getStatus().equals("APPROVED")) {
                ChefPreviewDTO chefDTO = new ChefPreviewDTO();
                chefDTO.setId(chef.getId());
                chefDTO.setFullName(chef.getName() + " " + chef.getSurname());
                chefDTO.setTotRecipes(chef.getTotalRecipes());
                chefDTO.setTotSaves(chef.getTotalSaves());
                chefsDTO.add(chefDTO);
            }
        }
        return chefsDTO;
    }

    /**
     * Converts a PendingChef entity to a RegisteredUserInfoDTO to display detailed information.
     * @param chef the pending chef entity
     * @return the detailed registered user info DTO
     */
    public RegisteredUserInfoDTO pendingChefToChefDetails (Chef chef){

        return new RegisteredUserInfoDTO(
                chef.getUsername(),
                chef.getName(),
                chef.getSurname(),
                chef.getEmail(),
                chef.getBirthdate()
        );
    }
}
