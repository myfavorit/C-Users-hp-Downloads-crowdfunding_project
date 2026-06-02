package com.ensias.crowdfunding_project.enums;

public enum StatutProjet {

    BROUILLON,
    // Project just created by creator
    // Can be edited, submitted, or deleted

    EN_ATTENTE,
    // Submitted by creator, waiting for admin review
    // Can be validated or rejected by admin

    VALIDE,
    // Approved by admin, visible to investors
    // Open for investments until dateFin or goal reached

    REJETE,
    // Rejected by admin with a motif
    // Creator can see the reason and create a new project

    ANNULE,
    // Cancelled by creator
    // Only possible if no investments yet

    CLOTURE_SUCCES,
    // Goal reached (montantActuel >= objectifFinancier)
    // Set automatically when last investment is confirmed

    ECHEC_REMBOURSE
    // Campaign ended without reaching goal (dateFin passed)
    // Investors should be reimbursed
}