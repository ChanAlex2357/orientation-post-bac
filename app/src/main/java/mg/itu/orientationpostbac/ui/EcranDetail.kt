package mg.itu.orientationpostbac.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.orientationpostbac.domain.Criterion
import mg.itu.orientationpostbac.domain.bandeDeScore

/**
 * Écran Détail formation (document §2.2.3) — « la spécificité du projet se
 * joue sur ce dernier écran ».
 *
 * Il répond à 2 questions séparées, et jamais à une seule : « est-ce que
 * cela me correspond ? » relève de l'indice de compatibilité ; « est-ce que
 * cela vaut quelque chose ? » relève du statut de reconnaissance. Les
 * additionner permettrait à une excellente correspondance de profil de
 * masquer un diplôme sans valeur. Le statut conditionne donc l'affichage du
 * score (tableau 12) mais ne le modifie jamais.
 */
@Composable
fun EcranDetail(
    detail: DetailFormation?,
    dansLeProjet: Boolean,
    onAjouterAuProjet: () -> Unit,
    onRetirerDuProjet: () -> Unit,
    onVoirMonProjet: () -> Unit,
    onRetour: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (detail == null) {
        Column(modifier.fillMaxSize().padding(16.dp)) {
            Text("Formation introuvable.", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onRetour) { Text("Retour") }
        }
        return
    }

    val formation = detail.evaluee.formation
    val statut = formation.statutReconnaissance

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onRetour) { Text("Retour") }
            TextButton(onClick = onVoirMonProjet) { Text("Mon projet") }
        }

        Text(formation.nom, style = MaterialTheme.typography.headlineSmall)
        Text(
            "Mention ${formation.mention} - ${libelleGrade(formation.grade)}",
            style = MaterialTheme.typography.titleMedium,
        )

        detail.etablissement?.let { etablissement ->
            Text(
                "${etablissement.nom} (${etablissement.sigle})",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        BlocReconnaissance(detail)

        if (detail.evaluee.eligible) {
            BlocCompatibilite(detail)
        } else {
            BlocNonEligible(detail)
        }

        BlocPratique(detail)

        // Une formation non eligible peut quand meme etre suivie : le
        // document interdit de la masquer, rien ne justifie d'interdire a
        // l'eleve de la garder sous les yeux.
        if (dansLeProjet) {
            OutlinedButton(onClick = onRetirerDuProjet, modifier = Modifier.fillMaxWidth()) {
                Text("Retirer de mon projet")
            }
        } else {
            Button(onClick = onAjouterAuProjet, modifier = Modifier.fillMaxWidth()) {
                Text("Ajouter a mon projet")
            }
        }

        Text(formation.description, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Le bloc qui porte la thèse du projet : référence d'arrêté, date de
 * vérification et source. C'est ce qu'aucune application privée ne peut
 * afficher (document §1.1) — une information adossée à un texte officiel.
 */
@Composable
private fun BlocReconnaissance(detail: DetailFormation) {
    val formation = detail.evaluee.formation
    val statut = formation.statutReconnaissance

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Reconnaissance officielle", style = MaterialTheme.typography.titleMedium)

            Ligne("Statut", statut.name)
            if (formation.referenceArrete.isNotBlank()) {
                Ligne("Arrete", formation.referenceArrete)
            } else {
                Ligne("Arrete", "aucune reference publiee")
            }
            if (formation.dateVerification.isNotBlank()) {
                Ligne("Verifie le", formation.dateVerification)
            }
            if (formation.sourceInformation.isNotBlank()) {
                Ligne("Source", formation.sourceInformation)
            }

            avertissementDe(statut)?.let { avertissement ->
                Text(
                    avertissement,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

/**
 * Le dépliage du score, critère par critère (document §2.2.1, étape 21 du
 * scénario). La bande reste dehors, le détail chiffré reste dedans : c'est
 * le compromis du document entre refuser la fausse précision et rendre le
 * classement explicable.
 *
 * L'état déplié/replié utilise `rememberSaveable` : contrairement aux
 * réponses du questionnaire, ce n'est pas une donnée de l'élève mais un
 * réglage d'affichage — sa place est dans l'écran, pas dans le ViewModel.
 * `rememberSaveable` et non `remember` pour qu'il survive quand même à la
 * rotation.
 */
@Composable
private fun BlocCompatibilite(detail: DetailFormation) {
    val compatibilite = detail.evaluee.compatibilite ?: return
    val statut = detail.evaluee.formation.statutReconnaissance
    var deplie by rememberSaveable { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth().clickable { deplie = !deplie }) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (bandeAffichable(statut)) {
                Text(
                    libelleDe(bandeDeScore(compatibilite.score)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                // Tableau 12, 3e ligne : la bande disparait, seul
                // l'avertissement reste. Le score existe toujours, il n'est
                // simplement pas montre.
                Text(
                    "Indice de compatibilite masque tant que la reconnaissance n'est pas etablie.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                if (deplie) "Masquer le detail" else "Pourquoi cette formation ?",
                style = MaterialTheme.typography.bodySmall,
            )

            if (deplie) {
                compatibilite.reasons.forEach { raison ->
                    Text(raison, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "Detail par critere",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Criterion.entries.forEach { critere ->
                    compatibilite.breakdown[critere]?.let { valeur ->
                        Ligne(libelleDe(critere), "$valeur / 100")
                    }
                }
            }
        }
    }
}

@Composable
private fun BlocNonEligible(detail: DetailFormation) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Non eligible : pourquoi ?", style = MaterialTheme.typography.titleMedium)
            detail.evaluee.motifs.forEach { motif ->
                Text(libelleDe(motif), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Les informations décisives pour candidater (document §2.2.3). */
@Composable
private fun BlocPratique(detail: DetailFormation) {
    val formation = detail.evaluee.formation

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("En pratique", style = MaterialTheme.typography.titleMedium)
            Ligne("Domaine", formation.domaine)
            Ligne("Lieu", formation.localisation)
            Ligne("Duree", "${formation.duree} ans")
            // Un cout non renseigne se dit, il ne se devine pas : la
            // nullabilite va jusqu'a l'ecran (regle S1, pattern du TP7).
            Ligne(
                "Cout indicatif",
                formation.coutIndicatif?.let { "$it Ar par an" } ?: "non renseigne",
            )
            Ligne("Admission", formation.modeAdmission)
            if (formation.conditionsAdmission.isNotBlank()) {
                Ligne("Conditions", formation.conditionsAdmission)
            }
        }
    }
}

@Composable
private fun Ligne(libelle: String, valeur: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "$libelle :",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.4f),
        )
        Text(valeur, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.6f))
    }
}
