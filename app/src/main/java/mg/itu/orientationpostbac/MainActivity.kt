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
import mg.itu.orientationpostbac.data.remplirSiVide

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.obtenir(this)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    EcranAmorcage(database)
                }
            }
        }
    }
}

/**
 * ÉCRAN TEMPORAIRE DE VÉRIFICATION (US2.3).
 *
 * Il sert uniquement à constater que le pré-remplissage a eu lieu et que la
 * base répond. Il lit le DAO directement, ce qui est volontairement contraire
 * à l'architecture visée : le Repository (US3.1) puis le ViewModel (Épopée 4)
 * s'intercaleront, et les vrais écrans (Épopée 5) remplaceront celui-ci.
 * Ne pas prendre ce fichier pour modèle.
 */
@Composable
private fun EcranAmorcage(database: AppDatabase) {
    LaunchedEffect(Unit) { remplirSiVide(database) }

    val formations by database.formationDao().toutes().collectAsState(initial = emptyList())
    val etablissements by database.etablissementDao().tous().collectAsState(initial = emptyList())

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
                }
            }
        }
    }
}
