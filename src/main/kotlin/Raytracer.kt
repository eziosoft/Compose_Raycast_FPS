import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import models.Player
import models.animate
import models.distanceTo
import models.walkRandom
import java.awt.image.BufferedImage
import kotlin.math.*
import kotlin.time.DurationUnit
import kotlin.time.measureTime


val pressedKeys = mutableSetOf<Long>()

private const val W = 640
private const val H = 480
private const val FPS = 30

private val FOV_RAD = 60.toRadian()
private const val MOVE_STEP = 0.2f
private val ROTATION_STEP_RAD = 2f.toRadian()

const val TEXTURE_SIZE = 64 //walls, floor and ceiling textures

private const val CELL_SIZE = 2
private const val PLAYER_SIZE = 5f // square on the map

// load textures

val floorTexture = readPpmImage("floor.ppm")


private val wallDepths = FloatArray(W) // depth buffer

private val playerPosition =
    findPositionBasedOnMapIndex(map = MAP, mapX = MAP_X, mapY = MAP_Y, cellSize = CELL_SIZE, index = MAP.indexOf(-1))
private val player =
    Player(
        x = playerPosition.first,
        y = playerPosition.second,
        z = 0f,
        rotationRad = -90f.toRadian().normalizeAngle()
    )

private val enemies = getEnemies()


fun findPositionBasedOnMapIndex(map: IntArray, mapX: Int, mapY: Int, cellSize: Int, index: Int): Pair<Float, Float> {
    val x = (index % mapX) * cellSize + cellSize / 2f
    val y = (index / mapX) * cellSize + cellSize / 2f
    return Pair(x, y)
}

fun generateRandomEnemies(): List<Player> {
    val enemies = mutableListOf<Player>()
    for (i in 0 until 10) {
        enemies.add(
            Player(
                x = CELL_SIZE * (1 + (Math.random() * (MAP_X - 2)).toInt()) + CELL_SIZE / 2f,
                y = CELL_SIZE * (1 + (Math.random() * (MAP_Y - 2)).toInt()) + CELL_SIZE / 2f,
                z = 0f,
                rotationRad = 0.toRadian()
            )
        )
    }
    return enemies
}

fun getEnemies(): List<Player> {
    val enemies = mutableListOf<Player>()
    MAP.forEachIndexed { index, value ->
        if (value == -2) {
            val position = findPositionBasedOnMapIndex(MAP, MAP_X, MAP_Y, CELL_SIZE, index)
            enemies.add(Player(x = position.first, y = position.second, z = 0f, rotationRad = 0f))
        }
    }
    return enemies
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RayCaster() {
    var frame by remember { mutableStateOf(ImageBitmap(W, H)) }

    var pistolOffset by remember { mutableStateOf(IntOffset(0, 0)) }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerMoveFilter(
            onMove = {
                player.rotationRad = (it.x / 200f).normalizeAngle()
                false
            }
        ), contentAlignment = Alignment.Center) {

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
    }



    LaunchedEffect(Unit) {
        // game loop
        while (true) {
            val renderTime = measureTime {

                enemies.forEach { enemy ->
                    enemy.walkRandom(MAP, MAP_X, MAP_Y, CELL_SIZE)
                    enemy.animate()
                }

                if (pressedKeys.isNotEmpty()) {
                    pistolOffset = IntOffset((20 * sin(player.x)).toInt(), (20 - 10 * sin(player.y)).toInt())
                }

                movePlayer(pressedKeys)
                frame = generateFrame()

            }
            delay((1000 - renderTime.toLong(DurationUnit.MILLISECONDS)) / FPS.toLong())

//            println("render time: $renderTime")
        }
    }
}


private fun generateFrame(): ImageBitmap {
    val bitmap = BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB)

    castRays(player, bitmap)
    drawMap(bitmap, 10, H - CELL_SIZE * MAP_Y - 10, player)

    enemies.sortedByDescending { it.distanceTo(player) }.forEach { enemy ->
        drawSprite(bitmap, player, enemy, wallDepths)
    }

    return bitmap.toComposeImageBitmap()
}


private fun drawPlayerOnMap(
    bitmap: BufferedImage,
    player: Player,
    xOffset: Int,
    yOffset: Int,
    color: Color = Color.Yellow
) {
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

    drawPlayerOnMap(bitmap, player, xOffset = xOffset, yOffset = yOffset)

    enemies.forEach { enemy ->
        drawPlayerOnMap(bitmap, enemy, xOffset = xOffset, yOffset = yOffset, color = Color.Red)
    }
}


// draws square in 3d world using 3d projection mapping
private fun drawSprite(bitmap: BufferedImage, player: Player, enemy: Player, wallDepths: FloatArray) {
    // Calculate the angle from the enemy to the player
    val angleToPlayer = atan2(player.y - enemy.y, player.x - enemy.x)

    // Calculate the difference between the enemy's rotation and the angle to the player
    var diff = angleToPlayer - enemy.rotationRad

    // Normalize the angle difference to be between -PI and PI
    diff = (diff + PI) % (2 * PI) - PI

    // Calculate the texture index, ensuring it falls within the valid range
    val numTextures = 8  // Assuming there are 8 textures in the textureSet
    val textureIndex = numTextures - ((diff / (2 * PI) * numTextures) + numTextures) % numTextures

    // Fetch the correct texture for rendering
    val texture = getGuardTexture(direction = textureIndex.toInt(), walking = true, walkingFrame = enemy.walkingFrame)

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
        val perceivedWidth = perceivedHeight

        // Draw the textured square
        for (y in topY..bottomY) {
            for (x in (screenX - perceivedWidth / 2)..(screenX + perceivedWidth / 2)) {
                if (x >= 0 && x < W) {
                    // Only draw the sprite pixel if it's closer than the wall
//                    println("$distance ${wallDepths[x]}")

                    if (wallDepths[x] - (distance / CELL_SIZE) > -0.1) {
                        // Calculate texture coordinates
                        val texX =
                            ((x - (screenX - perceivedWidth / 2)).toFloat() / perceivedWidth * SPRITE_SIZE).toInt() % SPRITE_SIZE
                        val texY = ((y - topY).toFloat() / perceivedHeight * SPRITE_SIZE).toInt() % SPRITE_SIZE

                        val texIndex = (texY * SPRITE_SIZE + texX) * 3

                        val texColor = Color(
                            texture[texIndex] / 255f,
                            texture[texIndex + 1] / 255f,
                            texture[texIndex + 2] / 255f,
                            1f
                        )



                        if (texColor != TRANSPARENT_COLOR) {
                            // Apply distance-based shading
                            val intensity = (1.0f - (distance / 30.0f)).coerceIn(0.2f, 1f)
                            val shadedColor = darkenColor(texColor, intensity)

                            bitmap.setRGB(x, y, shadedColor.toArgb())
                        }
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

        var wallTextureIndex: Int = 0

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


//            if (mapX < 0 || mapX >= MAP_X || mapY < 0 || mapY >= MAP_Y) {
//                hit = true
//            } else
            if (MAP[mapY * MAP_X + mapX] > 0) {
                hit = true
                wallTextureIndex = MAP[mapY * MAP_X + mapX]
            }
        }

        var perpWallDist: Float
        if (side == 0) {
            perpWallDist = (mapX - player.x / CELL_SIZE + (1 - stepX) / 2) / rayDirX
        } else {
            perpWallDist = (mapY - player.y / CELL_SIZE + (1 - stepY) / 2) / rayDirY
        }

        wallDepths[x] = perpWallDist

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


        val wallTexture = wallTextures[wallTextureIndex] ?: error("Wall texture not found $wallTextureIndex")

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
                if (texIndex > 0) {
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


fun movePlayer(pressedKeys: Set<Long>) {
//    println("pressedKeys: $pressedKeys")
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

    if (348429221888 in pressedKeys) { // A key
        dx -= MOVE_STEP * cos(player.rotationRad + 90.toRadian())
        dy -= MOVE_STEP * sin(player.rotationRad + 90.toRadian())
    }

    if (296889614336 in pressedKeys) { // D key
        dx += MOVE_STEP * cos(player.rotationRad + 90.toRadian())
        dy += MOVE_STEP * sin(player.rotationRad + 90.toRadian())
    }

    if (279709745152 in pressedKeys) { // Up arrow key
        dr -= ROTATION_STEP_RAD
    }
    if (292594647040 in pressedKeys) { // Down arrow key
        dr += ROTATION_STEP_RAD
    }

    // Apply rotation
    val newRotation = player.rotationRad + dr
    player.rotationRad = newRotation.normalizeAngle()

    // Apply movement with collision detection
    val newX = player.x + dx
    val newY = player.y + dy

    // Check X movement
    if (!isWall(newX, player.y, MAP, MAP_X, MAP_Y, CELL_SIZE)) {
        player.x = newX
    }

    // Check Y movement
    if (!isWall(player.x, newY, MAP, MAP_X, MAP_Y, CELL_SIZE)) {
        player.y = newY
    }
}






