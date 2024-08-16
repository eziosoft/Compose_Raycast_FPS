import androidx.compose.ui.res.useResource
import java.io.BufferedReader
import java.io.InputStreamReader

fun readPpmImage(path: String): IntArray {
    return useResource(path) {
        val reader = BufferedReader(InputStreamReader(it))

        // Read PPM header
        val format = reader.readLine()  // P3 or P6
        if (format != "P3") {
            throw IllegalArgumentException("Unsupported PPM format: $format")
        }

        // Read image size
        val sizeLine = reader.readLine()
        val size = sizeLine.split(" ")
        val width = size[0].toInt()
        val height = size[1].toInt()

        // Read max color value (e.g., 255)
        val maxColorValue = reader.readLine().toInt()

        // Initialize array to store the pixel data (RGB)
        val array = IntArray(width * height * 3)

        // Read pixel data
        for (i in array.indices) {
            array[i] = reader.readLine().toInt()
        }

        array
    }
}
