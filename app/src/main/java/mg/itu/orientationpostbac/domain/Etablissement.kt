package mg.itu.orientationpostbac.domain

/**
 * Établissement d'enseignement supérieur (document figure 4).
 * `niveauMaturite` est réservé à la V2 (article 12, arrêté 21408/2026) —
 * non exploité par le MVP, cf. document §2.3.3/§3.3.
 */
data class Etablissement(
    val id: String,
    val nom: String,
    val sigle: String,
    val type: TypeEtablissement,
    val localisation: String,
    val description: String,
    val siteOfficiel: String?,
    val statutVerification: RecognitionStatus,
    val dateVerification: String,
    val niveauMaturite: String? = null,
)
