package mg.itu.orientationpostbac.ui

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.orientationpostbac.domain.QuestionRiasec

/**
 * Écran Questionnaire (document §2.2.2) : adaptation courte du modèle
 * RIASEC, 12 items, une réponse sur 5 paliers.
 *
 * Tout l'état vit dans le ViewModel, jamais dans un `remember` local — le
 * document l'exige explicitement : l'Activity est détruite et recréée à la
 * rotation, et « un questionnaire de 12 questions perdu à la rotation serait
 * rédhibitoire sur un téléphone d'entrée de gamme ». Cet écran n'a donc
 * aucune mémoire propre : il affiche `etat` et remonte `onRepondre`.
 *
 * Le questionnaire peut être validé incomplet. Une question sans réponse
 * est ignorée dans le calcul et non comptée comme un désintérêt (US4.1) :
 * forcer 12 réponses ferait abandonner, et répondre au hasard fausserait
 * davantage le profil qu'une réponse manquante.
 */
@Composable
fun EcranQuestionnaire(
    etat: EtatProfil,
    onRepondre: (Int, Int) -> Unit,
    onTerminer: () -> Unit,
    onRetour: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Tes centres d'interet", style = MaterialTheme.typography.headlineSmall)
        Text(
            "${etat.questionsRepondues} reponses sur ${etat.questions.size}",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (etat.questions.isNotEmpty()) {
            LinearProgressIndicator(
                progress = { etat.questionsRepondues.toFloat() / etat.questions.size },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
        }

        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(etat.questions) { question ->
                CarteQuestion(
                    question = question,
                    reponse = etat.reponses[question.id],
                    onRepondre = { valeur -> onRepondre(question.id, valeur) },
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onRetour) { Text("Retour") }
            Button(onClick = onTerminer, modifier = Modifier.weight(1f)) {
                Text(if (etat.questionnaireComplet) "Voir mes resultats" else "Continuer sans tout remplir")
            }
        }
    }
}

@Composable
private fun CarteQuestion(
    question: QuestionRiasec,
    reponse: Int?,
    onRepondre: (Int) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(question.texte, style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PALIERS.forEach { (valeur, libelle) ->
                    FilterChip(
                        selected = reponse == valeur,
                        onClick = { onRepondre(valeur) },
                        label = { Text(libelle) },
                    )
                }
            }
        }
    }
}

/** Échelle 0..4 de l'US4.1, exprimée en mots : un chiffre nu n'aide pas à se situer. */
private val PALIERS: List<Pair<Int, String>> = listOf(
    0 to "Pas du tout",
    1 to "Peu",
    2 to "Moyen",
    3 to "Assez",
    4 to "Beaucoup",
)
