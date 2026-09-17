package mg.itu.orientationpostbac.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.orientationpostbac.domain.Serie

/**
 * Écran Profil (document §2.2.2) : série du Baccalauréat, niveau déclaré
 * dans 3 à 5 matières clés dépendant de cette série, puis les contraintes.
 *
 * Composable sans ViewModel : il reçoit un état et remonte des événements
 * (flux unidirectionnel de la séance 6). Le ViewModel est branché par
 * l'appelant, ce qui rend cet écran lisible et testable isolément.
 *
 * Aucun champ libre : toutes les saisies passent par des choix fermés.
 * La localisation est comparée telle quelle par le critère LOCATION du
 * moteur — une faute de frappe dans un champ texte rendrait la contrainte
 * silencieusement inopérante. Le niveau se déclare en 4 paliers plutôt
 * qu'en pourcentage : le document refuse la fausse précision à l'affichage
 * du score, la même prudence vaut à la saisie.
 */
@Composable
fun EcranProfil(
    etat: EtatProfil,
    onSerieChoisie: (Serie) -> Unit,
    onNiveauRegle: (String, Int) -> Unit,
    onBudgetRegle: (Int?) -> Unit,
    onLocalisationReglee: (String?) -> Unit,
    onDureeReglee: (Int?) -> Unit,
    onEnregistrer: () -> Unit,
    onContinuer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Mon profil", style = MaterialTheme.typography.headlineSmall)

        Section("Ta serie au Baccalaureat") {
            ChoixUnique(
                options = Serie.entries.map { it to it.name },
                selection = etat.serie,
                onChoisir = onSerieChoisie,
            )
        }

        if (etat.serie != null) {
            Section("Ton niveau dans les matieres cles") {
                etat.matieresAProposer.forEach { matiere ->
                    Text(matiere, style = MaterialTheme.typography.bodyMedium)
                    ChoixUnique(
                        options = NIVEAUX,
                        selection = etat.niveauParMatiere[matiere],
                        onChoisir = { niveau -> onNiveauRegle(matiere, niveau) },
                    )
                }
            }
        }

        Section("Ton budget annuel") {
            ChoixUnique(
                options = BUDGETS,
                selection = etat.budgetMaxAriary,
                onChoisir = onBudgetRegle,
            )
        }

        Section("Ou veux-tu etudier ?") {
            ChoixUnique(
                options = LOCALISATIONS,
                selection = etat.localisationSouhaitee,
                onChoisir = onLocalisationReglee,
            )
        }

        Section("Combien d'annees d'etudes ?") {
            ChoixUnique(
                options = DUREES,
                selection = etat.dureeMaxAnnees,
                onChoisir = onDureeReglee,
            )
        }

        Button(
            onClick = {
                onEnregistrer()
                onContinuer()
            },
            enabled = etat.peutEnregistrer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (etat.peutEnregistrer) "Continuer" else "Choisis d'abord ta serie")
        }

        // Les contraintes penalisent le score, elles n'excluent jamais une
        // formation (tableau 8) : l'eleve doit le savoir avant de les saisir.
        Text(
            "Tes contraintes ne suppriment aucune formation. Elles ajustent seulement l'ordre des propositions.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Section(titre: String, contenu: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(titre, style = MaterialTheme.typography.titleMedium)
            contenu()
        }
    }
}

/**
 * Une ligne de choix fermés : la valeur retenue est celle du modèle, pas une
 * saisie libre.
 *
 * FlowRow et non Row : sur un écran étroit, 4 libellés ne tiennent pas sur
 * une ligne et le dernier sortait de l'écran. La cible du projet est le
 * téléphone d'entrée de gamme (minSdk 24), pas la tablette.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChoixUnique(
    options: List<Pair<T, String>>,
    selection: T?,
    onChoisir: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (valeur, libelle) ->
            FilterChip(
                selected = selection == valeur,
                onClick = { onChoisir(valeur) },
                label = { Text(libelle) },
            )
        }
    }
}

private val NIVEAUX: List<Pair<Int, String>> = listOf(
    25 to "Faible",
    50 to "Moyen",
    75 to "Bon",
    100 to "Tres bon",
)

/**
 * Tranches alignées sur les frais annuels du privé cités par le document
 * (800 000 à 6 000 000 Ar) : "Confortable" couvre tout le marché, "Limite"
 * ne couvre que le public et le bas de gamme du privé.
 */
private val BUDGETS: List<Pair<Int?, String>> = listOf(
    1_000_000 to "Limite",
    3_000_000 to "Moyen",
    6_000_000 to "Confortable",
    null to "Peu importe",
)

private val LOCALISATIONS: List<Pair<String?, String>> = listOf(
    "Antananarivo" to "Antananarivo",
    "Fianarantsoa" to "Fianarantsoa",
    null to "Peu importe",
)

private val DUREES: List<Pair<Int?, String>> = listOf(
    2 to "2 ans",
    3 to "3 ans",
    5 to "5 ans",
    null to "Peu importe",
)
