package mg.itu.orientationpostbac.domain

/**
 * Résultat du premier étage du moteur (document figure 3) : seules les
 * impossibilités réglementaires sont bloquantes. Tout le reste (budget,
 * localisation, durée, mode d'admission) pénalise le score au second étage
 * (ScoringEngine, US1.3) sans jamais empêcher l'affichage (document §9.2).
 */
sealed interface EligibilityResult {
    object Eligible : EligibilityResult
    data class NotEligible(val reasons: List<BlockingReason>) : EligibilityResult
}

/**
 * Motif d'exclusion bloquant (tableau 8). Type "données", sans texte —
 * le libellé affiché à l'écran (US5.4, section "Non éligible : pourquoi ?")
 * est construit à partir de ces champs, pas stocké ici.
 */
sealed interface BlockingReason {
    data class SerieNonAdmissible(val serieActuelle: Serie, val seriesRequises: Set<Serie>) : BlockingReason
    data class NiveauInsuffisant(val gradeFormation: String) : BlockingReason
}

private const val GRADE_LICENCE = "L"

/**
 * Premier étage du moteur de compatibilité (document figure 3, tableau 8).
 *
 * Seules 2 contraintes sont bloquantes ici :
 * - Série non admissible.
 * - Niveau d'étude requis non atteint : le document ne détaille pas ce que
 *   recouvre ce motif au niveau champ. Hypothèse retenue, à confirmer avec
 *   Henintsoa : le public cible étant des élèves de Terminale (donc sans
 *   diplôme post-bac), seules les formations de grade Licence ("L") sont
 *   accessibles — Master/Doctorat sont bloquants faute de prérequis.
 *
 * Les autres contraintes du tableau 8 (budget, localisation, durée, mode
 * d'admission) sont pénalisantes, pas bloquantes : elles relèvent du
 * ScoringEngine (US1.3), pas de ce premier étage.
 */
object EligibilityEngine {

    fun evaluer(formation: Formation, profil: UserProfile): EligibilityResult {
        val motifs = mutableListOf<BlockingReason>()

        if (profil.serie !in formation.seriesAdmissibles) {
            motifs += BlockingReason.SerieNonAdmissible(profil.serie, formation.seriesAdmissibles)
        }
        if (formation.grade != GRADE_LICENCE) {
            motifs += BlockingReason.NiveauInsuffisant(formation.grade)
        }

        return if (motifs.isEmpty()) EligibilityResult.Eligible else EligibilityResult.NotEligible(motifs)
    }
}
