package com.ensias.crowdfunding_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double objectifFinancier;
    private Double montantActuel = 0.0;
    private Integer dureeJours;

    @ManyToOne
    @JoinColumn(name = "porteur_id")
    private Utilisateur porteur;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "statut_id")
    private StatutProjet statut;
}
