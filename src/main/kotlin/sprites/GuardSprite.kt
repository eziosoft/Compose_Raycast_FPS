package sprites

import engine.Screen
import engine.textures.Sprite
import engine.textures.readPpmImage
import models.PlayerState


object GuardSprite : Sprite {
    override val TRANSPARENT_COLOR = Screen.Color(152, 0, 136)

    override val SPRITE_SIZE = 64

    private val guardTextureSheet = readPpmImage("sprites/enemy.ppm")

    private val guardStill = mapOf(
        0 to loadFrame(guardTextureSheet, 0, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        1 to loadFrame(guardTextureSheet, 1, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        2 to loadFrame(guardTextureSheet, 2, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        3 to loadFrame(guardTextureSheet, 3, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        4 to loadFrame(guardTextureSheet, 4, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        5 to loadFrame(guardTextureSheet, 5, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        6 to loadFrame(guardTextureSheet, 6, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        7 to loadFrame(guardTextureSheet, 7, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    )

    private val guardWalking1 = mapOf(
        0 to loadFrame(guardTextureSheet, 0, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        1 to loadFrame(guardTextureSheet, 1, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        2 to loadFrame(guardTextureSheet, 2, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        3 to loadFrame(guardTextureSheet, 3, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        4 to loadFrame(guardTextureSheet, 4, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        5 to loadFrame(guardTextureSheet, 5, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        6 to loadFrame(guardTextureSheet, 6, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        7 to loadFrame(guardTextureSheet, 7, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    )

    private val guardWalking2 = mapOf(
        0 to loadFrame(guardTextureSheet, 0, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        1 to loadFrame(guardTextureSheet, 1, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        2 to loadFrame(guardTextureSheet, 2, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        3 to loadFrame(guardTextureSheet, 3, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        4 to loadFrame(guardTextureSheet, 4, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        5 to loadFrame(guardTextureSheet, 5, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        6 to loadFrame(guardTextureSheet, 6, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        7 to loadFrame(guardTextureSheet, 7, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    )

    private val guardWalking3 = mapOf(
        0 to loadFrame(guardTextureSheet, 0, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        1 to loadFrame(guardTextureSheet, 1, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        2 to loadFrame(guardTextureSheet, 2, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        3 to loadFrame(guardTextureSheet, 3, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        4 to loadFrame(guardTextureSheet, 4, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        5 to loadFrame(guardTextureSheet, 5, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        6 to loadFrame(guardTextureSheet, 6, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        7 to loadFrame(guardTextureSheet, 7, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    )

    private val guardWalking4 = mapOf(
        0 to loadFrame(guardTextureSheet, 0, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        1 to loadFrame(guardTextureSheet, 1, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        2 to loadFrame(guardTextureSheet, 2, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        3 to loadFrame(guardTextureSheet, 3, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        4 to loadFrame(guardTextureSheet, 4, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        5 to loadFrame(guardTextureSheet, 5, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        6 to loadFrame(guardTextureSheet, 6, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        7 to loadFrame(guardTextureSheet, 7, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    )

    private val guardDyingFrames = mapOf(
        0 to loadFrame(guardTextureSheet, 0, 5, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        1 to loadFrame(guardTextureSheet, 1, 5, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        2 to loadFrame(guardTextureSheet, 2, 5, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        3 to loadFrame(guardTextureSheet, 3, 5, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
        4 to loadFrame(guardTextureSheet, 4, 5, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    )

    override fun getTexture(
        direction: Int,
        state: PlayerState,
        walkingFrame: Int,
        dyingFrame: Int
    ): IntArray {
        when (state) {
            PlayerState.WALKING -> {
                return getGuardWalkingTexture(direction, walkingFrame)
            }

            PlayerState.DYING -> {
                return getGuardDyingTexture(dyingFrame)
            }

            PlayerState.DEAD -> {
                return getGuardDyingTexture(dyingFrame)
            }

            else -> {
                return getGuardStillTexture(direction)
            }
        }
    }

    override fun getFrame(frameIndex: Int): IntArray? {
        TODO("Not needed for this sprite")
    }

    private fun getGuardStillTexture(direction: Int): IntArray {
        return guardStill[direction]!!
    }

    private fun getGuardWalkingTexture(direction: Int, walkingFrame: Int): IntArray {
        return when (walkingFrame) {
            0 -> guardWalking1[direction]!!
            1 -> guardWalking2[direction]!!
            2 -> guardWalking3[direction]!!
            3 -> guardWalking4[direction]!!
            else -> guardWalking1[direction]!!
        }
    }

    private fun getGuardDyingTexture(dyingFrame: Int): IntArray {
        return guardDyingFrames[dyingFrame]!!
    }
}