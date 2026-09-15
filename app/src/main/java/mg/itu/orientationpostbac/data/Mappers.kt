package mg.itu.orientationpostbac.data

import mg.itu.orientationpostbac.domain.Etablissement
import mg.itu.orientationpostbac.domain.Formation
import mg.itu.orientationpostbac.domain.RecognitionStatus
import mg.itu.orientationpostbac.domain.RiasecProfile
import mg.itu.orientationpostbac.domain.Serie
import mg.itu.orientationpostbac.domain.TypeEtablissement
import mg.itu.orientationpostbac.domain.UserConstraints
import mg.itu.orientationpostbac.domain.UserPreferences
import mg.itu.orientationpostbac.domain.UserProfile

/**
 * Conversion entité Room -> classe de domaine. C'est ici que se paie le prix
 * d'avoir gardé le domaine sans import Android (US2.1) : les colonnes texte
 * redeviennent des collections et des enums.
 *
 * Aucune conversion ne lance d'exception. La base sera un jour un fichier
 * `.db` livré dans les assets (US-FINALE), généré hors de Kotlin : le test
 * qui valide le jeu de données ne le couvrira plus, et une valeur illisible
 * ne doit pas faire planter l'application sur le téléphone d'un élève.
 */

private const val SEPARATEUR_LISTE = ","
private const val SEPARATEUR_PAIRES = ";"
private const val SEPARATEUR_CLE_VALEUR = ":"

fun FormationEntity.versDomaine(): Formation = Formation(
    id = id,
    nom = nom,
    description = description,
    domaine = domaine,
    mention = mention,
    grade = grade,
    duree = duree,
    typeEtablissement = texteVersTypeEtablissement(typeEtablissement),
    etablissementId = etablissementId,
    localisation = localisation,
    seriesAdmissibles = texteVersSeries(seriesAdmissibles),
    matieresCles = texteVersListe(matieresCles),
    conditionsAdmission = conditionsAdmission,
    modeAdmission = modeAdmission,
    niveauCout = niveauCout,
    coutIndicatif = coutIndicatif,
    riasecProfile = RiasecProfile(
        realiste = riasecRealiste,
        investigateur = riasecInvestigateur,
        artistique = riasecArtistique,
        social = riasecSocial,
        entreprenant = riasecEntreprenant,
        conventionnel = riasecConventionnel,
    ),
    statutReconnaissance = texteVersStatut(statutReconnaissance),
    referenceArrete = referenceArrete,
    dateVerification = dateVerification,
    sourceInformation = sourceInformation,
)

fun EtablissementEntity.versDomaine(): Etablissement = Etablissement(
    id = id,
    nom = nom,
    sigle = sigle,
    type = texteVersTypeEtablissement(type),
    localisation = localisation,
    description = description,
    siteOfficiel = siteOfficiel,
    statutVerification = texteVersStatut(statutVerification),
    dateVerification = dateVerification,
    niveauMaturite = niveauMaturite,
)

fun texteVersListe(texte: String): List<String> =
    texte.split(SEPARATEUR_LISTE)
        .map { it.trim() }
        .filter { it.isNotEmpty() }

/**
 * Une série illisible est ignorée plutôt que devinée. La série est un filtre
 * BLOQUANT (tableau 8) : l'ignorer rend la formation plus restrictive, jamais
 * plus permissive. Une formation qui disparaît des résultats se remarque ;
 * une formation ouverte à tort n'enverrait l'élève nulle part.
 */
fun texteVersSeries(texte: String): Set<Serie> =
    texteVersListe(texte)
        .mapNotNull { nom -> Serie.entries.firstOrNull { it.name == nom } }
        .toSet()

/** "Maths:80;Physique:60" -> { Maths=80, Physique=60 }. Les paires illisibles sont ignorées. */
fun texteVersNiveaux(texte: String): Map<String, Int> =
    texte.split(SEPARATEUR_PAIRES)
        .mapNotNull { paire ->
            val morceaux = paire.split(SEPARATEUR_CLE_VALEUR)
            if (morceaux.size != 2) return@mapNotNull null
            val matiere = morceaux[0].trim()
            val niveau = morceaux[1].trim().toIntOrNull()
            if (matiere.isEmpty() || niveau == null) null else matiere to niveau
        }
        .toMap()

/**
 * Un statut illisible devient NON_VERIFIEE, qui est aussi le statut que le
 * document attribue à une filière absente des listes publiées (tableau 16).
 * C'est la seule valeur par défaut honnête : elle n'affirme jamais qu'un
 * diplôme est reconnu, et l'écran n'affichera pas de bande de compatibilité.
 */
fun texteVersStatut(texte: String): RecognitionStatus =
    RecognitionStatus.entries.firstOrNull { it.name == texte } ?: RecognitionStatus.NON_VERIFIEE

/** Le type d'établissement ne conditionne aucune règle du moteur : il est affiché, pas calculé. */
private fun texteVersTypeEtablissement(texte: String): TypeEtablissement =
    TypeEtablissement.entries.firstOrNull { it.name == texte } ?: TypeEtablissement.PRIVE

/**
 * Profil de l'élève. Retourne null si la série est illisible : la série
 * commande le filtre d'éligibilité, un profil sans elle ne peut rien
 * produire de juste. Mieux vaut redemander la saisie que calculer sur une
 * série devinée.
 */
fun ProfilUtilisateurEntity.versDomaine(): UserProfile? {
    val serieLue = Serie.entries.firstOrNull { it.name == serie } ?: return null
    return UserProfile(
        serie = serieLue,
        riasecProfile = RiasecProfile(
            realiste = riasecRealiste,
            investigateur = riasecInvestigateur,
            artistique = riasecArtistique,
            social = riasecSocial,
            entreprenant = riasecEntreprenant,
            conventionnel = riasecConventionnel,
        ),
        niveauParMatiere = texteVersNiveaux(niveauParMatiere),
        constraints = UserConstraints(
            budgetMaxAriary = budgetMaxAriary,
            localisationSouhaitee = localisationSouhaitee,
            dureeMaxAnnees = dureeMaxAnnees,
        ),
        preferences = UserPreferences(modeAdmissionAccepte = texteVersListe(modeAdmissionAccepte).toSet()),
    )
}

fun UserProfile.versEntite(): ProfilUtilisateurEntity = ProfilUtilisateurEntity(
    serie = serie.name,
    riasecRealiste = riasecProfile.realiste,
    riasecInvestigateur = riasecProfile.investigateur,
    riasecArtistique = riasecProfile.artistique,
    riasecSocial = riasecProfile.social,
    riasecEntreprenant = riasecProfile.entreprenant,
    riasecConventionnel = riasecProfile.conventionnel,
    niveauParMatiere = niveauxVersTexte(niveauParMatiere),
    budgetMaxAriary = constraints.budgetMaxAriary,
    localisationSouhaitee = constraints.localisationSouhaitee,
    dureeMaxAnnees = constraints.dureeMaxAnnees,
    modeAdmissionAccepte = preferences.modeAdmissionAccepte.joinToString(SEPARATEUR_LISTE),
)

fun niveauxVersTexte(niveaux: Map<String, Int>): String =
    niveaux.entries.joinToString(SEPARATEUR_PAIRES) { (matiere, niveau) ->
        "$matiere$SEPARATEUR_CLE_VALEUR$niveau"
    }
