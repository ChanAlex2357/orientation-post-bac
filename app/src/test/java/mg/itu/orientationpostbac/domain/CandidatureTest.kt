package mg.itu.orientationpostbac.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CandidatureTest {

    @Test
    fun `les 4 demarches du document sont toujours proposees`() {
        val etapes = etapesPour(
            formationDeTest().copy(statutReconnaissance = RecognitionStatus.VERIFIEE),
        )

        assertEquals(4, etapes.size)
        assertTrue(etapes.any { it.contains("pieces") })
        assertTrue(etapes.any { it.contains("periode de depot") })
        assertTrue(etapes.any { it.contains("admission") })
        assertTrue(etapes.any { it.contains("Contacter") })
    }

    /**
     * Une filiere dont l'habilitation n'est pas etablie porte un
     * avertissement sur sa fiche. Prevenir puis ne rien proposer laisserait
     * l'eleve avec l'inquietude et sans le geste qui la leve.
     */
    @Test
    fun `une habilitation non etablie ajoute une demarche de verification`() {
        listOf(RecognitionStatus.A_CONFIRMER, RecognitionStatus.NON_VERIFIEE).forEach { statut ->
            val etapes = etapesPour(formationDeTest().copy(statutReconnaissance = statut))

            assertEquals("Statut $statut", 5, etapes.size)
            assertTrue(
                "Statut $statut",
                etapes.any { it.contains("arrete d'habilitation") },
            )
        }
    }

    @Test
    fun `une habilitation verifiee n'ajoute pas cette demarche`() {
        val etapes = etapesPour(
            formationDeTest().copy(statutReconnaissance = RecognitionStatus.VERIFIEE),
        )
        assertFalse(etapes.any { it.contains("arrete d'habilitation") })
    }

    @Test
    fun `la demarche d'admission reprend le mode de la formation`() {
        val etapes = etapesPour(formationDeTest(modeAdmission = "Concours"))
        assertTrue(etapes.any { it.contains("concours") })
    }
}
