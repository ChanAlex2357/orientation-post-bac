package mg.itu.orientationpostbac.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests 2 et 3 des 3 spécifiés par le document (§2.2.1) :
 * - un budget insuffisant produit un score pénalisé mais une formation visible ;
 * - un score d'intérêts inférieur à 50 % déclenche le plafonnement de bande
 *   (même comportement que "Licence Gestion C" au tableau 11 : score 79,
 *   bande PARTIAL et non GOOD à cause des intérêts).
 */
class ScoringEngineTest {

    @Test
    fun `budget insuffisant penalise le score mais la formation reste visible`() {
        val formation = formationDeTest(coutIndicatif = 10_000_000)
        val profil = profilDeTest(budgetMaxAriary = 5_000_000)

        // Le budget n'est jamais bloquant (tableau 8) : la formation reste visible.
        assertEquals(EligibilityResult.Eligible, EligibilityEngine.evaluer(formation, profil))

        val resultat = ScoringEngine.evaluer(formation, profil)

        assertEquals(50, resultat.breakdown.getValue(Criterion.BUDGET))
        assertTrue(resultat.score < 100)
    }

    @Test
    fun `score d'interets sous 50 pour cent plafonne la bande a PARTIAL`() {
        val formation = formationDeTest(riasecProfile = RiasecProfile(0, 0, 0, 0, 0, 0))
        val profil = profilDeTest(riasecProfile = RiasecProfile(60, 60, 60, 60, 60, 60))

        val resultat = ScoringEngine.evaluer(formation, profil)

        assertEquals(40, resultat.breakdown.getValue(Criterion.INTERESTS))
        assertEquals(79, resultat.score)
        assertEquals(MatchBand.PARTIAL, resultat.band)
    }
}
