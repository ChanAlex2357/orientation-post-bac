package mg.itu.orientationpostbac.domain

/** Formation minimale pour les tests : valeurs neutres par défaut, à surcharger au besoin. */
fun formationDeTest(
    id: String = "F1",
    grade: String = "L",
    seriesAdmissibles: Set<Serie> = Serie.entries.toSet(),
    matieresCles: List<String> = emptyList(),
    coutIndicatif: Int? = null,
    localisation: String = "Antananarivo",
    duree: Int = 3,
    modeAdmission: String = "Dossier",
    riasecProfile: RiasecProfile = RiasecProfile(0, 0, 0, 0, 0, 0),
): Formation = Formation(
    id = id,
    nom = "Formation de test",
    description = "",
    domaine = "Informatique",
    mention = "Mention de test",
    grade = grade,
    duree = duree,
    typeEtablissement = TypeEtablissement.PUBLIC,
    etablissementId = "E1",
    localisation = localisation,
    seriesAdmissibles = seriesAdmissibles,
    matieresCles = matieresCles,
    conditionsAdmission = "",
    modeAdmission = modeAdmission,
    niveauCout = "moyen",
    coutIndicatif = coutIndicatif,
    riasecProfile = riasecProfile,
    statutReconnaissance = RecognitionStatus.VERIFIEE,
    referenceArrete = "",
    dateVerification = "2026-01-01",
    sourceInformation = "",
)

/** Profil élève minimal pour les tests : aucune contrainte déclarée par défaut. */
fun profilDeTest(
    serie: Serie = Serie.D,
    riasecProfile: RiasecProfile = RiasecProfile(0, 0, 0, 0, 0, 0),
    niveauParMatiere: Map<String, Int> = emptyMap(),
    budgetMaxAriary: Int? = null,
    localisationSouhaitee: String? = null,
    dureeMaxAnnees: Int? = null,
): UserProfile = UserProfile(
    serie = serie,
    riasecProfile = riasecProfile,
    niveauParMatiere = niveauParMatiere,
    constraints = UserConstraints(
        budgetMaxAriary = budgetMaxAriary,
        localisationSouhaitee = localisationSouhaitee,
        dureeMaxAnnees = dureeMaxAnnees,
    ),
    preferences = UserPreferences(),
)
