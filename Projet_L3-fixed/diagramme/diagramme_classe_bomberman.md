# Diagramme de classe — Bomberman

```mermaid
classDiagram
  class Main {
    +main(args: String[])$
    -showMenu()$
    -launchGame(botMode: boolean, diff: Difficulty)$
    -makeMenuButton(text: String, fg: Color, bg: Color, icon: String) JButton$
    -makeSectionLabel(text: String, color: Color) JLabel$
  }

  class GameSettings {
    -mapWidth: int = 15
    -mapHeight: int = 15
    -gameSpeed: int = 30
    -explosionDuration: int = 800
    -itemDropChance: double = 0.35
    +getMapWidth() int
    +getMapHeight() int
    +getGameSpeed() int
    +getExplosionDuration() int
    +getItemDropChance() double
  }

  class BombermanModel {
    -width: int
    -height: int
    -grid: TileType[][]
    -items: ItemType[][]
    -players: List~Player~
    -activeBombs: List~Bomb~
    -activeExplosions: Map~Long,Long~
    -explosionDurationMs: int
    -itemDropChance: double
    -phase: GamePhase
    -winner: Player
    +update()
    +placeBomb(playerId: int)
    +movePlayer(playerId: int, dx: float, dy: float)
    +resetGame()
    +getGrid() TileType[][]
    +getItems() ItemType[][]
    +getPlayers() List~Player~
    +getBombs() List~Bomb~
    +getPhase() GamePhase
    +getWinner() Player
  }

  class GamePhase {
    <<enumeration>>
    PLAYING
    GAME_OVER
  }

  class Player {
    -id: int
    -px: float
    -py: float
    -spawnPxX: float
    -spawnPxY: float
    -lives: int = 3
    -alive: boolean
    -bombCapacity: int = 1
    -activeBombCount: int
    -bombRange: int = 2
    -speed: float = 2.2
    -invincibleUntil: long
    -ownedBombTileX: int
    -ownedBombTileY: int
    +moveBy(dx: float, dy: float, grid: TileType[][], bombs: List~Bomb~, w: int, h: int)
    +hit() HitResult
    +respawn()
    +reset()
    +applyItem(type: ItemType)
    +canPlaceBomb() boolean
    +onBombPlaced()
    +onBombExploded()
    +setOwnedBombTile(x: int, y: int)
    +getX() int
    +getY() int
    +getPixelX() float
    +getPixelY() float
    +isAlive() boolean
    +isInvincible() boolean
    +getLives() int
    +getBombRange() int
  }

  class HitResult {
    <<enumeration>>
    HIT_NONE
    HIT_ALIVE
    HIT_DEAD
  }

  class Bomb {
    -x: int
    -y: int
    -range: int
    -explosionTimestamp: long
    -owner: Player
    +isReadyToExplode() boolean
    +getRemainingMs() long
    +getX() int
    +getY() int
    +getRange() int
    +getOwner() Player
  }

  class Tile {
    -type: TileType
    -item: ItemType
    +getType() TileType
    +setType(type: TileType)
    +getItem() ItemType
    +setItem(item: ItemType)
    +isWalkable() boolean
  }

  class TileType {
    <<enumeration>>
    EMPTY
    WALL
    DESTRUCTIBLE_BLOCK
    BOMB
    ITEM
    EXPLOSION
  }

  class ItemType {
    <<enumeration>>
    NONE
    BONUS_SPEED
    BONUS_BOMB_COUNT
    BONUS_RANGE
    MALUS_SLOW
  }

  class BombermanView {
    -gamePanel: GamePanel
    +display()
    +refresh()
    +updateModel(model: BombermanModel)
    +getGamePanel() GamePanel
  }

  class GamePanel {
    -model: BombermanModel
    -botMode: boolean
    -restartButtonBounds: Rectangle
    -restartClicked: boolean
    +setBotMode(bot: boolean)
    +updateModel(m: BombermanModel)
    +isRestartClicked() boolean
    #paintComponent(g: Graphics)
  }

  class GameController {
    -model: BombermanModel
    -view: BombermanView
    -settings: GameSettings
    -pressedKeys: Set~Integer~
    -gameLoop: Timer
    -botEnabled: boolean
    -bot: BotAI
    -placeBomb1: boolean
    -placeBomb2: boolean
    +keyPressed(e: KeyEvent)
    +keyReleased(e: KeyEvent)
    -tick()
    -handleHumanMovement()
    -handleBotTurn()
    -restartGame()
  }

  class BotAI {
    -difficulty: Difficulty
    -tickCooldown: int
    +computeAction(model: BombermanModel) BotAction
    -isInDanger(x: int, y: int, model: BombermanModel) boolean
    -findSafeDirection(x: int, y: int, model: BombermanModel) int[]
    -moveToward(x: int, y: int, tx: int, ty: int, model: BombermanModel) int[]
    -hasSafeEscape(bx: int, by: int, range: int, model: BombermanModel) boolean
    -randomWalk(x: int, y: int, model: BombermanModel) BotAction
    -canWalk(x: int, y: int, model: BombermanModel) boolean
  }

  class Difficulty {
    <<enumeration>>
    EASY
    MEDIUM
    HARD
  }

  class BotAction {
    +dx: int
    +dy: int
    +placeBomb: boolean
    +NONE$ BotAction
    +PLACE_BOMB$ BotAction
    +move(dx: int, dy: int)$ BotAction
    +isMove() boolean
  }

  Main ..> GameSettings : cree
  Main ..> BombermanModel : cree
  Main ..> BombermanView : cree
  Main ..> GameController : cree

  GameController --> BombermanModel : pilote
  GameController --> BombermanView : rafraichit
  GameController --> GameSettings : lit
  GameController --> BotAI : utilise

  BombermanModel --> GamePhase : contient
  BombermanModel "1" --> "2" Player : gere
  BombermanModel "1" --> "*" Bomb : gere
  BombermanModel ..> TileType : utilise
  BombermanModel ..> ItemType : utilise

  BombermanView "1" --> "1" GamePanel : contient

  Bomb --> Player : owner

  Player --> HitResult : retourne
  Player ..> ItemType : applique
  Player ..> TileType : consulte

  BotAI --> Difficulty : a
  BotAI --> BotAction : retourne
  BotAI ..> BombermanModel : analyse
  BotAI ..> Bomb : observe
  BotAI ..> TileType : consulte

  Tile ..> TileType : utilise
  Tile ..> ItemType : utilise
```

---

## Légende des relations

| Symbole | Signification |
|---|---|
| `-->` | Association (utilise / contient) |
| `..>` | Dépendance (crée / appelle) |
| `"1" --> "2"` | Cardinalité : 1 modèle gère 2 joueurs |
| `"1" --> "*"` | Cardinalité : 1 modèle gère N bombes |
| `<<enumeration>>` | Enum Java |
| `$` après une méthode | Méthode statique |
| `#` devant une méthode | Méthode protégée (protected) |
| `-` devant un attribut | Attribut privé |
| `+` devant un attribut/méthode | Public |

---

## Notes importantes

- `Tile` est déclarée dans le code mais jamais instanciée — la grille utilise directement `TileType[][]`
- `GamePhase` et `HitResult` sont des enums internes à leurs classes (`BombermanModel` et `Player`)
- `Difficulty` et `BotAction` sont des classes internes à `BotAI`
- `GameController` extends `KeyAdapter` (non représenté pour simplifier)
