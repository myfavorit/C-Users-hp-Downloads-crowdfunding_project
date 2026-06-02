package com.ensias.crowdfunding_project.services.utilisateur;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;  // ← IMPORT CORRECT
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads/kyc}")  // ← valeur par défaut si absente
    private String uploadDir;

    @PostConstruct
    public void init() {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("Répertoire d'upload créé : {}", uploadDir);
            } catch (IOException e) {
                throw new RuntimeException("Impossible de créer le répertoire d'upload", e);
            }
        }
    }

    public String storeFile(MultipartFile file, UUID userId) throws IOException {
        // Nettoyage du nom original (garde null safe)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Le fichier n'a pas de nom valide");
        }
        String cleanName = originalFilename.replaceAll("\\s+", "_"); // enlève espaces
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = userId + "_" + timestamp + "_" + cleanName;

        Path targetPath = Paths.get(uploadDir).resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/kyc/" + filename;
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) return;
        try {
            Path path = Paths.get(uploadDir).resolve(Paths.get(filePath).getFileName());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Erreur suppression fichier {}", filePath, e);
        }
    }
}