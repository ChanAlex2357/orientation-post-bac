package mg.itu.orientationpostbac.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mg.itu.orientationpostbac.domain.Candidature
import mg.itu.orientationpostbac.domain.EtapeChecklist
import mg.itu.orientationpostbac.domain.Formation
import mg.itu.orientationpostbac.domain.etapesPour

/**
 * La shortlist et ses démarches. « La shortlist est un enregistrement que
 * l'on interroge, donc Room ; un réglage comme le tri par défaut relèverait
 * de DataStore » (document §2.2.4).
 *
 * L'ajout crée la candidature ET ses étapes : une candidature sans
 * démarches laisserait l'élève au moment où il a le plus besoin d'aide,
 * juste après avoir choisi.
 */
class SelectionRepository(
    private val selectionDao: SelectionCandidatureDao,
    private val etapeDao: EtapeChecklistDao,
) {

    val candidatures: Flow<List<Candidature>> =
        selectionDao.toutes().map { entites -> entites.map { it.versDomaine() } }

    val etapes: Flow<List<EtapeChecklist>> =
        etapeDao.toutes().map { entites -> entites.map { it.versDomaine() } }

    suspend fun ajouter(formation: Formation, dateAjout: String) {
        selectionDao.ajouter(
            SelectionCandidatureEntity(formationId = formation.id, dateAjout = dateAjout),
        )
        // Les etapes sont regenerees : reajouter une candidature retiree ne
        // doit pas laisser les anciennes lignes derriere elle.
        etapeDao.supprimerPourFormation(formation.id)
        etapeDao.insererTous(
            etapesPour(formation).mapIndexed { index, libelle ->
                EtapeChecklistEntity(
                    formationId = formation.id,
                    ordre = index,
                    libelle = libelle,
                    fait = false,
                )
            },
        )
    }

    suspend fun retirer(formationId: String) {
        selectionDao.retirer(formationId)
        etapeDao.supprimerPourFormation(formationId)
    }

    suspend fun marquer(etapeId: Int, fait: Boolean) {
        etapeDao.marquer(etapeId, fait)
    }
}

fun SelectionCandidatureEntity.versDomaine(): Candidature =
    Candidature(formationId = formationId, dateAjout = dateAjout)

fun EtapeChecklistEntity.versDomaine(): EtapeChecklist = EtapeChecklist(
    id = id,
    formationId = formationId,
    ordre = ordre,
    libelle = libelle,
    fait = fait,
)
