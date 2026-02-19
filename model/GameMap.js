import { Cell } from "./Cell.js";

export class GameMap {

    constructor(width, height) {
        this.width = width;
        this.height = height;

        this.grid = [];

        for (let y = 0; y < height; y++) {
            this.grid[y] = [];
            for (let x = 0; x < width; x++) {
                this.grid[y][x] = new Cell(x, y);
            }
        }
    }

    getCell(x, y) {
        if (this.inBounds(x, y)) {
            return this.grid[y][x];
        }
        return null;
    }

    inBounds(x, y) {
        return x >= 0 && y >= 0 &&
               x < this.width && y < this.height;
    }

    isWalkable(x, y) {
        const cell = this.getCell(x, y);
        return cell && cell.isEmpty();
    }
}
