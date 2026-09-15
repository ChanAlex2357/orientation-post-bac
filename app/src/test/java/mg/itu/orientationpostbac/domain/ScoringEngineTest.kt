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

    /**
     * Verrou de non-regression sur la ponderation des interets.
     *
     * Deux formations face a un eleve nettement Investigateur : l'une colle a
     * sa dimension dominante, l'autre s'en ecarte mais s'accorde sur cinq
     * dimensions qui ne l'interessent pas. Avec une moyenne simple, la
     * seconde gagnait. C'est ce qui faisait passer une formation de
     * maintenance devant une licence d'informatique dans CatalogueTest.
     */
    @Test
    fun `la dimension dominante pese plus que des accords sur des dimensions faibles`() {
        val profil = profilDeTest(
            riasecProfile = RiasecProfile(
                realiste = 20,
                investigateur = 90,
                artistique = 10,
                social = 10,
                entreprenant = 10,
                conventionnel = 10,
            ),
        )
        val surLaDominante = formationDeTest(
            riasecProfile = RiasecProfile(60, 90, 40, 40, 40, 40),
        )
        val surLesDimensionsFaibles = formationDeTest(
            riasecProfile = RiasecProfile(20, 40, 10, 10, 10, 10),
        )

        val scoreDominante = ScoringEngine.evaluer(surLaDominante, profil)
            .breakdown.getValue(Criterion.INTERESTS)
        val scoreFaibles = ScoringEngine.evaluer(surLesDimensionsFaibles, profil)
            .breakdown.getValue(Criterion.INTERESTS)

        assertTrue(
            "Coller a la dimension dominante ($scoreDominante) doit primer sur " +
                "l'accord avec des dimensions sans interet ($scoreFaibles)",
            scoreDominante > scoreFaibles,
        )
    }
}
