# Gemmology — Design Document (Détaillé)

## Vision générale

Un mod **NeoForge** (Minecraft 26.1, Java 21) qui transforme la lumière en source d'énergie.  
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
- Niveaux Y : -20 à -64 (deep underground, similaire à l'améthyste)
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

Deux modes de propagation coexistent :

**Mode raycasting** (défaut, en ligne droite) :
- Calculé instantanément
- Voyage en ligne droite depuis la face de l'émetteur
- Portée maximale : 32 blocs
- Bloqué par tout bloc solide opaque
- Accumule du bruit dans l'air (voir ci-dessous)

**Mode fibre optique** (via Fiber Optic Cable) :
- Le beam suit le câble bloc par bloc, quelle que soit la direction
- Bruit : **0** sur tout le trajet câblé
- Pas de limite de portée tant que le câble est continu
- Permet de contourner les obstacles sans miroir
- Fonctionne comme un câble AE2 : le beam entre dans la fibre et en ressort à l'autre extrémité

### Accumulation du bruit dans l'air

Chaque bloc d'**air** traversé en mode raycasting ajoute du bruit au beam :

```
bruit += 0.005 par bloc d'air traversé
bruit plafonné à 0.5 (max 50% de pénalité, quelle que soit la distance)
```

| Distance (tout air) | Bruit accumulé | Pénalité d'efficacité |
|---|---|---|
| 5 blocs | 0.025 | −2.5% |
| 10 blocs | 0.05 | −5% |
| 20 blocs | 0.10 | −10% |
| 32 blocs | 0.16 | −16% |
| 100 blocs (cap) | 0.50 | −50% (plafonné) |

Le verre transparent et le verre teinté vanilla **n'accumulent pas** de bruit (le beam les traverse proprement).  
L'eau accumule le même bruit que l'air (en plus de la perte de débit).

### Réduction du bruit : Dampening Glass

Un bloc spécialisé — le **Dampening Glass** (verre antiparasitage) — peut être posé dans le trajet du beam à la place de l'air. Il :
- Laisse passer le beam **sans modifier λ, qualité, ni débit**
- N'accumule **aucun bruit** pour le bloc qu'il occupe
- Ne nécessite pas d'être posé sur tout le trajet — chaque bloc couvert supprime sa contribution

```
Exemple : trajet de 20 blocs, 10 blocs avec Dampening Glass
→ bruit = (10 × 0.005) + (10 × 0.0015) = 0.05 + 0.015 = 0.065 au lieu de 0.10
→ pénalité réduite de 10% à 6.5%

Exemple : trajet de 20 blocs, couverture totale en Dampening Glass
→ bruit = 20 × 0.0015 = 0.03  (30% du bruit sans glass qui aurait été 0.10)
→ pénalité : 3% au lieu de 10%
```

> Le Dampening Glass réduit le bruit du bloc couvert de **70%** (pas à 0). Un bloc d'air sans glass = +0.005, le même bloc avec Dampening Glass = +0.0015. Couverture totale → 30% du bruit normal. Pour atteindre exactement 0, il faut la fibre optique.

### Interactions avec les blocs (résumé)

| Bloc rencontré | Bruit | Autres effets |
|---|---|---|
| Air | +0.005/bloc | — |
| Verre transparent | 0 | Traverse sans modification |
| Verre teinté (vanilla) | 0 | Décale λ de ±30 nm |
| **Dampening Glass** | **+0.0015/bloc (−70%)** | **Traverse sans modification de λ** |
| **Fiber Optic Cable** | **0** (tout le trajet) | **Routage libre, pas de line-of-sight** |
| Eau | +0.005/bloc | −5 PH/bloc |
| Prism Stand (avec gemme) | 0 | Remplace λ, −15% débit |
| Prism Stand (sans gemme) | — | Bloque le beam |
| Light Battery | — | Absorbe, stocke PH |
| Machine réceptrice | — | Consomme pour fonctionner |
| Bloc solide | — | Arrêt du beam |

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

Chaque machine a une **longueur d'onde optimale exacte**. La machine n'est à **100% que si la longueur d'onde du beam correspond exactement à l'optimum ET que le bruit est nul**. Le bruit rend la longueur d'onde du beam imprécise à la réception — même une gemme parfaite ne suffit pas sans fibre optique.

### Les paramètres d'un beam

Chaque faisceau porte trois valeurs indépendantes :
- **Longueur d'onde** `λ` (nm) — détermine quelle machine peut l'utiliser
- **Qualité** `q` (0.0 → 1.0) — mesure la cohérence/pureté du beam (fixée à la source)
- **Bruit** `n` (0.0 → 1.0) — interférence accumulée pendant la transmission (commence à 0)

### Formule d'efficacité

Le bruit s'ajoute au delta de longueur d'onde effectif reçu par la machine. Même avec une gemme parfaitement accordée, du bruit dans le trajet dégrade la correspondance λ comme si la longueur d'onde était légèrement décalée.

```
delta_effectif = |λ_beam - λ_optimale| + bruit × 50

correspondance_λ =
  delta_effectif = 0.0 nm  → 1.00   ← impossible avec bruit > 0
  delta_effectif ≤ 1 nm    → 0.90
  delta_effectif ≤ 3 nm    → 0.75
  delta_effectif ≤ 10 nm   → 0.50
  delta_effectif ≤ 30 nm   → 0.25
  delta_effectif ≤ 80 nm   → 0.10
  delta_effectif > 80 nm   → 0.00 (machine inactive)

efficacité_finale = correspondance_λ × qualité
```

**Conséquence directe :** atteindre `correspondance_λ = 1.00` (delta_effectif = 0) n'est possible que si `bruit = 0`, c'est-à-dire uniquement avec la **Fiber Optic Cable** sur l'intégralité du trajet.

**Exemples avec une gemme parfaite (λ = optimum exact) :**

| Trajet | Bruit | delta_effectif | correspondance_λ |
|---|---|---|---|
| Fibre optique | 0.000 | 0.0 nm | 1.00 ✓ |
| 5 blocs air | 0.025 | 1.25 nm | 0.90 |
| 10 blocs air | 0.050 | 2.5 nm | 0.75 |
| 20 blocs air | 0.100 | 5.0 nm | 0.75 |
| 32 blocs air | 0.160 | 8.0 nm | 0.50 |
| 20 blocs air + Dampening Glass total | 0.030 | 1.5 nm | 0.90 |

**Exemple complet :** gemme à 533 nm (delta réel 3 nm sur optimum 530), qualité 0.85, 20 blocs d'air (bruit 0.10) :
`delta_effectif = 3 + 0.10 × 50 = 8 nm → correspondance 0.50`
`efficacité = 0.50 × 0.85 = 42.5%`

L'efficacité affecte :
- La **vitesse** de traitement (Crystal Furnace, Photosynthesis Accelerator…)
- Le **rendement** (chance de doubler les outputs)
- La **consommation** : une machine consomme toujours le même PH/tick mais produit proportionnellement moins

### Qualité transmise par la gemme

La qualité d'un beam est fixée par la gemme dans le Prism Stand qui le filtre :

| Origine de la gemme | Qualité transmise |
|---|---|
| Attunement verre teinté (±40 nm) | 0.50 |
| Attunement solaire naturel (±8 nm) | 0.70 |
| Spectral Refiner Tier 1 (pas 5.0 nm) | 0.75 |
| Spectral Refiner Tier 2 (pas 1.0 nm) | 0.85 |
| Spectral Refiner Tier 3 (pas 0.1 nm) | 0.95 |
| Spectral Refiner Tier 4 (pas 0.01 nm) | 1.00 |

Un beam non filtré (sorti directement de l'émetteur) a une qualité de `1.0` mais une longueur d'onde neutre non optimale.

### Fusion naturelle de deux beams

Quand deux beams se croisent dans le monde, ils **fusionnent automatiquement** sans bloc dédié.

```
λ_résultat        = (λ1 + λ2) / 2
cohérence         = max(0.0,  1.0 - |λ1 - λ2| / 400)
qualité_résultat  = ((q1 + q2) / 2) × cohérence
débit_résultat    = débit1 + débit2  (les PH s'additionnent toujours)
```

**Pourquoi la cohérence ?**  
Deux longueurs d'onde très différentes mélangées produisent une lumière incohérente (comme mélanger des couleurs complémentaires → gris). Plus les λ sont proches, plus la fusion est propre.

**Exemples :**

| Beam 1 | Beam 2 | λ résultat | Cohérence | Qualité résultat |
|---|---|---|---|---|
| 530 nm, q=1.0 | 530 nm, q=1.0 | 530 nm | 1.00 | 1.00 ✓ |
| 530 nm, q=0.5 | 530 nm, q=0.5 | 530 nm | 1.00 | 0.50 |
| 480 nm, q=1.0 | 580 nm, q=1.0 | 530 nm | 0.75 | 0.75 |
| 400 nm, q=1.0 | 660 nm, q=1.0 | 530 nm | 0.35 | 0.35 ✗ |
| 400 nm, q=0.5 | 660 nm, q=0.5 | 530 nm | 0.35 | 0.175 ✗✗ |

> **Règle** : on ne peut pas "tricher" en combinant deux beams éloignés pour tomber pile sur un optimum — la cohérence s'effondre. En revanche, fusionner deux beams identiques de haute qualité est intéressant pour doubler le débit sans pénalité.

### Longueurs d'onde optimales par machine

| Machine | Optimum exact | Plage active (>0%) | Effet à 100% |
|---|---|---|---|
| Crystal Furnace | 700.0 nm | 620–780 nm | Cuisson 3× + 10% ore doubling |
| Photosynthesis Accelerator | 530.0 nm | 450–610 nm | Croissance 3× + bonemeal aura |
| Light Battery (charge) | Aucun optimum | 380–780 nm | Absorption maximale (pas d'efficacité) |
| UV Sterilizer | 340.0 nm | 300–380 nm | 2 dégâts/s rayon 5 |
| Thermal Forge | 900.0 nm | 780–1400 nm | Cuisson 5× + alloys |
| Spectral Transmitter | 1200.0 nm | 1400+ nm | Transmission sans perte |
| X-Ray Scanner | 0.5 nm | < 10 nm | Reveal complet |

> La Light Battery est la seule machine sans optimum — elle stocke les PH quelle que soit la longueur d'onde.

---

## Tiers du Spectral Refiner

Le Spectral Refiner existe en **4 tiers**. Le tier détermine le **pas minimal de changement** (la précision) à chaque opération. Atteindre exactement l'optimum d'une machine nécessite un refiner suffisamment précis.

### Pourquoi le pas compte

L'optimum d'une machine est une valeur exacte (ex: `530.0 nm`). Si ton refiner ne peut changer la longueur d'onde que par paliers de 5 nm, il est impossible d'atteindre exactement 530.0 — tu te retrouves à 530 nm ou 535 nm, jamais pile à la cible si la gemme vient d'une valeur non-multiple de 5.

```
Exemple : gemme à 548.3 nm, cible 530.0 nm

Tier 1 (pas 5.0 nm) :  548.3 → 543.3 → 538.3 → 533.3 → 528.3  ← bloqué, ne peut pas atteindre 530.0
Tier 2 (pas 1.0 nm) :  548.3 → 547.3 → ... → 531.3 → 530.3 → 529.3  ← dépasse, ne peut pas faire 530.0
Tier 3 (pas 0.1 nm) :  548.3 → ... → 530.3 → 530.2 → 530.1 → 530.0  ← atteint exactement !
Tier 4 (pas 0.01 nm) : Permet d'atteindre n'importe quelle valeur à 2 décimales
```

### Tableau des tiers

| Tier | Nom | Pas (nm/op) | Précision max | Efficacité max atteignable | Recette |
|---|---|---|---|---|---|
| 1 | Crude Spectral Refiner | 5.0 nm | ±2.5 nm de la cible | ~75% | Fer + Quartz + gemme quelconque |
| 2 | Spectral Refiner | 1.0 nm | ±0.5 nm de la cible | ~90% | Or + Diamant + gemme raffinée |
| 3 | Precision Spectral Refiner | 0.1 nm | ±0.05 nm de la cible | ~99% | Netherite + Amethyste + gemme précise |
| 4 | Quantum Spectral Refiner | 0.01 nm | ±0.005 nm de la cible | 100% | Matériaux end-game (Photon Alloy, Echo Shard…) |

**Chaque tier est une upgrade du précédent** (on insère le refiner Tier N dans une station d'upgrade pour obtenir le Tier N+1 — pas de recette from scratch).

### Vitesse de raffinage par tier

| Tier | Vitesse | Consommation |
|---|---|---|
| 1 | 1 pas / 2 secondes (40 ticks) | 10 PH/tick |
| 2 | 1 pas / seconde (20 ticks) | 20 PH/tick |
| 3 | 2 pas / seconde (10 ticks) | 35 PH/tick |
| 4 | 5 pas / seconde (4 ticks) | 60 PH/tick |

### Indicateur visuel d'efficacité (GUI des machines)

Le GUI de chaque machine affiche :
- La longueur d'onde reçue (ex: `533.3 nm`)
- La longueur d'onde optimale (ex: `530.0 nm`)
- Le delta (ex: `Δ 3.3 nm`)
- Un indicateur coloré :
  - **Vert vif** : 100% (delta = 0.0)
  - **Vert** : ≥ 75%
  - **Jaune** : 25–74%
  - **Orange** : 10–24%
  - **Rouge** : < 10%
  - **Gris** : 0% (hors plage)

### Boucle de progression complète

```
[Verre teinté + beam]
       ↓
Gemme ≈ 548.3 nm → Crystal Furnace à 50% (delta 151.7 nm sur optimum 700.0)
       ↓ (le joueur veut mieux)
[Crude Spectral Refiner Tier 1, pas 5 nm]
       ↓
Gemme ≈ 703.3 nm → Crystal Furnace à 90% (delta 3.3 nm)
       ↓ (le joueur veut 100%)
[Spectral Refiner Tier 3, pas 0.1 nm]
       ↓
Gemme = 700.0 nm → Crystal Furnace à 100% !
```

---

## Blocs et Machines

### Sources de débit — Vue d'ensemble

Le débit (PH/tick) d'un beam dépend de la source qui alimente le Light Emitter. Trois mécaniques permettent de monter en débit :

| Méthode | Débit max | Dépendance soleil | Progression |
|---|---|---|---|
| Solar Collectors empilés | Illimité (linéaire) | Oui | Early game |
| Concentrating Lens | ×5 par collector | Oui | Mid game |
| Thermal Generator | 40 PH/tick | Non | Mid game |

---

### Solar Collector

**Description** : Collecte l'énergie solaire et la convertit en Photons. Ne produit pas de faisceau directement.

**Production :**
- Plein soleil (sky light 15, midi) : **10 PH/tick**
- Soleil partiel (sky light 10–14) : proportionnel (~6–9 PH/tick)
- Nuit / couvert : **0 PH/tick**
- Sous la pluie : **2 PH/tick** (diffusion)

**Cumul :** Plusieurs Solar Collectors peuvent être connectés au même Light Emitter. Leur production s'additionne. 3 collectors → 30 PH/tick en plein soleil.

**Stockage interne** : 5 000 PH (buffer pour les fluctuations nocturnes)  
**Output** : se connecte à un Light Emitter ou Light Battery adjacent (face du bas ou des côtés)

**GUI** : Production actuelle (PH/tick) + total stocké en buffer

**Recette craft :**
```
[Verre]   [Verre]        [Verre]
[Cuivre]  [Raw Crystal]  [Cuivre]    → 1 Solar Collector
[Fer]     [Redstone]     [Fer]
```
(Le Raw Crystal agit comme capteur photonique brut — il n'a pas besoin d'être accordé)

---

### Concentrating Lens

**Description** : Bloc placé sur la **face supérieure** de n'importe quelle machine pour concentrer et amplifier son fonctionnement. Existe en **4 tiers**, chaque tier concentrant davantage.

**Principe général :**
- Une Concentrating Lens se pose sur le dessus d'une machine
- Elle amplifie l'énergie que la machine traite (PH produits ou PH reçus)
- **Une seule lens active par machine** — seule la lens la plus haute est prise en compte si plusieurs sont empilées (sauf Solar Collector, voir ci-dessous)
- Le tier de la lens détermine le multiplicateur

### Tiers

| Tier | Nom | Multiplicateur | Craft |
|---|---|---|---|
| 1 | Basic Concentrating Lens | ×1.5 | Verre + Quartz |
| 2 | Concentrating Lens | ×2.5 | Verre + Quartz + Améthyste |
| 3 | Advanced Concentrating Lens | ×4.0 | Verre teinté + Diamant + Cristal |
| 4 | Perfect Concentrating Lens | ×6.0 | Verre parfait + Netherite + Gemme raffinée |

**Chaque tier est une upgrade du précédent** (station d'upgrade, pas de recette from scratch).

---

### Comportement selon la machine

**Sur un Solar Collector (cas spécial — empilement) :**

Le Solar Collector accepte plusieurs Concentrating Lens **empilées verticalement**. Toutes les lens de la pile contribuent, en multipliant leurs effets.

- La pile entière doit avoir le **ciel dégagé** au-dessus
- Les lens n'ont pas besoin d'être du même tier
- Les multiplicateurs se **multiplient entre eux**

| Exemple de pile | Calcul | PH/tick (plein soleil) |
|---|---|---|
| 1× Tier 1 | 10 × 1.5 | 15 |
| 2× Tier 1 | 10 × 1.5 × 1.5 | 22 |
| 1× Tier 2 | 10 × 2.5 | 25 |
| 4× Tier 1 | 10 × 1.5⁴ | 50 |
| 2× Tier 2 | 10 × 2.5² | 62 |
| 1× Tier 3 + 1× Tier 2 | 10 × 4.0 × 2.5 | 100 |
| 4× Tier 4 | 10 × 6.0⁴ | 1296 (late game) |

**Sur toute autre machine (Crystal Furnace, Photosynthesis Accelerator, Spectral Refiner…) :**

La lens amplifie le **débit effectif reçu** par la machine — comme si la machine recevait plus de PH/tick que le beam n'en transporte réellement. Le beam n'est pas modifié, seul le calcul interne de la machine change.

- Une seule lens active (la plus haute posée sur la machine)
- Si une lens Tier 3 est posée sur une Crystal Furnace qui reçoit 20 PH/tick → la machine se comporte comme si elle recevait `20 × 4.0 = 80 PH/tick`
- La longueur d'onde et la qualité du beam ne sont **pas** affectées

**Exemple (Crystal Furnace à 700 nm, optimum 700 nm, q=1.0) :**

| Lens | Débit réel | Débit effectif | Vitesse cuisson |
|---|---|---|---|
| Aucune | 16 PH/tick | 16 PH/tick | ×2 vanilla |
| Tier 1 | 16 PH/tick | 24 PH/tick | ×2.4 vanilla |
| Tier 3 | 16 PH/tick | 64 PH/tick | ×3 vanilla (cap) |

**Recette Tier 1 :**
```
[Air]     [Verre]   [Air]
[Verre]   [Quartz]  [Verre]    → 1 Basic Concentrating Lens
[Air]     [Verre]   [Air]
```

---

### Thermal Generator

**Description** : Brûle du combustible pour produire des Photons en permanence, indépendamment du soleil. Idéal pour la nuit ou les bases souterraines.

**Production selon le combustible :**

| Combustible | PH/tick | Durée | PH totaux |
|---|---|---|---|
| Charbon / Charbon de bois | 15 PH/tick | 80 s (1600 ticks) | 24 000 |
| Bois en bûche | 8 PH/tick | 30 s (600 ticks) | 4 800 |
| Blaze Rod | 25 PH/tick | 120 s (2400 ticks) | 60 000 |
| Lava Bucket | 30 PH/tick | 200 s (4000 ticks) | 120 000 |
| Charbon en bloc | 20 PH/tick | 800 s (16000 ticks) | 320 000 |

**Caractéristiques :**
- Fonctionne 24h/24, par tous les temps
- Pas de dépendance à la lumière du ciel
- **Contrainte** : nécessite un approvisionnement en combustible continu
- Peut être automatisé avec des hoppers vanilla
- **Stockage interne** : 10 000 PH (buffer)

**Output** : se connecte à un Light Emitter ou Light Battery adjacent

**GUI** : Slot combustible, barre de combustion restante, production actuelle (PH/tick)

**Recette craft :**
```
[Pierre Lisse] [Fer]          [Pierre Lisse]
[Fer]          [Raw Crystal]  [Fer]           → 1 Thermal Generator
[Pierre Lisse] [Redstone]     [Pierre Lisse]
```
(Le Raw Crystal converti la chaleur de combustion en Photons)

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
[Quartz]  [Verre]    [Quartz]
[Fer]     [Gemme]    [Fer]       → 1 Light Emitter
[Quartz]  [Redstone] [Quartz]
```
(La gemme est le cœur émetteur — la longueur d'onde initiale du beam sera celle de la gemme si une est insérée, sinon 600 nm neutre)

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
[Verre]  [Raw Crystal] [Verre]
[Quartz] [Air]         [Quartz]    → 1 Prism Stand
[Pierre] [Fer]         [Pierre]
```
(Le Raw Crystal est le foyer optique — c'est lui qui capte la lumière ambiente)

---

### Energy Converter (RF/FE → Photons)

**Description** : Convertit l'énergie d'autres mods tech en Photons. Permet d'utiliser l'infrastructure énergétique existante d'autres mods pour alimenter les machines Gemmology.

> **Note technique — Fabric vs Forge :**  
> RF et FE sont des standards **Forge/NeoForge** uniquement. Sur Fabric, l'équivalent standard est **Team Reborn Energy (TR Energy)**, utilisé par Tech Reborn, Industrial Revolution, et la majorité des mods tech Fabric. L'Energy Converter cible donc **TR Energy** en priorité.  
> Si un pont RF↔TR existe via un mod tiers (ex: *Modern Industrialization*), la compatibilité RF/FE est automatique.

**Comportement :**
- Accepte du TR Energy (E) en entrée via l'interface standard `EnergyStorage`
- Convertit en PH et les stocke dans son buffer interne
- Alimente un Light Emitter ou une Light Battery adjacent

**Taux de conversion :**
```
10 E (TR Energy) → 1 PH
```

**Exemples :**
- 200 E/tick entrant → 20 PH/tick (Spectral Refiner T1 alimenté)
- 800 E/tick entrant → 80 PH/tick (Thermal Forge alimenté)

**Caractéristiques :**
- Fonctionne 24h/24, indépendant du soleil
- Buffer interne : 100 000 PH
- Taux de conversion configurable via le GUI (pour limiter la consommation d'énergie externe)
- Compatible avec tout mod exposant une interface TR Energy

**GUI :** Énergie entrante (E/tick), taux de conversion, PH/tick produits, buffer

**Recette craft :**
```
[Or]      [Redstone] [Or]
[Diamant] [Gemme]    [Diamant]    → 1 Energy Converter
[Or]      [Redstone] [Or]
```
(La gemme est le transducteur entre l'énergie électrique et les Photons)

**Dépendance optionnelle :** `team_reborn_energy` — si absent au runtime, le bloc est enregistré mais inactif (pas de crash). La recette reste craftable mais le bloc ne reçoit pas d'énergie externe.

---

### Light Battery (Condensateur Lumineux)

**Description** : Stocke les Photons reçus par un faisceau ou un Solar Collector adjacent.

**Capacité :** 50 000 PH (Tier 1) — augmentable avec upgrades  
**Charge :** Absorbe tout faisceau entrant, convertit en PH stockés  
**Décharge :** Alimente un Light Emitter adjacent avec un débit configurable  
**GUI :** Barre de charge, débit de charge/décharge, toggle charge/décharge

**Recette craft :**
```
[Cuivre] [Verre]    [Cuivre]
[Gemme]  [Quartz]   [Gemme]     → 1 Light Battery
[Cuivre] [Redstone] [Cuivre]
```
(Deux gemmes encadrent le cœur de quartz — elles absorbent et restituent les Photons)

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
[Briques de Pierre] [Verre]  [Briques de Pierre]
[Fer]               [Gemme]  [Fer]                → 1 Crystal Furnace
[Briques de Pierre] [Quartz] [Briques de Pierre]
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
[Verre]   [Poudre d'Os] [Verre]
[Cuivre]  [Gemme]       [Cuivre]    → 1 Photosynthesis Accelerator
[Fer]     [Quartz]      [Fer]
```

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
[Or]              [Améthyste]  [Or]
[Dampening Glass] [Diamant]    [Dampening Glass]    → 1 Spectral Refiner (Tier 1)
[Fer]             [Redstone]   [Fer]
```
(Le Dampening Glass assure la stabilité optique nécessaire à la précision du raffinement)

---

### Beam Splitter

**Description** : Reçoit un faisceau et le **duplique** sur plusieurs sorties. Chaque sortie active reçoit le beam **complet** (même λ, même qualité, même débit). C'est un bloc mid game, le dernier tier étant accessible en début de late game.

| Tier | Nom | Sorties max | Progression |
|---|---|---|---|
| 1 | Basic Beam Splitter | 2 sorties | Début mid game |
| 2 | Beam Splitter | 3 sorties | Mid game |
| 3 | Advanced Beam Splitter | 4 sorties | Fin mid game |
| 4 | Perfect Beam Splitter | 5 sorties | Début late game |

> Un bloc a 6 faces. Le beam entre par 1 face → 5 faces de sortie possibles au maximum (Tier 4).

**Comportement :**
- Le beam entre par la face arrière (fixée à la pose)
- Le débit (PH/tick) est divisé **équitablement entre les sorties actives**
- La longueur d'onde et la qualité sont identiques sur toutes les sorties actives
- Les sorties désactivées ne reçoivent rien — leur part est redistribuée aux sorties actives

**Exemple Tier 2, 2 sorties actives sur 3 :** beam 60 PH/tick → 30 PH/tick sur chaque sortie active  
**Exemple Tier 4, 4 sorties actives :** beam 100 PH/tick → 25 PH/tick sur chacune des 4 sorties

**GUI (clic droit sur le bloc) :**
- Affiche les 5 faces de sortie possibles sous forme de boutons
- Les sorties disponibles pour le tier actuel sont cliquables (actif = vert, inactif = gris)
- Les sorties non débloquées par le tier sont grisées et verrouillées
- Affichage : débit entrant → débit par sortie active

**Recette Tier 1 :**
```
[Verre]           [Raw Crystal]    [Verre]
[Dampening Glass] [Quartz]         [Dampening Glass]    → 1 Basic Beam Splitter
[Verre]           [Raw Crystal]    [Verre]
```
(Le Raw Crystal divise le beam, le Dampening Glass évite le bruit introduit par la division)

**Upgrade :** Chaque tier s'obtient en upgrageant le tier précédent dans une station dédiée (pas de recette from scratch).

---

### Light Gate

**Description** : Bloc transparent au beam par défaut. Un signal redstone inverse son état (ouvert → fermé ou fermé → ouvert selon la configuration).

**Comportement :**
- **Sans signal redstone :** laisse passer le beam sans modification (transparent)
- **Avec signal redstone :** bloque le beam complètement
- Mode inversable (sneak + clic droit) : par défaut bloqué, s'ouvre avec redstone
- La longueur d'onde, la qualité, le débit et le bruit sont préservés quand ouvert

**Usages :**
- Allumer/éteindre des machines à distance
- Systèmes automatisés (horloge redstone → pulses de beam)
- Sécurité (couper l'alimentation d'une zone)

**GUI :** Aucun — uniquement interaction directe (sneak + clic droit pour changer le mode)

**Recette craft :**
```
[Fer]      [Dampening Glass] [Fer]
[Redstone] [Or]              [Redstone]    → 1 Light Gate
[Fer]      [Dampening Glass] [Fer]
```

---

### Dampening Glass

**Description** : Bloc posé dans le trajet d'un faisceau pour supprimer l'accumulation de bruit sur ce bloc. Transparent et sans effet sur λ, qualité ni débit.

**Comportement :**
- Posé à la place de l'air dans le chemin du beam
- Le beam le traverse normalement (pas de blocage, pas de modification)
- Réduit le bruit du bloc couvert de **70%** : `+0.005 → +0.0015` par bloc
- N'a pas besoin de couvrir tout le trajet — chaque bloc couvert réduit sa contribution individuelle
- Couverture totale du trajet → **30% du bruit qu'il y aurait eu sans glass**

**Usage :**
- Réduction partielle ou totale du bruit sur des faisceaux en ligne droite
- Plus économique que la fibre optique pour de courtes distances

**Progression :** late early game — nécessite une gemme (donc un Prism Stand fonctionnel) et de l'améthyste (biome géode, underground).

**Recette craft :**
```
[Air]        [Améthyste]  [Air]
[Verre]      [Gemme]      [Verre]    → 4 Dampening Glass
[Air]        [Améthyste]  [Air]
```

---

### Fiber Optic Cable

**Description** : Câble qui transporte un faisceau lumineux le long de son trajet, sans perte de bruit et sans contrainte de ligne droite. Fonctionne comme un câble AE2 — le beam entre à une extrémité et ressort à l'autre, en suivant le câble bloc par bloc.

**Comportement :**
- Se pose bloc par bloc pour former un chemin continu
- Le beam entre dans la fibre depuis un Light Emitter ou un autre bloc émetteur
- Il suit le câble quelle que soit la direction (horizontal, vertical, courbes, coins)
- **Bruit : 0** sur l'intégralité du trajet câblé
- Pas de limite de portée tant que le câble est continu et non interrompu
- Un bloc non-fibre dans le câble interrompt le beam (bruit reprend depuis zéro après la coupure)
- À la sortie du câble : le beam reprend en mode raycasting normal (bruit repart de 0 à ce point)

**Pas besoin de miroir** : la fibre permet tous les angles, remplaçant le miroir pour le routage.

**Comparaison avec les autres solutions :**

| Solution | Bruit | Flexibilité trajet | Coût |
|---|---|---|---|
| Air seul | +0.005/bloc | Ligne droite | Gratuit |
| Dampening Glass | 0 par bloc couvert | Ligne droite | Bas |
| **Fiber Optic Cable** | **0 total** | **Libre (tout angle)** | **Élevé** |

**Progression :** mid-game — nécessite le Nether (Blaze Rod, Gold, Nether Quartz) et une gemme raffinée (Spectral Refiner Tier 1 minimum).

**Recette craft (4 câbles) :**
```
[Or]          [Quartz Nether] [Or]
[Raw Crystal] [Blaze Rod]     [Raw Crystal]    → 4 Fiber Optic Cable
[Or]          [Quartz Nether] [Or]
```
(Le Raw Crystal fondu par la Blaze Rod forme le cœur de la fibre ; l'or assure la gaine conductrice)

**Rendu visuel :**
- Câble fin (modèle type pipe) avec une teinte colorée selon la λ du beam qu'il transporte
- Si pas de beam actif : câble gris neutre
- Légère lueur de la couleur du beam quand actif

---

### Wavelength Sensor

**Description** : Détecte un faisceau passant devant lui et émet un signal redstone proportionnel à la longueur d'onde ou à la qualité.

**Comportement :**
- Se place sur le côté d'un beam (pas dans son trajet — il lit le beam sans l'arrêter)
- Mode configurable (sneak + clic droit) :
  - **Mode λ :** signal redstone = `(λ - 380) / 400 × 15` (arrondi à l'entier 0–15)
  - **Mode qualité :** signal redstone = `qualité × 15` (arrondi à l'entier 0–15)
  - **Mode bruit :** signal redstone = `bruit × 15` (arrondi à l'entier 0–15) — utile pour déclencher une alarme si le bruit dépasse un seuil
- Si aucun beam détecté : signal = 0

**Exemples mode λ :**
- 380 nm → signal 0
- 580 nm → signal 7
- 780 nm → signal 15

**Usages :**
- Déclencher une machine quand le bon beam est présent
- Comparateur redstone pour trier selon la longueur d'onde
- Alarme si le beam change de qualité

**GUI :** Aucun — affichage du mode actuel sur le bloc (icône λ ou q)

**Recette craft :**
```
[Dampening Glass] [Or]           [Dampening Glass]
[Quartz]          [Redstone]     [Quartz]           → 1 Wavelength Sensor
[Fer]             [Quartz Nether][Fer]
```
(Le Dampening Glass lit le beam sans le perturber ; le Quartz Nether amplifie le signal redstone)

---

### Spectral Goggles

**Description** : Item équipable (slot casque) permettant de voir les faisceaux UV et IR, invisibles à l'œil nu, et d'afficher des informations sur les beams visibles.

**Effets portés :**
- Les beams UV (< 380 nm) apparaissent en violet translucide
- Les beams IR (> 780 nm) apparaissent en rouge translucide
- Sur tous les beams visibles : affichage en overlay de `λ = XXX.X nm | q = 0.XX | X PH/t`
- Mise en évidence des machines actives dans un rayon de 8 blocs (contour vert = active, rouge = inactive)

**Recette craft :**
```
[Or]    [Cuir]           [Or]
[Gemme] [Fiber Optic Cable] [Gemme]    → 1 Spectral Goggles
[Or]    [Verre]          [Or]
```
(Les deux gemmes sont les oculaires — une rouge ~700 nm et une bleue ~450 nm pour couvrir tout le spectre visible. La fibre optique relie les deux lentilles pour transmettre les données de beam en overlay)

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

### Beam Mirror

**Description** : Réfléchit un faisceau dans une nouvelle direction. Se place comme un **panneau** — la direction de la surface réfléchissante est déterminée par l'endroit où regarde le joueur au moment de la pose.

**Placement :**
- Posé sur une face de bloc (mur, sol, plafond)
- La surface réfléchissante fait face à la direction du regard du joueur à la pose
- La combinaison input → output est automatiquement déduite de l'orientation de la surface
- Après la pose : **sneak + clic droit** pour cycler entre les paires input/output valides de ce miroir

**Directions supportées :**

Un miroir peut rediriger un beam dans n'importe quelle combinaison de faces de bloc, tant que la géométrie est cohérente avec l'orientation de la surface :

| Exemples de redirection | Description |
|---|---|
| Est → Nord | Virage horizontal 90° |
| Nord → Haut | Virage vertical 90° |
| Est → Haut | Virage vertical-horizontal |
| Nord → Sud | Demi-tour (miroir face au beam) |

Le mod calcule le vecteur réfléchi : `R = D - 2(D·N)N` où `N` est la normale de la surface.

**Perte :** −5% du débit par réflexion. λ et qualité préservés.

**Usage :**
- Contourner des obstacles sans fiber optic
- Router un beam à la verticale (du sol vers une machine en hauteur)
- Complément naturel entre le raycasting et la fibre optique

**Progression :** early-mid game — avant la fiber optic.

**Recette craft :**
```
[Fer]    [Verre Poli]  [Fer]
[Fer]    [Raw Crystal] [Fer]    → 1 Beam Mirror
[Fer]    [Fer]         [Fer]
```
("Verre Poli" = Glass Pane ou Polished item — à confirmer selon disponibilité MC 26.1)

---

### Photon Relay

**Description** : Reçoit un beam, remet son bruit à 0, et le réémet dans la même direction. Agit comme un répéteur optique pour les longues distances.

**Comportement :**
- Posé dans le trajet du beam
- Absorbe le beam entrant (bruit accumulé quelconque)
- Réémet un beam identique en λ, qualité et débit, mais **bruit = 0**
- Consomme de l'énergie pour régénérer le signal : **5 PH/tick** en fonctionnement

**Usage :**
- Long trajet en air sans fiber optic sur tout le parcours
- Associé au Dampening Glass : relay tous les 20 blocs + glass entre les relays → bruit quasi nul sans le coût de la fibre

**Progression :** mid game.

**Recette craft :**
```
[Dampening Glass] [Quartz Nether]   [Dampening Glass]
[Raw Crystal]     [Fiber Optic ×1] [Raw Crystal]       → 1 Photon Relay
[Dampening Glass] [Redstone]        [Dampening Glass]
```

---

### Purification Chamber

**Description** : Améliore progressivement la **qualité** d'une gemme sans modifier sa longueur d'onde. La vitesse de purification dépend de la proximité entre la longueur d'onde du beam entrant et celle de la gemme à purifier.

**Principe :**
- Slot : 1 gemme à purifier
- Reçoit un beam en entrée
- La machine compare `λ_beam` et `λ_gem` à chaque tick
- Plus le beam est proche de la longueur d'onde de la gemme, plus la purification est rapide

**Formule de vitesse :**
```
proximité = max(0.0,  1.0 - |λ_beam - λ_gem| / 100)

qualité   += 0.0001 × proximité par tick

Exemples :
  |λ_beam - λ_gem| =  0 nm  → proximité 1.00 → +0.0001/tick  (qualité 0.50→1.00 en ~83 min)
  |λ_beam - λ_gem| = 10 nm  → proximité 0.90 → +0.00009/tick
  |λ_beam - λ_gem| = 50 nm  → proximité 0.50 → +0.00005/tick
  |λ_beam - λ_gem| = 100 nm → proximité 0.00 → aucun effet
```

**Design intent :** pour purifier une gemme à 700.0 nm rapidement, il faut un beam à 700.0 nm — donc une autre gemme déjà accordée ou le résultat d'un Spectral Refiner. La Purification Chamber crée une dépendance circulaire intéressante : améliorer la qualité d'une gemme demande déjà d'avoir une bonne gemme de référence.

**Le bruit du beam affecte aussi la vitesse :**
```
vitesse_finale = proximité × (1 - bruit)
```
Envoyer un beam bruité dans une Purification Chamber est contre-productif — il faut un beam propre (fiber optic ou relay).

**GUI :** slot gemme, λ gemme affichée, λ beam reçu, delta, barre de progression, qualité actuelle → qualité cible

**Progression :** mid game.

**Recette craft :**
```
[Or]              [Améthyste]      [Or]
[Dampening Glass] [Raw Crystal]    [Dampening Glass]    → 1 Purification Chamber
[Or]              [Redstone]       [Or]
```

---

### Light Condenser

**Description** : Fusionne plusieurs beams en un seul avec un débit additionné et une qualité **préservée** (contrairement à la fusion naturelle qui dégrade la cohérence). La machine stabilise les longueurs d'onde avant de les fusionner.

**Comportement :**
- Jusqu'à 4 faces d'entrée, 1 face de sortie
- Fusionne tous les beams actifs entrants
- λ résultat = moyenne pondérée par débit
- Qualité résultat = moyenne des qualités pondérée par débit × facteur de cohérence amélioré

**Comparaison fusion naturelle vs Light Condenser :**

| | Fusion naturelle | Light Condenser |
|---|---|---|
| λ résultat | moyenne | moyenne pondérée |
| Cohérence | `max(0, 1 - Δλ/400)` | `max(0, 1 - Δλ/200)` (2× moins pénalisante) |
| Débit | additionné | additionné |
| Consommation | 0 | 15 PH/tick |

**Usage :** fusionner le output de plusieurs Solar Collectors en un seul beam de haut débit, en perdant moins de qualité que la fusion naturelle.

**Progression :** mid game.

**Recette craft :**
```
[Verre]           [Raw Crystal]    [Verre]
[Fiber Optic ×1]  [Diamant]        [Fiber Optic ×1]    → 1 Light Condenser
[Verre]           [Redstone]       [Verre]
```

---

### Spectral Analyzer

**Description** : Bloc posé sur le côté d'un beam (ne l'interrompt pas). Affiche en GUI toutes les métriques du beam en temps réel, et calcule l'efficacité théorique pour les machines adjacentes.

**Affichage GUI :**
```
┌──────────────────────────────────────┐
│  Spectral Analyzer                   │
│                                      │
│  λ        : 533.3 nm                 │
│  Qualité  : 0.85                     │
│  Bruit    : 0.08  (+4.0 nm effectif) │
│  Débit    : 24 PH/tick               │
│  δ effectif : 7.65 nm                │
│                                      │
│  Machines adjacentes :               │
│  ✓ Crystal Furnace    → 50%          │
│  ✗ Photosynthesis Acc → 0% (hors λ) │
└──────────────────────────────────────┘
```

**Fonctionnement :**
- Détecte automatiquement les machines dans un rayon de 3 blocs
- Calcule `efficacité = correspondance_λ(delta_effectif) × qualité` pour chacune
- Met à jour en temps réel (1×/seconde)
- Signal redstone optionnel : émet un signal proportionnel à l'efficacité de la machine la plus proche (0–15)

**Progression :** mid game — essentiel pour optimiser un setup.

**Recette craft :**
```
[Or]              [Dampening Glass] [Or]
[Quartz Nether]   [Améthyste]      [Quartz Nether]    → 1 Spectral Analyzer
[Or]              [Redstone]        [Or]
```

---

### Beam Amplifier

**Description** : Augmente le débit d'un beam en injectant de l'énergie FE. Permet de booster un beam existant sans ajouter de Solar Collectors.

**Comportement :**
- Reçoit un beam en entrée, émet un beam amplifié en sortie
- Accepte du FE en entrée (compatible Energy Converter ou tout mod NeoForge)
- Taux de conversion configurable : `X FE/tick → +Y PH/tick` sur le beam

**Taux de conversion :**
```
10 FE/tick entrant  → débit beam +1 PH/tick
100 FE/tick         → +10 PH/tick
1000 FE/tick (max)  → +100 PH/tick
```

**Contrainte :** n'améliore pas λ ni qualité — sert uniquement à augmenter le débit pour atteindre les seuils de vitesse des machines (Crystal Furnace ×3, Thermal Forge actif, etc.)

**GUI :** débit entrant (PH/tick), FE/tick consommé, débit sortant (PH/tick), curseur de puissance

**Progression :** mid game — bon puits d'énergie FE pour les mods tech adjacents.

**Recette craft :**
```
[Or]      [Fiber Optic ×1] [Or]
[Diamant] [Raw Crystal]    [Diamant]    → 1 Beam Amplifier
[Or]      [Redstone]       [Or]
```

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

**Sources de débit :**

| Source | PH/tick max | Condition |
|---|---|---|
| Solar Collector (×1) | 10 | Plein soleil |
| Solar Collector + 4 Lens | 50 | Plein soleil, ciel dégagé |
| N Solar Collectors | N × 10 | Plein soleil |
| Thermal Generator (lava) | 30 | Aucune |
| Energy Converter | Illimité (dépend du mod source) | TR Energy requis |

**Consommation des machines :**

| Machine | PH/tick min | Longueur d'onde | Effet |
|---|---|---|---|
| Crystal Furnace | 8–32 | 620–780 nm | Cuisson 1.5× à 3× |
| Photosynthesis Acc. | 12 | 500–570 nm | Croissance 3× |
| Spectral Refiner T1 | 10 | Tout visible | 1 pas/2 s |
| Spectral Refiner T4 | 60 | Tout visible | 5 pas/s |
| Light Battery | — (stockage) | Tout | Buffer 50 000 PH |
| UV Sterilizer | 50 | 300–380 nm | 2 dégâts/s (rayon 5) |
| Thermal Forge | 80 | 780–1400 nm | Cuisson 5× |
| Spectral Transmitter | 200 | 1400+ nm | Énergie sans fil |
| X-Ray Scanner | 200 | < 10 nm | Reveal minerais |

**Perte par Prism Stand :** 15% du débit  
**Perte dans l'eau :** 5 PH/bloc  
**Capacité Light Battery Tier 1 :** 50 000 PH

---

## Architecture technique

### Stack technique

| Composant | Choix |
|---|---|
| Mod loader | **NeoForge** (26.1) |
| Build system | ForgeGradle + MDK NeoForge |
| Java | 21 |
| Énergie inter-mods | **Forge Energy (FE / RF)** natif |
| Registres | `DeferredRegister<T>` |
| Événements | Bus NeoForge (`@SubscribeEvent`) |
| GUI | `AbstractContainerScreen` + `MenuType` |
| Datagen | NeoForge `DataGenerator` |

### Forge ou NeoForge ?

**NeoForge**, sans hésitation pour un nouveau mod en 2026.

En juillet 2023, la quasi-totalité de l'équipe de développement de Forge a forké le projet pour créer NeoForge suite à des conflits internes. Depuis :

- **Forge** — reçoit des mises à jour minimales. Essentiellement maintenu pour 1.20.1 et versions antérieures. Quasi abandonné pour 1.21+.
- **NeoForge** — développement actif, APIs modernes, meilleures performances. C'est là où vont tous les nouveaux mods tech (Applied Energistics, Mekanism, Create...).

---

### Versioning NeoForge (schéma)

NeoForge utilise un versioning adapté de semver qui reflète directement la version Minecraft :

```
NeoForge X.Y.Z  →  Minecraft 1.X.Y  (build Z)
NeoForge 26.1.2.76  →  Minecraft 26.1  (versioning calendaire MC, build 76)
```

En 2026, Mojang a basculé vers un **versioning calendaire** (26.1, 26.2, …). NeoForge a suivi avec un format 4-composants : `26.1.2.x`.

---

### Version cible

**Minecraft 26.1 — NeoForge 26.1.2.76**

| Version MC | NeoForge stable | Écosystème mods | Data Components | Statut |
|---|---|---|---|---|
| 1.20.1 | Forge (legacy) | ★★★★★ (record historique) | ✗ (NBT uniquement) | ✗ Incompatible |
| 1.21.1 | 21.1.233 | ★★★★☆ Très large | ✓ Mature | Alternative (ancienne ère MC) |
| **26.1** | **26.1.2.76** | **★★★☆☆ En croissance** | **✓ Mature** | **✓ Choisi** |
| 26.2 | 26.2.0.6-beta | ★★☆☆☆ Limité | ✓ Mature | Trop récent (beta) |

> **Pourquoi pas 1.20.1 malgré son énorme écosystème ?**  
> Les Data Components n'existent pas avant 1.20.5. Le stockage de la longueur d'onde de la gemme repose entièrement dessus — en 1.20.1 il faudrait utiliser NBT, moins propre et moins stable.

> **Pourquoi MC 26.1 plutôt que 1.21.1 ?**  
> MC 26.1 est la nouvelle ère calendaire de Minecraft (2026). NeoForge 26.1.2.76 est stable. On préfère partir sur une base moderne plutôt que de partir sur 1.21.1 et devoir migrer ensuite. L'écosystème est en croissance mais suffisant pour un nouveau mod.

---

### Migration Fabric → NeoForge : détail complet

#### 1. Build system

**Supprimer :**
- Plugin `fabric-loom` dans `build.gradle`
- Toutes les dépendances Fabric (`fabric-loader`, `fabric-api`, `yarn`)

**Remplacer par (build.gradle) :**
```groovy
plugins {
    id 'net.neoforged.gradle.userdev' version '7.0.x'
}

dependencies {
    implementation "net.neoforged:neoforge:26.1.2.76"
}
```

**gradle.properties :**
```properties
# Supprimer
fabric_version=...
yarn_mappings=...

# Ajouter
neo_version=26.1.2.76        # dernière stable MC 26.1 (juin 2026)
minecraft_version=26.1
```

---

#### 2. Métadonnées du mod

**Supprimer :**
- `src/main/resources/fabric.mod.json`
- `src/main/resources/gemmology.mixins.json` (le mixin était vide de toute façon)

**Créer `src/main/resources/META-INF/neoforge.mods.toml` :**
```toml
modLoader = "javafml"
loaderVersion = "[4,)"
license = "CC0-1.0"

[[mods]]
    modId = "gemmology"
    version = "1.0.0"
    displayName = "Gemmology"
    description = "Light-based energy mod."

[[dependencies.gemmology]]
    modId = "neoforge"
    type = "required"
    versionRange = "[26.1.2.76,)"
    ordering = "NONE"
    side = "BOTH"

[[dependencies.gemmology]]
    modId = "minecraft"
    type = "required"
    versionRange = "[26.1,27)"
    ordering = "NONE"
    side = "BOTH"
```

**Créer `src/main/resources/pack.mcmeta` :**
```json
{
  "pack": {
    "description": "Gemmology resources",
    "pack_format": 34
  }
}
```

---

#### 3. Classe principale (`Gemmology.java`)

**Fabric :**
```java
public class Gemmology implements ModInitializer {
    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        ModComponents.initialize();
    }
}
```

**NeoForge :**
```java
@Mod(Gemmology.MOD_ID)
public class Gemmology {
    public static final String MOD_ID = "gemmology";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Gemmology(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModComponents.COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Gemmology initialized.");
    }
}
```

---

#### 4. Registre des items (`ModItems.java`)

**Fabric :**
```java
public static final Item GEM = Registry.register(
    Registries.ITEM,
    Identifier.of(MOD_ID, "gem"),
    new GemItem(new Item.Settings())
);
```

**NeoForge :**
```java
public static final DeferredRegister<Item> ITEMS =
    DeferredRegister.create(Registries.ITEM, Gemmology.MOD_ID);

public static final DeferredHolder<Item, GemItem> GEM =
    ITEMS.register("gem", () -> new GemItem(new Item.Properties()));
```

Tab créatif — **Fabric** (`ItemGroupEvents`) → **NeoForge** (`BuildCreativeModeTabContentsEvent`) :
```java
// Dans ModEvents.java
@SubscribeEvent
public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
        event.accept(ModItems.GEM);
    }
}
```

---

#### 5. Composant de données (`ModComponents.java`)

**Fabric :**
```java
public static final ComponentType<Float> WAVE_LENGTH = Registry.register(
    Registries.DATA_COMPONENT_TYPE,
    Identifier.of(MOD_ID, "wave_length"),
    ComponentType.<Float>builder().codec(Codec.FLOAT).build()
);
```

**NeoForge :**
```java
public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
    DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Gemmology.MOD_ID);

public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> WAVE_LENGTH =
    COMPONENTS.register("wave_length",
        () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT).build()
    );
```

---

#### 6. `GemItem.java`

Changements mineurs :

| Fabric | NeoForge |
|---|---|
| `onCraft(ItemStack, World)` | `onCraftedBy(ItemStack, Level, Player)` |
| `appendTooltip(stack, ctx, list, type)` | `appendHoverText(stack, ctx, list, type)` |
| `TooltipContext` | `Item.TooltipContext` |
| `World world` | `Level level` |
| `world.isClient()` | `level.isClientSide()` |

---

#### 7. Client — couleur des gemmes (`GemmologyClient.java`)

**Fabric :**
```java
public class GemmologyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack, layer) -> { ... }, ModItems.GEM);
    }
}
```

**NeoForge :**
```java
@EventBusSubscriber(modid = Gemmology.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class GemmologyClient {
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> { ... }, ModItems.GEM.get());
    }
}
```

---

#### 8. Ce qui reste identique

- Logique interne de `GemItem` (wavelength, tooltip texte)
- Algorithme `getColorFromWavelength()` — 100% Java pur, aucun changement
- DataComponents codec (`Codec.FLOAT`) — identique
- Structure des packages
- Datagen (providers similaires, légères différences d'API)

### Packages prévus

```
fr.skylined.gemmology/
├── Gemmology.java                    (point d'entrée @Mod)
├── GemmologyClient.java              (setup client — FMLClientSetupEvent)
├── GemmologyDataGenerator.java       (datagen entry)
├── component/
│   └── ModComponents.java            (DataComponentType WAVE_LENGTH — DeferredRegister)
├── item/
│   ├── ModItems.java                 (DeferredRegister<Item>)
│   └── custom/
│       ├── GemItem.java
│       ├── RawCrystalItem.java
│       └── SpectralGogglesItem.java  (ArmorItem + overlay client)
├── block/
│   ├── ModBlocks.java                (DeferredRegister<Block>)
│   └── custom/
│       ├── SolarCollectorBlock.java
│       ├── ConcentratingLensBlock.java
│       ├── ThermalGeneratorBlock.java
│       ├── EnergyConverterBlock.java       (IEnergyStorage → PH, FE natif)
│       ├── LightEmitterBlock.java
│       ├── PrismStandBlock.java
│       ├── CrystalFurnaceBlock.java
│       ├── PhotosynthesisAcceleratorBlock.java
│       ├── SpectralRefinerBlock.java       (tiers 1–4, niveau en BlockEntity)
│       ├── LightBatteryBlock.java
│       ├── BeamSplitterBlock.java          (tiers 1–4, sorties actives en BlockEntity)
│       ├── LightGateBlock.java
│       └── WavelengthSensorBlock.java
├── blockentity/
│   ├── ModBlockEntities.java         (DeferredRegister<BlockEntityType>)
│   └── custom/
│       └── (un BlockEntity par machine)
├── menu/
│   ├── ModMenuTypes.java             (DeferredRegister<MenuType>)
│   └── custom/
│       └── (un AbstractContainerMenu + Screen par machine avec GUI)
├── beam/
│   ├── LightBeam.java                (record : λ, qualité, débit, direction)
│   └── LightBeamManager.java         (propagation raycasting, recalcul sur neighborChanged)
├── capability/
│   └── PhotonStorage.java            (implémentation IEnergyStorage pour PH internes)
├── datagen/
│   └── (providers NeoForge : recettes, loot tables, tags, modèles)
├── event/
│   └── ModEvents.java                (couleurs items, tabs créatif, etc.)
└── util/
    └── WavelengthUtil.java           (constantes, formule efficacité, formule fusion)
```

### Points techniques clés

**Registres (NeoForge) :**
```java
public static final DeferredRegister<Item> ITEMS =
    DeferredRegister.create(Registries.ITEM, Gemmology.MOD_ID);

public static final DeferredHolder<Item, GemItem> GEM =
    ITEMS.register("gem", () -> new GemItem(new Item.Properties()));
```

**Faisceaux :**
- Raycasting via `level.clip()` à chaque tick sur les BlockEntity actifs
- Re-calcul uniquement sur `onNeighborChange()` ou update de chunk
- Rendu client : `RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS`

**Énergie (FE natif) :**
- `IEnergyStorage` pour Solar Collector, Thermal Generator, Light Battery, Energy Converter
- Capabilities NeoForge : `BlockCapabilityCache` pour la découverte des adjacents
- Taux de conversion Energy Converter : `10 FE → 1 PH`

**Light Detection (Prism Stand) :**
- `level.getBrightness(LightLayer.SKY, pos.above())` pour la lumière du ciel
- `level.getDayTime() % 24000L` pour l'heure du jour
- `level.getBiome(pos)` pour le biome
- `level.isRaining()` / `level.isThundering()` pour la météo

**Couleur des gemmes (NeoForge) :**
```java
@SubscribeEvent
public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
    event.register((stack, layer) -> { ... }, ModItems.GEM.get());
}
```

---

## Effets visuels

> Le rendu du faisceau est **inclus dès la Phase 1** — sans le voir, impossible de tester que le système fonctionne. Les autres effets (particules machines, halos gemmes UV/IR, overlay Goggles) restent reportés en Phase 3.

Reportés à plus tard :
- Faisceaux UV/IR invisibles (particules à la place)
- Animations machines actives/inactives
- Overlay des Spectral Goggles
- Halo lumineux des gemmes hors-visible

---

## Rendu du faisceau lumineux

### Approche technique : Block Entity Renderer

Le rendu est effectué par un **`BlockEntityRenderer<LightEmitterBlockEntity>`**. Le `LightEmitterBlockEntity` connaît en permanence le segment de beam qu'il émet (start, end, λ, débit). C'est lui qui pilote le rendu côté client.

**Enregistrement du BER (client uniquement) :**
```java
@EventBusSubscriber(modid = Gemmology.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class GemmologyClient {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LIGHT_EMITTER.get(),
            LightEmitterRenderer::new);
    }
}
```

### Géométrie du beam

Le beam est rendu comme un **quad billboard** (deux quads croisés en X) ou un **quad face-caméra**, centré sur l'axe du beam, de la face du Light Emitter jusqu'au point d'impact.

```java
public class LightEmitterRenderer implements BlockEntityRenderer<LightEmitterBlockEntity> {

    @Override
    public void render(LightEmitterBlockEntity be, float partialTick,
                       PoseStack pose, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {

        LightBeam beam = be.getCurrentBeam();
        if (beam == null) return;

        int color = WavelengthUtil.toRGB(beam.wavelength()); // λ → ARGB
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float b = ( color        & 0xFF) / 255f;
        float alpha = 0.6f + 0.4f * (beam.debit() / 50f); // plus lumineux = plus opaque

        Vec3 start = Vec3.atCenterOf(be.getBlockPos());
        Vec3 end   = beam.endPos(); // calculé par LightBeamManager

        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        renderBeamQuad(pose, vc, start, end, 0.05f, r, g, b, alpha);
    }
}
```

### `renderBeamQuad`

Dessine deux quads perpendiculaires centrés sur l'axe du beam (forme de croix) pour donner l'illusion d'un volume sans géométrie 3D coûteuse.

```java
private void renderBeamQuad(PoseStack pose, VertexConsumer vc,
                              Vec3 start, Vec3 end, float radius,
                              float r, float g, float b, float a) {
    Vec3 dir    = end.subtract(start).normalize();
    Vec3 perpH  = dir.cross(new Vec3(0, 1, 0)).normalize().scale(radius);
    Vec3 perpV  = dir.cross(perpH).normalize().scale(radius);

    // Quad horizontal
    addQuad(pose, vc, start, end, perpH, r, g, b, a);
    // Quad vertical
    addQuad(pose, vc, start, end, perpV, r, g, b, a);
}
```

### Émission de lumière (`setLightBlock`)

Le beam ne projette pas de lumière dynamique nativement en Minecraft. Deux options :

| Option | Description | Complexité |
|---|---|---|
| **Aucune** | Le beam est visible mais n'éclaire pas | Simple (Phase 1) |
| **Light level sur les blocs touchés** | `level.setBlock(pos, lightBlock, ...)` sur chaque bloc traversé | Moyen |
| **Mod Embeddium / Iris shaders** | Lumière dynamique via shader | Hors scope |

> **Phase 1 : option "Aucune"** — le beam est visible mais n'éclaire pas les blocs. La lumière dynamique est ajoutée plus tard.

### `WavelengthUtil.toRGB`

Méthode statique qui convertit une longueur d'onde (nm) en couleur ARGB packed. C'est l'algorithme déjà présent dans `GemmologyClient.java` (converti en utilitaire partagé) :

```java
public class WavelengthUtil {

    public static final float MIN_WAVELENGTH = 380f;
    public static final float MAX_WAVELENGTH = 780f;

    public static int toRGB(float wavelength) {
        // Algorithme spectre visible → RGB
        // (repris de GemmologyClient.getColorFromWavelength)
        ...
        return (0xFF << 24) | ((int) red << 16) | ((int) green << 8) | (int) blue;
    }
}
```

### Synchronisation serveur → client

Le `LightEmitterBlockEntity` doit envoyer les données du beam au client pour le rendu :

```java
// Dans LightEmitterBlockEntity
@Override
public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
}

@Override
public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag tag = new CompoundTag();
    if (currentBeam != null) {
        tag.putFloat("wavelength", currentBeam.wavelength());
        tag.putFloat("quality",    currentBeam.quality());
        tag.putFloat("debit",      currentBeam.debit());
        tag.putDouble("endX", currentBeam.endPos().x);
        tag.putDouble("endY", currentBeam.endPos().y);
        tag.putDouble("endZ", currentBeam.endPos().z);
    }
    return tag;
}
```

Chaque fois que le beam change (nouveau bloc bloquant, changement de λ, débit fluctue), le serveur appelle `level.sendBlockUpdated(pos, ...)` pour re-synchroniser.

---

## Système de recettes machines

### Principe général

NeoForge étend le système de recettes vanilla (JSON-driven) avec des **types de recettes custom**. Chaque machine du mod qui transforme des items enregistre son propre `RecipeType<T>`. Les recettes sont des fichiers JSON dans `data/gemmology/recipe/`, modifiables par datapack.

Les machines se divisent en deux catégories :

| Catégorie | Description | Exemples |
|---|---|---|
| **Recette fixe** | Input → Output défini en JSON, comme un four vanilla | Crystal Furnace, Chromatic Compressor, Thermal Expander |
| **Processus continu** | Pas d'input/output fixe — la machine modifie un état progressivement | Spectral Refiner (ajuste λ), Prism Stand (attunement), Photosynthesis Accelerator (effet zone) |

---

### Machines à recette fixe

#### Enregistrement

```java
// ModRecipeTypes.java
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, Gemmology.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrystalFurnaceRecipe>> CRYSTAL_FURNACE =
        RECIPE_TYPES.register("crystal_furnace",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Gemmology.MOD_ID, "crystal_furnace")));

    public static final DeferredHolder<RecipeType<?>, RecipeType<ChromaticCompressorRecipe>> CHROMATIC_COMPRESSOR =
        RECIPE_TYPES.register("chromatic_compressor", () -> ...);
}
```

#### Comment la machine cherche sa recette

```java
// Dans CrystalFurnaceBlockEntity.serverTick()
Optional<CrystalFurnaceRecipe> recipe = level.getRecipeManager()
    .getAllRecipesFor(ModRecipeTypes.CRYSTAL_FURNACE.get())
    .stream()
    .map(RecipeHolder::value)
    .filter(r -> r.matches(inputStack, currentWavelength))
    .findFirst();

if (recipe.isPresent()) {
    // avancer le craft
} else {
    // aucune recette applicable, stopper
}
```

#### Format JSON — Crystal Furnace

```json
// data/gemmology/recipe/crystal_furnace/iron_ingot.json
{
  "type": "gemmology:crystal_furnace",
  "ingredient": { "item": "minecraft:raw_iron" },
  "result":     { "item": "minecraft:iron_ingot", "count": 1 },
  "min_wavelength": 620.0,
  "max_wavelength": 780.0,
  "min_debit": 8,
  "cook_time": 200
}
```

Champs disponibles :
- `ingredient` — item en entrée (ou tag `#tag`)
- `result` — item produit
- `min_wavelength` / `max_wavelength` — plage λ acceptée (la machine vérifie à chaque tick)
- `min_debit` — PH/tick minimum pour que la recette soit active
- `cook_time` — durée en ticks à efficacité 100% (se divise par l'efficacité réelle)

#### Format JSON — Chromatic Compressor (gemme → gemme UV)

```json
{
  "type": "gemmology:chromatic_compressor",
  "input_max_wavelength": 400.0,
  "output_wavelength_min": 300.0,
  "output_wavelength_max": 380.0,
  "required_items": [
    { "item": "minecraft:amethyst_shard", "count": 4 },
    { "item": "minecraft:echo_shard",     "count": 1 }
  ],
  "process_time": 6000,
  "ph_per_tick": 100
}
```

---

### Machines à processus continu

Ces machines n'ont pas de `RecipeType`. Leur logique est entièrement dans le `BlockEntity` :

**Spectral Refiner** — pas de recette JSON. La gemme en slot A est modifiée tick par tick : `λ += step` ou `λ -= step` selon que la cible est plus haute ou plus basse. Aucune lookup dans le RecipeManager.

**Prism Stand (attunement)** — lit la lumière du monde (`getLightLevel`, `getDayTime`, biome…) et calcule λ progressivement. Aucune recette.

**Photosynthesis Accelerator** — scanne les blocs dans un rayon, applique `BonemealableBlock.performBonemeal()` ou accélère les `RandomTick` des plantes. Aucune recette.

---

### Système d'upgrade (Spectral Refiner, Beam Splitter, Concentrating Lens)

Les blocs upgradables ne se craftent pas from scratch au delà du Tier 1. Ils s'upgradent dans un **Photon Upgrade Station** (bloc dédié, mid-game) :

- Slot A : bloc Tier N à upgrader
- Slot B : matériaux d'upgrade (variable selon le tier cible)
- Output : bloc Tier N+1

Le Photon Upgrade Station utilise son propre `RecipeType<UpgradeRecipe>` :

```json
// data/gemmology/recipe/upgrade/spectral_refiner_t2.json
{
  "type": "gemmology:upgrade",
  "base": { "item": "gemmology:spectral_refiner" },
  "additions": [
    { "item": "minecraft:diamond",         "count": 2 },
    { "item": "minecraft:gold_ingot",      "count": 4 },
    { "item": "gemmology:dampening_glass", "count": 2 }
  ],
  "result": { "item": "gemmology:spectral_refiner_t2" }
}
```

---

### Intégration JEI

[Just Enough Items](https://www.curseforge.com/minecraft/mc-mods/jei) est le mod standard pour afficher les recettes en jeu. L'intégration est **optionnelle** (`compileOnly` dans build.gradle) — le mod fonctionne sans JEI, mais sans affichage des recettes machines.

```groovy
// build.gradle
dependencies {
    compileOnly "mezz.jei:jei-${minecraft_version}-neoforge-api:${jei_version}"
    runtimeOnly  "mezz.jei:jei-${minecraft_version}-neoforge:${jei_version}"   // en dev seulement
}
```

#### Une category JEI par machine

```java
@JeiPlugin
public class GemmologyJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Gemmology.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(
            new CrystalFurnaceCategory(reg.getJeiHelpers()),
            new ChromaticCompressorCategory(reg.getJeiHelpers()),
            new SpectralRefinerCategory(reg.getJeiHelpers()),
            new UpgradeStationCategory(reg.getJeiHelpers())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        List<CrystalFurnaceRecipe> furnaceRecipes = level.getRecipeManager()
            .getAllRecipesFor(ModRecipeTypes.CRYSTAL_FURNACE.get())
            .stream().map(RecipeHolder::value).toList();
        reg.addRecipes(CrystalFurnaceCategory.TYPE, furnaceRecipes);
        // ... idem pour les autres machines
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(ModBlocks.CRYSTAL_FURNACE.get()),
            CrystalFurnaceCategory.TYPE);
        // ... idem
    }
}
```

#### Mise en page de la Crystal Furnace dans JEI

```
┌─────────────────────────────────────────────────┐
│  Crystal Furnace                                  │
│                                                   │
│  [Raw Iron]  →→→→→→→→→→→→→→→  [Iron Ingot ×1]  │
│                                                   │
│  Longueur d'onde requise :                        │
│  ████████████████░░░░░░░░░░░░░░░░░░░░░░░░░  [nm] │
│  620 nm ───────────────────────────── 780 nm      │
│  (gradient rouge-orange du spectre visible)       │
│                                                   │
│  Débit minimum : 8 PH/tick                        │
│  Durée à 100% : 10 s                              │
└─────────────────────────────────────────────────┘
```

Le **gradient spectral** est rendu avec `IGuiHelper.createDrawable()` en dessinant des pixels colorés via `WavelengthUtil.toRGB(λ)` sur la plage `[min_wavelength, max_wavelength]`. La zone grisée indique les longueurs d'onde hors plage.

#### Mise en page du Spectral Refiner dans JEI

Le Spectral Refiner n'a pas de recette fixe, mais JEI peut afficher sa **logique** sous forme d'entrée informative :

```
┌─────────────────────────────────────────────────┐
│  Spectral Refiner (Tier 1 — pas 5.0 nm)          │
│                                                   │
│  [Gemme quelconque]  →→→  [Gemme affinée]        │
│                                                   │
│  Ajuste λ vers la valeur cible par pas de 5.0 nm │
│  Vitesse : 1 pas / 2 s  │  Consomme : 10 PH/tick │
│                                                   │
│  Qualité transmise : 0.75                         │
│  ⚠ Ne peut pas atteindre λ = 0 bruit sans fibre  │
└─────────────────────────────────────────────────┘
```

#### Mise en page de l'Upgrade Station dans JEI

```
┌─────────────────────────────────────────────────┐
│  Photon Upgrade Station                           │
│                                                   │
│  [Spectral Refiner T1]                            │
│  [Diamond ×2]          →→→  [Spectral Refiner T2]│
│  [Gold Ingot ×4]                                  │
│  [Dampening Glass ×2]                             │
│                                                   │
│  Upgrade : Tier 1 → Tier 2  (pas : 5.0 → 1.0 nm)│
└─────────────────────────────────────────────────┘
```

---



### Phase 1 — Version basique (vérifier le fonctionnement)

1. **Nettoyage du code existant** (typos, constantes, imports, mixin vide)
2. **`WavelengthUtil.java`** (constantes MIN/MAX, formule efficacité, formule fusion)
3. **`RawCrystalItem` + `RawCrystalOre`** (item + bloc + génération monde)
4. **`LightBeam.java`** (structure de données : λ, qualité, débit, direction)
5. **`LightBeamManager.java`** (propagation par raycasting, détection intersections)
6. **`SolarCollectorBlock`** (production PH/tick selon lumière du ciel)
6b. **`ConcentratingLensBlock`** (amplificateur empilable au-dessus d'un Solar Collector)
6c. **`ThermalGeneratorBlock`** (combustible → PH/tick constant, indépendant du soleil)
7. **`LightEmitterBlock`** (convertit PH → beam, émission directionelle)
7b. **`LightEmitterRenderer`** (BER — rendu quad billboard coloré selon λ, synchronisation NBT serveur→client)
8. **`PrismStandBlock`** (attunement Raw Crystal + filtrage beam)
9. **`CrystalFurnaceBlock`** (premier consommateur — four accéléré)
10. **`LightBatteryBlock`** (stockage PH)
11. **`BasicBeamSplitterBlock`** (Tier 1 — 2 sorties)
12. **`LightGateBlock`** (contrôle redstone)
13. **`WavelengthSensorBlock`** (sortie redstone selon λ ou qualité)
14. **`SpectralRefinerBlock` Tier 1** (Crude — pas 5 nm)
15. **`PhotosynthesisAcceleratorBlock`** (second consommateur)
16. **`SpectralGogglesItem`** (overlay basique sans effets visuels avancés)

### Phase 2 — Extension (après validation)

17. **Spectral Refiner Tiers 2–4** (upgrades du Tier 1)
18. **Beam Splitter Tiers 2–3**
19. **Chromatic Compressor + Thermal Expander** (gemmes UV/IR)
20. **Machines late game** (UV Sterilizer, Thermal Forge)
21. **Spectral Forge** (gemmes Tier 4–5)
22. **Machines end game** (Transmitter/Receiver, X-Ray Scanner)

### Phase 3 — Effets visuels (après Phase 2)

23. **Overlay Spectral Goggles**
24. **Particules et animations machines**
25. **Faisceaux UV/IR** (invisibles + particules à la place)
26. **Rendu gemmes UV/IR** (halos, particules)
27. **Lumière dynamique** sur les blocs traversés par le beam
