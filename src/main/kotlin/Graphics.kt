import java.awt.image.BufferedImage
import kotlin.math.abs

class Screen(val w: Int, val h: Int, color: Color = Color(0, 0, 0)) {
    private val bitmap = Array(w * h) { color }

    fun setRGB(x: Int, y: Int, color: Color) {
        if(x < 0 || x >= w || y < 0 || y >= h) return

        bitmap[y * w + x] = color
    }



    private fun getRGB(x: Int, y: Int): Color {
        return bitmap[y * w + x]
    }

    fun getBitmap(): BufferedImage {
        val image = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        for (i in 0 until w) {
            for (j in 0 until h) {
                image.setRGB(i, j, getRGB(i, j).toArgb())
            }
        }
        return image
    }

    fun drawFilledRect(x: Int, y: Int, w: Int, h: Int, color: Color) {
        for (i in x until x + w) {
            for (j in y until y + h) {
                setRGB(i, j, color)
            }
        }
    }

    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Color) {
        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)
        val sx = if (x1 < x2) 1 else -1
        val sy = if (y1 < y2) 1 else -1
        var err = dx - dy

        var x = x1
        var y = y1

        while (true) {
            setRGB(x, y, color)

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

    fun drawBitmap(bitmap:IntArray, x: Int, y: Int, bitmapSizeX: Int, bitmapSizeY: Int, transparentColor:Color){
        for (i in 0 until bitmapSizeX) {
            for (j in 0 until bitmapSizeY) {
                val color = Color(
                    bitmap[(j * bitmapSizeX + i) * 3],
                    bitmap[(j * bitmapSizeX + i) * 3 + 1],
                    bitmap[(j * bitmapSizeX + i) * 3 + 2]
                )
                if (color != transparentColor) {
                    setRGB(x + i, y + j, color)
                }
            }
        }
    }


    data class Color(val red: Int, val green: Int, val blue: Int) {
        fun toArgb(): Int {
            return (red shl 16) or (green shl 8) or blue
        }
    }
}







