package mg.itu.orientationpostbac.domain

import kotlin.math.roundToInt

/** Les 6 dimensions du modèle RIASEC (document, glossaire §10.1). */
enum class RiasecDimension { REALISTE, INVESTIGATEUR, ARTISTIQUE, SOCIAL, ENTREPRENANT, CONVENTIONNEL }

/**
 * Un item du questionnaire court, 12 à 15 questions (document §2.2.2).
 * Chaque question alimente une seule dimension.
 */
data class QuestionRiasec(
    val id: Int,
    val ordre: Int,
    val texte: String,
    val dimension: RiasecDimension,
)

/** Échelle de réponse : 0 = pas du tout, 4 = beaucoup. */
const val REPONSE_MIN = 0
const val REPONSE_MAX = 4

/**
 * Convertit les réponses en profil d'intérêts sur 0..100, dimension par
 * dimension. Kotlin pur : testable sans émulateur, comme le moteur.
 *
 * Une question sans réponse est ignorée, et non comptée comme un zéro :
 * un questionnaire à moitié rempli ne doit pas produire un profil
 * artificiellement plat. Une dimension dont aucune question n'a reçu de
 * réponse vaut 0, faute de mieux — c'est à l'écran (US5.2) d'inciter à
 * terminer le questionnaire.
 *
 * @param reponses identifiant de question -> valeur de 0 à 4.
 */
fun calculerProfilRiasec(
    questions: List<QuestionRiasec>,
    reponses: Map<Int, Int>,
): RiasecProfile {
    fun scoreDe(dimension: RiasecDimension): Int {
        val valeurs = questions
            .filter { it.dimension == dimension }
            .mapNotNull { reponses[it.id] }
            .map { it.coerceIn(REPONSE_MIN, REPONSE_MAX) }

        if (valeurs.isEmpty()) return 0
        return (valeurs.average() / REPONSE_MAX * 100).roundToInt()
    }

    return RiasecProfile(
        realiste = scoreDe(RiasecDimension.REALISTE),
        investigateur = scoreDe(RiasecDimension.INVESTIGATEUR),
        artistique = scoreDe(RiasecDimension.ARTISTIQUE),
        social = scoreDe(RiasecDimension.SOCIAL),
        entreprenant = scoreDe(RiasecDimension.ENTREPRENANT),
        conventionnel = scoreDe(RiasecDimension.CONVENTIONNEL),
    )
}
