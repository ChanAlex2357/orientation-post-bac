package mg.itu.orientationpostbac.domain

/**
 * Matières clés proposées à l'auto-positionnement, selon la série
 * (document §2.2.2 : « le niveau déclaré dans 3 à 5 matières clés
 * dépendant de cette série »).
 *
 * Ces libellés doivent rester identiques à ceux employés dans
 * `Formation.matieresCles`, sinon le critère LEVEL du ScoringEngine ne
 * retrouve pas le niveau déclaré et retombe silencieusement sur sa valeur
 * neutre. Un test du jeu de données verrouille cette correspondance.
 *
 * Un élève ne déclare que les matières de sa série : une formation qui en
 * exige une autre reste notée, sur la valeur neutre. C'est voulu — le
 * document interdit de masquer une formation à cause du niveau.
 */
fun matieresClesDe(serie: Serie): List<String> = when (serie) {
    Serie.A1 -> listOf("Francais", "Anglais", "Philosophie")
    Serie.A2 -> listOf("Francais", "Histoire-Geographie", "Philosophie")
    Serie.C -> listOf("Mathematiques", "Physique", "Sciences de la vie et de la Terre")
    Serie.D -> listOf("Mathematiques", "Physique", "Sciences de la vie et de la Terre")
    Serie.OSE -> listOf("Mathematiques", "Francais", "Sciences economiques")
}
