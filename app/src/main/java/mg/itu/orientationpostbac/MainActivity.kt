package mg.itu.orientationpostbac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.orientationpostbac.data.AppDatabase
import mg.itu.orientationpostbac.data.FormationRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.obtenir(this)
        val repository = FormationRepository(database.formationDao(), database.etablissementDao())
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    EcranAmorcage(repository)
                }
            }
        }
    }
}

/**
 * ÉCRAN TEMPORAIRE DE VÉRIFICATION (US2.3, passé au Repository en US3.1).
 *
 * Il sert uniquement à constater que le pré-remplissage a eu lieu et que la
 * chaîne Room -> Repository -> domaine répond. Il manque encore le ViewModel :
 * l'état est lu directement dans le composable, ce qui ne survivra pas à la
 * rotation. L'Épopée 4 corrige ce point et l'Épopée 5 remplace cet écran.
 * Ne pas prendre ce fichier pour modèle.
 */
@Composable
private fun EcranAmorcage(repository: FormationRepository) {
    LaunchedEffect(Unit) { repository.preparerDonnees() }

    val formations by repository.formations.collectAsState(initial = emptyList())
    val etablissements by repository.etablissements.collectAsState(initial = emptyList())

    Column(Modifier.padding(16.dp)) {
        Text("Orientation post-Bac", style = MaterialTheme.typography.headlineSmall)
        Text(
            "${formations.size} formations, ${etablissements.size} etablissements en base",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        LazyColumn(Modifier.padding(top = 12.dp)) {
            items(formations) { formation ->
                Column(Modifier.padding(vertical = 6.dp)) {
                    Text(
                        "${formation.nom} (${formation.grade})",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "${formation.domaine} · ${formation.localisation} · ${formation.statutReconnaissance}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "series : ${formation.seriesAdmissibles.joinToString { it.name }}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
