# Gemmology — Design Document (Détaillé)

## Vision générale

Un mod Fabric (Minecraft 1.21.1, Java 21) qui transforme la lumière en source d'énergie.  
La lumière est collectée, convertie en **Photons (PH)**, transportée via des **faisceaux**, filtrée par des **gemmes**, et consommée par des **machines**.  
Chaque gemme possède une longueur d'onde unique (en nm) qui détermine sa couleur et les effets qu'elle peut produire sur un faisceau.

---

## Unité d'énergie : les Photons (PH)

- Unité interne : **Photon (PH)**
- Toutes les machines produisent et consomment des PH/tick (1 tick = 1/20e de seconde)
- Les Photons ne peuvent pas voyager seuls dans des câbles classiques : ils voyagent uniquement via des **faisceaux lumineux** ou sont stockés dans des **Light Battery**
- Un faisceau transporte un **débit** (PH/tick) et une **longueur d'onde** (nm)

---

## Les Gemmes

### Propriétés d'une gemme
- Stocke une **longueur d'onde** `float` (en nm) via le DataComponent `gemmology:wave_length`
- La longueur d'onde détermine :
  - La **couleur affichée** (via l'algorithme de conversion spectre → RGB)
  - Les **effets produits** lorsqu'elle filtre un faisceau
  - La **compatibilité** avec les machines réceptrices
- Deux gemmes ne sont jamais exactement identiques (valeurs flottantes)
- Le tooltip affiche la longueur d'onde avec 1 décimale : `"542.3 nm"`

### Spectre complet et progression

| Tier | Nom | Plage (nm) | Couleur visible | Obtention | Effets principaux |
|---|---|---|---|---|---|
| 1 | Visible | 380–780 | Toutes couleurs | Prism Stand + lumière | Faisceaux colorés, machines de base |
| 2 | Proche UV | 300–380 | Noir + halo violet | Chromatic Compressor | Révèle minerais, désinfection, buff plantes |
| 3 | Proche IR | 780–1400 | Noir + halo rouge | Thermal Expander | Chaleur, forge rapide, vision thermique |
| 4 | UV profond | 100–300 | Noir + halo blanc-violet | Spectral Forge | Désintégration, purification, dégâts mobs |
| 5 | IR lointain | 1400–10 000 | Noir + halo orange intense | Spectral Forge | Transmission énergie sans fil, chaleur extrême |
| 6 | X-ray | 0.01–10 | Blanc éblouissant + glow | Ultime (end-game) | Vision à travers les murs, énergie maximale |
| 7 | Gamma | < 0.01 | Arc-en-ciel pulsant | Ultime (end-game) | Effets combinés, dégâts massifs, téléportation |

**Règle stricte** : les Tiers 2–7 ne peuvent **jamais** être produits par exposition à la lumière naturelle.  
La plage d'exposition naturelle est clampée à `[380, 780]` dans le code.

### Rendu visuel par tier

| Tier | Texture gemme | Effet visuel |
|---|---|---|
| Visible | Translucide colorée dynamiquement | Légère émission de lumière de la même couleur |
| Proche UV | Noire avec veines violettes | Halo violet pulsant (particules `WITCH`) |
| Proche IR | Noire avec veines rouges | Halo rouge/orange chaud (particules `FLAME`) |
| UV profond | Noire très sombre | Halo blanc-violet intense, distorsion visuelle |
| IR lointain | Noire avec reflets orange | Chaleur ondulante (effet heat-haze) |
| X-ray | Blanche opaque éblouissante | Glow constant, éclaire les blocs proches |
| Gamma | Iridescente arc-en-ciel | Pulsation rapide de toutes les couleurs, aura |

---

## Obtention des Gemmes

### Étape 0 — Raw Crystal Ore

**Génération dans le monde :**
- Biomes : tous les biomes (légèrement plus fréquent dans les biomes froids et sombres)
- Niveaux Y : -20 à -80 (deep underground, similaire à l'améthyste)
- Taille de veine : 1 à 4 blocs
- Fréquence : plus rare que le fer, légèrement plus commun que le diamant
- Nécessite un pickaxe en fer ou mieux pour être miné
- Drops : 1 à 3 `Raw Crystal` (Fortune augmente le drop)

**Aspect visuel du bloc :**
- Base de pierre profonde (`deepslate`) avec des inclusions translucides blanc-gris scintillantes
- Légère lueur (light level 2) pour être repérable dans l'obscurité

### Étape 1 — Raw Crystal (item)

- Cristal brut, incolore, sans longueur d'onde
- Ne peut pas être utilisé comme gemme directement
- Peut être posé dans un Prism Stand pour l'attunement
- Stack de 64

### Étape 2a — Attunement via faisceau + verre teinté (Early game, rapide)

La méthode la plus accessible dès le début : tirer un faisceau à travers du **verre teinté** vers un Prism Stand contenant un Raw Crystal.

```
[Light Emitter] ──► [Verre teinté] ──► [Prism Stand (Raw Crystal)] → Gem
   (beam blanc)      (filtre grossier)    (absorbe la longueur d'onde)
```

**Le verre teinté filtre le beam vers une plage approximative :**

| Couleur verre | Longueur d'onde produite | Variation aléatoire |
|---|---|---|
| Rouge | ~700 nm | ±40 nm |
| Orange | ~630 nm | ±40 nm |
| Jaune | ~575 nm | ±40 nm |
| Vert | ~530 nm | ±40 nm |
| Cyan | ~495 nm | ±40 nm |
| Bleu | ~465 nm | ±40 nm |
| Violet | ~415 nm | ±40 nm |
| Blanc / incolore | ~590 nm | ±60 nm |
| Noir | Bloque le beam | — |

**Caractéristiques :**
- Durée d'attunement : **1 minute** (bien plus rapide que le soleil)
- La variation `±40 nm` est intentionnelle : la gemme obtenue est **rarement à la valeur optimale d'une machine**
- C'est la méthode de démarrage — fonctionnelle mais imprécise
- Plusieurs panneaux de verre en série n'affinent pas davantage (le premier verre fixe la plage)

> **Design intent** : le joueur obtient une gemme verte à ~548 nm alors que la Photosynthesis Accelerator est optimale à 530 nm. La machine fonctionne à 70% d'efficacité. Il a envie de raffiner sa gemme.

---

### Étape 2b — Attunement via lumière naturelle (Early game, lent mais plus précis)

Le `Prism Stand` détecte la lumière ambiente et attribue une longueur d'onde au Raw Crystal.

**Algorithme de détermination de la longueur d'onde :**

1. **Source principale** (détermine la plage de base) :

| Condition | Plage de base | Longueur d'onde cible |
|---|---|---|
| Lumière du soleil (sky light ≥ 12) + midi (ticks 9000–15000) | 520–570 nm | Vert/jaune |
| Lumière du soleil + matin/après-midi (ticks 6000–9000 / 15000–18000) | 570–640 nm | Jaune/orange |
| Lever/coucher de soleil (ticks 4000–6000 / 18000–20000) | 600–680 nm | Orange/rouge |
| Nuit + lune visible (ticks 13000–23000, sky light ≥ 4) | 410–460 nm | Violet/bleu |
| Nuit sans lune / orage (sky light = 0) | 380–420 nm | Violet profond |
| Torche / lanterne dans rayon de 3 blocs | 650–700 nm | Rouge chaud |
| Lave dans rayon de 3 blocs | 680–740 nm | Rouge profond |
| Feu dans rayon de 3 blocs | 620–670 nm | Orange-rouge |
| Glowstone dans rayon de 3 blocs | 560–600 nm | Jaune |
| Beacon (avec verre coloré) | Fixe selon couleur | Précise |

2. **Modificateur de biome** (±15 nm) :
   - Biomes désertiques/chauds : +10 nm (vers le rouge)
   - Biomes enneigés/froids : -10 nm (vers le bleu)
   - Biomes océaniques : -5 nm

3. **Modificateur météo** :
   - Pluie : -20 nm
   - Orage : -40 nm

4. **Variation aléatoire finale** : ±8 nm (gaussian)

5. **Clamp final** : `[380, 780]` nm (jamais hors visible)

**Durée d'attunement :**
- Plein soleil à midi : 3 minutes (3600 ticks)
- Conditions partielles : 5 à 8 minutes
- Torche/lave uniquement : 6 minutes
- Nuit (lune) : 10 minutes
- Un indicateur de progression (barre) s'affiche dans le GUI du Prism Stand

**Cas spéciaux avec le Beacon :**
- Couleur du verre → longueur d'onde précise mappée
- Rouge → 700 nm, Orange → 630 nm, Jaune → 575 nm, Vert → 530 nm, Cyan → 495 nm, Bleu → 465 nm, Violet → 415 nm
- Durée d'attunement réduite à 1 minute (source de lumière pure)

### Étape 3 — Raffinement (Spectral Refiner — Mid game)

- Insère une gemme existante + définit une longueur d'onde cible
- La cible peut être définie :
  - En insérant une **gemme de référence** dans un second slot
  - Manuellement via un curseur dans le GUI (précision ±5 nm)
- Vitesse de raffinement : **1 nm par seconde** (20 ticks) en conditions normales
- Consomme 20 PH/tick pendant l'opération
- Limite du Spectral Refiner de base : ne peut pas dépasser les bornes du visible `[380, 780]`
- Les gemmes hors-visible nécessitent les machines Tier 2+

### Étape 4 — Gemmes hors-visible (Late/End game)

#### Chromatic Compressor (UV — Tier 2)
- Prend une gemme visible à ≤400 nm
- La compresse sous les 380 nm → Proche UV
- Recette coûteuse : gemme + amethyste + cristal de nether + echo shard
- Temps de traitement : 5 minutes + consommation de 100 PH/tick

#### Thermal Expander (IR — Tier 3)
- Prend une gemme visible à ≥720 nm
- L'expand au-delà des 780 nm → Proche IR
- Recette : gemme + blaze rod + magma block + netherite ingot

#### Spectral Forge (Tiers 4–5)
- Machine end-game nécessitant un beacon actif à proximité (rayon 10 blocs)
- Produit les gemmes UV profond et IR lointain
- Consomme des nether stars dans le craft
- 500 PH/tick pendant 10 minutes

#### Ultime / Gamma (Tiers 6–7)
- Nécessite un portail de l'end actif à proximité (8 blocs)
- Ingredients : dragon egg, nether star × 4, gemmes de tous les tiers précédents
- Extrêmement coûteux et rare, 1 seul par partie (non renouvelable sauf dragon respawn)

---

## Système de Faisceau Lumineux

### Principe physique

```
[Solar Collector]
       │ (Photons)
       ▼
[Light Emitter] ──────────────────────────────────────────► [Machine Réceptrice]
  (beam blanc,           [Prism Stand]                        reçoit beam coloré
   débit X PH/tick)    (Gemme 530nm)                         à 530nm → effet vert
                              │
                        filtre le beam :
                        longueur d'onde = gem
                        débit × 0.85 (perte 15%)
```

### Propagation du faisceau

- Le faisceau est calculé **instantanément** par raycasting (pas de délai de propagation)
- Il voyage en **ligne droite** dans la direction de la face de l'émetteur
- **Portée maximale** : 32 blocs par défaut (extensible avec upgrades)
- **Bloqué par** : tout bloc solide opaque
- **Traverse** : verre transparent (sans teinte), air, eau (avec perte de 5 PH/bloc)

### Interactions avec les blocs

| Bloc rencontré | Comportement |
|---|---|
| Bloc solide | Arrêt du beam, impact visuel (étincelles) |
| Verre transparent | Traverse sans modification |
| Verre teinté | Traverse et décale la longueur d'onde (±30 nm vers la couleur du verre) |
| Prism Stand (avec gemme) | Remplace la longueur d'onde du beam par celle de la gemme. Perte : 15% du débit |
| Prism Stand (sans gemme) | Bloque le beam |
| Light Battery | Absorbe le beam, stocke les PH |
| Machine réceptrice | Consomme le beam pour fonctionner |
| Eau / pluie | Atténue le débit (-5 PH/bloc), légère dispersion |
| Miroir (futur) | Réfléchit le beam à 90° |
| Beam Splitter (futur) | Divise le beam en 2 (50% chacun) |

### Chaînage des Prism Stands

Plusieurs Prism Stands peuvent être placés en série sur le chemin d'un beam :
- Chaque Prism Stand **remplace** la longueur d'onde par celle de sa gemme
- Exemple : beam blanc → Prism Stand (700nm rouge) → Prism Stand (530nm vert) → beam vert à 530nm
- Utile pour créer des effets complexes ou rediriger la longueur d'onde progressivement

### Rendu visuel du faisceau

- Rendu sous forme d'un cylindre lumineux fin (rayon ~0.1 bloc)
- Couleur du cylindre = couleur RGB correspondant à la longueur d'onde du beam
- Gemmes UV/IR : le beam est **invisible** mais des particules le trahissent
  - UV : particules violettes flottant autour du trajet
  - IR : légère distorsion thermique (heat-haze) le long du trajet
- Intensité visuelle proportionnelle au débit (PH/tick)
- Effets de scintillement si le débit fluctue (ex: Solar Collector en fin de journée)
- Particules d'impact là où le beam touche un bloc solide

---

## Système d'efficacité des machines

Chaque machine réceptrice a une **longueur d'onde optimale**. L'efficacité dépend de la distance entre la longueur d'onde du beam reçu et cet optimum.

### Formule d'efficacité

```
delta = |wavelength_beam - wavelength_optimale|

efficacité =
  delta ≤ 5 nm   → 100% (parfait)
  delta ≤ 20 nm  →  80%
  delta ≤ 50 nm  →  55%
  delta ≤ 100 nm →  30%
  delta > 100 nm →   0% (machine inactive, trop loin de l'optimum)
```

L'efficacité affecte :
- La **vitesse** de traitement (Crystal Furnace, Photosynthesis Accelerator…)
- Le **rendement** (chance de doubler les outputs)
- La **consommation** (une machine sous-optimale consomme autant mais produit moins)

### Longueurs d'onde optimales par machine

| Machine | Optimum | Plage fonctionnelle | Effet à 100% |
|---|---|---|---|
| Crystal Furnace | 700 nm | 600–780 nm | Cuisson 3× + 10% ore doubling |
| Photosynthesis Accelerator | 530 nm | 430–630 nm | Croissance 3× + bonemeal aura |
| Spectral Refiner | Toute longueur visible | 380–780 nm | 1 nm/s (pas d'optimum unique) |
| Light Battery (charge) | Toute longueur visible | 380–780 nm | Absorption maximale |
| UV Sterilizer | 340 nm | 300–380 nm | 2 dégâts/s rayon 5 |
| Thermal Forge | 900 nm | 780–1400 nm | Cuisson 5× + alloys |
| Spectral Transmitter | 1200 nm | 1400+ nm | Transmission sans perte |
| X-Ray Scanner | 0.5 nm | < 10 nm | Reveal complet |

### Indicateur visuel d'efficacité

Le GUI de chaque machine affiche :
- La longueur d'onde reçue (en nm)
- La longueur d'onde optimale (en nm)
- Un indicateur coloré :
  - **Vert** : efficacité ≥ 80%
  - **Jaune** : efficacité 30–79%
  - **Rouge** : efficacité < 30%
  - **Gris** : machine inactive (hors plage)

### Boucle de progression induite

```
Verre teinté → gemme ≈ approx. → machine à ~55% efficacité
                                          ↓
                             Joueur veut mieux → Spectral Refiner
                                          ↓
                             gemme à ±5 nm de l'optimum → 100% efficacité
```

---

## Blocs et Machines

### Solar Collector

**Description** : Collecte l'énergie solaire et la convertit en Photons. Ne produit pas de faisceau directement.

**Production :**
- Plein soleil (sky light 15, midi) : **10 PH/tick**
- Soleil partiel (sky light 10–14) : proportionnel (~6–9 PH/tick)
- Nuit / couvert : **0 PH/tick**
- Sous la pluie : **2 PH/tick** (diffusion)

**Stockage interne** : 5 000 PH (buffer pour les fluctuations)  
**Output** : se connecte à un Light Emitter ou Light Battery adjacent (face du bas ou des côtés)

**GUI** : Barre de production actuelle + total stocké en buffer

**Recette craft :**
```
[Verre]   [Verre]   [Verre]
[Quartz]  [Gemme]   [Quartz]    → 1 Solar Collector
[Fer]     [Fer]     [Fer]
```
(Gemme de n'importe quelle longueur d'onde — elle sert de capteur)

---

### Light Emitter

**Description** : Convertit des Photons (depuis un Solar Collector ou une Light Battery adjacente) en faisceau lumineux.

**Comportement :**
- S'active automatiquement quand il reçoit des PH (ou via signal redstone pour l'éteindre)
- Direction du faisceau : la **face avant** du bloc (configurable par rotation)
- Débit du faisceau = débit reçu en PH/tick (1 PH/tick reçu = 1 PH/tick émis)
- Longueur d'onde initiale du faisceau : **600 nm** (lumière blanche/neutre)
- Portée par défaut : 32 blocs

**GUI** : Indicateur de débit entrant/sortant, longueur d'onde actuelle du beam émis

**Recette craft :**
```
[Fer]       [Quartz]    [Fer]
[Redstone]  [Gemme]     [Redstone]    → 1 Light Emitter
[Fer]       [Quartz]    [Fer]
```

---

### Prism Stand

**Description** : Bloc à double fonction — attunement des Raw Crystals ET filtrage des faisceaux.

**Mode Attunement** (pas de beam entrant) :
- Slot : 1 Raw Crystal en entrée
- Détecte la lumière ambiente (algorithme détaillé plus haut)
- Après la durée d'attunement, produit 1 Gem en sortie
- GUI : slot input, slot output, barre de progression, affichage de la longueur d'onde en cours d'attribution

**Mode Filtre** (beam entrant détecté) :
- Slot : 1 Gemme insérée
- Le faisceau entrant est remplacé par la longueur d'onde de la gemme
- Perte de 15% du débit (absorption partielle)
- Le beam de sortie sort par la face opposée à l'entrée
- GUI : slot gemme, affichage longueur d'onde entrante / sortante, débit

**Recette craft :**
```
[Verre]   [Verre]   [Verre]
[Quartz]  [Air]     [Quartz]    → 1 Prism Stand
[Quartz]  [Fer]     [Quartz]
```

---

### Light Battery (Condensateur Lumineux)

**Description** : Stocke les Photons reçus par un faisceau ou un Solar Collector adjacent.

**Capacité :** 50 000 PH (Tier 1) — augmentable avec upgrades  
**Charge :** Absorbe tout faisceau entrant, convertit en PH stockés  
**Décharge :** Alimente un Light Emitter adjacent avec un débit configurable  
**GUI :** Barre de charge, débit de charge/décharge, toggle charge/décharge

**Recette craft :**
```
[Fer]       [Quartz]    [Fer]
[Verre]     [Gemme]     [Verre]    → 1 Light Battery
[Fer]       [Redstone]  [Fer]
```

---

### Crystal Furnace

**Description** : Four alimenté par faisceau lumineux. Plus rapide que le four vanilla.

**Longueur d'onde requise :** 620–780 nm (orange à rouge)  
**Débit minimum :** 8 PH/tick pour fonctionner  
**Vitesse de cuisson :**
- 8 PH/tick → 1.5× la vitesse vanilla
- 16 PH/tick → 2× la vitesse vanilla
- 32+ PH/tick → 3× la vitesse vanilla

**Bonus avec gemme rouge (700 nm)** : chance 10% de doubler l'output (ore doubling)  
**GUI :** Slot input, slot output, indicateur longueur d'onde reçue, barre de progression, vitesse actuelle

**Recette craft :**
```
[Pierre]  [Pierre]  [Pierre]
[Pierre]  [Air]     [Pierre]    → 1 Crystal Furnace
[Pierre]  [Quartz]  [Pierre]
```

---

### Photosynthesis Accelerator

**Description** : Accélère la croissance des plantes dans une zone grâce à la lumière verte.

**Longueur d'onde requise :** 500–570 nm (cyan à vert-jaune)  
**Débit minimum :** 12 PH/tick  
**Rayon d'effet :** 5 blocs (cubique)  
**Effets :**
- Crops (blé, carotte, pomme de terre…) : croissance 3× plus rapide
- Arbres (saplings) : poussent en 30 secondes (au lieu d'aléatoire)
- Herbe / fougères : apparaissent sur la terre exposée
- Bonus : à 530 nm exactement, effet "bonemeal aura" 1×/minute sur toutes les plantes du rayon

**GUI :** Longueur d'onde reçue, rayon d'effet, liste des plantes affectées

**Recette craft :**
```
[Verre]     [Feuilles]  [Verre]
[Fer]       [Gemme]     [Fer]       → 1 Photosynthesis Accelerator
[Fer]       [Terre]     [Fer]
```
(Gemme verte ~530 nm recommandée dans la recette mais pas obligatoire)

---

### Spectral Refiner

**Description** : Affine la longueur d'onde d'une gemme vers une valeur cible précise.

**Longueur d'onde requise (pour fonctionner) :** Toute longueur d'onde visible  
**Débit minimum :** 20 PH/tick  
**Fonctionnement :**
- Slot A : Gemme à raffiner
- Slot B : Gemme de référence (optionnel) **ou** curseur de cible dans le GUI
- Chaque seconde (20 ticks) de fonctionnement : la longueur d'onde de la gemme se rapproche de 1 nm de la cible
- Limite : reste dans la plage visible `[380, 780]` nm
- Si la longueur d'onde actuelle est à moins de 1 nm de la cible : la gemme est finalisée

**Exemple :** Gemme à 650 nm, cible 530 nm → 120 secondes de traitement (2 minutes)

**GUI :** Slot gemme (A), slot référence (B), curseur cible, barre de progression, longueur d'onde actuelle → cible

**Recette craft :**
```
[Or]      [Quartz]  [Or]
[Quartz]  [Diamant] [Quartz]    → 1 Spectral Refiner
[Fer]     [Redstone][Fer]
```

---

### UV Sterilizer (Late game — Tier 2)

**Description** : Émet un rayonnement UV dans une zone, tuant/repoussant les mobs hostiles.

**Longueur d'onde requise :** 300–380 nm (UV proche)  
**Débit minimum :** 50 PH/tick  
**Effets :**
- Mobs hostiles dans un rayon de 5 blocs : 2 dégâts/seconde + effet "Brûlure UV"
- Mobs hostiles dans un rayon de 10 blocs : ralentissement + aveuglement
- Joueurs dans le rayon : effet "Nausée" si exposés plus de 30 secondes sans protection
- Les plantes et cultures dans le rayon poussent 2× plus vite (UV stimule la chlorophylle)
- Stérilise l'eau (dans des cauldrons) → eau purifiée (futur usage potions)

**Recette craft :** Nécessite une gemme UV proche (via Chromatic Compressor)

---

### Thermal Forge (Late game — Tier 3)

**Description** : Four extrême alimenté par faisceau IR, permet le craft d'alliages spéciaux.

**Longueur d'onde requise :** 780–1400 nm (IR proche)  
**Débit minimum :** 80 PH/tick  
**Fonctions :**
- Smelt 5× plus vite que le four vanilla
- Ore doubling (25% chance)
- Permet de créer des **Photon Alloys** (alliages spéciaux) nécessaires pour les machines end-game
- Les recettes de la Thermal Forge ne peuvent pas être craftées autrement

---

### Spectral Transmitter + Receiver (End game — Tier 5)

**Description** : Transmet de l'énergie (Photons) sans fil entre deux points.

**Longueur d'onde requise :** > 1400 nm (IR lointain)  
**Débit maximum :** 200 PH/tick  
**Portée :** 64 blocs (ligne de vue non requise)  
**Fonctionnement :**
- Le Transmitter reçoit un faisceau IR
- Le Receiver reconstruit le faisceau de l'autre côté (même longueur d'onde, même débit - 10%)
- Un seul Transmitter peut être couplé à un Receiver via un item de couplage (Spectral Link Crystal)
- Traversent les murs et le terrain

---

### X-Ray Scanner (End game — Tier 6)

**Description** : Révèle tous les minerais dans un rayon autour du bloc.

**Longueur d'onde requise :** < 10 nm (X-ray)  
**Débit minimum :** 200 PH/tick  
**Effets :**
- Tous les blocs de minerai dans un rayon de 16 blocs s'affichent avec un contour lumineux (effect Glowing)
- Effet actif en continu tant que la machine est alimentée
- Affecte uniquement le joueur qui a activé la machine (et ceux dans un rayon de 8 blocs)

---

## Progression du joueur

```
EARLY GAME (Jours 1–7)
├── Trouver et miner du Raw Crystal Ore (Y: -20 à -80)
├── Crafter un Prism Stand
├── Exposer les Raw Crystals à la lumière (soleil, torche) → gemmes visibles aléatoires
├── Crafter un Solar Collector + Light Emitter
└── Premier faisceau lumineux → Crystal Furnace → four amélioré

MID GAME (Jours 7–30)
├── Crafter un Spectral Refiner
├── Affiner les gemmes vers des longueurs d'onde précises
├── Construire des réseaux de faisceaux (Solar → Emitter → Prism Stand → Machine)
├── Photosynthesis Accelerator → ferme automatisée
└── Light Battery → stockage d'énergie pour la nuit

LATE GAME (Jours 30–100)
├── Chromatic Compressor → premières gemmes UV (Tier 2)
├── Thermal Expander → premières gemmes IR (Tier 3)
├── UV Sterilizer → défense passive de la base
├── Thermal Forge → alliages spéciaux, meilleures recettes
└── Spectral Forge → gemmes Tier 4–5

END GAME
├── Spectral Transmitter/Receiver → énergie sans fil sur toute la carte
├── X-Ray Scanner → trouver facilement tous les minerais rares
├── Gemmes X-ray et Gamma → effets combinés, puissance maximale
└── Machines ultimes (à définir en cours de développement)
```

---

## Balance et chiffres

| Machine | PH/tick consommés | Longueur d'onde | Effet |
|---|---|---|---|
| Solar Collector | +10 (production) | — | Source principale |
| Crystal Furnace | -8 à -32 | 620–780 nm | Cuisson 1.5× à 3× |
| Photosynthesis Acc. | -12 | 500–570 nm | Croissance 3× |
| Spectral Refiner | -20 | Tout visible | 1 nm/seconde |
| Light Battery | ±50 000 (stockage) | Tout | Buffer |
| UV Sterilizer | -50 | 300–380 nm | 2 dégâts/s (rayon 5) |
| Thermal Forge | -80 | 780–1400 nm | Cuisson 5× |
| Spectral Transmitter | -200 | 1400+ nm | Énergie sans fil |
| X-Ray Scanner | -200 | < 10 nm | Reveal minerais |

**Perte par Prism Stand :** 15% du débit  
**Perte dans l'eau :** 5 PH/bloc  
**Buffer interne Solar Collector :** 5 000 PH  
**Capacité Light Battery Tier 1 :** 50 000 PH

---

## Architecture technique

### Packages prévus

```
fr.skylined.gemmology/
├── Gemmology.java                    (existant — main init)
├── GemmologyClient.java              (existant — color provider)
├── GemmologyDataGenerator.java       (existant — datagen)
├── component/
│   └── ModComponents.java            (existant — WAVE_LENGTH component)
├── item/
│   ├── ModItems.java                 (existant + Raw Crystal)
│   └── custom/
│       ├── GemItem.java              (existant)
│       └── RawCrystalItem.java       (nouveau)
├── block/
│   ├── ModBlocks.java                (nouveau — registre blocs)
│   └── custom/
│       ├── PrismStandBlock.java
│       ├── LightEmitterBlock.java
│       ├── SolarCollectorBlock.java
│       ├── CrystalFurnaceBlock.java
│       ├── PhotosynthesisAcceleratorBlock.java
│       ├── SpectralRefinerBlock.java
│       └── LightBatteryBlock.java
├── blockentity/
│   ├── ModBlockEntities.java
│   ├── PrismStandBlockEntity.java
│   ├── LightEmitterBlockEntity.java
│   └── (un BlockEntity par machine avec GUI)
├── beam/
│   ├── LightBeam.java                (données d'un faisceau : direction, longueur d'onde, débit)
│   └── LightBeamManager.java         (calcul/propagation des faisceaux par tick)
├── energy/
│   └── PhotonStorage.java            (interface de stockage PH, implémentée par Solar/Battery)
├── screen/
│   ├── ModScreenHandlers.java
│   └── (un ScreenHandler + Screen par machine avec GUI)
├── datagen/
│   └── (providers existants + nouveaux pour blocs/tags/loot)
├── util/
│   └── WavelengthUtil.java           (constantes MIN/MAX, conversion couleur, clamping)
└── mixin/                            (vide pour l'instant — ExampleMixin à supprimer)
```

### Points techniques clés

**Faisceaux :**
- Raycasting via `world.raycast()` à chaque tick sur les BlockEntity actifs (LightEmitter)
- Résultat stocké dans le BlockEntity (liste de blocs touchés + longueur d'onde courante)
- Re-calcul seulement si un bloc dans la trajectoire change (via `neighborUpdate`)
- Rendu client : `WorldRenderEvents.AFTER_TRANSLUCENT_BLOCKS` pour le cylindre lumineux

**Énergie (Photons) :**
- Pas de câbles : l'énergie circule uniquement via les faisceaux
- Interface `PhotonStorage` : `int receivePhotons(int amount)` / `int extractPhotons(int amount)` / `int getStoredPhotons()`
- Découverte des adjacents via `BlockApiLookup` (Fabric Transfer API ou API custom)

**Light Detection (Prism Stand) :**
- `world.getLightLevel(LightType.SKY, pos.up())` pour la lumière du ciel
- `world.getTimeOfDay() % 24000L` pour l'heure du jour
- `world.getBiome(pos)` pour le biome
- `world.isRaining()` / `world.isThundering()` pour la météo
- Scan des blocs voisins (rayon 3) pour torches/lave/feu

**Rendu des gemmes :**
- `ColorProviderRegistry.ITEM.register()` — déjà en place
- Extension pour les gemmes UV/IR : couleur noire (`0x111111`) + shader ou particules côté client

---

## Ordre d'implémentation recommandé

1. **Nettoyage du code existant** (typos, constantes, imports, mixin vide)
2. **`WavelengthUtil.java`** (constantes partagées, méthodes utilitaires)
3. **`RawCrystalItem` + `RawCrystalOre`** (item + bloc + génération monde)
4. **`PrismStandBlock` + `PrismStandBlockEntity`** (attunement + GUI)
5. **`SolarCollectorBlock` + `LightEmitterBlock`** (production + émission faisceau)
6. **`LightBeam` + `LightBeamManager`** (propagation + rendu faisceau)
7. **`CrystalFurnaceBlock`** (premier consommateur de faisceau)
8. **`LightBatteryBlock`** (stockage énergie)
9. **`PhotosynthesisAcceleratorBlock`** (second consommateur)
10. **`SpectralRefinerBlock`** (raffinement longueur d'onde)
11. **Machines late game** (Chromatic Compressor, Thermal Expander, Thermal Forge)
12. **Machines end game** (Spectral Forge, Transmitter/Receiver, X-Ray Scanner)
