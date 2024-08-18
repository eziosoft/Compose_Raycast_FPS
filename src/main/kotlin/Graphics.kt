import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import java.awt.image.BufferedImage
import kotlin.math.abs

fun drawFilledRect(bitmap: BufferedImage, x: Int, y: Int, w: Int, h: Int, color: Color) {
    for (i in x until x + w) {
        for (j in y until y + h) {
            if (i < 0 || i >= bitmap.width || j < 0 || j >= bitmap.height) {
                continue
            }

            bitmap.setRGB(i, j, color.toArgb())
        }
    }
}

fun drawLine(bitmap: BufferedImage, x1: Int, y1: Int, x2: Int, y2: Int, color: Color) {
    val dx = abs(x2 - x1)
    val dy = abs(y2 - y1)

    val sx = if (x1 < x2) 1 else -1
    val sy = if (y1 < y2) 1 else -1

    var err = dx - dy

    var x = x1
    var y = y1

    while (true) {
        bitmap.setRGB(x, y, color.toArgb())

        if (x == x2 && y == y2) break

        val e2 = 2 * err
        if (e2 > -dy) {
            err -= dy
            x += sx
        }
        if (e2 < dx) {
            err += dx
            y += sy
        }
    }
}

fun drawText(bitmap: BufferedImage, x:Int, y: Int, text: String, color: Color) {
    val g = bitmap.graphics
    g.color = java.awt.Color(color.red, color.green, color.blue)
    g.drawString(text, x, y)
}