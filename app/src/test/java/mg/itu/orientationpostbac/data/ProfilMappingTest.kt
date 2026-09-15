package mg.itu.orientationpostbac.data

import mg.itu.orientationpostbac.domain.RiasecProfile
import mg.itu.orientationpostbac.domain.Serie
import mg.itu.orientationpostbac.domain.UserConstraints
import mg.itu.orientationpostbac.domain.UserPreferences
import mg.itu.orientationpostbac.domain.UserProfile
import mg.itu.orientationpostbac.domain.matieresClesDe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilMappingTest {

    private val profil = UserProfile(
        serie = Serie.D,
        riasecProfile = RiasecProfile(72, 85, 30, 40, 45, 60),
        niveauParMatiere = mapOf("Mathematiques" to 80, "Physique" to 65),
        constraints = UserConstraints(
            budgetMaxAriary = 500_000,
            localisationSouhaitee = "Antananarivo",
            dureeMaxAnnees = 5,
        ),
        preferences = UserPreferences(modeAdmissionAccepte = setOf("Dossier")),
    )

    @Test
    fun `un profil survit a l'aller-retour vers la base`() {
        assertEquals(profil, profil.versEntite().versDomaine())
    }

    @Test
    fun `un profil sans contrainte declaree survit aussi`() {
        val sansContrainte = profil.copy(
            constraints = UserConstraints(null, null, null),
            preferences = UserPreferences(),
        )
        assertEquals(sansContrainte, sansContrainte.versEntite().versDomaine())
    }

    @Test
    fun `une serie illisible rend le profil inutilisable plutot que devine`() {
        val corrompu = profil.versEntite().copy(serie = "Z9")
        assertNull(corrompu.versDomaine())
    }

    /**
     * Le critère LEVEL du ScoringEngine croise `Formation.matieresCles` avec
     * les niveaux déclarés par l'élève. Si les libellés divergent, le moteur
     * retombe silencieusement sur sa valeur neutre et le critère ne sert plus
     * à rien — sans aucune erreur visible. Ce test verrouille la cohérence
     * des deux vocabulaires.
     */
    @Test
    fun `les matieres du jeu de donnees sont declarables par au moins une serie`() {
        val matieresDeclarables = Serie.entries.flatMap { matieresClesDe(it) }.toSet()
        val matieresDemandees = formationsInitiales
            .flatMap { texteVersListe(it.matieresCles) }
            .toSet()

        val orphelines = matieresDemandees - matieresDeclarables
        assertTrue(
            "Matieres exigees par une formation mais qu'aucun eleve ne peut declarer : $orphelines",
            orphelines.isEmpty(),
        )
    }

    @Test
    fun `chaque serie propose 3 a 5 matieres`() {
        Serie.entries.forEach { serie ->
            val matieres = matieresClesDe(serie)
            assertTrue("$serie : ${matieres.size} matieres", matieres.size in 3..5)
        }
    }
}
