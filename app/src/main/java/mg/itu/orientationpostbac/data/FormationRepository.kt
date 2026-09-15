package mg.itu.orientationpostbac.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mg.itu.orientationpostbac.domain.Etablissement
import mg.itu.orientationpostbac.domain.Formation

/**
 * Le Repository de la séance 6 : il fournit les données et reste seul à
 * savoir d'où elles viennent. Au-dessus de lui, le ViewModel (Épopée 4) ne
 * manipule que des classes de domaine et ignore jusqu'à l'existence de Room.
 *
 * Couche volontairement fine : aucune règle métier ici. L'éligibilité et le
 * score restent dans le moteur (Épopée 1), qui ne dépend d'aucune source.
 *
 * La règle Flow/suspend de la séance 7 traverse cette couche sans changer :
 * le catalogue est un Flow, la recherche par identifiant une fonction suspend.
 */
class FormationRepository(
    private val formationDao: FormationDao,
    private val etablissementDao: EtablissementDao,
) {

    val formations: Flow<List<Formation>> =
        formationDao.toutes().map { entites -> entites.map { it.versDomaine() } }

    val etablissements: Flow<List<Etablissement>> =
        etablissementDao.tous().map { entites -> entites.map { it.versDomaine() } }

    fun rechercher(motCle: String): Flow<List<Formation>> =
        formationDao.rechercher(motCle).map { entites -> entites.map { it.versDomaine() } }

    suspend fun formationParId(id: String): Formation? =
        formationDao.parId(id)?.versDomaine()

    suspend fun etablissementParId(id: String): Etablissement? =
        etablissementDao.parId(id)?.versDomaine()

    /** Pré-remplissage au premier lancement : l'origine des données ne regarde que cette couche. */
    suspend fun preparerDonnees() {
        remplirSiVide(formationDao, etablissementDao)
    }
}
