package mg.itu.orientationpostbac.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Les 6 tables Room du document (§2.1.2). Ces entités sont volontairement
 * SÉPARÉES des classes de domaine : le moteur reste du Kotlin pur sans
 * import Android (document §2.2.1), donc il ne peut pas porter lui-même
 * les annotations Room. La conversion entité <-> domaine arrive avec le
 * Repository (US3.1), seule couche qui sait d'où viennent les données.
 *
 * Colonnes primitives uniquement, comme au mini-TP 7 : pas de TypeConverter.
 * Les collections sont sérialisées en texte, avec 2 conventions :
 *   - liste simple  : valeurs séparées par des virgules   ("C,D")
 *   - dictionnaire  : paires cle:valeur séparées par ";"  ("Maths:80;Physique:60")
 */

@Entity(tableName = "etablissement")
data class EtablissementEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val sigle: String,
    val type: String,
    val localisation: String,
    val description: String,
    val siteOfficiel: String?,
    val statutVerification: String,
    val dateVerification: String,
    val niveauMaturite: String?,
)

@Entity(tableName = "formation")
data class FormationEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val description: String,
    val domaine: String,
    val mention: String,
    val grade: String,
    val duree: Int,
    val typeEtablissement: String,
    val etablissementId: String,
    val localisation: String,
    val seriesAdmissibles: String,
    val matieresCles: String,
    val conditionsAdmission: String,
    val modeAdmission: String,
    val niveauCout: String,
    val coutIndicatif: Int?,
    val riasecRealiste: Int,
    val riasecInvestigateur: Int,
    val riasecArtistique: Int,
    val riasecSocial: Int,
    val riasecEntreprenant: Int,
    val riasecConventionnel: Int,
    val statutReconnaissance: String,
    val referenceArrete: String,
    val dateVerification: String,
    val sourceInformation: String,
)

/** Questionnaire RIASEC court, 12 à 15 items (document §2.2.2). */
@Entity(tableName = "question_riasec")
data class QuestionRiasecEntity(
    @PrimaryKey val id: Int,
    val ordre: Int,
    val texte: String,
    val dimension: String,
)

/** Profil de l'élève : une seule ligne, d'identifiant 1. */
@Entity(tableName = "profil_utilisateur")
data class ProfilUtilisateurEntity(
    @PrimaryKey val id: Int = 1,
    val serie: String,
    val riasecRealiste: Int,
    val riasecInvestigateur: Int,
    val riasecArtistique: Int,
    val riasecSocial: Int,
    val riasecEntreprenant: Int,
    val riasecConventionnel: Int,
    val niveauParMatiere: String,
    val budgetMaxAriary: Int?,
    val localisationSouhaitee: String?,
    val dureeMaxAnnees: Int?,
    val modeAdmissionAccepte: String,
)

/**
 * Shortlist de l'écran Mon projet (document §2.2.4). La formation est la clé :
 * une même formation ne peut être sélectionnée qu'une fois.
 */
@Entity(tableName = "selection_candidature")
data class SelectionCandidatureEntity(
    @PrimaryKey val formationId: String,
    val dateAjout: String,
)

/**
 * Étapes de démarche attachées à une candidature (document §2.2.4 :
 * pièces à réunir, période de dépôt, mode d'admission, contact).
 */
@Entity(tableName = "etape_checklist")
data class EtapeChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val formationId: String,
    val ordre: Int,
    val libelle: String,
    val fait: Boolean,
)
