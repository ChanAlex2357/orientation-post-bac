package mg.itu.orientationpostbac.domain

/**
 * Profil de l'élève (document figure 4), rempli à l'écran Profil (US5.1).
 * `niveauParMatiere` : auto-positionnement déclaré par matière (0..100),
 * cf. document §9.4 — "une contrainte renseignée par l'élève, exactement
 * comme le budget", pas une prédiction de réussite.
 */
data class UserProfile(
    val serie: Serie,
    val riasecProfile: RiasecProfile,
    val niveauParMatiere: Map<String, Int>,
    val constraints: UserConstraints,
    val preferences: UserPreferences,
)

/** Contraintes dures déclarées par l'élève (budget/localisation/durée, tableau 8). */
data class UserConstraints(
    val budgetMaxAriary: Int?,
    val localisationSouhaitee: String?,
    val dureeMaxAnnees: Int?,
)

/**
 * Préférences non bloquantes de l'élève. Champ minimal pour l'instant —
 * son usage exact (critère ADMISSION du ScoringEngine) sera affiné à l'US1.3.
 */
data class UserPreferences(
    val modeAdmissionAccepte: Set<String> = emptySet(),
)
