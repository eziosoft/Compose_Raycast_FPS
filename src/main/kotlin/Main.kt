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
const val MAP_SCALE = 30f
const val PLAYER_SIZE = 10.0

const val ROTATION_STEP_DEGREE = 1f
const val MOVE_STEP = 1

val MAX_FLOOR_CEILING_HEIGHT = 1000f // Maximum height for shading
val MIN_FLOOR_CEILING_HEIGHT = 0f // Minimum height for shading
val CEILING_COLOR = Color.LightGray
val FLOOR_COLOR = Color.DarkGray

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

var player = Player(100.0, 100.0, 0f)

@Composable
@Preview
fun App() {
    var frame by mutableStateOf(ImageBitmap(W, H))

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
        println("LaunchedEffect")
        while (true) {
            frame = updateFrame(player = player)
            delay(1000 / FPS.toLong())
        }
    }
}

fun updateFrame(player: Player): ImageBitmap {
    val bitmap = ImageBitmap(W, H)
    val canvas = Canvas(bitmap)

    drawMap(canvas)
    drawCeilingAndFloor(player, canvas)
    drawRays(player, canvas)
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
    val white = Paint().apply {
        color = Color.White
        style = PaintingStyle.Fill
    }

    val black = Paint().apply {
        color = Color.Black
        style = PaintingStyle.Fill
    }

    val gray = Paint().apply {
        color = Color.Gray
        style = PaintingStyle.Fill
    }

    canvas.drawRect(
        paint = black,
        rect = Rect(0f, 0f, W.toFloat(), H.toFloat())
    )

    for (y in 0 until MAP_Y) {
        for (x in 0 until MAP_X) {
            if (MAP[y * MAP_X + x] == 1) {
                canvas.drawRect(
                    paint = white,
                    rect = Rect(x * MAP_SCALE, y * MAP_SCALE, (x + 1) * MAP_SCALE - 1, (y + 1) * MAP_SCALE - 1)
                )
            } else {
                canvas.drawRect(
                    paint = black,
                    rect = Rect(x * MAP_SCALE, y * MAP_SCALE, (x + 1) * MAP_SCALE - 1, (y + 1) * MAP_SCALE - 1)
                )
            }
        }
    }
}

fun drawRays(player: Player, canvas: Canvas) {
    val rayLength = 1000f // Maximum length of the ray
    val numRays = W * 2 // Number of rays to cast
    val fov = 30.0 // Field of view in degrees
    val angleStep = Math.toRadians(fov / numRays) // Angle between each ray in radians
    val playerAngle = player.rotationRad

    // Aspect ratio correction factor
    val aspectRatio = H.toFloat() / W.toFloat()

    // Define the maximum brightness and minimum brightness factors
    val maxBrightness = 1.0
    val minBrightness = 0.2 // Minimum brightness factor for the farthest wall

    for (i in 0 until numRays) {
        // Calculate the angle of the ray relative to the player's direction
        val rayAngle = playerAngle - Math.toRadians(fov / 2) + i * angleStep

        var rayX = player.x
        var rayY = player.y
        var hit = false
        var distToWall = 0.0
        var isVertical = false

        val deltaX = cos(rayAngle).toFloat()
        val deltaY = sin(rayAngle).toFloat()

        while (!hit && rayX in 0f..(W * MAP_SCALE) && rayY in 0f..(H * MAP_SCALE) && distToWall < rayLength) {
            val mapX = (rayX / MAP_SCALE).toInt()
            val mapY = (rayY / MAP_SCALE).toInt()

            if (mapX in 0 until MAP_X && mapY in 0 until MAP_Y && MAP[mapY * MAP_X + mapX] == 1) {
                hit = true

                // Check if hit is vertical or horizontal
                val nextX = rayX + deltaX
                val nextY = rayY + deltaY
                isVertical = abs(nextX - rayX) < abs(nextY - rayY)
            } else {
                rayX += deltaX
                rayY += deltaY
                distToWall = sqrt((rayX - player.x).pow(2) + (rayY - player.y).pow(2))
            }
        }

        // Apply fisheye correction
        val correctedDistToWall = distToWall * cos(rayAngle - playerAngle).toFloat()

        // Calculate the height of the wall based on the corrected distance
        val wallHeight = (H / (correctedDistToWall * aspectRatio)).toFloat() // Scaling with aspect ratio

        val wallTop = (H * 0.5f - wallHeight / 2).toInt()
        val wallBottom = (H * 0.5f + wallHeight / 2).toInt()

        // Ensure that the height is within the screen bounds
        val clampedWallTop = wallTop.coerceAtLeast(0)
        val clampedWallBottom = wallBottom.coerceAtMost(H)

        // Calculate the brightness factor based on the distance
        val brightnessFactor =
            maxBrightness - (correctedDistToWall / rayLength * (maxBrightness - minBrightness)).coerceIn(
                minBrightness,
                maxBrightness
            )

        // Create a paint object with adjusted color brightness
        val wallColor = if (isVertical) Color.Gray else Color.DarkGray
        val adjustedColor = wallColor.copy(alpha = brightnessFactor.toFloat())

        canvas.drawRect(
            paint = Paint().apply {
                color = adjustedColor
                style = PaintingStyle.Fill
            },
            rect = Rect(i.toFloat(), clampedWallTop.toFloat(), i.toFloat() + 1, clampedWallBottom.toFloat())
        )
    }
}


fun drawCeilingAndFloor(player: Player, canvas: Canvas) {
    // draw gradient rectangle sky

    for (y in 0 until H / 2) {
        val brightness = 1 - (y / (H / 2).toFloat())
        val color = Color(0.5f, 0.5f, 0.5f, brightness)
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
    },
) {
    App()
}

fun movePlayer(keyCode: Long) {
    when (keyCode) {
        374199025664 -> {
            player = player.copy(
                x = player.x + (MOVE_STEP * cos(player.rotationRad.toDouble())),
                y = player.y + (MOVE_STEP * sin(player.rotationRad.toDouble()))
            )
        }

        357019156480 -> {
            player = player.copy(
                x = player.x - (MOVE_STEP * cos(player.rotationRad.toDouble())),
                y = player.y - (MOVE_STEP * sin(player.rotationRad.toDouble()))
            )
        }

        279709745152 -> {
            player = player.copy(rotationRad = player.rotationRad - ROTATION_STEP_DEGREE.toRadian())
        }

        292594647040 -> {
            player = player.copy(rotationRad = player.rotationRad + ROTATION_STEP_DEGREE.toRadian())
        }
    }
}

data class Player(
    val x: Double,
    val y: Double,
    val rotationRad: Float,
)

fun Float.toRadian(): Float = (this / 180f * Math.PI).toFloat()
fun Double.toRadian(): Double = (this / 180 * Math.PI)
fun Float.toDegree(): Float = (this * 180f / Math.PI).toFloat()
