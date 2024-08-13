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
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.lang.Math.random
import kotlin.math.abs
import kotlin.math.cos
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

private var wallTexture = arrayOf(1, 0, 1, 0, 1, 0, 2, 0)


private var player = Player(CELL_SIZE + CELL_SIZE / 2f, CELL_SIZE + CELL_SIZE / 2f, 0f.toRadian())

private val floorBrush = Brush.verticalGradient(
    0f to Color(0xFF000000),
    1f to Color(0xFF777755)
)

private val cellingBrush = Brush.verticalGradient(
    0f to Color(0xFF335555),
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
                if (timer < 5) {
                    wallTexture[6] = 2
                } else {
                    wallTexture[6] = 0
                }

                if (timer > 10) timer = 0 else timer++


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


// Ray casting using DDA algorithm. Draw vertical and horizontal walls in different colors. Add fish-eye correction.
private fun castRays(player: Player, bitmap: BufferedImage) {
    val rayCount = W
    val rayStep = FOV_RAD / rayCount

    for (i in 0 until rayCount) {
        val rayAngle = player.rotationRad - FOV_RAD / 2 + i * rayStep

        val sinAngle = sin(rayAngle)
        val cosAngle = cos(rayAngle)

        var mapX = player.x.toInt() / CELL_SIZE.toInt()
        var mapY = player.y.toInt() / CELL_SIZE.toInt()

        val deltaDistX = abs(1 / cosAngle)
        val deltaDistY = abs(1 / sinAngle)

        var sideDistX: Float
        var sideDistY: Float
        val stepX: Int
        val stepY: Int


        if (cosAngle < 0) {
            stepX = -1
            sideDistX = (player.x / CELL_SIZE - mapX) * deltaDistX
        } else {
            stepX = 1
            sideDistX = (mapX + 1.0f - player.x / CELL_SIZE) * deltaDistX
        }

        if (sinAngle < 0) {
            stepY = -1
            sideDistY = (player.y / CELL_SIZE - mapY) * deltaDistY
        } else {
            stepY = 1
            sideDistY = (mapY + 1.0f - player.y / CELL_SIZE) * deltaDistY
        }

        var hit = false
        var side = 0 // 0 for vertical wall, 1 for horizontal wall

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

            if (MAP[mapY * MAP_X + mapX] > 0) hit = true

        }

        var perpWallDist: Float
        if (side == 0) {
            perpWallDist = (mapX - player.x / CELL_SIZE + (1 - stepX) / 2) / cosAngle
        } else {
            perpWallDist = (mapY - player.y / CELL_SIZE + (1 - stepY) / 2) / sinAngle
        }

        // Fish-eye correction
        val correctedDist = perpWallDist * cos(rayAngle - player.rotationRad)

        val lineHeight = (H / correctedDist).toInt()
        val drawStart = -lineHeight / 2 + H / 2
        val drawEnd = lineHeight / 2 + H / 2

        // Calculate color intensity based on distance
        val intensity = if (side == 0) {
            (1 - (correctedDist * correctedDist / (MAP_X))).coerceIn(0f, 1f)
        } else {
            (0.8f - (correctedDist * correctedDist / (MAP_Y))).coerceIn(0f, 1f)
        }


        val scale = lineHeight / (wallTexture.size)


        for (y in 0 until drawEnd - drawStart) {
            val c = (y / scale).coerceIn(0, wallTexture.size - 1)

            val color =
                if (wallTexture[c] == 1) darkenColor(Color.Gray, intensity) else
                    if (wallTexture[c] == 2) darkenColor(Color.Red, intensity) else Color.Black

            val xx = i
            val yy = drawStart + y

            if (xx in 0 until W && yy in 0 until H)
                bitmap.setRGB(xx, yy, color.toArgb())
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




