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
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private const val FPS = 60

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
        movePlayer(pressedKeys)
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
                gameLoop(
                    pressedKeys = pressedKeys,
                    onFrame = {
                        screen = it
                    }
                )

            }

            delay((1000 - renderTime.toLong(DurationUnit.MILLISECONDS)) / FPS.toLong())

//            println("render time: $renderTime")
        }
    }
}








