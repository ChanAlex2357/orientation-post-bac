package mg.itu.orientationpostbac.domain

import mg.itu.orientationpostbac.data.formationsInitiales
import mg.itu.orientationpostbac.data.versDomaine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Le moteur (Épopée 1) confronté au vrai jeu de données (Épopée 2), sur le
 * profil de l'Annexe B : Toky, Terminale D à Antananarivo, intérêts dominés
 * par l'Investigateur, budget limité, 3 à 5 ans d'études.
 *
 * Ces tests valident le scénario de démonstration avant qu'un seul écran
 * n'existe : si une étape de l'Annexe B ne peut pas se produire, elle échoue
 * ici plutôt que pendant la soutenance.
 */
class CatalogueTest {

    private val toky = UserProfile(
        serie = Serie.D,
        riasecProfile = RiasecProfile(
            realiste = 72,
            investigateur = 85,
            artistique = 30,
            social = 40,
            entreprenant = 40,
            conventionnel = 60,
        ),
        niveauParMatiere = mapOf(
            "Mathematiques" to 75,
            "Physique" to 70,
            "Sciences de la vie et de la Terre" to 65,
        ),
        constraints = UserConstraints(
            budgetMaxAriary = 1_000_000,
            localisationSouhaitee = "Antananarivo",
            dureeMaxAnnees = 5,
        ),
        preferences = UserPreferences(),
    )

    private val catalogue = formationsInitiales.map { it.versDomaine() }
    private val evaluees = evaluerCatalogue(catalogue, toky)

    private fun evaluee(id: String) = evaluees.first { it.formation.id == id }

    @Test
    fun `sans profil, rien n'est juge`() {
        val brut = evaluerCatalogue(catalogue, null)

        assertEquals(catalogue.size, brut.size)
        assertTrue(brut.none { it.evaluee })
        assertTrue(brut.all { it.compatibilite == null })
        assertTrue("Aucune formation ne doit etre exclue faute de profil", brut.all { it.eligible })
    }

    @Test
    fun `etape 24 - une formation reservee aux series litteraires est non eligible pour un serie D`() {
        val commerce = evaluee("F009")

        assertFalse(commerce.eligible)
        assertTrue(commerce.motifs.any { it is BlockingReason.SerieNonAdmissible })
        assertNull("Une formation non eligible n'est pas notee", commerce.compatibilite)
    }

    @Test
    fun `etape 23 - le Master ENI est non eligible pour un terminale, la Licence l'est`() {
        val master = evaluee("F002")
        val licence = evaluee("F001")

        assertFalse(master.eligible)
        assertTrue(master.motifs.any { it is BlockingReason.NiveauInsuffisant })
        assertTrue(licence.eligible)

        // Le point du document : meme etablissement, meme mention, meme arrete,
        // mais deux statuts de reconnaissance differents.
        assertEquals(licence.formation.mention, master.formation.mention)
        assertEquals(licence.formation.referenceArrete, master.formation.referenceArrete)
        assertEquals(RecognitionStatus.A_CONFIRMER, licence.formation.statutReconnaissance)
        assertEquals(RecognitionStatus.VERIFIEE, master.formation.statutReconnaissance)
    }

    @Test
    fun `une formation non eligible reste affichee, avec son motif`() {
        val nonEligibles = classerNonEligibles(evaluees)

        assertTrue(nonEligibles.isNotEmpty())
        assertTrue(
            "Tout motif doit etre exploitable par l'ecran",
            nonEligibles.all { it.motifs.isNotEmpty() },
        )
        assertEquals(catalogue.size, classerEligibles(evaluees).size + nonEligibles.size)
    }

    @Test
    fun `les eligibles sont classes du meilleur score au moins bon`() {
        val scores = classerEligibles(evaluees).map { it.compatibilite?.score ?: -1 }

        assertTrue("Tous les eligibles sont notes", scores.none { it == -1 })
        assertEquals(scores.sortedDescending(), scores)
    }

    @Test
    fun `etape 20 - l'informatique remonte en tete pour un profil investigateur`() {
        val meilleur = classerEligibles(evaluees).first()

        assertEquals("Informatique", meilleur.formation.domaine)
        assertNotNull(meilleur.compatibilite)
    }

    @Test
    fun `etape 19 - les domaines sont classes par indice de compatibilite`() {
        val domaines = classerDomaines(evaluees)
        val indices = domaines.map { it.indice }

        assertTrue(domaines.isNotEmpty())
        assertEquals(indices.sortedDescending(), indices)
        assertEquals("Informatique", domaines.first().domaine)
        assertTrue("Un domaine compte au moins une formation", domaines.all { it.nombreFormations >= 1 })
    }

    @Test
    fun `le budget penalise sans exclure`() {
        // Sciences infirmieres coute 3 500 000 Ar, Toky a declare 1 000 000.
        val infirmier = evaluee("F005")

        assertTrue("Le budget n'est jamais bloquant (tableau 8)", infirmier.eligible)

        val scoreBudget = infirmier.compatibilite?.breakdown?.get(Criterion.BUDGET)
        assertNotNull(scoreBudget)
        assertTrue("Un cout superieur au budget doit penaliser", (scoreBudget ?: 100) < 100)
    }
}
