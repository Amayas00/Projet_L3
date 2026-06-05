# Bomberman — Projet L3

Un jeu de stratégie 2D inspiré du Bomberman original (1983), développé en Java avec Swing. Jouable à deux au clavier ou contre un bot avec trois niveaux de difficulté.

![Java](https://img.shields.io/badge/Java-21-orange) ![Swing](https://img.shields.io/badge/UI-Swing-blue) ![License](https://img.shields.io/badge/license-academic-green)

## Sommaire

- [Aperçu](#aperçu)
- [Fonctionnalités](#fonctionnalités)
- [Démarrage rapide](#démarrage-rapide)
- [Contrôles](#contrôles)
- [Règles du jeu](#règles-du-jeu)
- [Architecture](#architecture)
- [Intelligence artificielle](#intelligence-artificielle)
- [Structure du projet](#structure-du-projet)
- [Choix techniques notables](#choix-techniques-notables)
- [Pour aller plus loin](#pour-aller-plus-loin)

## Aperçu

Bomberman se joue sur une grille de 15×15 cases composée de murs indestructibles, de blocs destructibles, et de couloirs. Chaque joueur pose des bombes pour faire sauter des blocs (qui révèlent parfois des bonus) et éliminer son adversaire. La dernière personne en vie gagne.

Le projet a été conçu autour de trois objectifs : appliquer rigoureusement le pattern MVC, offrir un mouvement fluide en pixel (pas case par case), et implémenter une IA réellement compétitive avec planification BFS et évaluation du danger.

## Fonctionnalités

- **Deux modes de jeu** : 1 vs 1 au clavier, ou 1 joueur vs Bot
- **Trois niveaux d'IA** : Easy, Medium, Hard, avec comportements différenciés
- **Mouvement pixel-perfect** : hitbox plus petite que la tile pour glisser dans les couloirs
- **Système de bonus/malus** : vitesse, capacité de bombes, portée d'explosion, ralentissement
- **Spawn invincible** : 2 secondes d'immunité après respawn pour éviter les morts injustes
- **Menu Swing** avec sélection de mode et de difficulté

## Démarrage rapide

### Prérequis

- JDK 17 ou supérieur (testé avec JDK 21)
- Aucune dépendance externe — uniquement la bibliothèque standard Java + Swing

### Compilation

Depuis la racine du projet :

```bash
mkdir -p out
javac -d out Main.java src/model/*.java src/controller/*.java src/View/*.java
```

### Lancement

```bash
java -cp out Main
```

Un menu s'ouvre. Sélectionnez le mode souhaité et la partie démarre.

## Contrôles

| Touche | Joueur 1 | Joueur 2 |
|---|---|---|
| Déplacement | `↑` `↓` `←` `→` | `W` `A` `S` `D` |
| Poser une bombe | `Entrée` | `Espace` |

| Touche | Action |
|---|---|
| `R` | Rejouer (après une fin de partie) |
| `Échap` | Quitter |

## Règles du jeu

- **Vies** : chaque joueur démarre avec 3 vies
- **Bombes** : capacité initiale de 1, portée initiale de 2 cases
- **Délai** : une bombe explose après 3 secondes
- **Explosion** : la flamme se propage en croix et est stoppée par les murs indestructibles. Elle détruit les blocs destructibles qu'elle touche.
- **Items** : chaque bloc détruit a 35 % de chance de révéler un item
- **Victoire** : dernier joueur en vie

### Items disponibles

| Item | Effet | Limite |
|---|---|---|
| Vitesse + | +0.4 vitesse | max 4.5 |
| Bombes + | +1 capacité simultanée | max 8 |
| Portée + | +1 case de flamme | max 9 |
| Ralentissement | −0.5 vitesse | min 1.4 |

## Architecture

Le projet suit strictement le pattern **MVC** :

```
┌──────────────┐      ┌────────────────┐      ┌──────────────────┐
│  GameController  │─── pilote ───────▶│  BombermanModel  │
│  (Controller)    │                   │  (Model)         │
└──────────────┘      └────────────────┘
        │                       │
        │ rafraîchit            │ gère
        ▼                       ▼
┌──────────────┐      ┌──────────────────┐
│ BombermanView │      │ Player, Bomb,    │
│  (View)       │      │ TileType, etc.   │
└──────────────┘      └──────────────────┘
```

- **Model** (`src/model/`) : toute la logique métier. Aucun import Swing.
- **View** (`src/View/`) : affichage Swing pur. Lit le modèle, ne le modifie jamais.
- **Controller** (`src/controller/`) : reçoit les entrées clavier, pilote le modèle, déclenche la vue. Contient également le `BotAI`.

### Boucle de jeu

Un `javax.swing.Timer` déclenche `tick()` toutes les 30 ms. Chaque tick :

1. Lit les touches pressées (et les flags de pose de bombe)
2. Si mode bot : calcule l'action du bot via `BotAI.computeAction()`
3. Met à jour le modèle (`model.update()`) : physique, bombes, explosions, collisions
4. Rafraîchit la vue (`view.refresh()`)

> Les événements clavier ne posent que des flags ; la pose de bombe est traitée dans `tick()`. Cela évite toute race condition entre l'EDT Swing et la boucle de jeu.

## Intelligence artificielle

Le bot (`BotAI.java`) raisonne case par case et suit un arbre de décision à trois priorités :

1. **Suis-je en danger ?** Si oui, chercher une direction sûre via `findSafeDirection()`. En dernier recours, poser une bombe par désespoir.
2. **Puis-je poser une bombe utile ?** Si le joueur humain est proche OU si des blocs adjacents peuvent être détruits, ET qu'une issue sûre existe (vérifiée par `hasSafeEscape()`), poser une bombe.
3. **Sinon, où aller ?** Pathfinding **BFS** vers le joueur humain. Fallback : marche aléatoire.

### Algorithmes clés

| Méthode | Rôle | Complexité |
|---|---|---|
| `moveToward()` | BFS sur la grille de tiles, retourne le premier pas du plus court chemin | O(W × H) |
| `isInDanger()` | Vérifie si la tile du bot est dans la zone d'explosion d'une bombe active | O(bombes × portée) |
| `hasSafeEscape()` | Simule la zone de danger d'une bombe à poser et vérifie qu'une case hors zone est atteignable | O(portée²) |
| `findSafeDirection()` | Énumère les 4 cases adjacentes walkable hors danger | O(1) |

### Découplage décision / mouvement

Le `tickCooldown` ne gèle **plus** le mouvement (ancien bug : le bot freezait entre deux décisions). La dernière action est cachée et rejouée chaque tick, et la replanification a lieu :

- à la fin du cooldown,
- quand le bot atteint le centre d'une nouvelle case (intersection),
- quand un danger apparaît,
- quand l'action mise en cache devient invalide.

## Structure du projet

```
Projet_L3/
├── Main.java                          # Point d'entrée + menu Swing
├── README.md
├── diagramme/
│   ├── diagramme_classe_bomberman.md  # Diagramme UML complet (Mermaid)
│   └── bomber.gan                     # Diagramme de Gantt
└── src/
    ├── model/
    │   ├── BombermanModel.java        # État du jeu, update()
    │   ├── Player.java                # Joueur, mouvement, hitbox
    │   ├── Bomb.java                  # Bombe et explosion
    │   ├── Tile.java                  # (déclarée mais non instanciée)
    │   ├── TileType.java              # Enum : EMPTY, WALL, ...
    │   ├── ItemType.java              # Enum : BONUS_SPEED, ...
    │   └── GameSettings.java          # Paramètres centralisés
    ├── View/
    │   ├── BombermanView.java         # Fenêtre principale
    │   └── GamePanel.java             # Rendu du jeu (paintComponent)
    └── controller/
        ├── GameController.java        # Boucle de jeu, gestion clavier
        └── BotAI.java                 # IA du bot
```

Le diagramme UML complet est disponible dans `diagramme/diagramme_classe_bomberman.md`.

## Choix techniques notables

### Hitbox sub-tile

Le joueur fait 30×30 pixels dans une case de 44×44 (7 pixels de jeu de chaque côté). Sans ce padding, le moindre décalage pixel rend la traversée des couloirs frustrante. Avec, le joueur glisse naturellement le long des murs grâce à `snapAxis()` qui recentre progressivement sur l'axe libre.

### Mouvement en float, IA en int

`Player.moveBy()` raisonne en pixels (`float`) pour la fluidité visuelle. L'IA raisonne en cases entières (`int`) car le BFS et l'évaluation de danger n'ont pas besoin de précision sub-tile. Le pont entre les deux se fait dans `GameController.handleBotTurn()` qui cast les `dx/dy` du bot en `float` au moment d'appeler `model.movePlayer()`.

### Bombe traversable par son poseur

Quand un joueur pose une bombe, il se tient dessus. Sans précaution, la collision le bloquerait immédiatement. La solution : `Player.ownedBombTileX/Y` mémorise la tile de la bombe qui vient d'être posée, et la collision avec elle est désactivée tant que le joueur n'a pas quitté cette case.

### Pas de race condition

Les événements `keyPressed` arrivent sur l'Event Dispatch Thread de Swing, qui n'est **pas** celui du `Timer`. Pour éviter qu'une bombe soit posée pendant qu'on calcule la frame, les `keyPressed` ne lèvent que des flags (`placeBomb1`, `placeBomb2`) consommés dans `tick()`.

## Pour aller plus loin

Pistes d'évolution envisagées :

- **Mode 4 joueurs** : étendre `BombermanModel` pour supporter N joueurs et N spawns
- **IA Minimax** : remplacer le BFS + heuristiques par une recherche d'arbre alpha-bêta
- **Sprites & sons** : remplacer les ovales par des sprites animés, ajouter SFX et musique
- **Mode réseau** : sérialisation de l'état et synchronisation client/serveur

## Licence

Projet académique — Licence 3, 2025-2026.
