package com.ensias.crowdfunding_project.services.utilisateur;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
@Service
@Slf4j
public class ModerationService {
    // ✅ Enum pour catégoriser les types de contenu interdit
    public enum CategorieMot {
        VIOLENCE, HARCELEMENT, DISCRIMINATION, TERRORISME, ILLEGAL
    }

    // ✅ Map avec catégories pour un meilleur suivi
    private static final Map<String, CategorieMot> MOTS_INTERDITS = Map.ofEntries(
            Map.entry("violence",       CategorieMot.VIOLENCE),
            Map.entry("meurtre",        CategorieMot.VIOLENCE),
            Map.entry("tuer",           CategorieMot.VIOLENCE),
            Map.entry("arme à feu",     CategorieMot.ILLEGAL),
            Map.entry("haine",          CategorieMot.HARCELEMENT),
            Map.entry("harcèlement",    CategorieMot.HARCELEMENT),
            Map.entry("discrimination", CategorieMot.DISCRIMINATION),
            Map.entry("racisme",        CategorieMot.DISCRIMINATION),
            Map.entry("terrorisme",     CategorieMot.TERRORISME),
            Map.entry("attentat",       CategorieMot.TERRORISME)
    );

    // ✅ DTO de résultat structuré (remplace le simple boolean)
    public record ResultatModeration(
            boolean estInterdit,
            List<String> motsTrouves,
            Map<CategorieMot, List<String>> parCategorie,
            String resume
    ) {
        public static ResultatModeration propre() {
            return new ResultatModeration(false, List.of(), Map.of(), "Aucun contenu interdit détecté.");
        }
    }

    /**
     * ✅ Méthode principale : analyse complète du texte
     * Remplace contenuViolent() + motsTrouves() en une seule passe
     */
    public ResultatModeration analyser(String texte) {
        // ✅ Guard clause : texte vide ou null → propre par défaut
        if (texte == null || texte.isBlank()) {
            log.debug("Texte vide soumis à la modération.");
            return ResultatModeration.propre();
        }

        String texteLower = texte.toLowerCase(Locale.FRENCH); // ✅ Locale explicite

        List<String> trouves = new ArrayList<>();
        Map<CategorieMot, List<String>> parCategorie = new EnumMap<>(CategorieMot.class);

        for (Map.Entry<String, CategorieMot> entry : MOTS_INTERDITS.entrySet()) {
            String mot = entry.getKey();
            CategorieMot categorie = entry.getValue();

            // ✅ Recherche par mot entier (évite "tuer" dans "actuel")
            if (contiendMotEntier(texteLower, mot)) {
                trouves.add(mot);
                parCategorie.computeIfAbsent(categorie, k -> new ArrayList<>()).add(mot);
                log.warn("Contenu interdit détecté — catégorie: {}, mot: '{}'", categorie, mot);
            }
        }

        if (trouves.isEmpty()) {
            return ResultatModeration.propre();
        }

        String resume = "Contenu interdit détecté : %d terme(s) dans %d catégorie(s)."
                .formatted(trouves.size(), parCategorie.size());

        return new ResultatModeration(true, Collections.unmodifiableList(trouves), parCategorie, resume);
    }

    /**
     * ✅ Vérifie la présence d'un mot entier (pas une sous-chaîne)
     * Exemple : "tuer" ne matche PAS "actuel"
     */
    private boolean contiendMotEntier(String texte, String mot) {
        // Regex : délimiteurs = début/fin de chaîne ou espace/ponctuation
        String pattern = "(?<![\\wÀ-ÿ])" + Pattern.quote(mot) + "(?![\\wÀ-ÿ])";
        return Pattern.compile(pattern).matcher(texte).find();
    }

    /**
     * ✅ Raccourci booléen pour les vérifications rapides (ex: @PreAuthorize)
     */
    public boolean estInterdit(String texte) {
        return analyser(texte).estInterdit();
    }
}