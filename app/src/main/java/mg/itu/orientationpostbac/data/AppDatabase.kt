package mg.itu.orientationpostbac.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Point d'assemblage de la couche données — même structure qu'au mini-TP 7.
 *
 * `fallbackToDestructiveMigration` est le choix de développement du TP :
 * si le schéma change, on repart d'une base neuve. À revoir avant la remise
 * si le pré-remplissage bascule sur un fichier .db livré (US-FINALE).
 */
@Database(
    entities = [
        EtablissementEntity::class,
        FormationEntity::class,
        QuestionRiasecEntity::class,
        ProfilUtilisateurEntity::class,
        SelectionCandidatureEntity::class,
        EtapeChecklistEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun etablissementDao(): EtablissementDao
    abstract fun formationDao(): FormationDao
    abstract fun questionRiasecDao(): QuestionRiasecDao
    abstract fun profilUtilisateurDao(): ProfilUtilisateurDao
    abstract fun selectionCandidatureDao(): SelectionCandidatureDao
    abstract fun etapeChecklistDao(): EtapeChecklistDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun obtenir(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "orientation.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
