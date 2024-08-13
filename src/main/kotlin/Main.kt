import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.awt.Graphics
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.DurationUnit
import kotlin.time.measureTime


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
}









