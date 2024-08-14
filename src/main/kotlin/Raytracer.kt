import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.awt.image.BufferedImage
import java.lang.Math.random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.time.DurationUnit
import kotlin.time.measureTime


val pressedKeys = mutableSetOf<Long>()

private const val W = 640
private const val H = 480

private const val FPS = 30

private const val MAP_X = 20
private const val MAP_Y = 20
private const val CELL_SIZE = 5
private const val PLAYER_SIZE = 5f

private val FOV_RAD = 60.toRadian()


private const val PI = 3.1415927f

private val ROTATION_STEP_RAD = 2f.toRadian()
private const val MOVE_STEP = 0.2f

private val MAP = generateMap()

fun generateMap(): Array<Int> {
    val map = Array(MAP_X * MAP_Y) { 0 }
    for (y in 0 until MAP_Y) {
        for (x in 0 until MAP_X) {
            if (x == 0 || y == 0 || x == MAP_X - 1 || y == MAP_Y - 1) {
                map[y * MAP_X + x] = 1
            } else {
                map[y * MAP_X + x] = if (random() < 0.2) 1 else 0
            }

            if (x == 1 && y == 1) {
                map[y * MAP_X + x] = 0
            }
        }
    }
    return map
}



private var player = Player(CELL_SIZE + CELL_SIZE / 2f, CELL_SIZE + CELL_SIZE / 2f, 0f.toRadian())

private val floorBrush = Brush.verticalGradient(
    0f to Color(0xFF000000),
    0.4f to Color(0xFF111111),
    1f to Color(0xFF777755)
)

private val cellingBrush = Brush.verticalGradient(
    0f to Color(0xFF335555),
    0.4f to Color(0xFF111111),
    1f to Color(0xFF000000)
)

@Composable
fun RayCaster() {
    var frame by remember { mutableStateOf(ImageBitmap(W, H)) }

    var pistolOffset by remember { mutableStateOf(IntOffset(0, 0)) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        // background: floor and celling
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(brush = cellingBrush))
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(brush = floorBrush))
        }

        // walls
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = frame,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.None,
        )

        // pistol
        Image(
            modifier = Modifier.align(Alignment.BottomCenter).size(200.dp).offset(pistolOffset.x.dp, pistolOffset.y.dp),
            bitmap = useResource("pistol.webp") { loadImageBitmap(it) },
            contentDescription = null,
        )

//        Dpad(
//            modifier = Modifier.align(Alignment.BottomEnd),
//            onUp = { movePlayer(374199025664) },
//            onDown = { movePlayer(357019156480) },
//            onLeft = { movePlayer(279709745152) },
//            onRight = { movePlayer(292594647040) },
//            onCenter = { }
//        )
    }



    LaunchedEffect(Unit) {
        var timer = 0
        // game loop
        while (true) {
            val renderTime = measureTime {
                if (pressedKeys.isNotEmpty()) {
                    pistolOffset = IntOffset((20 * sin(player.x)).toInt(), (20 - 10 * sin(player.y)).toInt())
                }

                movePlayer(pressedKeys)
                frame = updateFrame(player)

            }
            delay((1000 - renderTime.toLong(DurationUnit.MILLISECONDS)) / FPS.toLong())

            println("render time: $renderTime")
        }
    }
}


private fun updateFrame(player: Player): ImageBitmap {
    val bitmap = BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB)

    castRays(player, bitmap)
    drawMap(bitmap, 10, H - CELL_SIZE * MAP_Y - 10, player)

    return bitmap.toComposeImageBitmap()
}


private fun drawPlayer(bitmap: BufferedImage, player: Player, xOffset: Int, yOffset: Int) {
    drawFilledRect(
        bitmap,
        xOffset + (player.x - PLAYER_SIZE / 2).toInt(),
        yOffset + (player.y - PLAYER_SIZE / 2).toInt(),
        PLAYER_SIZE.toInt(),
        PLAYER_SIZE.toInt(),
        Color.Yellow
    )
    drawLine(
        bitmap,
        xOffset + player.x.toInt(),
        yOffset + player.y.toInt(),
        (xOffset + player.x + cos(player.rotationRad.toDouble()).toFloat() * PLAYER_SIZE * 2).toInt(),
        (yOffset + player.y + sin(player.rotationRad.toDouble()).toFloat() * PLAYER_SIZE * 2).toInt(),
        Color.Yellow
    )
}

private fun drawMap(bitmap: BufferedImage, xOffset: Int, yOffset: Int, player: Player) {

    for (y: Int in 0 until MAP_Y) {
        for (x in 0 until MAP_X) {
            var color = Color.Black
            if (MAP[y * MAP_X + x] > 0) {
                color = Color.White
            } else {
                color = Color.Black
            }

            drawFilledRect(bitmap, xOffset + x * CELL_SIZE, yOffset + y * CELL_SIZE, CELL_SIZE, CELL_SIZE, color)
        }
    }

    drawPlayer(bitmap, player, xOffset = xOffset, yOffset = yOffset)

}

private fun drawFilledRect(bitmap: BufferedImage, x: Int, y: Int, w: Int, h: Int, color: Color) {
    for (i in x until x + w) {
        for (j in y until y + h) {
            bitmap.setRGB(i, j, color.toArgb())
        }
    }
}

private fun drawLine(bitmap: BufferedImage, x1: Int, y1: Int, x2: Int, y2: Int, color: Color) {
    val dx = abs(x2 - x1)
    val dy = abs(y2 - y1)

    val sx = if (x1 < x2) 1 else -1
    val sy = if (y1 < y2) 1 else -1

    var err = dx - dy

    var x = x1
    var y = y1

    while (true) {
        bitmap.setRGB(x, y, color.toArgb())

        if (x == x2 && y == y2) break

        val e2 = 2 * err
        if (e2 > -dy) {
            err -= dy
            x += sx
        }
        if (e2 < dx) {
            err += dx
            y += sy
        }
    }
}


// Ray casting using DDA algorithm. Cover walls with walltexture. Add fish-eye correction.
private fun castRays(player: Player, bitmap: BufferedImage) {
    val rayCount = W
    val rayStep = FOV_RAD / rayCount

    for (x in 0 until rayCount) {
        val rayAngle = player.rotationRad - FOV_RAD / 2 + x * rayStep

        val rayDirX = cos(rayAngle)
        val rayDirY = sin(rayAngle)

        var mapX = floor(player.x / CELL_SIZE).toInt()
        var mapY = floor(player.y / CELL_SIZE).toInt()

        val deltaDistX = abs(1 / rayDirX)
        val deltaDistY = abs(1 / rayDirY)

        var sideDistX: Float
        var sideDistY: Float

        val stepX: Int
        val stepY: Int

        if (rayDirX < 0) {
            stepX = -1
            sideDistX = (player.x / CELL_SIZE - mapX) * deltaDistX
        } else {
            stepX = 1
            sideDistX = (mapX + 1.0f - player.x / CELL_SIZE) * deltaDistX
        }

        if (rayDirY < 0) {
            stepY = -1
            sideDistY = (player.y / CELL_SIZE - mapY) * deltaDistY
        } else {
            stepY = 1
            sideDistY = (mapY + 1.0f - player.y / CELL_SIZE) * deltaDistY
        }

        var hit = false
        var side = 0

        while (!hit) {
            if (sideDistX < sideDistY) {
                sideDistX += deltaDistX
                mapX += stepX
                side = 0
            } else {
                sideDistY += deltaDistY
                mapY += stepY
                side = 1
            }

            if (mapX < 0 || mapX >= MAP_X || mapY < 0 || mapY >= MAP_Y) {
                hit = true
            } else if (MAP[mapY * MAP_X + mapX] > 0) {
                hit = true
            }
        }

        var perpWallDist: Float
        if (side == 0) {
            perpWallDist = (mapX - player.x / CELL_SIZE + (1 - stepX) / 2) / rayDirX
        } else {
            perpWallDist = (mapY - player.y / CELL_SIZE + (1 - stepY) / 2) / rayDirY
        }

        // Fish-eye correction
        perpWallDist *= cos(player.rotationRad - rayAngle)

        val lineHeight = (H / perpWallDist).toInt()

        val drawStart = (-lineHeight / 2 + H / 2).coerceAtLeast(0)
        val drawEnd = (lineHeight / 2 + H / 2).coerceAtMost(H - 1)

        // Texture mapping
        var wallX: Float
        if (side == 0) {
            wallX = player.y / CELL_SIZE + perpWallDist * rayDirY
        } else {
            wallX = player.x / CELL_SIZE + perpWallDist * rayDirX
        }
        wallX -= floor(wallX)

        var texX = (wallX * TEXTURE_SIZE).toInt()
        if ((side == 0 && rayDirX > 0) || (side == 1 && rayDirY < 0)) {
            texX = TEXTURE_SIZE - texX - 1
        }

        val step = TEXTURE_SIZE.toFloat() / lineHeight
        var texPos = (drawStart - H / 2 + lineHeight / 2) * step

        for (y in drawStart until drawEnd) {
            val texY = (texPos.toInt() and (TEXTURE_SIZE - 1))
            texPos += step

            val texIndex = (texY * TEXTURE_SIZE + texX) * 3
            val texColor = Color(
                wallTexture[texIndex] / 255f,
                wallTexture[texIndex + 1] / 255f,
                wallTexture[texIndex + 2] / 255f,
                1f
            )

            val intensity = 1.0f - ((perpWallDist / 20.0f) + 0.3f * side).coerceAtMost(0.8f)
            val color = darkenColor(texColor, intensity)

            bitmap.setRGB(x, y, color.toArgb())
        }
    }
}

// Helper function to darken a color based on intensity
private fun darkenColor(color: Color, intensity: Float): Color {
    return Color(
        red = (color.red * intensity).coerceIn(0f, 1f),
        green = (color.green * intensity).coerceIn(0f, 1f),
        blue = (color.blue * intensity).coerceIn(0f, 1f),
        alpha = 1f
    )
}

private fun isWall(x: Float, y: Float): Boolean {
    val mapX = (x / CELL_SIZE).toInt()
    val mapY = (y / CELL_SIZE).toInt()
    return if (mapX in 0 until MAP_X && mapY in 0 until MAP_Y) {
        MAP[mapY * MAP_X + mapX] == 1
    } else {
        true // Treat out-of-bounds as walls
    }
}


fun movePlayer(pressedKeys: Set<Long>) {
    var dx = 0f
    var dy = 0f
    var dr = 0f

    if (374199025664 in pressedKeys) { // Right arrow key
        dx += MOVE_STEP * cos(player.rotationRad)
        dy += MOVE_STEP * sin(player.rotationRad)
    }
    if (357019156480 in pressedKeys) { // Left arrow key
        dx -= MOVE_STEP * cos(player.rotationRad)
        dy -= MOVE_STEP * sin(player.rotationRad)
    }
    if (279709745152 in pressedKeys) { // Up arrow key
        dr -= ROTATION_STEP_RAD
    }
    if (292594647040 in pressedKeys) { // Down arrow key
        dr += ROTATION_STEP_RAD
    }

    // Apply rotation
    val newRotation = player.rotationRad + dr
    player = player.copy(rotationRad = newRotation)

    // Apply movement with collision detection
    val newX = player.x + dx
    val newY = player.y + dy

    // Check X movement
    if (!isWall(newX, player.y)) {
        player = player.copy(x = newX)
    }

    // Check Y movement
    if (!isWall(player.x, newY)) {
        player = player.copy(y = newY)
    }
}

data class Player(
    val x: Float,
    val y: Float,
    val rotationRad: Float
)

fun Float.toRadian(): Float = this * PI / 180f
fun Int.toRadian(): Float = this.toFloat() * PI / 180f




