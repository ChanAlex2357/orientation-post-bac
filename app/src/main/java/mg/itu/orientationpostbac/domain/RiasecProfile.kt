package mg.itu.orientationpostbac.domain

/**
 * Profil d'intérêts RIASEC (document glossaire §10.1), version courte à 6 dimensions.
 * Chaque score est sur 0..100 (cf. exemple document §2.2.1 : "I 85, R 72, C 60").
 * Utilisé à la fois pour le profil de l'élève (UserProfile.riasecProfile) et pour
 * le profil cible d'une formation (Formation.riasecProfile), comparés par le
 * critère INTERESTS du ScoringEngine.
 */
data class RiasecProfile(
    val realiste: Int,
    val investigateur: Int,
    val artistique: Int,
    val social: Int,
    val entreprenant: Int,
    val conventionnel: Int,
)
