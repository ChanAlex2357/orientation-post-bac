package mg.itu.orientationpostbac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import mg.itu.orientationpostbac.data.AppDatabase
import mg.itu.orientationpostbac.data.FormationRepository
import mg.itu.orientationpostbac.ui.CatalogueViewModel
import mg.itu.orientationpostbac.ui.EcranFormations
import mg.itu.orientationpostbac.ui.EcranParcours
import mg.itu.orientationpostbac.ui.EcranProfil
import mg.itu.orientationpostbac.ui.EcranQuestionnaire
import mg.itu.orientationpostbac.ui.ProfilViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pre-remplissage au lancement, en un seul endroit : les ViewModels
        // supposent ensuite que la base repond, sans avoir chacun a s'en
        // assurer. L'operation est gardee par un comptage, donc rejouable.
        val database = AppDatabase.obtenir(this)
        lifecycleScope.launch {
            FormationRepository(
                database.formationDao(),
                database.etablissementDao(),
                database.questionRiasecDao(),
            ).preparerDonnees()
        }

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppOrientation()
                }
            }
        }
    }
}

object Routes {
    const val PROFIL = "profil"
    const val QUESTIONNAIRE = "questionnaire"
    const val PARCOURS = "parcours"
    const val FORMATIONS = "formations"
}

/**
 * Navigation, reprise du patron du mini-TP 5 : un `NavHost`, une route par
 * écran, `popBackStack()` pour le retour.
 *
 * Elle apparaît dès le deuxième écran plutôt qu'à l'US6.1 comme prévu au
 * backlog : un aiguillage maison, écrit puis jeté, aurait coûté plus cher
 * que le `NavHost` lui-même. L'US6.1 le complètera avec les routes
 * restantes et l'argument `detail/{formationId}`.
 *
 * Les 2 écrans partagent le même ProfilViewModel, obtenu ici : le profil et
 * le questionnaire remplissent un seul et même objet.
 */
@Composable
private fun AppOrientation() {
    val navController = rememberNavController()
    val profilViewModel: ProfilViewModel = viewModel()
    val catalogueViewModel: CatalogueViewModel = viewModel()
    val etat by profilViewModel.etat.collectAsState()
    val etatCatalogue by catalogueViewModel.etat.collectAsState()

    NavHost(navController = navController, startDestination = Routes.PROFIL) {

        composable(Routes.PROFIL) {
            EcranProfil(
                etat = etat,
                onSerieChoisie = profilViewModel::choisirSerie,
                onNiveauRegle = profilViewModel::reglerNiveau,
                onBudgetRegle = profilViewModel::reglerBudget,
                onLocalisationReglee = profilViewModel::reglerLocalisation,
                onDureeReglee = profilViewModel::reglerDuree,
                onEnregistrer = profilViewModel::enregistrer,
                onContinuer = { navController.navigate(Routes.QUESTIONNAIRE) },
            )
        }

        composable(Routes.QUESTIONNAIRE) {
            EcranQuestionnaire(
                etat = etat,
                onRepondre = profilViewModel::repondre,
                onTerminer = {
                    // Le profil est reenregistre : les reponses du
                    // questionnaire changent le RiasecProfile calcule, et
                    // c'est cet enregistrement que le catalogue observe.
                    profilViewModel.enregistrer()
                    navController.navigate(Routes.PARCOURS)
                },
                onRetour = { navController.popBackStack() },
            )
        }

        composable(Routes.PARCOURS) {
            EcranParcours(
                etat = etatCatalogue,
                reponsesDonnees = etat.questionsRepondues,
                questionsTotal = etat.questions.size,
                onCompleterQuestionnaire = { navController.navigate(Routes.QUESTIONNAIRE) },
                onDomaineChoisi = { domaine ->
                    catalogueViewModel.filtrerParDomaine(domaine)
                    navController.navigate(Routes.FORMATIONS)
                },
                onVoirToutesLesFormations = {
                    catalogueViewModel.filtrerParDomaine(null)
                    navController.navigate(Routes.FORMATIONS)
                },
                onModifierProfil = { navController.popBackStack(Routes.PROFIL, inclusive = false) },
            )
        }

        composable(Routes.FORMATIONS) {
            EcranFormations(
                etat = etatCatalogue,
                onRechercher = catalogueViewModel::rechercher,
                onRetirerFiltre = { catalogueViewModel.filtrerParDomaine(null) },
                onFormationChoisie = { /* US5.5 : detail/{formationId} */ },
            )
        }
    }
}
