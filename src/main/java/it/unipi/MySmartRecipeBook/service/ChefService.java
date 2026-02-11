package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.ChefResponseDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChefService {

    private final ChefRepository chefRepository;
    private final PasswordEncoder passwordEncoder;

    public ChefService(ChefRepository chefRepository,
                       PasswordEncoder passwordEncoder) {
        this.chefRepository = chefRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* =========================
       PROFILE MANAGEMENT
       ========================= */

    public ChefResponseDTO getByUsername(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        return mapToResponse(chef);
    }

    public ChefResponseDTO updateChef(String username,
                                      UpdateChefDTO dto) {

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

        return mapToResponse(chef);
    }

    public void deleteChef(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        chefRepository.delete(chef);
    }

    /* =========================
       MAPPING
       ========================= */

    private ChefResponseDTO mapToResponse(Chef chef) {

        return new ChefResponseDTO(
                chef.getUsername(),
                chef.getName(),
                chef.getSurname(),
                chef.getEmail()
        );
    }
}
