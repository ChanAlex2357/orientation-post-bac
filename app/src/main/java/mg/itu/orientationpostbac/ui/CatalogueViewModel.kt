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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mg.itu.orientationpostbac.data.AppDatabase
import mg.itu.orientationpostbac.data.FormationRepository
import mg.itu.orientationpostbac.data.ProfilRepository
import mg.itu.orientationpostbac.domain.DomaineSuggere
import mg.itu.orientationpostbac.domain.Etablissement
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
    val domaineFiltre: String? = null,
    val profilRenseigne: Boolean = false,
    val eligibles: List<FormationEvaluee> = emptyList(),
    val nonEligibles: List<FormationEvaluee> = emptyList(),
    val domaines: List<DomaineSuggere> = emptyList(),
) {
    val total: Int = eligibles.size + nonEligibles.size
}

/**
 * Ce que l'ecran Detail affiche. L'etablissement peut manquer si la donnee
 * est incomplete : on l'affiche alors en moins, sans empecher la fiche.
 */
data class DetailFormation(
    val evaluee: FormationEvaluee,
    val etablissement: Etablissement?,
)

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
     * Le domaine choisi a l'ecran Parcours suggeres. C'est un filtre, pas une
     * identite : il vit dans le ViewModel plutot que dans la route. L'argument
     * de route reste reserve a ce qui designe une ressource, comme
     * detail/{formationId} (US6.1, patron du mini-TP 5).
     */
    private val domaine = MutableStateFlow<String?>(null)

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
        domaine,
    ) { formations, profil, motCle, domaineChoisi ->
        val retenues =
            if (domaineChoisi == null) formations
            else formations.filter { it.domaine == domaineChoisi }
        val evaluees = evaluerCatalogue(retenues, profil)
        EtatCatalogue(
            recherche = motCle,
            domaineFiltre = domaineChoisi,
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

    fun filtrerParDomaine(domaineChoisi: String?) {
        domaine.value = domaineChoisi
    }

    private val _detail = MutableStateFlow<DetailFormation?>(null)
    val detail: StateFlow<DetailFormation?> = _detail

    /**
     * L'ecran Detail ne recoit pas la formation, il la RETROUVE a partir de
     * l'identifiant porte par la route — "la route est du texte comme une
     * URL" (document 2.2.3, patron du mini-TP 5). La recherche par
     * identifiant est une requete suspend, pas un Flow : c'est une question
     * ponctuelle et non un etat a suivre (regle de la seance 7).
     */
    fun ouvrirDetail(formationId: String) {
        viewModelScope.launch {
            val formation = formationRepository.formationParId(formationId)
            if (formation == null) {
                _detail.value = null
                return@launch
            }
            val profil = profilRepository.profil.first()
            val etablissement = formationRepository.etablissementParId(formation.etablissementId)

            _detail.value = DetailFormation(
                evaluee = evaluerCatalogue(listOf(formation), profil).first(),
                etablissement = etablissement,
            )
        }
    }
}
