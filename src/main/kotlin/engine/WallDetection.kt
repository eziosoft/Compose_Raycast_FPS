package engine

enum class WallType {
    NONE,
    WALL,
    DOOR,
    SECRET,
    EXIT
}


fun isWall(x: Float, y: Float, map: IntArray, mapW: Int, mapH: Int, cellSize: Int): WallType {
    val mapX = (x / cellSize).toInt()
    val mapY = (y / cellSize).toInt()
    return if (mapX in 0 until mapW && mapY in 0 until mapH) {
        when (map[mapY * mapW + mapX]) {
            1, 2, 3 -> WallType.WALL
            8 -> WallType.EXIT
            9 -> WallType.DOOR
            else -> WallType.NONE

        }
    } else {
        WallType.WALL // Treat out-of-bounds as walls
    }
}