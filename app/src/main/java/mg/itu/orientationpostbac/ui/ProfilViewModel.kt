package mg.itu.orientationpostbac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mg.itu.orientationpostbac.data.AppDatabase
import mg.itu.orientationpostbac.data.ProfilRepository
import mg.itu.orientationpostbac.data.QuestionnaireRepository
import mg.itu.orientationpostbac.domain.QuestionRiasec
import mg.itu.orientationpostbac.domain.Serie
import mg.itu.orientationpostbac.domain.UserConstraints
import mg.itu.orientationpostbac.domain.UserPreferences
import mg.itu.orientationpostbac.domain.UserProfile
import mg.itu.orientationpostbac.domain.calculerProfilRiasec
import mg.itu.orientationpostbac.domain.matieresClesDe

/**
 * État des écrans Profil et Questionnaire. Un seul ViewModel pour les deux :
 * ils produisent le même objet, le `UserProfile`, et le document rappelle que
 * « une information demandée une fois n'est plus redemandée ailleurs »
 * (§2.2.2).
 *
 * Point de la séance 3 que le document souligne explicitement : le système
 * détruit et recrée l'Activity à la rotation, et `remember` n'y survit pas.
 * Un questionnaire de 12 questions perdu à la rotation serait rédhibitoire.
 * L'état de saisie vit donc ICI et jamais dans un `remember` d'écran.
 */
data class EtatProfil(
    val serie: Serie? = null,
    val niveauParMatiere: Map<String, Int> = emptyMap(),
    val budgetMaxAriary: Int? = null,
    val localisationSouhaitee: String? = null,
    val dureeMaxAnnees: Int? = null,
    val questions: List<QuestionRiasec> = emptyList(),
    val reponses: Map<Int, Int> = emptyMap(),
    val profilEnregistre: UserProfile? = null,
) {
    /** Les matières à proposer découlent de la série (document §2.2.2). */
    val matieresAProposer: List<String> = serie?.let { matieresClesDe(it) } ?: emptyList()

    val questionnaireComplet: Boolean =
        questions.isNotEmpty() && questions.all { it.id in reponses }

    val questionsRepondues: Int = questions.count { it.id in reponses }

    /** La série est la seule saisie indispensable : sans elle, aucun filtre d'éligibilité. */
    val peutEnregistrer: Boolean = serie != null
}

class ProfilViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.obtenir(application)
    private val profilRepository = ProfilRepository(database.profilUtilisateurDao())
    private val questionnaireRepository = QuestionnaireRepository(database.questionRiasecDao())

    private val saisie = MutableStateFlow(SaisieEnCours())

    val etat: StateFlow<EtatProfil> = combine(
        saisie,
        questionnaireRepository.questions,
        profilRepository.profil,
    ) { saisieCourante, questions, profilEnregistre ->
        EtatProfil(
            serie = saisieCourante.serie,
            niveauParMatiere = saisieCourante.niveauParMatiere,
            budgetMaxAriary = saisieCourante.budgetMaxAriary,
            localisationSouhaitee = saisieCourante.localisationSouhaitee,
            dureeMaxAnnees = saisieCourante.dureeMaxAnnees,
            questions = questions,
            reponses = saisieCourante.reponses,
            profilEnregistre = profilEnregistre,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EtatProfil(),
    )

    init {
        // Un profil deja enregistre repeuple la saisie : l'eleve retrouve ses
        // reponses, il ne recommence pas.
        viewModelScope.launch {
            profilRepository.profil.collect { profil ->
                if (profil != null && saisie.value.serie == null) {
                    saisie.value = saisie.value.copy(
                        serie = profil.serie,
                        niveauParMatiere = profil.niveauParMatiere,
                        budgetMaxAriary = profil.constraints.budgetMaxAriary,
                        localisationSouhaitee = profil.constraints.localisationSouhaitee,
                        dureeMaxAnnees = profil.constraints.dureeMaxAnnees,
                    )
                }
            }
        }
    }

    fun choisirSerie(serie: Serie) {
        // Changer de serie change les matieres proposees : les niveaux saisis
        // pour l'ancienne serie n'ont plus de sens, on ne les traine pas.
        saisie.value = saisie.value.copy(serie = serie, niveauParMatiere = emptyMap())
    }

    fun reglerNiveau(matiere: String, niveau: Int) {
        saisie.value = saisie.value.copy(
            niveauParMatiere = saisie.value.niveauParMatiere + (matiere to niveau.coerceIn(0, 100)),
        )
    }

    fun reglerBudget(budgetMaxAriary: Int?) {
        saisie.value = saisie.value.copy(budgetMaxAriary = budgetMaxAriary)
    }

    fun reglerLocalisation(localisation: String?) {
        saisie.value = saisie.value.copy(localisationSouhaitee = localisation?.takeIf { it.isNotBlank() })
    }

    fun reglerDuree(dureeMaxAnnees: Int?) {
        saisie.value = saisie.value.copy(dureeMaxAnnees = dureeMaxAnnees)
    }

    fun repondre(questionId: Int, valeur: Int) {
        saisie.value = saisie.value.copy(reponses = saisie.value.reponses + (questionId to valeur))
    }

    fun enregistrer() {
        val etatCourant = etat.value
        val serie = etatCourant.serie ?: return

        val profil = UserProfile(
            serie = serie,
            riasecProfile = calculerProfilRiasec(etatCourant.questions, etatCourant.reponses),
            niveauParMatiere = etatCourant.niveauParMatiere,
            constraints = UserConstraints(
                budgetMaxAriary = etatCourant.budgetMaxAriary,
                localisationSouhaitee = etatCourant.localisationSouhaitee,
                dureeMaxAnnees = etatCourant.dureeMaxAnnees,
            ),
            preferences = UserPreferences(),
        )
        viewModelScope.launch { profilRepository.enregistrer(profil) }
    }
}

/** Saisie en cours, avant enregistrement. Séparée de l'état exposé à l'écran. */
private data class SaisieEnCours(
    val serie: Serie? = null,
    val niveauParMatiere: Map<String, Int> = emptyMap(),
    val budgetMaxAriary: Int? = null,
    val localisationSouhaitee: String? = null,
    val dureeMaxAnnees: Int? = null,
    val reponses: Map<Int, Int> = emptyMap(),
)
