import androidx.compose.ui.res.useResource
import javazoom.jl.player.Player

suspend fun playMp3(filePath: String) {
    useResource(filePath) {
        val player = Player(it)
        player.play()
    }
}