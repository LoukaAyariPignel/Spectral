# Gemmology — Design Document

## Vision générale

Un mod Fabric (Minecraft 1.21.1) basé sur la lumière comme source d'énergie. La lumière est collectée, transportée via des faisceaux, filtrée par des gemmes, et consommée par des machines. Chaque gemme possède une longueur d'onde unique qui détermine sa couleur et ses propriétés énergétiques.

---

## Les Gemmes

### Propriétés
- Chaque gemme possède une **longueur d'onde** (Float, en nm) stockée via un DataComponent
- La longueur d'onde détermine la **couleur visible** de la gemme (algorithme spectre visible)
- Deux gemmes ne sont jamais exactement identiques

### Spectre et progression

| Tier | Spectre | Longueur d'onde | Obtention | Effets / Usages |
|---|---|---|---|---|
| 1 | Visible | 380–780 nm | Prism Stand + lumière naturelle | Machines de base, faisceaux colorés |
| 2 | Proche UV | 300–380 nm | Machines avancées + crafts coûteux | Révèle minerais cachés, croissance accélérée |
| 3 | Proche IR | 780–1400 nm | Machines avancées + crafts coûteux | Chaleur, fusion rapide, vision thermique |
| 4 | UV profond | 100–300 nm | Crafts très difficiles | Désintégration, purification, dégâts mobs |
| 5 | IR lointain | 1400+ nm | Crafts très difficiles | Transmission d'énergie sans fil longue distance |
| 6 | X-ray / Gamma | < 10 nm | Crafts extrêmes, très rares | Vision à travers les murs, énergie maximale |

**Règle importante** : les gemmes hors spectre visible (Tier 2+) ne peuvent **jamais** être obtenues par exposition à la lumière naturelle. Elles sont réservées aux machines et crafts avancés. La valeur de longueur d'onde est strictement clampée entre 380 et 780 nm lors de toute exposition naturelle.

### Rendu visuel des gemmes hors-visible
- **UV** → gemme noire avec halo violet (particules)
- **IR** → gemme noire avec halo rouge/orange brillant
- **X-ray / Gamma** → gemme blanche éblouissante avec effet glow

---

## Obtention des Gemmes (Visible — Tier 1)

### Étape 1 — Miner le Raw Crystal Ore
- Nouveau minerai généré dans les grottes/underground
- En le minant : obtention d'un `Raw Crystal` (cristal brut, sans couleur ni longueur d'onde)

### Étape 2 — Exposition à la lumière (Prism Stand)
- Placer le `Raw Crystal` dans un bloc **Prism Stand**
- La longueur d'onde se fixe selon la source de lumière et l'environnement :

| Source de lumière | Longueur d'onde approximative |
|---|---|
| Soleil à midi | ~550 nm (vert/jaune) |
| Lever / coucher de soleil | ~620 nm (orange/rouge) |
| Nuit / lune | ~430 nm (violet/bleu) |
| Torche / lave | ~680 nm (rouge chaud) |
| Beacon (avec verre coloré) | Longueur d'onde précise selon couleur |

→ La gemme obtenue a une longueur d'onde **aléatoire dans la plage correspondante** (pas exactement fixe, mais guidée par l'environnement)

### Étape 3 — Raffinement (Spectral Refiner — Mid game)
- Machine dédiée permettant de **faire passer un faisceau coloré** à travers la gemme
- Déplace la longueur d'onde vers une valeur précise ciblée par le joueur
- Permet d'obtenir exactement la gemme nécessaire pour une machine spécifique

---

## Système de Faisceau Lumineux

### Principe
```
[Émetteur] ──────► [Prism Stand + Gemme] ──────► [Machine / Récepteur]
 (beam blanc)        (filtre la longueur d'onde)   (reçoit un beam coloré)
```

### Composants du système

**Émetteur**
- Génère un faisceau de lumière brute dans une direction
- Comparable à un beacon mais horizontal et directionnel

**Prism Stand / Support de gemme**
- Bloc dans lequel on insère une gemme
- Le faisceau entrant est filtré selon la longueur d'onde de la gemme
- Peut être chaîné : plusieurs gemmes en série pour combiner/mélanger les couleurs
- Sert aussi à exposer les `Raw Crystal` à la lumière naturelle (voir Obtention)

**Machines réceptrices**
- Reçoivent un faisceau d'une certaine longueur d'onde et produisent un effet
- Chaque machine est sensible à une **plage de longueur d'onde** :

| Longueur d'onde | Machine / Effet |
|---|---|
| ~400 nm (violet) | Transmutation / alchimie |
| ~470 nm (bleu) | Purification de l'eau, potions |
| ~530 nm (vert) | Croissance accélérée des plantes |
| ~580 nm (jaune) | Énergie générale, stockage |
| ~650 nm (orange) | Forge / fusion accélérée |
| ~700 nm (rouge) | Chaleur intense, four avancé |
| UV (< 380 nm) | Effets spéciaux, machines end-game |
| IR (> 780 nm) | Chaleur extrême, énergie sans fil |

**Spectral Refiner**
- Machine mid-game permettant d'affiner la longueur d'onde d'une gemme existante
- Utilise elle-même un faisceau pour fonctionner

---

## Progression du joueur

```
EARLY GAME
├── Trouve du Raw Crystal Ore en minant
├── Pose sur Prism Stand, expose à la lumière naturelle
└── Obtient des gemmes visibles (~aléatoires selon l'environnement)
         ↓
MID GAME
├── Construit le Spectral Refiner
├── Affine les gemmes vers des longueurs d'onde précises
├── Construit des faisceaux pour alimenter des machines
└── Débloqueattente des effets ciblés (croissance, forge, etc.)
         ↓
LATE GAME
├── Machines avancées + crafts coûteux
├── Gemmes Proche UV et Proche IR
└── Effets puissants, énergie dense
         ↓
END GAME
├── Crafts extrêmes, matériaux très rares
├── UV profond, IR lointain, X-ray, Gamma
└── Capacités exceptionnelles : vision à travers les murs, énergie sans fil, etc.
```

---

## Améliorations du code existant (à faire en premier)

Avant d'ajouter les nouvelles fonctionnalités, corriger les problèmes identifiés :

1. Supprimer `java.awt.Color` des imports — remplacer par calcul entier direct
2. Corriger la valeur de retour par défaut du color provider : `0` → `-1`
3. Extraire les constantes `MIN_WAVELENGTH = 380f` et `MAX_WAVELENGTH = 780f`
4. Corriger la typo `waveLenght` → `wavelength`
5. Remplacer `new Random()` par `ThreadLocalRandom.current()`
6. Corriger les conventions de nommage Java (`Gamma` → `gamma`, `IntensityMax` → `intensityMax`)
7. Simplifier le bloc de vérification dans le color provider (double négation, 3 appels → 1)
8. Corriger le message de log dans `ModComponents.java`
9. Supprimer le code placeholder dans les tag providers
10. Supprimer le mixin vide `ExampleMixin.java`
11. Ajouter un `.gitignore`

---

## Ordre d'implémentation recommandé

1. **Nettoyage du code existant** (corrections listées ci-dessus)
2. **Raw Crystal Ore + Raw Crystal** (item + génération monde)
3. **Prism Stand** (bloc + logique d'exposition à la lumière)
4. **Émetteur de faisceau** (bloc + rendu beam)
5. **Récepteur / machines de base** (four accéléré, croissance)
6. **Spectral Refiner** (affinement longueur d'onde)
7. **Gemmes hors-visible** (UV, IR, X-ray) + machines end-game
