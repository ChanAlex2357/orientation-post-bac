package mg.itu.orientationpostbac.ui

import mg.itu.orientationpostbac.domain.MatchBand
import mg.itu.orientationpostbac.domain.RecognitionStatus

/**
 * Vocabulaire affiché. Le document impose des formulations précises et en
 * interdit d'autres (§2.2.1) : l'application dit « indice de compatibilité »
 * et jamais « probabilité de réussite ». Les regrouper ici évite qu'une
 * formulation approximative se glisse dans un écran.
 */

/** Libellés exacts du tableau 10. */
fun libelleDe(bande: MatchBand): String = when (bande) {
    MatchBand.EXCELLENT -> "Excellente correspondance"
    MatchBand.GOOD -> "Bonne correspondance"
    MatchBand.PARTIAL -> "Correspondance partielle"
    MatchBand.WEAK -> "Correspondance faible"
}

/**
 * Avertissement lié au statut de reconnaissance (tableau 12). Le statut
 * conditionne l'affichage du score, il ne le modifie jamais : c'est un axe
 * orthogonal, pas un septième critère (document §2.2.3).
 */
fun avertissementDe(statut: RecognitionStatus): String? = when (statut) {
    RecognitionStatus.VERIFIEE -> null
    RecognitionStatus.A_CONFIRMER ->
        "Habilitation a confirmer : verifie aupres de l'etablissement avant de candidater."
    RecognitionStatus.NON_VERIFIEE ->
        "Filiere absente des listes publiees : nous ne pouvons pas confirmer la valeur du diplome."
}

/** La bande disparaît quand le statut est NON_VERIFIEE (tableau 12, 3e ligne). */
fun bandeAffichable(statut: RecognitionStatus): Boolean =
    statut != RecognitionStatus.NON_VERIFIEE
