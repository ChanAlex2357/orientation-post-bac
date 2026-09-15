package mg.itu.orientationpostbac.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Les DAO suivent la règle de la séance 7 : un Flow quand l'écran doit être
 * tenu au courant (catalogue, shortlist, checklist), une fonction suspend
 * pour une question ponctuelle (recherche par identifiant, comptage).
 */

@Dao
interface EtablissementDao {

    @Query("SELECT * FROM etablissement ORDER BY nom ASC")
    fun tous(): Flow<List<EtablissementEntity>>

    @Query("SELECT * FROM etablissement WHERE id = :id")
    suspend fun parId(id: String): EtablissementEntity?

    @Insert
    suspend fun insererTous(etablissements: List<EtablissementEntity>)
}

@Dao
interface FormationDao {

    @Query("SELECT * FROM formation ORDER BY nom ASC")
    fun toutes(): Flow<List<FormationEntity>>

    /** Recherche de l'écran Formations : sur le nom, la mention et le domaine. */
    @Query(
        """
        SELECT * FROM formation
        WHERE nom LIKE '%' || :motCle || '%'
           OR mention LIKE '%' || :motCle || '%'
           OR domaine LIKE '%' || :motCle || '%'
        ORDER BY nom ASC
        """
    )
    fun rechercher(motCle: String): Flow<List<FormationEntity>>

    /** Écran Détail : la route porte l'identifiant, l'écran retrouve la formation (séance 5). */
    @Query("SELECT * FROM formation WHERE id = :id")
    suspend fun parId(id: String): FormationEntity?

    @Query("SELECT COUNT(*) FROM formation")
    suspend fun compter(): Int

    @Insert
    suspend fun insererTous(formations: List<FormationEntity>)
}

@Dao
interface QuestionRiasecDao {

    @Query("SELECT * FROM question_riasec ORDER BY ordre ASC")
    fun toutes(): Flow<List<QuestionRiasecEntity>>

    @Query("SELECT COUNT(*) FROM question_riasec")
    suspend fun compter(): Int

    @Insert
    suspend fun insererTous(questions: List<QuestionRiasecEntity>)
}

@Dao
interface ProfilUtilisateurDao {

    @Query("SELECT * FROM profil_utilisateur WHERE id = 1")
    fun profil(): Flow<ProfilUtilisateurEntity?>

    /** Le profil reste modifiable après coup (document §2.2.2) : on remplace la ligne. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enregistrer(profil: ProfilUtilisateurEntity)
}

@Dao
interface SelectionCandidatureDao {

    @Query("SELECT * FROM selection_candidature ORDER BY dateAjout DESC")
    fun toutes(): Flow<List<SelectionCandidatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ajouter(selection: SelectionCandidatureEntity)

    @Query("DELETE FROM selection_candidature WHERE formationId = :formationId")
    suspend fun retirer(formationId: String)
}

@Dao
interface EtapeChecklistDao {

    @Query("SELECT * FROM etape_checklist WHERE formationId = :formationId ORDER BY ordre ASC")
    fun pourFormation(formationId: String): Flow<List<EtapeChecklistEntity>>

    /** Toutes les etapes : l'ecran Mon projet les regroupe par candidature. */
    @Query("SELECT * FROM etape_checklist ORDER BY formationId ASC, ordre ASC")
    fun toutes(): Flow<List<EtapeChecklistEntity>>

    @Query("UPDATE etape_checklist SET fait = :fait WHERE id = :id")
    suspend fun marquer(id: Int, fait: Boolean)

    @Query("DELETE FROM etape_checklist WHERE formationId = :formationId")
    suspend fun supprimerPourFormation(formationId: String)

    @Insert
    suspend fun insererTous(etapes: List<EtapeChecklistEntity>)
}
