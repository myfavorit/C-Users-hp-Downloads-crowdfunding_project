package com.ensias.crowdfunding_project.; // Assure-t-il que ce package correspond à ton dossier

import com.ensias.crowdfunding_project.entities.Utilisateur;
import com.ensias.crowdfunding_project.repositories.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void testSaveAndFind() {
        // 1. Création
        Utilisateur user = new Utilisateur();
        user.setId(UUID.randomUUID());
        user.setNom("Manal");
        user.setPrenom("ENSIAS");
        user.setEmail("manal@example.com");

        // 2. Action
        Utilisateur savedUser = utilisateurRepository.save(user);

        // 3. Vérification
        assertNotNull(savedUser);
        assertEquals("Manal", savedUser.getNom());
    }
}