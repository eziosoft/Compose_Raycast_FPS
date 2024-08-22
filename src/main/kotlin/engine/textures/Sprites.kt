package engine.textures

import engine.Screen
import models.PlayerState


interface Sprite {
    val TRANSPARENT_COLOR: Screen.Color
    val SPRITE_SIZE: Int

    fun getTexture(
        direction: Int,
        state: PlayerState,
        walkingFrame: Int,
        dyingFrame: Int
    ): IntArray

    fun getFrame(frameIndex: Int): IntArray?

    fun loadFrame(
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
}