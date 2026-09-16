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
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Écran Mon projet (document §2.2.4) : « le dernier écran développé
 * transforme la consultation en action ».
 *
 * Sans lui, l'application laisserait l'élève au moment où il a le plus
 * besoin d'aide, juste après avoir choisi. Le dépôt réel d'une candidature
 * reste hors périmètre — il supposerait des conventions avec chaque
 * établissement — et un échec partiel sur ce point serait plus dommageable
 * pour l'élève que l'absence de la fonctionnalité.
 */
@Composable
fun EcranProjet(
    etat: EtatProjet,
    onEtapeCochee: (Int, Boolean) -> Unit,
    onRetirer: (String) -> Unit,
    onVoirFormations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Mon projet", style = MaterialTheme.typography.headlineSmall)

        if (etat.candidatures.isEmpty()) {
            Column(
                Modifier.padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Tu n'as encore retenu aucune formation.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    "Ouvre une fiche de formation et ajoute-la a ton projet pour preparer tes demarches.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onVoirFormations) { Text("Parcourir les formations") }
            }
            return@Column
        }

        Text(
            "${etat.candidatures.size} candidature" +
                (if (etat.candidatures.size > 1) "s" else "") +
                " preparee" + (if (etat.candidatures.size > 1) "s" else ""),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(etat.candidatures) { candidature ->
                CarteCandidature(
                    candidature = candidature,
                    onEtapeCochee = onEtapeCochee,
                    onRetirer = { onRetirer(candidature.formation.id) },
                )
            }
        }
    }
}

@Composable
private fun CarteCandidature(
    candidature: CandidatureDetaillee,
    onEtapeCochee: (Int, Boolean) -> Unit,
    onRetirer: () -> Unit,
) {
    val formation = candidature.formation

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formation.nom, style = MaterialTheme.typography.titleMedium)
            Text(
                "${formation.localisation} - ${libelleGrade(formation.grade)}",
                style = MaterialTheme.typography.bodySmall,
            )

            avertissementDe(formation.statutReconnaissance)?.let { avertissement ->
                Text(
                    avertissement,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                "Demarches : ${candidature.etapesFaites} sur ${candidature.etapes.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )

            candidature.etapes.forEach { etape ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onEtapeCochee(etape.id, !etape.fait) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = etape.fait,
                        onCheckedChange = { coche -> onEtapeCochee(etape.id, coche) },
                    )
                    Text(
                        etape.libelle,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (etape.fait) TextDecoration.LineThrough else null,
                    )
                }
            }

            TextButton(onClick = onRetirer, modifier = Modifier.padding(top = 4.dp)) {
                Text("Retirer de mon projet")
            }
        }
    }
}
