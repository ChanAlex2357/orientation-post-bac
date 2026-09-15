package mg.itu.orientationpostbac.domain

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Résultat du second étage du moteur pour UNE formation déjà éligible
 * (document §2.2.1, code verbatim). `reasons` porte des messages déjà
 * formatés pour le dépliage écran (US5.5), pas de logique de présentation
 * côté UI — même esprit que l'exemple de dépliage du document.
 */
data class CompatibilityResult(
    val formationId: String,
    val score: Int,
    val band: MatchBand,
    val breakdown: Map<Criterion, Int>,
    val reasons: List<String>,
)

/**
 * Bandes du tableau 10. Definie hors du ScoringEngine parce que l'ecran
 * Parcours suggeres en a besoin pour les domaines : deux jeux de seuils qui
 * derivent finiraient par afficher deux verdicts differents sur un meme score.
 */
fun bandeDeScore(score: Int): MatchBand = when {
    score >= 85 -> MatchBand.EXCELLENT
    score >= 70 -> MatchBand.GOOD
    score >= 50 -> MatchBand.PARTIAL
    else -> MatchBand.WEAK
}

private const val NIVEAU_PAR_DEFAUT = 50
private const val SEUIL_MESSAGE_POSITIF = 70
private const val SEUIL_PLAFONNEMENT_INTERETS = 50

/**
 * Second étage du moteur (document figure 3, tableaux 9-10-11) : pondère
 * 6 critères (0..100 chacun) en un score global, puis une bande qualitative
 * plafonnée si les intérêts sont trop faibles (document §2.2.1 : "si la
 * compatibilité d'intérêts est inférieure à 50 %, la bande ne dépasse
 * jamais Correspondance partielle").
 *
 * N'est appelé que sur une formation déjà jugée `Eligible` par
 * l'EligibilityEngine (US1.2) — ce moteur ne filtre rien, il note.
 *
 * Les formules par critère ne sont pas détaillées dans le document
 * (seul le tableau de pondération et un exemple chiffré le sont) : les
 * choix ci-dessous sont une implémentation raisonnable, à ajuster si le
 * jeu de données de Henintsoa (US2.2) ou le scénario Annexe B (US7.1)
 * révèlent un écart.
 */
object ScoringEngine {

    private val PONDERATION: Map<Criterion, Double> = mapOf(
        Criterion.INTERESTS to 0.35,
        Criterion.LEVEL to 0.25,
        Criterion.BUDGET to 0.20,
        Criterion.LOCATION to 0.10,
        Criterion.DURATION to 0.05,
        Criterion.ADMISSION to 0.05,
    )

    fun evaluer(formation: Formation, profil: UserProfile): CompatibilityResult {
        val breakdown = mapOf(
            Criterion.INTERESTS to scoreInterets(profil.riasecProfile, formation.riasecProfile),
            Criterion.LEVEL to scoreNiveau(profil, formation),
            Criterion.BUDGET to scoreBudget(profil, formation),
            Criterion.LOCATION to scoreLocalisation(profil, formation),
            Criterion.DURATION to scoreDuree(profil, formation),
            Criterion.ADMISSION to scoreAdmission(profil, formation),
        )

        val score = breakdown.entries
            .sumOf { (critere, valeur) -> PONDERATION.getValue(critere) * valeur }
            .roundToInt()
            .coerceIn(0, 100)

        val bande = bandeDeScore(score).let { bandeBrute ->
            val scoreInterets = breakdown.getValue(Criterion.INTERESTS)
            if (scoreInterets < SEUIL_PLAFONNEMENT_INTERETS &&
                (bandeBrute == MatchBand.EXCELLENT || bandeBrute == MatchBand.GOOD)
            ) {
                MatchBand.PARTIAL
            } else {
                bandeBrute
            }
        }

        return CompatibilityResult(
            formationId = formation.id,
            score = score,
            band = bande,
            breakdown = breakdown,
            reasons = construireExplications(breakdown),
        )
    }

    /**
     * Similarité entre le profil de l'élève et le profil cible de la
     * formation, dimension par dimension, PONDÉRÉE par l'intérêt déclaré de
     * l'élève sur chaque dimension.
     *
     * La moyenne simple, écrite d'abord, avait un défaut que l'essai sur le
     * vrai jeu de données a révélé : elle récompense l'accord sur des
     * dimensions dont l'élève ne se soucie pas. Six petits accords sur des
     * dimensions faibles pouvaient l'emporter sur un écart franc sur la
     * dimension dominante, et une formation de maintenance passait devant
     * une licence d'informatique pour un profil Investigateur.
     *
     * Pondérer par l'intérêt de l'élève suit le modèle RIASEC, qui lit un
     * profil par ses dimensions dominantes et non par ses six valeurs à
     * égalité. Un élève sans aucun intérêt déclaré (questionnaire non
     * rempli) retombe sur la moyenne simple, faute de pondération possible.
     */
    private fun scoreInterets(profil: RiasecProfile, cible: RiasecProfile): Int {
        val dimensions = listOf(
            profil.realiste to cible.realiste,
            profil.investigateur to cible.investigateur,
            profil.artistique to cible.artistique,
            profil.social to cible.social,
            profil.entreprenant to cible.entreprenant,
            profil.conventionnel to cible.conventionnel,
        )
        val similarites = dimensions.map { (interet, cibleScore) ->
            interet to (100 - abs(interet - cibleScore)).coerceIn(0, 100)
        }

        val totalInterets = similarites.sumOf { (interet, _) -> interet }
        if (totalInterets == 0) return similarites.map { (_, similarite) -> similarite }.average().roundToInt()

        return similarites
            .sumOf { (interet, similarite) -> interet.toDouble() * similarite }
            .div(totalInterets)
            .roundToInt()
    }

    /** Moyenne du niveau déclaré sur les matières clés de la formation. Matière non renseignée = neutre. */
    private fun scoreNiveau(profil: UserProfile, formation: Formation): Int {
        if (formation.matieresCles.isEmpty()) return 100
        return formation.matieresCles
            .map { matiere -> profil.niveauParMatiere[matiere] ?: NIVEAU_PAR_DEFAUT }
            .average()
            .roundToInt()
            .coerceIn(0, 100)
    }

    /** Coût non renseigné ou pas de budget déclaré : jamais pénalisant (bénéfice du doute). */
    private fun scoreBudget(profil: UserProfile, formation: Formation): Int {
        val budgetMax = profil.constraints.budgetMaxAriary ?: return 100
        val cout = formation.coutIndicatif ?: return 100
        if (cout <= budgetMax) return 100
        return ((budgetMax.toDouble() / cout) * 100).roundToInt().coerceIn(0, 100)
    }

    private fun scoreLocalisation(profil: UserProfile, formation: Formation): Int {
        val souhaitee = profil.constraints.localisationSouhaitee ?: return 100
        return if (souhaitee.equals(formation.localisation, ignoreCase = true)) 100 else 0
    }

    private fun scoreDuree(profil: UserProfile, formation: Formation): Int {
        val dureeMax = profil.constraints.dureeMaxAnnees ?: return 100
        if (formation.duree <= dureeMax) return 100
        return ((dureeMax.toDouble() / formation.duree) * 100).roundToInt().coerceIn(0, 100)
    }

    private fun scoreAdmission(profil: UserProfile, formation: Formation): Int {
        val accepte = profil.preferences.modeAdmissionAccepte
        if (accepte.isEmpty()) return 100
        return if (accepte.any { it.equals(formation.modeAdmission, ignoreCase = true) }) 100 else 0
    }

    private fun construireExplications(breakdown: Map<Criterion, Int>): List<String> = listOf(
        messagePour(breakdown.getValue(Criterion.INTERESTS), "très bonne correspondance avec tes intérêts", "cette formation correspond peu à tes intérêts"),
        messagePour(breakdown.getValue(Criterion.LEVEL), "ton niveau dans les matières clés correspond aux attentes", "ton niveau actuel dans les matières clés est en dessous de ce qui est généralement attendu"),
        messagePour(breakdown.getValue(Criterion.BUDGET), "le coût est compatible avec ton budget", "le coût est supérieur à ton budget"),
        messagePour(breakdown.getValue(Criterion.LOCATION), "établissement situé dans ta zone souhaitée", "établissement en dehors de ta zone souhaitée"),
        messagePour(breakdown.getValue(Criterion.DURATION), "durée conforme à ton objectif", "durée plus longue que ton objectif"),
        messagePour(breakdown.getValue(Criterion.ADMISSION), "mode d'admission compatible avec tes préférences", "mode d'admission différent de tes préférences"),
    )

    private fun messagePour(score: Int, messagePositif: String, messageNegatif: String): String =
        if (score >= SEUIL_MESSAGE_POSITIF) "+ ${messagePositif.replaceFirstChar { it.uppercase() }}"
        else "! ${messageNegatif.replaceFirstChar { it.uppercase() }}"
}
