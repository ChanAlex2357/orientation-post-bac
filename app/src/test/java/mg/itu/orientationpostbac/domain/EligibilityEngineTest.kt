package mg.itu.orientationpostbac.domain

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test 1 des 3 spécifiés par le document (§2.2.1) : une série non
 * admissible produit une exclusion assortie d'un motif.
 */
class EligibilityEngineTest {

    @Test
    fun `serie non admissible produit une exclusion motivee`() {
        val formation = formationDeTest(seriesAdmissibles = setOf(Serie.C))
        val profil = profilDeTest(serie = Serie.D)

        val resultat = EligibilityEngine.evaluer(formation, profil)

        assertTrue(resultat is EligibilityResult.NotEligible)
        val motifs = (resultat as EligibilityResult.NotEligible).reasons
        assertTrue(motifs.any { it is BlockingReason.SerieNonAdmissible })
    }
}
