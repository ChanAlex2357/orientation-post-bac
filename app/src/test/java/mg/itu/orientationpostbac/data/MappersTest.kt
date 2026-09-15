package mg.itu.orientationpostbac.data

import mg.itu.orientationpostbac.domain.RecognitionStatus
import mg.itu.orientationpostbac.domain.Serie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Les conventions de sérialisation ("C,D", "Maths:80;Physique:60") ne sont
 * vérifiées par aucun compilateur : une virgule en trop ne casse rien à la
 * compilation et vide simplement un ensemble de séries à l'exécution — donc
 * une formation qui n'apparaît plus, sans erreur nulle part.
 */
class MappersTest {

    @Test
    fun `tout le jeu de donnees se convertit en domaine`() {
        val formations = formationsInitiales.map { it.versDomaine() }
        val etablissements = etablissementsInitiaux.map { it.versDomaine() }

        assertEquals(formationsInitiales.size, formations.size)
        assertEquals(etablissementsInitiaux.size, etablissements.size)
        assertTrue(
            "Formations sans serie admissible : " + formations.filter { it.seriesAdmissibles.isEmpty() },
            formations.none { it.seriesAdmissibles.isEmpty() },
        )
        assertTrue(
            "Formations sans matiere cle : " + formations.filter { it.matieresCles.isEmpty() },
            formations.none { it.matieresCles.isEmpty() },
        )
    }

    @Test
    fun `le cas reel ENI se relit correctement`() {
        val licence = formationsInitiales.first { it.id == "F001" }.versDomaine()
        val master = formationsInitiales.first { it.id == "F002" }.versDomaine()

        assertEquals(setOf(Serie.C, Serie.D), licence.seriesAdmissibles)
        assertEquals(listOf("Mathematiques", "Physique"), licence.matieresCles)
        assertEquals(RecognitionStatus.A_CONFIRMER, licence.statutReconnaissance)
        assertEquals(RecognitionStatus.VERIFIEE, master.statutReconnaissance)
        assertEquals(licence.referenceArrete, master.referenceArrete)
        assertEquals(90, licence.riasecProfile.investigateur)
    }

    @Test
    fun `un cout non renseigne reste null jusqu'au domaine`() {
        val sansCout = formationsInitiales.first().copy(coutIndicatif = null).versDomaine()
        assertNull(sansCout.coutIndicatif)
    }

    @Test
    fun `une serie illisible est ignoree, pas devinee`() {
        assertEquals(setOf(Serie.C, Serie.D), texteVersSeries("C,D"))
        assertEquals(setOf(Serie.C), texteVersSeries("C,ZZ"))
        assertEquals(emptySet<Serie>(), texteVersSeries(""))
        assertEquals(setOf(Serie.C, Serie.D), texteVersSeries(" C , D , "))
    }

    @Test
    fun `un statut illisible devient NON_VERIFIEE`() {
        assertEquals(RecognitionStatus.VERIFIEE, texteVersStatut("VERIFIEE"))
        assertEquals(RecognitionStatus.NON_VERIFIEE, texteVersStatut(""))
        assertEquals(RecognitionStatus.NON_VERIFIEE, texteVersStatut("habilitee"))
    }

    @Test
    fun `les niveaux par matiere se relisent, les paires cassees sont ignorees`() {
        assertEquals(
            mapOf("Mathematiques" to 80, "Physique" to 60),
            texteVersNiveaux("Mathematiques:80;Physique:60"),
        )
        assertEquals(mapOf("Mathematiques" to 80), texteVersNiveaux("Mathematiques:80;Physique"))
        assertEquals(mapOf("Mathematiques" to 80), texteVersNiveaux("Mathematiques:80;:60"))
        assertEquals(emptyMap<String, Int>(), texteVersNiveaux(""))
    }
}
