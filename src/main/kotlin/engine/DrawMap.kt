package engine

import maps.Map
import models.Player
import kotlin.math.cos
import kotlin.math.sin

fun drawMap(
    screen: Screen,
    map: Map,
    xOffset: Int,
    yOffset: Int,
    cellSize: Int,
    player: Player,
    playerSize: Float,
    enemies: List<Player>
) {
    for (y: Int in 0 until map.MAP_Y) {
        for (x in 0 until map.MAP_X) {

            val color: Screen.Color = if (map.MAP[y * map.MAP_X + x] > 0) {
                Screen.Color(255, 255, 255)
            } else {
                Screen.Color(0, 0, 0)
            }

            screen.drawFilledRect(xOffset + x * cellSize, yOffset + y * cellSize, cellSize, cellSize, color)
        }
    }

    drawPlayerOnMap(
        screen,
        player,
        xOffset = xOffset,
        yOffset = yOffset,
        color = Screen.Color(0, 255, 255),
        playerSize = playerSize
    )

    enemies.forEach { enemy ->
        drawPlayerOnMap(
            screen,
            enemy,
            xOffset = xOffset,
            yOffset = yOffset,
            color = Screen.Color(255, 0, 0),
            playerSize = playerSize
        )
    }
}

private fun drawPlayerOnMap(
    bitmap: Screen,
    player: Player,
    xOffset: Int,
    yOffset: Int,
    color: Screen.Color = Screen.Color(0, 255, 255),
    playerSize: Float
) {
    bitmap.drawFilledRect(
        xOffset + (player.x - playerSize / 2).toInt(),
        yOffset + (player.y - playerSize / 2).toInt(),
        playerSize.toInt(),
        playerSize.toInt(),
        color
    )
    bitmap.drawLine(
        xOffset + player.x.toInt(),
        yOffset + player.y.toInt(),
        (xOffset + player.x + cos(player.rotationRad.toDouble()).toFloat() * playerSize * 2).toInt(),
        (yOffset + player.y + sin(player.rotationRad.toDouble()).toFloat() * playerSize * 2).toInt(),
        color
    )
}
