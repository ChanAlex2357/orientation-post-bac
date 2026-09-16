package mg.itu.orientationpostbac.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.orientationpostbac.domain.FormationEvaluee
import mg.itu.orientationpostbac.domain.bandeDeScore

/**
 * Écran Formations (document §2.2.3) : recherche, filtre par domaine, et
 * surtout une section distincte « Non eligible : pourquoi ? ».
 *
 * La règle du document est nette (§9.2) : « une formation non éligible
 * n'est jamais supprimée de l'affichage ». Elle bascule dans une section
 * séparée, avec le motif visible. Masquer reviendrait à décider à la place
 * de l'élève, et à lui cacher qu'une filière existe.
 *
 * Le statut de reconnaissance conditionne l'affichage de la bande sans
 * jamais modifier le score (tableau 12) : c'est un axe orthogonal, pas un
 * septième critère.
 */
@Composable
fun EcranFormations(
    etat: EtatCatalogue,
    onRechercher: (String) -> Unit,
    onRetirerFiltre: () -> Unit,
    onFormationChoisie: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Formations", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = etat.recherche,
            onValueChange = onRechercher,
            label = { Text("Rechercher une formation") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )

        if (etat.domaineFiltre != null) {
            FilterChip(
                selected = true,
                onClick = onRetirerFiltre,
                label = { Text("Domaine : ${etat.domaineFiltre}  X") },
            )
        }

        LazyColumn(
            Modifier.weight(1f).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (etat.eligibles.isEmpty() && etat.nonEligibles.isEmpty()) {
                item {
                    Text(
                        "Aucune formation ne correspond a cette recherche.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            items(etat.eligibles) { evaluee ->
                CarteFormation(evaluee = evaluee, onClick = { onFormationChoisie(evaluee.formation.id) })
            }

            if (etat.nonEligibles.isNotEmpty()) {
                item {
                    Text(
                        "Non eligible : pourquoi ?",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                items(etat.nonEligibles) { evaluee ->
                    CarteNonEligible(evaluee = evaluee, onClick = { onFormationChoisie(evaluee.formation.id) })
                }
            }
        }
    }
}

@Composable
private fun CarteFormation(evaluee: FormationEvaluee, onClick: () -> Unit) {
    val formation = evaluee.formation
    val statut = formation.statutReconnaissance

    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formation.nom, style = MaterialTheme.typography.titleMedium)
            Text(
                "${formation.domaine} - ${formation.localisation} - ${libelleGrade(formation.grade)}",
                style = MaterialTheme.typography.bodySmall,
            )

            // Tableau 12 : la bande disparait quand le statut est NON_VERIFIEE.
            // Seul l'avertissement reste, parce qu'un indice de compatibilite
            // sur un diplome dont on ignore la valeur n'aiderait pas l'eleve.
            val compatibilite = evaluee.compatibilite
            if (compatibilite != null && bandeAffichable(statut)) {
                Text(
                    libelleDe(bandeDeScore(compatibilite.score)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            avertissementDe(statut)?.let { avertissement ->
                Text(
                    avertissement,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun CarteNonEligible(evaluee: FormationEvaluee, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(evaluee.formation.nom, style = MaterialTheme.typography.titleMedium)
            Text(
                "${evaluee.formation.domaine} - ${evaluee.formation.localisation}",
                style = MaterialTheme.typography.bodySmall,
            )
            evaluee.motifs.forEach { motif ->
                Text(
                    libelleDe(motif),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
