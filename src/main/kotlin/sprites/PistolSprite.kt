package sprites

import engine.Screen
import engine.textures.Sprite
import engine.textures.readPpmImage
import models.PlayerState

object PistolSprite : Sprite {

    private val pistolTextureSheet = readPpmImage("sprites/pistol.ppm")
    private val pistolTexture = mapOf(
        0 to loadFrame(pistolTextureSheet, 0, 0, 128, 384, 0),
        1 to loadFrame(pistolTextureSheet, 1, 0, 128, 384, 0),
        2 to loadFrame(pistolTextureSheet, 2, 0, 128, 384, 0),
        3 to loadFrame(pistolTextureSheet, 0, 1, 128, 384, 0),
        4 to loadFrame(pistolTextureSheet, 1, 1, 128, 384, 0),
        5 to loadFrame(pistolTextureSheet, 2, 1, 128, 384, 0)
    )

    override val TRANSPARENT_COLOR = Screen.Color(0, 255, 255)
    override val SPRITE_SIZE = 128

    override fun getTexture(direction: Int, state: PlayerState, walkingFrame: Int, dyingFrame: Int): IntArray {
        TODO("Not yet implemented")
    }

    override fun getFrame(frameIndex: Int): IntArray? {
        return pistolTexture[frameIndex]
    }
}