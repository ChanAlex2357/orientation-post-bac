package mg.itu.orientationpostbac.domain

/**
 * Séries du Baccalauréat malgache retenues pour le filtre d'éligibilité
 * (Formation.seriesAdmissibles, tableau 8 du document).
 * À confirmer/étendre selon le jeu de données final de Henintsoa (US2.2).
 */
enum class Serie { A1, A2, C, D, OSE }

/** Catégorisation minimale MVP — à affiner si le jeu de données en distingue davantage. */
enum class TypeEtablissement { PUBLIC, PRIVE }

/** Les 6 critères pondérés du ScoringEngine (document §2.2.1, tableau 9). */
enum class Criterion { INTERESTS, LEVEL, BUDGET, LOCATION, DURATION, ADMISSION }

/** Bandes qualitatives de correspondance issues du score (document §2.2.1, tableau 10). */
enum class MatchBand { EXCELLENT, GOOD, PARTIAL, WEAK }

/**
 * Statut tri-état de la reconnaissance officielle (habilitation/accréditation) —
 * argument central du projet (document §2.1.1, §2.4) : le ministère publie
 * lui-même une donnée à 3 états, ce modèle le reprend tel quel.
 */
enum class RecognitionStatus { VERIFIEE, A_CONFIRMER, NON_VERIFIEE }
