export class Bomb {

    constructor(x, y, ownerId, range = 2, fuseMs = 3000) {
        this.x = x;
        this.y = y;
        this.ownerId = ownerId;
        this.range = range;
        this.fuseMs = fuseMs;
    }

    tick(deltaMs) {
        this.fuseMs -= deltaMs;
    }

    isExploded() {
        return this.fuseMs <= 0;
    }
}
