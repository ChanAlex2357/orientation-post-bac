package mg.itu.orientationpostbac.domain

/** Une formation retenue par l'élève (document §2.2.4, écran Mon projet). */
data class Candidature(
    val formationId: String,
    val dateAjout: String,
)

/** Une démarche à accomplir pour cette candidature. */
data class EtapeChecklist(
    val id: Int,
    val formationId: String,
    val ordre: Int,
    val libelle: String,
    val fait: Boolean,
)

/**
 * Les démarches proposées à l'ajout d'une candidature. Le document les
 * énumère (§2.2.4) : pièces à réunir, période de dépôt, mode d'admission,
 * contact de l'établissement.
 *
 * La liste s'adapte à la formation plutôt que d'être identique pour toutes.
 * Une filière dont l'habilitation n'est pas établie ajoute une démarche de
 * vérification : c'est la conséquence pratique de l'avertissement affiché
 * sur sa fiche. Prévenir puis ne rien proposer laisserait l'élève avec
 * l'inquiétude et sans le geste qui la lève.
 */
fun etapesPour(formation: Formation): List<String> = buildList {
    add("Reunir les pieces du dossier")
    add("Verifier la periode de depot")
    add("Preparer l'admission : ${formation.modeAdmission.lowercase()}")
    add("Contacter l'etablissement")
    if (formation.statutReconnaissance != RecognitionStatus.VERIFIEE) {
        add("Demander a l'etablissement l'arrete d'habilitation en cours de validite")
    }
}
