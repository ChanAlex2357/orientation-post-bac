package mg.itu.orientationpostbac.data

import mg.itu.orientationpostbac.domain.QuestionRiasec
import mg.itu.orientationpostbac.domain.RiasecDimension

/**
 * Adaptation courte du modèle RIASEC : 12 items, 2 par dimension
 * (document §2.2.2 — « 60 items décourageraient un lycéen sans améliorer
 * sensiblement le classement à notre échelle »).
 *
 * Les items sont formulés au présent et à la première personne, sans
 * vocabulaire de métier : un élève de Terminale doit pouvoir répondre sans
 * connaître les filières, sinon le questionnaire présuppose la réponse
 * qu'il est censé aider à trouver.
 */
val questionsRiasecInitiales = listOf(
    QuestionRiasecEntity(1, 1, "Reparer un appareil ou un moteur m'interesse.", "REALISTE"),
    QuestionRiasecEntity(2, 2, "Je prefere travailler avec mes mains et des outils plutot que sur papier.", "REALISTE"),
    QuestionRiasecEntity(3, 3, "Comprendre pourquoi un phenomene se produit me motive.", "INVESTIGATEUR"),
    QuestionRiasecEntity(4, 4, "Resoudre un probleme de mathematiques ou de logique me plait.", "INVESTIGATEUR"),
    QuestionRiasecEntity(5, 5, "J'aime dessiner, ecrire ou inventer quelque chose de nouveau.", "ARTISTIQUE"),
    QuestionRiasecEntity(6, 6, "Je prefere les activites ou il n'existe pas une seule bonne reponse.", "ARTISTIQUE"),
    QuestionRiasecEntity(7, 7, "Aider quelqu'un a comprendre une lecon me procure de la satisfaction.", "SOCIAL"),
    QuestionRiasecEntity(8, 8, "Je me sens utile quand je m'occupe des autres.", "SOCIAL"),
    QuestionRiasecEntity(9, 9, "Convaincre un groupe de me suivre ne me fait pas peur.", "ENTREPRENANT"),
    QuestionRiasecEntity(10, 10, "J'aimerais diriger une equipe ou creer ma propre activite.", "ENTREPRENANT"),
    QuestionRiasecEntity(11, 11, "Tenir des comptes ou classer des documents ne me derange pas.", "CONVENTIONNEL"),
    QuestionRiasecEntity(12, 12, "Je prefere des consignes claires et une organisation precise.", "CONVENTIONNEL"),
)

/** Une dimension illisible n'a pas de valeur par défaut honnête : la question est écartée. */
fun QuestionRiasecEntity.versDomaine(): QuestionRiasec? {
    val dimensionLue = RiasecDimension.entries.firstOrNull { it.name == dimension } ?: return null
    return QuestionRiasec(id = id, ordre = ordre, texte = texte, dimension = dimensionLue)
}
