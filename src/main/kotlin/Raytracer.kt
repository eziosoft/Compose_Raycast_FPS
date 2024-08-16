import androidx.compose.foundation.Image
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
import kotlin.math.*
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

const val TEXTURE_SIZE = 32

// load textures
private val wallTexture = readPpmImage("wall.ppm")
private val floorTexture = readPpmImage("floor.ppm")
private val enemyTexture = readPpmImage("enemy.ppm")

private val enemy00= selectFrame(enemyTexture, 0, 0, TEXTURE_SIZE, 8 * TEXTURE_SIZE)


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


private var player =
    Player(
        x = CELL_SIZE + CELL_SIZE / 2f,
        y = CELL_SIZE + CELL_SIZE / 2f,
        z = 0f,
        rotationRad = 0f.toRadian()
    )

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
            Image(
                modifier = Modifier.fillMaxSize().aspectRatio(1f)
                    .graphicsLayer(
                        scaleX = 20f,
                        scaleY = 20f,
                        rotationX = -25f,
                        rotationZ = (player.rotationRad * 180 / PI),
                        translationY = -H * 2.toFloat()
                    ),
                bitmap = useResource("sky4.jpg") { loadImageBitmap(it) },
                contentDescription = null,
                contentScale = ContentScale.FillBounds,

                )
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

//            println("render time: $renderTime")
        }
    }
}


private fun updateFrame(player: Player): ImageBitmap {
    val bitmap = BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB)

    castRays(player, bitmap)
    drawMap(bitmap, 10, H - CELL_SIZE * MAP_Y - 10, player, enemy)
    drawSprite(bitmap, player, enemy)

    return bitmap.toComposeImageBitmap()
}


private fun drawPlayer(bitmap: BufferedImage, player: Player, xOffset: Int, yOffset: Int, color: Color = Color.Yellow) {
    drawFilledRect(
        bitmap,
        xOffset + (player.x - PLAYER_SIZE / 2).toInt(),
        yOffset + (player.y - PLAYER_SIZE / 2).toInt(),
        PLAYER_SIZE.toInt(),
        PLAYER_SIZE.toInt(),
        color
    )
    drawLine(
        bitmap,
        xOffset + player.x.toInt(),
        yOffset + player.y.toInt(),
        (xOffset + player.x + cos(player.rotationRad.toDouble()).toFloat() * PLAYER_SIZE * 2).toInt(),
        (yOffset + player.y + sin(player.rotationRad.toDouble()).toFloat() * PLAYER_SIZE * 2).toInt(),
        color
    )
}

private fun drawMap(bitmap: BufferedImage, xOffset: Int, yOffset: Int, player: Player, enemy: Player) {

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
    drawPlayer(bitmap, enemy, xOffset = xOffset, yOffset = yOffset, color = Color.Red)

}

val enemy = Player(3f * CELL_SIZE, 3f * CELL_SIZE, 100f, 0f)

// draws square in 3d world using 3d projection mapping
private fun drawSprite(bitmap: BufferedImage, player: Player, enemy: Player) {
    val pointHeight = CELL_SIZE // Height of the square in world units

    // Calculate vector from player to enemy
    val dx = enemy.x - player.x
    val dy = enemy.y - player.y

    // Calculate distance to enemy
    val distance = sqrt(dx * dx + dy * dy)

    // Calculate angle to enemy relative to player's rotation
    var angle = atan2(dy, dx) - player.rotationRad

    // Normalize angle to be between -PI and PI
    angle = (angle + PI) % (2 * PI) - PI

    // Check if enemy is within player's FOV
    if (abs(angle) < FOV_RAD / 2) {
        // Calculate screen x-coordinate
        val screenX = ((W / 2) * (1 + angle / (FOV_RAD / 2))).toInt()

        // Calculate perceived height of the square
        val perceivedHeight = (H / distance * pointHeight).toInt()

        // Calculate top and bottom y-coordinates
        val topY = (H / 2 - perceivedHeight / 2).coerceIn(0, H - 1)
        val bottomY = (H / 2 + perceivedHeight / 2).coerceIn(0, H - 1)

        // Calculate perceived width of the square
        val perceivedWidth = perceivedHeight //(perceivedHeight * pointSize).toInt()

        // Draw the textured square
        for (y in topY..bottomY) {
            for (x in (screenX - perceivedWidth / 2)..(screenX + perceivedWidth / 2)) {
                if (x >= 0 && x < W) {
                    // Calculate texture coordinates
                    val texX =
                        ((x - (screenX - perceivedWidth / 2)).toFloat() / perceivedWidth * TEXTURE_SIZE).toInt() % TEXTURE_SIZE
                    val texY = ((y - topY).toFloat() / perceivedHeight * TEXTURE_SIZE).toInt() % TEXTURE_SIZE

                    val texIndex = (texY * TEXTURE_SIZE + texX) * 3

                    val texture = enemy00

                    val texColor = Color(
                        texture[texIndex] / 255f,
                        texture[texIndex + 1] / 255f,
                        texture[texIndex + 2] / 255f,
                        1f
                    )

                    // Apply distance-based shading
                    val intensity = (1.0f - (distance / 20.0f)).coerceIn(0.2f, 1f)
                    val shadedColor = darkenColor(texColor, intensity)

                    if (texColor.red != 152 / 255f && texColor.green != 0f && texColor.blue != 136 / 255f) {
                        bitmap.setRGB(x, y, shadedColor.toArgb())
                    }
                }
            }
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

        // Texture mapping for walls
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

            val intensity = 1.0f - ((perpWallDist / 20.0f) + 0.4f * side).coerceAtMost(1f)
            val color = darkenColor(texColor, intensity)

            bitmap.setRGB(x, y, color.toArgb())
        }

        // Floor casting
        if (drawEnd < H) {
            for (y in drawEnd until H) {
                val floorDistance = H.toFloat() / (2.0f * y - H)

                val floorX = player.x / CELL_SIZE + floorDistance * rayDirX
                val floorY = player.y / CELL_SIZE + floorDistance * rayDirY

                val floorTexX = (floorX * TEXTURE_SIZE % TEXTURE_SIZE).toInt()
                val floorTexY = (floorY * TEXTURE_SIZE % TEXTURE_SIZE).toInt()

                val texIndex = (floorTexY * TEXTURE_SIZE + floorTexX) * 3
                val texColor = Color(
                    floorTexture[texIndex] / 255f,
                    floorTexture[texIndex + 1] / 255f,
                    floorTexture[texIndex + 2] / 255f,
                    1f
                )

                val color = darkenColor(texColor, 0.5f) // Apply some darkness to the floor
                bitmap.setRGB(x, y, color.toArgb())
            }
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
    val z: Float,
    val rotationRad: Float
)

fun Float.toRadian(): Float = this * PI / 180f
fun Int.toRadian(): Float = this.toFloat() * PI / 180f

private fun drawFilledRect(bitmap: BufferedImage, x: Int, y: Int, w: Int, h: Int, color: Color) {
    for (i in x until x + w) {
        for (j in y until y + h) {
            if (i < 0 || i >= bitmap.width || j < 0 || j >= bitmap.height) {
                continue
            }

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



