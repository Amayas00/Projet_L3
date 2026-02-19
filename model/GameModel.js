import { GameMap } from "./GameMap.js";
import { GameState } from "./GameState.js";
import { Bomb } from "./Bomb.js";

export class GameModel {

    constructor() {

        this.map = new GameMap(10, 10);

        this.players = [];
        this.bombs = [];

        this.state = GameState.MENU;
    }

    addPlayer(player) {
        this.players.push(player);
    }

    update(deltaMs) {

        for (let bomb of this.bombs) {
            bomb.tick(deltaMs);
        }

        this.bombs = this.bombs.filter(b => !b.isExploded());
    }

    placeBomb(player) {

        if (player.canPlaceBomb()) {

            const bomb = new Bomb(
                player.x,
                player.y,
                player.id,
                player.bombRange
            );

            this.bombs.push(bomb);

            player.activeBombs++;
        }
    }

    isGameOver() {
        return this.players.filter(p => p.alive).length <= 1;
    }
}
