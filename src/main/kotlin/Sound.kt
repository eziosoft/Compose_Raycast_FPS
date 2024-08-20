import androidx.compose.ui.res.useResource
import javazoom.jl.player.advanced.AdvancedPlayer

fun playSound(resourceFilePath: String) {
    Thread {
        try {
            useResource(resourceFilePath) {
                val player = AdvancedPlayer(it)
                player.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
