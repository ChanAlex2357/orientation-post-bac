package mg.itu.orientationpostbac.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mg.itu.orientationpostbac.domain.UserProfile

/**
 * Profil de l'élève : une seule ligne, relue au lancement et réécrite à
 * chaque enregistrement. « Tout reste modifiable après coup, et le
 * classement se recalcule » (document §2.2.2).
 *
 * Le profil est un enregistrement que l'on interroge, donc Room — et non
 * DataStore, qui reste pour les réglages (document §2.2.4).
 */
class ProfilRepository(private val profilDao: ProfilUtilisateurDao) {

    /** null tant qu'aucun profil n'a été saisi, ou si la ligne stockée est illisible. */
    val profil: Flow<UserProfile?> =
        profilDao.profil().map { entite -> entite?.versDomaine() }

    suspend fun enregistrer(profil: UserProfile) {
        profilDao.enregistrer(profil.versEntite())
    }
}
