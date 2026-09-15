package mg.itu.orientationpostbac.data

import mg.itu.orientationpostbac.domain.RiasecDimension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionsRiasecTest {

    @Test
    fun `le questionnaire compte 12 a 15 items, tous convertibles`() {
        val questions = questionsRiasecInitiales.mapNotNull { it.versDomaine() }

        assertEquals(questionsRiasecInitiales.size, questions.size)
        assertTrue("12 a 15 items attendus", questions.size in 12..15)
    }

    @Test
    fun `les 6 dimensions sont couvertes a parts egales`() {
        val parDimension = questionsRiasecInitiales
            .mapNotNull { it.versDomaine() }
            .groupBy { it.dimension }

        assertEquals(RiasecDimension.entries.toSet(), parDimension.keys)
        assertEquals(
            "Une dimension pesant plus qu'une autre biaiserait le profil",
            1,
            parDimension.values.map { it.size }.distinct().size,
        )
    }

    @Test
    fun `les identifiants et les ordres sont uniques`() {
        assertEquals(
            questionsRiasecInitiales.size,
            questionsRiasecInitiales.map { it.id }.distinct().size,
        )
        assertEquals(
            questionsRiasecInitiales.size,
            questionsRiasecInitiales.map { it.ordre }.distinct().size,
        )
    }

    @Test
    fun `une dimension illisible ecarte la question plutot que de la deformer`() {
        assertNull(QuestionRiasecEntity(99, 99, "texte", "PHILOSOPHE").versDomaine())
    }
}
