# Karoo Stamina

Extension Hammerhead Karoo 3 qui ajoute un champ de données **Stamina** (0 à 100 %), avec stamina potentiel,
temps estimé avant 0 % et tendance. Aucun accès réseau : CP et W' sont saisis à la main.

## Ce que fait le champ

- **Case « ligne »** (pleine largeur) : chiffre centré, flèche de tendance à droite, jauge à deux niveaux,
  « 0 % dans X min » et « Pot. N % ».
- **Case « demi-ligne »** : chiffre, flèche, jauge et « Pot. N % ».
- Couleur : vert au-dessus de 60 %, orange de 30 à 60 %, rouge sous 30 %.
- Jauge : partie pleine = stamina potentiel, partie claire = ce que tu perds en gardant l'effort actuel.

## Calcul (estimateur, pas la formule Firstbeat de Garmin)

- W'bal : modèle différentiel (Froncioni-Clarke) à partir de la puissance, 1 Hz.
- Fatigue lente : travail cumulé rapporté à une capacité d'endurance (auto = CP x 5,4 kJ).
- `stamina = 100 x reste_capacité x (0,4 + 0,6 x W'bal / W')`
- Potentiel : projection à 10 min à la puissance moyenne des 120 dernières secondes.
- Les constantes sont des valeurs de départ à calibrer sur tes sorties.

## Réglages

Ouvre l'application « Stamina » : CP (W), W' (kJ), capacité d'endurance (kJ, 0 = auto).
Valeurs lues au démarrage d'une sortie.

## Compiler l'APK avec GitHub Actions

1. Crée un dépôt GitHub et pousse ce dossier sur la branche `main`.
2. L'onglet **Actions** lance « Build APK ». L'APK est dans les artefacts (`karoo-stamina-apk`).
3. Pour une release : `git tag v1.0 && git push origin v1.0` publie l'APK dans **Releases**.
4. Si le build échoue sur la dépendance `io.hammerhead:karoo-ext` (401/403) : crée un jeton classique
   avec le scope `read:packages` et ajoute-le dans *Settings > Secrets and variables > Actions* sous le nom `GPR_TOKEN`.

## Installer sur le Karoo 3

Sideload de l'APK (voir la documentation Hammerhead), puis ajoute le champ « Stamina » dans une page de données.

## Compiler en local

`gradle.properties` utilisateur : `gpr.user=<ton pseudo GitHub>` et `gpr.key=<jeton read:packages>`, puis `./gradlew assembleRelease`.
