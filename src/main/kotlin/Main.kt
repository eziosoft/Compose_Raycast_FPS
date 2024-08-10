import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin


const val W = 320
const val H = 240
const val FPS = 30

const val MAP_X = 8
const val MAP_Y = 8
const val MAP_SCALE = 30f
const val PLAYER_SIZE = 10f

const val ROTATION_STEP_DEGREE = 5f
const val MOVE_STEP = 5

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

var player = Player(100, 100, 0f)


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


//@Composable
fun updateFrame(player: Player): ImageBitmap {
    val bitmap = ImageBitmap(W, H)
    val canvas = Canvas(bitmap)

    drawMap(canvas)
    drawPlayer(canvas, player)

    drawRays(player, canvas)

    return bitmap
}

fun drawPlayer(canvas: Canvas, player: Player) {
    canvas.drawRect(
        paint = Paint().apply {
            color = Color.Yellow
            style = PaintingStyle.Fill
        },
        rect = Rect(player.x.toFloat(), player.y.toFloat(), player.x + PLAYER_SIZE, player.y + PLAYER_SIZE)
    )

    canvas.drawLine(
        p1 = Offset(player.x + PLAYER_SIZE / 2, player.y + PLAYER_SIZE / 2),
        p2 = Offset(
            player.x + PLAYER_SIZE / 2 + 20 * cos(player.rotation.toDouble()).toFloat(),
            player.y + PLAYER_SIZE / 2 + 20 * sin(player.rotation.toDouble()).toFloat()
        ),
        paint = Paint().apply {
            color = Color.Red
            style = PaintingStyle.Stroke
            strokeWidth = 2f
        })

}

fun drawMap(canvas: Canvas): Canvas {
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
        paint = gray,
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

    return canvas
}

fun drawRays(player: Player, canvas: Canvas) {
    val rayLength = 1000
    val rayWidth = 1
    val rayStep = 0.1f

    for (i in 0 until W) {
        val angle = player.rotation - 30f.toRadian() + i * 0.1f.toRadian()
        val ray = castRay(player, angle, rayLength)
        canvas.drawLine(
            p1 = Offset(player.x + PLAYER_SIZE / 2, player.y + PLAYER_SIZE / 2),
            p2 = Offset(ray.x.toFloat(), ray.y.toFloat()),
            paint = Paint().apply {
                color = Color.Red
                style = PaintingStyle.Stroke
                strokeWidth = rayWidth.toFloat()
            }
        )
    }

}

fun castRay(player: Player, angle: Float, rayLength: Int): Offset {
    var x = player.x + PLAYER_SIZE / 2
    var y = player.y + PLAYER_SIZE / 2

    while (true) {
        x += (rayLength * cos(angle.toDouble())).toInt()
        y += (rayLength * sin(angle.toDouble())).toInt()

        if (MAP[y.toInt() / MAP_SCALE.toInt() * MAP_X + x.toInt() / MAP_SCALE.toInt()] == 1) {
            return Offset(x.toFloat(), y.toFloat())
        }
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
                x = player.x + (MOVE_STEP * cos(player.rotation.toDouble())).toInt(),
                y = player.y + (MOVE_STEP * sin(player.rotation.toDouble())).toInt()
            )
        }

        357019156480 -> {
            player = player.copy(
                x = player.x - (MOVE_STEP * cos(player.rotation.toDouble())).toInt(),
                y = player.y - (MOVE_STEP * sin(player.rotation.toDouble())).toInt()
            )
        }

        279709745152 -> {
            player = player.copy(rotation = player.rotation - 5f.toRadian())
        }

        292594647040 -> {
            player = player.copy(rotation = player.rotation + 5f.toRadian())
        }
    }
}


data class Player(
    val x: Int,
    val y: Int,
    val rotation: Float,
)

fun Float.toRadian(): Float = (this / 180f * Math.PI).toFloat()
fun Float.toDegree(): Float = (this * 180f / Math.PI).toFloat()
