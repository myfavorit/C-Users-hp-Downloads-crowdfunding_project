package com.ensias.crowdfunding_project.enums;

import java.math.BigDecimal;

public enum RegleDomaine {

    TECHNOLOGIE(90, 200_000),
    AGRICULTURE(120, 150_000),
    SANTE(180, 300_000),
    EDUCATION(60, 50_000),
    COMMERCE(90, 100_000),
    IMMOBILIER(365, 1_000_000),
    ENERGIE(150, 500_000),
    TOURISME(90, 80_000),
    INDUSTRIE(120, 400_000),
    ART_CULTURE(45, 30_000),
    JEUX_SOCIETE(60, 40_000),
    FILM_VIDEO(90, 150_000),
    MUSIQUE(60, 50_000),
    LIVRE(45, 20_000),
    MODE(60, 60_000),
    BEAUTE(60, 50_000);

    private final int dureeMaxJours;
    private final BigDecimal objectifMax;

    RegleDomaine(int dureeMaxJours, double objectifMaxEuro) {
        this.dureeMaxJours = dureeMaxJours;
        this.objectifMax = BigDecimal.valueOf(objectifMaxEuro);
    }

    public int getDureeMaxJours() {
        return dureeMaxJours;
    }

    public BigDecimal getObjectifMax() {
        return objectifMax;
    }

    // Méthode utilitaire pour obtenir la règle à partir du domaine existant
    public static RegleDomaine fromDomaine(DomaineProjet domaine) {
        return RegleDomaine.valueOf(domaine.name()); // même nom, même ordre
    }
}