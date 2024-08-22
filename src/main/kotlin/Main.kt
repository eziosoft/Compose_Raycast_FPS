import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.singleWindowApplication
import engine.Screen
import engine.playSound
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private const val FPS = 60

const val W = 640
const val H = 480

val raytracer = RaytracerEngine(W, H)

val pressedKeys = mutableSetOf<Long>()

@Composable
@Preview
fun App() {
    MaterialTheme {
        RayCaster()
    }
}


fun main() = singleWindowApplication(
    onKeyEvent = { event ->
        when (event.type) {
            KeyEventType.KeyDown -> pressedKeys.add(event.key.keyCode)
            KeyEventType.KeyUp -> pressedKeys.remove(event.key.keyCode)
        }
        false
    }


) {
    App()
    playSound("sound/soundtrack.mp3") // play soundtrack
}


@Composable
fun RayCaster() {

    var screen by remember { mutableStateOf(Screen(W, H)) }


    Image(
        modifier = Modifier.fillMaxSize(),
        bitmap = screen.getBitmap().toComposeImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.None,
    )



    LaunchedEffect(Unit) {
        while (true) {
            val renderTime = measureTime {
                raytracer.gameLoop(
                    pressedKeys = mapKeysToMoves(pressedKeys),
                    onFrame = {
                        screen = it
                    }
                )
            }

            delay((1000 - renderTime.toLong(DurationUnit.MILLISECONDS)) / FPS.toLong())

            println("render time: $renderTime, maxFPS = ${1000 / renderTime.toInt(DurationUnit.MILLISECONDS)}")
        }
    }
}


private fun mapKeysToMoves(keys: Set<Long>): Set<Moves> {
    val moves = mutableSetOf<Moves>()

    keys.forEach {
        when (it) {
            374199025664 -> moves.add(Moves.UP)
            357019156480 -> moves.add(Moves.DOWN)
            348429221888 -> moves.add(Moves.MOVE_LEFT)
            296889614336 -> moves.add(Moves.MOVE_RIGHT)
            279709745152 -> moves.add(Moves.LEFT)
            292594647040 -> moves.add(Moves.RIGHT)
            137975824384 -> moves.add(Moves.SHOOT)
        }
    }

    return moves
}
