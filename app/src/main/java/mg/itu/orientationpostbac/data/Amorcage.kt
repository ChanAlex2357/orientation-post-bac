package mg.itu.orientationpostbac.data

/**
 * Pré-remplissage de la base au premier lancement — patron du mini-TP 7
 * (`if (dao.parId(1) == null) dao.insererTous(produitsInitiaux)`), transposé
 * ici sur un comptage, puisque les identifiants sont des String.
 *
 * C'est le seed de DÉVELOPPEMENT : il permet d'itérer sur le jeu de données
 * sans dépendre d'un fichier `.db` pré-généré. En fin de projet, il cède la
 * place au fichier livré dans les assets via `createFromAsset`, conformément
 * au tableau 4 du document (US-FINALE, Épopée 7).
 *
 * Les établissements sont insérés avant les formations : une formation
 * référence un établissement par son identifiant.
 */
suspend fun remplirSiVide(database: AppDatabase) {
    if (database.formationDao().compter() > 0) return

    database.etablissementDao().insererTous(etablissementsInitiaux)
    database.formationDao().insererTous(formationsInitiales)
}
