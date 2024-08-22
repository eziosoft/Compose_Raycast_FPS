import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.singleWindowApplication
import engine.playSound


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
    playSound("sound/soundtrack.mp3")
}









