fun generateMap(w:Int, h:Int): IntArray {
    val map = IntArray(w * h) { 0 }
    for (y in 0 until h) {
        for (x in 0 until w) {
            if (x == 0 || y == 0 || x == w - 1 || y == h - 1) {
                map[y * w + x] = 1
            } else {
                map[y * w + x] = if (Math.random() < 0.2) 1 else 0
            }

            if (x == 1 && y == 1) {
                map[y * w + x] = 0
            }
        }
    }
    return map
}