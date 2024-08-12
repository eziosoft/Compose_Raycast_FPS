import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.delay
import kotlin.math.*


private const val W = 1000
private const val H = 800
private const val FPS = 30

private const val MAP_X = 8
private const val MAP_Y = 8
private const val CELL_SIZE = 30f
private const val PLAYER_SIZE = 5f

private val FOV_RAD = 60.toRadian()


private const val PI = 3.1415927f

private val ROTATION_STEP_RAD = 2f.toRadian()
private const val MOVE_STEP = 1

private val MAP = arrayOf(
    1, 1, 1, 1, 1, 1, 1, 1,
    1, 0, 1, 0, 0, 0, 1, 1,
    1, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 1, 0, 1, 0, 0, 1,
    1, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 1,
    1, 1, 1, 1, 1, 1, 1, 1,
)


private var player = Player(CELL_SIZE + CELL_SIZE / 2, CELL_SIZE + CELL_SIZE / 2, 90f.toRadian())

private val floorBrush = Brush.verticalGradient(
    0f to Color(0xFF000000),
    1f to Color(0xFFAAAAAA)
)

private val cellingBrush = Brush.verticalGradient(
    0f to Color(0xFFAAAAAA),
    1f to Color(0xFF000000)
)

private val white = Paint().apply {
    color = Color.White
    style = PaintingStyle.Fill
}

private val black = Paint().apply {
    color = Color.Black
    style = PaintingStyle.Fill
}

private val yellow = Paint().apply {
    color = Color.Yellow
    style = PaintingStyle.Fill
}

private val red = Paint().apply {
    color = Color.Red
    style = PaintingStyle.Fill
}


private val verticalColor = Color(0xFF0000FF)

private val horizontalColor = Color(0xFF0000AA)


@Composable
@Preview
fun App() {
    MaterialTheme {
        RayCaster()
    }
}

private val pressedKeys = mutableSetOf<Long>()

fun main() = singleWindowApplication(
    onKeyEvent = { event ->
        when (event.type) {
            KeyEventType.KeyDown -> pressedKeys.add(event.key.keyCode)
            KeyEventType.KeyUp -> pressedKeys.remove(event.key.keyCode)
        }
        movePlayer(pressedKeys)
        false
    }
) {
    App()
}

@Composable
private fun RayCaster() {
    var frame by remember { mutableStateOf(ImageBitmap(W, H)) }

    var pistolOffset by remember { mutableStateOf(0f) }

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
            modifier = Modifier.align(Alignment.BottomCenter).size(200.dp).offset(y = pistolOffset.toInt().dp),
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
        // game loop
        while (true) {

            if(pressedKeys.isNotEmpty()){
                pistolOffset = 20-10* sin((player.x+player.y)/10)
            }


            movePlayer(pressedKeys) // Call movePlayer every frame
            frame = updateFrame(player)
            delay(1000 / FPS.toLong())
        }
    }
}

private fun updateFrame(player: Player): ImageBitmap {
    val bitmap = ImageBitmap(W, H)
    val canvas = Canvas(bitmap)

    castRays(player, canvas)
    drawMap(canvas)
    drawPlayer(canvas, player)

    return bitmap
}

private fun drawPlayer(canvas: Canvas, player: Player) {
    canvas.drawRect(
        paint = yellow,
        rect = Rect(
            (player.x - PLAYER_SIZE / 2),
            (player.y - PLAYER_SIZE / 2),
            (player.x + PLAYER_SIZE / 2),
            (player.y + PLAYER_SIZE / 2)
        )
    )

    canvas.drawLine(
        p1 = Offset(player.x, player.y),
        p2 = Offset(
            (player.x + cos(player.rotationRad.toDouble()).toFloat() * PLAYER_SIZE * 2),
            (player.y + sin(player.rotationRad.toDouble()).toFloat() * PLAYER_SIZE * 2)
        ),
        paint = yellow
    )
}

private fun drawMap(canvas: Canvas) {
    for (y in 0 until MAP_Y) {
        for (x in 0 until MAP_X) {
            if (MAP[y * MAP_X + x] == 1) {
                canvas.drawRect(
                    paint = white,
                    rect = Rect(
                        (x * CELL_SIZE),
                        (y * CELL_SIZE),
                        ((x + 1) * CELL_SIZE),
                        ((y + 1) * CELL_SIZE)
                    )
                )
            } else {
                canvas.drawRect(
                    paint = black,
                    rect = Rect(
                        (x * CELL_SIZE),
                        (y * CELL_SIZE),
                        ((x + 1) * CELL_SIZE),
                        ((y + 1) * CELL_SIZE)
                    )
                )
            }
        }
    }
}

// Ray casting using DDA algorithm. Draw vertical and horizontal walls in different colors. Add fish-eye correction.
private fun castRays(player: Player, canvas: Canvas) {
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
        val intensity = (1 - (correctedDist / (MAP_X))).coerceIn(0f, 1f)

        val paint: Paint = if (side == 0) {
            Paint().apply {
                color = darkenColor(verticalColor, intensity)
            }

        } else {
            Paint().apply {
                color = darkenColor(horizontalColor, intensity)
            }
        }


        canvas.drawRect(
            paint = paint,
            rect = Rect(
                i.toFloat(),
                drawStart.toFloat(),
                i.toFloat() + 1,
                drawEnd.toFloat()
            )
        )
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


private fun movePlayer(pressedKeys: Set<Long>) {
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




