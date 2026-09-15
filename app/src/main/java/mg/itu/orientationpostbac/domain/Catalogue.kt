package mg.itu.orientationpostbac.domain

/**
 * Une formation passée au moteur, telle que les écrans la consomment.
 *
 * Les 3 états possibles sont distincts et ne doivent pas être confondus :
 * - profil non renseigné : `evaluee` est faux, la formation est affichée
 *   sans jugement ;
 * - éligible : `compatibilite` porte le score, la bande et le dépliage ;
 * - non éligible : `motifs` porte le pourquoi, et la formation reste
 *   affichée (document §9.2 — aucune formation n'est masquée à l'élève).
 */
data class FormationEvaluee(
    val formation: Formation,
    val evaluee: Boolean,
    val motifs: List<BlockingReason> = emptyList(),
    val compatibilite: CompatibilityResult? = null,
) {
    val eligible: Boolean = motifs.isEmpty()
}

/** Un domaine de l'écran Parcours suggérés, avec son indice de compatibilité. */
data class DomaineSuggere(
    val domaine: String,
    val indice: Int,
    val nombreFormations: Int,
)

/**
 * Applique le moteur à 2 étages au catalogue : éligibilité d'abord, score
 * ensuite, et seulement pour ce qui a passé le premier étage (figure 3).
 */
fun evaluerCatalogue(
    formations: List<Formation>,
    profil: UserProfile?,
): List<FormationEvaluee> {
    if (profil == null) {
        return formations.map { FormationEvaluee(formation = it, evaluee = false) }
    }

    return formations.map { formation ->
        when (val eligibilite = EligibilityEngine.evaluer(formation, profil)) {
            is EligibilityResult.NotEligible -> FormationEvaluee(
                formation = formation,
                evaluee = true,
                motifs = eligibilite.reasons,
            )

            EligibilityResult.Eligible -> FormationEvaluee(
                formation = formation,
                evaluee = true,
                compatibilite = ScoringEngine.evaluer(formation, profil),
            )
        }
    }
}

/** Les éligibles, du meilleur score au moins bon. Sans profil, ordre alphabétique. */
fun classerEligibles(evaluees: List<FormationEvaluee>): List<FormationEvaluee> =
    evaluees.filter { it.eligible }
        .sortedWith(
            compareByDescending<FormationEvaluee> { it.compatibilite?.score ?: -1 }
                .thenBy { it.formation.nom },
        )

fun classerNonEligibles(evaluees: List<FormationEvaluee>): List<FormationEvaluee> =
    evaluees.filterNot { it.eligible }.sortedBy { it.formation.nom }

/**
 * Domaines classés pour l'écran Parcours suggérés (document §2.2.3), qui
 * répond au problème de l'élève « qui ne sait pas quoi faire ».
 *
 * L'indice d'un domaine est le MEILLEUR score qu'on y trouve, et non la
 * moyenne. Un élève explore un domaine pour y trouver une formation : le
 * domaine vaut donc ce que vaut sa meilleure option. Une moyenne
 * enterrerait un domaine contenant une correspondance excellente parmi
 * beaucoup d'autres qui ne lui conviennent pas — exactement le cas où
 * l'écran doit au contraire l'orienter.
 */
fun classerDomaines(evaluees: List<FormationEvaluee>): List<DomaineSuggere> =
    evaluees.filter { it.eligible && it.compatibilite != null }
        .groupBy { it.formation.domaine }
        .map { (domaine, formations) ->
            DomaineSuggere(
                domaine = domaine,
                indice = formations.maxOf { it.compatibilite?.score ?: 0 },
                nombreFormations = formations.size,
            )
        }
        .sortedWith(compareByDescending<DomaineSuggere> { it.indice }.thenBy { it.domaine })
