package com.ensias.crowdfunding_project.enums;
public enum TypeNotification {
    // KYC (seulement les décisions finales)
    KYC_VALIDE,
    KYC_REJETE,
    KYC_SOUMIS,

    // Projets
    PROJET_VALIDE,
    PROJET_REJETE,
    PROJET_ANNULE,

    // Investissements
    INVESTISSEMENT_RECU,
    INVESTISSEMENT_CONFIRME,
    OBJECTIF_ATTEINT,
    FIN_PROJET,

    // Compte utilisateur
    COMPTE_BANNI,
    COMPTE_SUSPENDU,
    COMPTE_ACTIVE,

    // Sécurité
    NOUVEAU_LOGIN,
    MDP_MODIFIE,

    // Commentaires
    COMMENTAIRE_PROJET,    // notification au créateur du projet
    COMMENTAIRE_REPONSE,   // notification à l'auteur du commentaire parent

    // Système
    RAPPEL_KYC,
}
