package mg.itu.orientationpostbac.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionnaireRiasecTest {

    private val questions = listOf(
        QuestionRiasec(1, 1, "R1", RiasecDimension.REALISTE),
        QuestionRiasec(2, 2, "R2", RiasecDimension.REALISTE),
        QuestionRiasec(3, 3, "I1", RiasecDimension.INVESTIGATEUR),
        QuestionRiasec(4, 4, "I2", RiasecDimension.INVESTIGATEUR),
        QuestionRiasec(5, 5, "S1", RiasecDimension.SOCIAL),
    )

    @Test
    fun `les reponses maximales donnent 100 sur la dimension`() {
        val profil = calculerProfilRiasec(questions, mapOf(1 to 4, 2 to 4))
        assertEquals(100, profil.realiste)
    }

    @Test
    fun `les reponses minimales donnent 0`() {
        val profil = calculerProfilRiasec(questions, mapOf(1 to 0, 2 to 0))
        assertEquals(0, profil.realiste)
    }

    @Test
    fun `chaque dimension est moyennee sur ses propres questions`() {
        val profil = calculerProfilRiasec(questions, mapOf(3 to 4, 4 to 2))
        assertEquals(75, profil.investigateur)
        assertEquals(0, profil.realiste)
    }

    @Test
    fun `une question sans reponse est ignoree et non comptee comme un zero`() {
        // Une seule des 2 questions Realiste est repondue, au maximum.
        val profil = calculerProfilRiasec(questions, mapOf(1 to 4))

        // Moyenne sur la seule reponse donnee : 100, et non 50.
        assertEquals(100, profil.realiste)
    }

    @Test
    fun `une dimension sans aucune reponse vaut 0`() {
        val profil = calculerProfilRiasec(questions, mapOf(1 to 4))
        assertEquals(0, profil.social)
        assertEquals(0, profil.conventionnel)
    }

    @Test
    fun `une valeur hors echelle est ramenee dans les bornes`() {
        val profil = calculerProfilRiasec(questions, mapOf(1 to 99, 2 to -5))
        assertEquals(50, profil.realiste)
    }
}
