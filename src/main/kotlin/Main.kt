import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.delay
import kotlin.math.*



const val W = 640
const val H = 480
const val FPS = 30

const val MAP_X = 8
const val MAP_Y = 8
const val MAP_SCALE = 15f
const val PLAYER_SIZE = 10.0

const val ROTATION_STEP_DEGREE = 2f
const val MOVE_STEP = 1

val MAP = arrayOf(
    1, 1, 1, 1, 1, 1, 1, 1,
    1, 0, 1, 1, 0, 0, 0, 1,
    1, 0, 1, 1, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 1, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 1,
    1, 1, 1, 1, 1, 1, 1, 1,
)

val white = Paint().apply {
    color = Color.White
    style = PaintingStyle.Fill
}

val black = Paint().apply {
    color = Color.Black
    style = PaintingStyle.Fill
}

var player = Player(50.0, 50.0, 0f)

@Composable
@Preview
fun App() {
    var frame by remember { mutableStateOf(ImageBitmap(W, H)) }

    MaterialTheme {
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = frame,
            contentDescription = "",
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            frame = updateFrame(player)
            delay(1000 / FPS.toLong())
        }
    }
}

fun updateFrame(player: Player): ImageBitmap {
    val bitmap = ImageBitmap(W, H)
    val canvas = Canvas(bitmap)

    canvas.drawRect(
        paint = black,
        rect = Rect(0f, 0f, W.toFloat(), H.toFloat())
    )

    drawCeilingAndFloor(canvas)
    drawRays(player, canvas)
    drawMap(canvas)
    drawPlayer(canvas, player)

    return bitmap
}

fun drawPlayer(canvas: Canvas, player: Player) {
    val paint = Paint().apply {
        color = Color.Yellow
        style = PaintingStyle.Fill
    }

    canvas.drawRect(
        paint = paint,
        rect = Rect(
            (player.x - PLAYER_SIZE / 2).toFloat(),
            (player.y - PLAYER_SIZE / 2).toFloat(),
            (player.x + PLAYER_SIZE / 2).toFloat(),
            (player.y + PLAYER_SIZE / 2).toFloat()
        )
    )

    canvas.drawLine(
        p1 = Offset(player.x.toFloat(), player.y.toFloat()),
        p2 = Offset(
            (player.x + cos(player.rotationRad.toDouble()).toFloat() * 20).toFloat(),
            (player.y + sin(player.rotationRad.toDouble()).toFloat() * 20).toFloat()
        ),
        paint = paint
    )
}

fun drawMap(canvas: Canvas) {
    for (y in 0 until MAP_Y) {
        for (x in 0 until MAP_X) {
            val paint = if (MAP[y * MAP_X + x] == 1) white else black
            canvas.drawRect(
                paint = paint,
                rect = Rect(x * MAP_SCALE, y * MAP_SCALE, (x + 1) * MAP_SCALE, (y + 1) * MAP_SCALE)
            )
        }
    }
}

fun drawRays(player: Player, canvas: Canvas) {
    val rayLength = 1000f
    val numRays = W
    val fov = 30.0
    val angleStep = Math.toRadians(fov / numRays)
    val playerAngle = player.rotationRad
    val aspectRatio = H.toFloat() / W.toFloat()

    for (i in 0 until numRays) {
        val rayAngle = playerAngle - Math.toRadians(fov / 2) + i * angleStep

        var rayX = player.x
        var rayY = player.y
        var hit = false
        var distToWall = 0.0

        val deltaX = cos(rayAngle).toFloat()
        val deltaY = sin(rayAngle).toFloat()

        while (!hit && rayX in 0f..(W * MAP_SCALE) && rayY in 0f..(H * MAP_SCALE) && distToWall < rayLength) {
            val mapX = (rayX / MAP_SCALE).toInt()
            val mapY = (rayY / MAP_SCALE).toInt()

            if (mapX in 0 until MAP_X && mapY in 0 until MAP_Y && MAP[mapY * MAP_X + mapX] == 1) {
                hit = true
            } else {
                rayX += deltaX
                rayY += deltaY
                distToWall = sqrt((rayX - player.x).pow(2) + (rayY - player.y).pow(2))
            }
        }

        val correctedDistToWall = distToWall * cos(rayAngle - playerAngle).toFloat()

        val wallHeight = (H / (correctedDistToWall * aspectRatio) * 10f).toFloat()
        val wallTop = (H * 0.5f - wallHeight / 2)
        val wallBottom = (H * 0.5f + wallHeight / 2)

        val clampedWallTop = wallTop.coerceAtLeast(0f)
        val clampedWallBottom = wallBottom.coerceAtMost(H.toFloat())

        val brightnessFactor = (correctedDistToWall / 100).coerceIn(0.2, 0.5).toFloat()
        val wallColor = Color(0.5f - brightnessFactor, 0.5f - brightnessFactor, 0.5f - brightnessFactor)

        canvas.drawRect(
            paint = Paint().apply {
                color = wallColor
                style = PaintingStyle.Fill
            },
            rect = Rect(i.toFloat(), clampedWallTop, i.toFloat() + 1f, clampedWallBottom)
        )
    }
}

fun drawCeilingAndFloor(canvas: Canvas) {
    for (y in 0 until H) {
        val brightness = ((abs(H / 2 - y) / (H / 2f)) - 0.3f).coerceIn(0f, 1f)
        val color = Color(brightness, brightness, brightness)

        canvas.drawRect(
            paint = Paint().apply {
                this.color = color
                style = PaintingStyle.Fill
            },
            rect = Rect(0f, y.toFloat(), W.toFloat(), y.toFloat() + 1)
        )
    }
}

fun main() = singleWindowApplication(
    onKeyEvent = {
        movePlayer(it.key.keyCode)
        false
    }
) {
    App()
}

fun movePlayer(keyCode: Long) {
    when (keyCode) {
        374199025664 -> { // Right arrow key
            player = player.copy(
                x = player.x + (MOVE_STEP * cos(player.rotationRad.toDouble())),
                y = player.y + (MOVE_STEP * sin(player.rotationRad.toDouble()))
            )
        }

        357019156480 -> { // Left arrow key
            player = player.copy(
                x = player.x - (MOVE_STEP * cos(player.rotationRad.toDouble())),
                y = player.y - (MOVE_STEP * sin(player.rotationRad.toDouble()))
            )
        }

        279709745152 -> { // Up arrow key
            player = player.copy(rotationRad = player.rotationRad - ROTATION_STEP_DEGREE.toRadian())
        }

        292594647040 -> { // Down arrow key
            player = player.copy(rotationRad = player.rotationRad + ROTATION_STEP_DEGREE.toRadian())
        }
    }
}

data class Player(
    val x: Double,
    val y: Double,
    val rotationRad: Float
)

fun Float.toRadian(): Float = (this / 180f * Math.PI).toFloat()
fun Double.toRadian(): Double = (this / 180 * Math.PI)
fun Float.toDegree(): Float = (this * 180f / Math.PI).toFloat()

