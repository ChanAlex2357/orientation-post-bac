package mg.itu.orientationpostbac.domain

/**
 * Filière/formation (document figure 4). `mention` et `grade` reprennent le
 * vocabulaire exact des arrêtés (document §2.1.2) : pas d'enum, ce sont des
 * libellés officiels bruts ("Licence en Informatique", "L"/"M"/"D"...).
 *
 * `coutIndicatif` est nullable — un coût non renseigné va jusqu'en base sans
 * `!!` (règle S1, même pattern que `Produit.prixKg` en TP7).
 */
data class Formation(
    val id: String,
    val nom: String,
    val description: String,
    val domaine: String,
    val mention: String,
    val grade: String,
    val duree: Int,
    val typeEtablissement: TypeEtablissement,
    val etablissementId: String,
    val localisation: String,
    val seriesAdmissibles: Set<Serie>,
    val matieresCles: List<String>,
    val conditionsAdmission: String,
    val modeAdmission: String,
    val niveauCout: String,
    val coutIndicatif: Int?,
    val riasecProfile: RiasecProfile,
    val statutReconnaissance: RecognitionStatus,
    val referenceArrete: String,
    val dateVerification: String,
    val sourceInformation: String,
)
