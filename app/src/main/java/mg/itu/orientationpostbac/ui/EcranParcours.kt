package mg.itu.orientationpostbac.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.orientationpostbac.domain.DomaineSuggere
import mg.itu.orientationpostbac.domain.bandeDeScore

/**
 * Écran Parcours suggérés (document §2.2.3) : les domaines classés par
 * indice de compatibilité. Il répond au problème A — l'élève qui ne sait
 * pas quoi faire — en lui donnant un point d'entrée plutôt qu'un catalogue.
 *
 * L'indice n'est pas affiché en pourcentage mais en bande qualitative.
 * Le document écarte le chiffre brut : « un pourcentage au point près
 * relèverait de la fausse précision ». Le classement, lui, reste visible
 * puisque c'est l'ordre de la liste.
 */
@Composable
fun EcranParcours(
    etat: EtatCatalogue,
    reponsesDonnees: Int,
    questionsTotal: Int,
    onCompleterQuestionnaire: () -> Unit,
    onDomaineChoisi: (String) -> Unit,
    onVoirToutesLesFormations: () -> Unit,
    onModifierProfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Parcours suggeres", style = MaterialTheme.typography.headlineSmall)

        if (!etat.profilRenseigne) {
            ProfilManquant(onModifierProfil)
            return@Column
        }

        Text(
            "Classes selon ce que tu as indique. Ce n'est pas un pronostic de reussite.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Les interets pesent 35 % du score. Un questionnaire a peine
        // entame produit donc un classement qui n'a pas la valeur que sa
        // presentation laisse croire : il faut le dire, pas l'habiller.
        if (questionsTotal > 0 && reponsesDonnees < questionsTotal) {
            Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Questionnaire incomplet : $reponsesDonnees reponses sur $questionsTotal.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Ce classement repose surtout sur tes contraintes. Termine le questionnaire pour qu'il tienne compte de tes interets.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                        onClick = onCompleterQuestionnaire,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("Completer le questionnaire")
                    }
                }
            }
        }

        if (etat.domaines.isEmpty()) {
            Text(
                "Aucun domaine ne ressort pour l'instant. Complete ton profil ou ton questionnaire.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(etat.domaines) { domaine ->
                CarteDomaine(domaine = domaine, onClick = { onDomaineChoisi(domaine.domaine) })
            }
        }

        Button(
            onClick = onVoirToutesLesFormations,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("Voir toutes les formations")
        }
    }
}

@Composable
private fun CarteDomaine(domaine: DomaineSuggere, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(domaine.domaine, style = MaterialTheme.typography.titleMedium)
                Text(
                    libelleDe(bandeDeScore(domaine.indice)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                "${domaine.nombreFormations} formation" + if (domaine.nombreFormations > 1) "s" else "",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ProfilManquant(onModifierProfil: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Renseigne d'abord ta serie pour obtenir des suggestions.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onModifierProfil) { Text("Remplir mon profil") }
    }
}
