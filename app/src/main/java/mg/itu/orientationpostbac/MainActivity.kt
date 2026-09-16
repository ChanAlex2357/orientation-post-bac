package mg.itu.orientationpostbac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import mg.itu.orientationpostbac.data.AppDatabase
import mg.itu.orientationpostbac.data.FormationRepository
import mg.itu.orientationpostbac.ui.CatalogueViewModel
import mg.itu.orientationpostbac.ui.EcranDetail
import mg.itu.orientationpostbac.ui.EcranFormations
import mg.itu.orientationpostbac.ui.EcranParcours
import mg.itu.orientationpostbac.ui.EcranProfil
import mg.itu.orientationpostbac.ui.EcranProjet
import mg.itu.orientationpostbac.ui.EcranQuestionnaire
import mg.itu.orientationpostbac.ui.ProfilViewModel
import mg.itu.orientationpostbac.ui.ProjetViewModel

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

    const val PROJET = "projet"

    /** Route a argument, patron exact du mini-TP 5 : la route porte du texte. */
    const val DETAIL = "detail/{formationId}"
    const val ARG_FORMATION_ID = "formationId"

    fun detailDe(formationId: String) = "detail/$formationId"
}

/**
 * Navigation, reprise du patron du mini-TP 5 : un `NavHost`, une route par
 * écran, `popBackStack()` pour le retour, et une seule route à argument,
 * `detail/{formationId}`.
 *
 * Les 3 ViewModels sont obtenus ici et partagés par les écrans qui en
 * dépendent : le profil et le questionnaire remplissent un seul et même
 * objet, et les 3 écrans de consultation lisent un seul et même calcul.
 *
 * Le parcours forme un entonnoir — profil, questionnaire, puis consultation
 * — et l'élève peut remonter à chaque étape pour corriger. Les allers-retours
 * entre ces écrans ne doivent pas empiler de doublons dans la pile de retour :
 * c'est le rôle de `retourner()` ci-dessous.
 */
@Composable
private fun AppOrientation() {
    val navController = rememberNavController()
    val profilViewModel: ProfilViewModel = viewModel()
    val catalogueViewModel: CatalogueViewModel = viewModel()
    val etat by profilViewModel.etat.collectAsState()
    val etatCatalogue by catalogueViewModel.etat.collectAsState()
    val detail by catalogueViewModel.detail.collectAsState()
    val projetViewModel: ProjetViewModel = viewModel()
    val etatProjet by projetViewModel.etat.collectAsState()

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
                    navController.retourner(Routes.PARCOURS)
                },
                onRetour = { navController.popBackStack() },
            )
        }

        composable(Routes.PARCOURS) {
            EcranParcours(
                etat = etatCatalogue,
                reponsesDonnees = etat.questionsRepondues,
                questionsTotal = etat.questions.size,
                onCompleterQuestionnaire = { navController.retourner(Routes.QUESTIONNAIRE) },
                onDomaineChoisi = { domaine ->
                    catalogueViewModel.filtrerParDomaine(domaine)
                    navController.navigate(Routes.FORMATIONS)
                },
                onVoirToutesLesFormations = {
                    catalogueViewModel.filtrerParDomaine(null)
                    navController.navigate(Routes.FORMATIONS)
                },
                onModifierProfil = { navController.popBackStack(Routes.PROFIL, inclusive = false) },
                onVoirMonProjet = { navController.retourner(Routes.PROJET) },
            )
        }

        composable(Routes.FORMATIONS) {
            EcranFormations(
                etat = etatCatalogue,
                onRechercher = catalogueViewModel::rechercher,
                onRetirerFiltre = { catalogueViewModel.filtrerParDomaine(null) },
                onFormationChoisie = { formationId ->
                    navController.navigate(Routes.detailDe(formationId))
                },
                onVoirMonProjet = { navController.retourner(Routes.PROJET) },
            )
        }

        composable(Routes.DETAIL) { backStackEntry ->
            // L'ecran ne recoit pas la formation : il relit l'identifiant
            // porte par la route et la retrouve en base (mini-TP 5 + seance 7).
            val formationId = backStackEntry.arguments?.getString(Routes.ARG_FORMATION_ID)
            LaunchedEffect(formationId) {
                if (formationId != null) catalogueViewModel.ouvrirDetail(formationId)
            }
            val fiche = detail?.takeIf { it.evaluee.formation.id == formationId }
            EcranDetail(
                detail = fiche,
                dansLeProjet = etatProjet.candidatures.any { it.formation.id == formationId },
                onAjouterAuProjet = { fiche?.let { projetViewModel.ajouter(it.evaluee.formation) } },
                onRetirerDuProjet = { formationId?.let(projetViewModel::retirer) },
                onVoirMonProjet = { navController.retourner(Routes.PROJET) },
                onRetour = { navController.popBackStack() },
            )
        }

        composable(Routes.PROJET) {
            EcranProjet(
                etat = etatProjet,
                onEtapeCochee = projetViewModel::marquerEtape,
                onRetirer = projetViewModel::retirer,
                // navigate et non popBackStack : Mon projet est atteignable
                // depuis plusieurs ecrans, donc "revenir" n'a pas de sens ici.
                onVoirFormations = { navController.retourner(Routes.FORMATIONS) },
            )
        }
    }
}

/**
 * Navigue vers un ecran deja visite sans en empiler un doublon.
 *
 * Sans cela, un aller-retour entre le questionnaire et les parcours
 * suggeres ajoutait une entree a chaque passage : apres trois corrections
 * du questionnaire, il fallait appuyer six fois sur Retour pour revenir au
 * profil. La pile doit refleter le chemin de l'eleve, pas son hesitation.
 */
private fun NavController.retourner(route: String) {
    navigate(route) {
        popUpTo(route) { inclusive = true }
        launchSingleTop = true
    }
}
