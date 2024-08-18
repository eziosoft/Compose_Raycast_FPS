import androidx.compose.ui.res.useResource
import javazoom.jl.player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun playMp3(filePath: String) {
     val job = Job()
     val scope = CoroutineScope(Dispatchers.Default + job)

    scope.launch {
        useResource(filePath) {
            val player = Player(it)
            player.play()
        }
    }
}