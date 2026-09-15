package mg.itu.orientationpostbac.data

import mg.itu.orientationpostbac.domain.RecognitionStatus
import mg.itu.orientationpostbac.domain.Serie
import mg.itu.orientationpostbac.domain.TypeEtablissement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Le jeu de données est saisi à la main, colonne par colonne : une faute de
 * frappe dans un identifiant d'établissement ou dans une valeur d'enum ne se
 * verrait qu'à l'exécution, une fois la base remplie. Ces tests la font
 * échouer à la compilation des tests, donc avant l'émulateur.
 */
class JeuDeDonneesTest {

    @Test
    fun `les identifiants sont uniques`() {
        assertEquals(
            etablissementsInitiaux.size,
            etablissementsInitiaux.map { it.id }.distinct().size,
        )
        assertEquals(
            formationsInitiales.size,
            formationsInitiales.map { it.id }.distinct().size,
        )
    }

    @Test
    fun `chaque formation pointe vers un etablissement existant`() {
        val idsConnus = etablissementsInitiaux.map { it.id }.toSet()
        val orphelines = formationsInitiales.filter { it.etablissementId !in idsConnus }
        assertTrue("Formations sans etablissement : $orphelines", orphelines.isEmpty())
    }

    @Test
    fun `les colonnes texte se relisent en valeurs du domaine`() {
        formationsInitiales.forEach { formation ->
            formation.seriesAdmissibles.split(",").forEach { serie -> Serie.valueOf(serie) }
            RecognitionStatus.valueOf(formation.statutReconnaissance)
            TypeEtablissement.valueOf(formation.typeEtablissement)
        }
        etablissementsInitiaux.forEach { etablissement ->
            RecognitionStatus.valueOf(etablissement.statutVerification)
            TypeEtablissement.valueOf(etablissement.type)
        }
    }

    @Test
    fun `le cas reel ENI porte le meme arrete sur deux grades et deux statuts`() {
        val eni = formationsInitiales.filter { it.etablissementId == "E001" }

        assertEquals(2, eni.size)
        assertEquals(1, eni.map { it.referenceArrete }.distinct().size)
        assertEquals(setOf("L", "M"), eni.map { it.grade }.toSet())
        assertEquals(
            setOf("A_CONFIRMER", "VERIFIEE"),
            eni.map { it.statutReconnaissance }.toSet(),
        )
    }

    @Test
    fun `aucune formation fictive ne porte de reference d'arrete inventee`() {
        val fictivesAvecArrete = formationsInitiales
            .filter { it.sourceInformation.startsWith("Donnee fictive") }
            .filter { it.referenceArrete.isNotBlank() }

        assertTrue("Arretes inventes : $fictivesAvecArrete", fictivesAvecArrete.isEmpty())
    }

    @Test
    fun `le jeu de donnees couvre les 6 domaines du tableau 17`() {
        assertEquals(6, formationsInitiales.map { it.domaine }.distinct().size)
    }
}
