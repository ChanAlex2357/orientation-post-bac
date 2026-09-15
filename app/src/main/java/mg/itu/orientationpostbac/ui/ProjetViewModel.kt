package mg.itu.orientationpostbac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mg.itu.orientationpostbac.data.AppDatabase
import mg.itu.orientationpostbac.data.FormationRepository
import mg.itu.orientationpostbac.data.SelectionRepository
import mg.itu.orientationpostbac.domain.EtapeChecklist
import mg.itu.orientationpostbac.domain.Formation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * État de l'écran Mon projet. Séparé du CatalogueViewModel : consulter des
 * formations et préparer ses candidatures sont deux moments distincts, et
 * l'écran Mon projet reste utile même quand le catalogue n'est pas ouvert.
 *
 * L'ajout à la shortlist illustre le flux unidirectionnel de la séance 6 :
 * l'écran remonte un événement, le ViewModel écrit en base, le Flow ré-émet,
 * le StateFlow produit un nouvel état et l'écran se recompose. À aucun
 * moment l'écran ne modifie lui-même la liste qu'il affiche.
 */
data class CandidatureDetaillee(
    val formation: Formation,
    val etapes: List<EtapeChecklist>,
) {
    val etapesFaites: Int = etapes.count { it.fait }
}

data class EtatProjet(
    val candidatures: List<CandidatureDetaillee> = emptyList(),
)

class ProjetViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.obtenir(application)
    private val selectionRepository = SelectionRepository(
        database.selectionCandidatureDao(),
        database.etapeChecklistDao(),
    )
    private val formationRepository = FormationRepository(
        database.formationDao(),
        database.etablissementDao(),
        database.questionRiasecDao(),
    )

    val etat: StateFlow<EtatProjet> = combine(
        selectionRepository.candidatures,
        selectionRepository.etapes,
        formationRepository.formations,
    ) { candidatures, etapes, formations ->
        val parId = formations.associateBy { it.id }
        val etapesParFormation = etapes.groupBy { it.formationId }

        EtatProjet(
            candidatures = candidatures.mapNotNull { candidature ->
                // Une candidature dont la formation a disparu du catalogue est
                // ignoree plutot qu'affichee a moitie.
                val formation = parId[candidature.formationId] ?: return@mapNotNull null
                CandidatureDetaillee(
                    formation = formation,
                    etapes = etapesParFormation[candidature.formationId].orEmpty(),
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EtatProjet(),
    )

    fun ajouter(formation: Formation) {
        viewModelScope.launch {
            selectionRepository.ajouter(formation, dateDuJour())
        }
    }

    /**
     * SimpleDateFormat et non java.time : LocalDate demande l'API 26 alors
     * que le projet vise minSdk 24 (document 2.3.1, parc d'entree de gamme).
     * Le compilateur ne l'aurait pas signale, l'application aurait plante a
     * l'ajout d'une candidature sur les telephones justement vises.
     */
    private fun dateDuJour(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun retirer(formationId: String) {
        viewModelScope.launch { selectionRepository.retirer(formationId) }
    }

    fun marquerEtape(etapeId: Int, fait: Boolean) {
        viewModelScope.launch { selectionRepository.marquer(etapeId, fait) }
    }
}
