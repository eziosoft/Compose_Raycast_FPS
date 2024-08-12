import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun Dpad(
    modifier: Modifier,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit
) {
    var state by remember { mutableStateOf(0) }

    LaunchedEffect(state) {
        while (true) {
            when (state) {
                1 -> onUp()
                2 -> onDown()
                3 -> onLeft()
                4 -> onRight()
                5 -> onCenter()
            }
            delay(100)
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { state = 1 }) {
            Text("U")
        }
        Row {
            Button(onClick = { state = 3 }) {
                Text("L")
            }
            Button(onClick = { state = 5 }) {
                Text("C")
            }
            Button(onClick = { state = 4 }) {
                Text("R")
            }
        }
        Button(onClick = { state = 2 }) {
            Text("D")
        }
    }

}