package mg.itu.orientationpostbac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import mg.itu.orientationpostbac.data.AppDatabase
import mg.itu.orientationpostbac.data.FormationRepository
import mg.itu.orientationpostbac.data.ProfilRepository
import mg.itu.orientationpostbac.domain.DomaineSuggere
import mg.itu.orientationpostbac.domain.Formation
import mg.itu.orientationpostbac.domain.FormationEvaluee
import mg.itu.orientationpostbac.domain.classerDomaines
import mg.itu.orientationpostbac.domain.classerEligibles
import mg.itu.orientationpostbac.domain.classerNonEligibles
import mg.itu.orientationpostbac.domain.evaluerCatalogue

/**
 * État commun aux écrans Parcours suggérés, Formations et Détail : les 3
 * consomment le même calcul — le moteur appliqué au catalogue — et le
 * dupliquer dans 3 ViewModels ferait diverger 3 classements censés être le
 * même.
 */
data class EtatCatalogue(
    val recherche: String = "",
    val profilRenseigne: Boolean = false,
    val eligibles: List<FormationEvaluee> = emptyList(),
    val nonEligibles: List<FormationEvaluee> = emptyList(),
    val domaines: List<DomaineSuggere> = emptyList(),
) {
    val total: Int = eligibles.size + nonEligibles.size
}

class CatalogueViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.obtenir(application)
    private val formationRepository = FormationRepository(
        database.formationDao(),
        database.etablissementDao(),
        database.questionRiasecDao(),
    )
    private val profilRepository = ProfilRepository(database.profilUtilisateurDao())

    private val recherche = MutableStateFlow("")

    /**
     * La recherche change la SOURCE des formations, elle ne filtre pas la
     * liste déjà chargée : c'est une requête Room (séance 7), qui doit être
     * relancée à chaque frappe. flatMapLatest abandonne la requête précédente
     * dès qu'une nouvelle arrive, ce qu'un combine ne ferait pas.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val formationsAffichees: Flow<List<Formation>> =
        recherche.flatMapLatest { motCle ->
            if (motCle.isBlank()) formationRepository.formations
            else formationRepository.rechercher(motCle)
        }

    val etat: StateFlow<EtatCatalogue> = combine(
        formationsAffichees,
        profilRepository.profil,
        recherche,
    ) { formations, profil, motCle ->
        val evaluees = evaluerCatalogue(formations, profil)
        EtatCatalogue(
            recherche = motCle,
            profilRenseigne = profil != null,
            eligibles = classerEligibles(evaluees),
            nonEligibles = classerNonEligibles(evaluees),
            domaines = classerDomaines(evaluees),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EtatCatalogue(),
    )

    fun rechercher(motCle: String) {
        recherche.value = motCle
    }
}
