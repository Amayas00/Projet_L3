import { BlockType } from "./BlockType.js";

export class Cell {

    constructor(x, y, block = BlockType.EMPTY) {
        this.x = x;
        this.y = y;
        this.block = block;
    }

    isEmpty() {
        return this.block === BlockType.EMPTY;
    }
}
