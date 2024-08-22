package maps

import models.Player
import models.PlayerState

interface Map {
    val MAP_X: Int
    val MAP_Y: Int
    val MAP: IntArray
}

// 1,2,3 - wall
// 9 - door
// 8 - exit
// 0 - empty
// -1 - player
// -2 - guard

enum class MapObject(val id: Int) {
    guard(-2),
    player(-1),
    empty(0),
    wall1(1),
    wall2(2),
    wall3(3),
}


fun Map.getEnemiesFromMap(cellSize: Int): List<Player> {
    val enemies = mutableListOf<Player>()
    this.MAP.forEachIndexed { index, value ->
        if (value == -2) {
            val position = findPositionBasedOnMapIndex(this.MAP_X, this.MAP_Y, cellSize, index)
            enemies.add(
                Player(
                    x = position[0],
                    y = position[1],
                    rotationRad = 0f,
                    state = PlayerState.WALKING,
                    isMainPlayer = false
                )
            )
        }
    }
    return enemies
}

fun findPositionBasedOnMapIndex(mapX: Int, mapY: Int, cellSize: Int, index: Int): Array<Float> {
    val x = (index % mapX) * cellSize + cellSize / 2f
    val y = (index / mapY) * cellSize + cellSize / 2f
    return arrayOf(x, y)
}