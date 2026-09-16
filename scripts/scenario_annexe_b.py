"""Scenario de demonstration de l'Annexe B, joue sur l'emulateur.

Chaque etape agit par le TEXTE des elements (uiautomator), puis verifie ce
que l'ecran affiche reellement. Sortie : un rapport PASS/FAIL par etape et
des captures dans scripts/captures/.

Usage (emulateur demarre, APK installe) :
    ./gradlew installDebug
    python scripts/scenario_annexe_b.py

Pourquoi par le texte et non par coordonnees : Compose ne compose que les
elements visibles, et la position d'un bouton change des qu'une section
apparait au-dessus (les matieres apres le choix de la serie, par exemple).
Des coordonnees fixes tapent a cote sans erreur visible.

Ce script verifie que le scenario FONCTIONNE. Il ne remplace pas les
repetitions humaines avant la soutenance (jalon J5) : le critere du
document (3.1) est qu'un eleve parcoure seul le scenario, pas un script.

Chemin adb : %LOCALAPPDATA%/Android/Sdk (installation Android Studio par
defaut sous Windows).
"""
import os, re, subprocess, sys, time

ADB = os.path.join(os.environ["LOCALAPPDATA"], "Android", "Sdk", "platform-tools", "adb.exe")
DEV = ["-s", "emulator-5554"]
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "captures")
os.makedirs(OUT, exist_ok=True)
rapport = []


def adb(*args):
    return subprocess.run([ADB, *DEV, *args], capture_output=True, text=True,
                          encoding="utf-8", errors="replace").stdout


def noeuds():
    xml = adb("exec-out", "uiautomator", "dump", "/dev/tty")
    brut = re.findall(r'<node [^>]*?text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    return [(t.replace("&apos;", "'").replace("&amp;", "&"),
             (int(a) + int(c)) // 2, (int(b) + int(d)) // 2) for t, a, b, c, d in brut]


def textes():
    return [t for t, _, _ in noeuds() if t.strip()]


def present(fragment):
    return any(fragment.lower() in t.lower() for t in textes())


def attendre(fragment, essais=15):
    for _ in range(essais):
        if present(fragment):
            return True
        time.sleep(2)
    return False


def tap(x, y):
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(1.2)


def taper(fragment, occurrence=0, exact=False):
    hits = [(t, x, y) for t, x, y in noeuds()
            if (t == fragment if exact else fragment.lower() in t.lower()) and t]
    if len(hits) <= occurrence:
        return False
    tap(hits[occurrence][1], hits[occurrence][2])
    return True


def defiler(sens="haut", amplitude=900):
    y1, y2 = (1900, 1900 - amplitude) if sens == "haut" else (600, 600 + amplitude)
    adb("shell", "input", "swipe", "540", str(y1), "540", str(y2), "350")
    time.sleep(1.2)


def chercher(fragment, max_defilements=10):
    """Fait defiler jusqu'a ce que le texte soit visible (Compose ne compose que le visible)."""
    for _ in range(max_defilements):
        if present(fragment):
            return True
        defiler("haut")
    return present(fragment)


def repondre(fragment_question, reponse):
    """Tape la reponse situee dans la carte de CETTE question : le chip de ce libelle le plus proche en dessous."""
    for _ in range(8):
        n = noeuds()
        q = [(t, x, y) for t, x, y in n if fragment_question.lower() in t.lower()]
        if q:
            yq = q[0][2]
            chips = sorted((y, x) for t, x, y in n if t == reponse and y > yq)
            if chips and chips[0][0] - yq < 450:
                tap(chips[0][1], chips[0][0])
                return True
        defiler("haut", 500)
    return False


def capture(nom):
    with open(os.path.join(OUT, nom), "wb") as f:
        f.write(subprocess.run([ADB, *DEV, "exec-out", "screencap", "-p"], capture_output=True).stdout)


def etape(numero, libelle, ok, detail=""):
    rapport.append((numero, libelle, ok, detail))
    print(f"[{'PASS' if ok else 'FAIL'}] etape {numero} - {libelle}" + (f"  ({detail})" if detail else ""), flush=True)


# ---------------------------------------------------------------- lancement
adb("shell", "input", "keyevent", "KEYCODE_WAKEUP")
adb("shell", "am", "force-stop", "mg.itu.orientationpostbac")
adb("shell", "pm", "clear", "mg.itu.orientationpostbac")
adb("shell", "am", "start", "-n", "mg.itu.orientationpostbac/.MainActivity")
ok = attendre("Mon profil", 30)
etape(15, "l'application s'ouvre sur le profil", ok)
if not ok:
    capture("echec_lancement.png"); sys.exit(1)

# ---------------------------------------------------------------- 15-16-18 profil
etape(15, "choix de la serie D", taper("D", exact=True) and attendre("Ton niveau dans les matieres cles"))

niveaux_ok = True
for matiere in ["Mathematiques", "Physique", "Sciences de la vie et de la Terre"]:
    n = noeuds()
    ym = [y for t, x, y in n if t == matiere]
    chips = sorted((y, x) for t, x, y in n if t == "Bon" and ym and y > ym[0])
    if chips:
        tap(chips[0][1], chips[0][0])
    else:
        niveaux_ok = False
etape(16, "niveau Bon en maths, physique, SVT", niveaux_ok)

chercher("Combien d'annees")
contraintes = taper("Limite", exact=True) and taper("Antananarivo", exact=True) and taper("5 ans", exact=True)
etape(18, "contraintes : budget limite, Antananarivo, 5 ans", contraintes)
capture("scn_18_profil.png")

chercher("Continuer")
etape(17, "passage au questionnaire", taper("Continuer", exact=True) and attendre("Tes centres d'interet"))

# ---------------------------------------------------------------- 17 questionnaire (profil Investigateur)
reponses = [
    ("Reparer un appareil", "Assez"), ("mains et des outils", "Assez"),
    ("Comprendre pourquoi", "Beaucoup"), ("Resoudre un probleme", "Beaucoup"),
    ("dessiner", "Peu"), ("seule bonne reponse", "Peu"),
    ("Aider quelqu", "Peu"), ("sens utile", "Peu"),
    ("Convaincre un groupe", "Peu"), ("diriger une equipe", "Peu"),
    ("Tenir des comptes", "Moyen"), ("consignes claires", "Moyen"),
]
manquees = [q for q, r in reponses if not repondre(q, r)]
complet = attendre("12 reponses sur 12", 3)
etape(17, "questionnaire rempli (profil Investigateur)", not manquees and complet,
      f"non repondues : {manquees}" if manquees else "12 reponses sur 12")

etape(17, "validation du questionnaire", taper("Voir mes resultats") and attendre("Parcours suggeres"))

# ---------------------------------------------------------------- 19 parcours
domaines_vus = [d for d in ["Informatique", "Sante", "Droit", "Gestion", "Ingenierie", "Formation professionnelle"] if present(d)]
avertissement = present("Questionnaire incomplet")
etape(19, "plusieurs domaines avec un indice de compatibilite", len(domaines_vus) >= 3 and not avertissement,
      f"domaines visibles : {domaines_vus}" + (" / AVERTISSEMENT INCOMPLET AFFICHE" if avertissement else ""))
capture("scn_19_parcours.png")

# ---------------------------------------------------------------- 20 explorer Informatique + filtre
etape(20, "explore le domaine Informatique", taper("Informatique", exact=True) and attendre("Domaine : Informatique"))
capture("scn_20_formations.png")

# ---------------------------------------------------------------- 21-22 fiche ENI, arrete, depliage
chercher("Licence en Informatique")
fiche = False
for t, x, y in noeuds():
    if t == "Licence en Informatique":
        tap(x, y); fiche = attendre("Reconnaissance officielle"); break
etape(22, "fiche avec reference d'arrete et date", fiche and present("31175/2012") and present("liste regionale 2026"))
etape(22, "cout non renseigne affiche comme tel (cas prix null)", chercher("Cout indicatif") and present("non renseigne"))
capture("scn_22_fiche.png")

defiler("bas", 1800)
chercher("Pourquoi cette formation")
depli = taper("Pourquoi cette formation") and attendre("Detail par critere")
etape(21, "depliage du score, critere par critere", depli)
capture("scn_21_depliage.png")

chercher("Ajouter a mon projet")
etape(25, "ajout de la Licence ENI au projet", taper("Ajouter a mon projet") and attendre("Retirer de mon projet"))
adb("shell", "input", "keyevent", "KEYCODE_BACK"); time.sleep(2)

# ---------------------------------------------------------------- 23-24 non eligibles
taper("Domaine : Informatique"); time.sleep(2)  # retire le filtre : les non eligibles sont dans tout le catalogue
trouve_section = chercher("Non eligible : pourquoi", 14)
etape(24, "section Non eligible : pourquoi ? visible", trouve_section)
motif_serie = chercher("Reservee aux series A1, A2", 6)
etape(24, "motif de serie lisible (Commerce international)", motif_serie)
motif_master = chercher("Formation de niveau Master", 6)
etape(23, "Master ENI non eligible, motif de niveau", motif_master)
capture("scn_24_non_eligibles.png")

master = False
for t, x, y in noeuds():
    if t == "Master en Informatique":
        tap(x, y); master = attendre("Reconnaissance officielle"); break
etape(23, "Master ENI : meme arrete, statut VERIFIEE", master and present("VERIFIEE") and present("31175/2012"))
capture("scn_23_master.png")
adb("shell", "input", "keyevent", "KEYCODE_BACK"); time.sleep(2)

# ---------------------------------------------------------------- 25 deuxieme candidature
defiler("bas", 4000); defiler("bas", 4000)
second = False
if chercher("Licence en Informatique appliquee", 8):
    for t, x, y in noeuds():
        if t == "Licence en Informatique appliquee":
            tap(x, y); break
    if attendre("Reconnaissance officielle") and chercher("Ajouter a mon projet"):
        second = taper("Ajouter a mon projet") and attendre("Retirer de mon projet")
etape(25, "ajout d'une deuxieme candidature", second)

# ---------------------------------------------------------------- 26 checklist
defiler("bas", 4000)
taper("Mon projet", exact=True)
projet = attendre("2 candidatures preparees")
etape(26, "Mon projet : 2 candidatures", projet)
etape(26, "checklist visible avec demarches", present("Reunir les pieces du dossier") and present("Demarches :"))
etape(26, "demarche d'habilitation ajoutee pour un statut a confirmer",
      chercher("arrete d'habilitation", 4))
capture("scn_26_projet.png")

# ---------------------------------------------------------------- bilan
crash = adb("logcat", "-d", "-t", "600")
fatal = [l for l in crash.splitlines() if "FATAL EXCEPTION" in l or ("AndroidRuntime" in l and " E " in l)]
etape("-", "aucun crash dans le logcat", not fatal, fatal[0] if fatal else "")

print()
echecs = [r for r in rapport if not r[2]]
print(f"BILAN : {len(rapport) - len(echecs)}/{len(rapport)} verifications reussies")
sys.exit(1 if echecs else 0)
