import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.useResource
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs


const val PI = 3.1415927f

fun readPpmImage(path: String): IntArray {
    return useResource(path) {
        val reader = BufferedReader(InputStreamReader(it))

        // Read PPM header
        val format = readNonCommentLine(reader)  // P3 or P6
        if (format != "P3") {
            throw IllegalArgumentException("Unsupported PPM format: $format")
        }

        // Read image size
        val sizeLine = readNonCommentLine(reader)
        val size = sizeLine.split(" ")
        val width = size[0].toInt()
        val height = size[1].toInt()

        // Read max color value (e.g., 255)
        val maxColorValue = readNonCommentLine(reader).toInt()

        // Initialize array to store the pixel data (RGB)
        val array = IntArray(width * height * 3)

        // Read pixel data
        for (i in array.indices) {
            array[i] = readNonCommentLine(reader).toInt()
        }

        array
    }
}

private fun readNonCommentLine(reader: BufferedReader): String {
    var line: String
    do {
        line = reader.readLine().trim()
    } while (line.startsWith("#") || line.isEmpty())
    return line
}


fun selectFrame(
    array: IntArray,
    xIndex: Int,
    yIndex: Int,
    frameSize: Int,
    sheetWidth: Int,
    dividerWidth: Int
): IntArray {
    val subArray = IntArray(frameSize * frameSize * 3)

    for (y in 0 until frameSize) {
        for (x in 0 until frameSize) {
            // Calculate the index in the original array, considering the divider width
            val index =
                ((yIndex * (frameSize + dividerWidth) + y) * (sheetWidth + 7 * dividerWidth) + xIndex * (frameSize + dividerWidth) + x) * 3
            val subIndex = (y * frameSize + x) * 3

            subArray[subIndex] = array[index]
            subArray[subIndex + 1] = array[index + 1]
            subArray[subIndex + 2] = array[index + 2]
        }
    }

    return subArray
}


fun Float.normalizeAngle(): Float = (this + PI) % (2 * PI) - PI
fun Float.toRadian(): Float = this * PI / 180f
fun Int.toRadian(): Float = this.toFloat() * PI / 180f



fun isWall(x: Float, y: Float, map:IntArray, mapW:Int, mapH:Int, cellSize:Int): Boolean {
    val mapX = (x / cellSize).toInt()
    val mapY = (y / cellSize).toInt()
    return if (mapX in 0 until mapW && mapY in 0 until mapH) {
        map[mapY * mapW + mapX] == 1
    } else {
        true // Treat out-of-bounds as walls
    }
}







