package mg.itu.orientationpostbac.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mg.itu.orientationpostbac.domain.QuestionRiasec

/**
 * Accès aux items du questionnaire. Séparé du FormationRepository : ce sont
 * deux sujets différents, et un écran qui affiche le questionnaire n'a aucune
 * raison de dépendre du catalogue.
 *
 * Les questions dont la dimension est illisible sont écartées ici plutôt que
 * remontées incomplètes : une question qui n'alimente aucune dimension
 * fausserait le profil sans que l'élève puisse s'en apercevoir.
 */
class QuestionnaireRepository(private val questionRiasecDao: QuestionRiasecDao) {

    val questions: Flow<List<QuestionRiasec>> =
        questionRiasecDao.toutes().map { entites -> entites.mapNotNull { it.versDomaine() } }
}
