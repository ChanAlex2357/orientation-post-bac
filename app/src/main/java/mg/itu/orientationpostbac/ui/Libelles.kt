package mg.itu.orientationpostbac.ui

import mg.itu.orientationpostbac.domain.BlockingReason
import mg.itu.orientationpostbac.domain.Criterion
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

/**
 * Motif d'exclusion, en clair. Le document impose que le motif soit VISIBLE
 * (§9.2) : une formation non eligible n'est jamais masquee, elle bascule
 * dans une section distincte ou l'eleve lit pourquoi.
 *
 * Le motif est formule comme un fait verifiable, jamais comme un verdict
 * sur l'eleve. "Reservee aux series C et D" dit ce qu'exige la formation ;
 * "tu n'as pas le bon bac" jugerait la personne.
 */
fun libelleDe(motif: BlockingReason): String = when (motif) {
    is BlockingReason.SerieNonAdmissible ->
        "Reservee aux series " + motif.seriesRequises.joinToString(", ") { it.name } +
            ". Tu es en serie " + motif.serieActuelle.name + "."

    is BlockingReason.NiveauInsuffisant ->
        "Formation de niveau " + libelleGrade(motif.gradeFormation) +
            ". Elle demande un diplome que l'on n'a pas en sortant du Baccalaureat."
}

/** Vocabulaire des arretes : le grade y est note L, M ou D (document, glossaire). */
fun libelleGrade(grade: String): String = when (grade) {
    "L" -> "Licence"
    "M" -> "Master"
    "D" -> "Doctorat"
    else -> grade
}

/** Les 6 criteres du tableau 9, tels qu'ils apparaissent dans le depliage du score. */
fun libelleDe(critere: Criterion): String = when (critere) {
    Criterion.INTERESTS -> "Interets"
    Criterion.LEVEL -> "Niveau dans les matieres cles"
    Criterion.BUDGET -> "Budget"
    Criterion.LOCATION -> "Localisation"
    Criterion.DURATION -> "Duree"
    Criterion.ADMISSION -> "Mode d'admission"
}
