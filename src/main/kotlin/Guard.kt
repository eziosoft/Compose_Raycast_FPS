
const val SPRITE_SIZE = 64

val TRANSPARENT_COLOR = Screen.Color(152, 0, 136)

private val guardTextureSheet = readPpmImage("enemy1.txt")
private val guardStill = mapOf(
    0 to selectFrame(guardTextureSheet, 0, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    1 to selectFrame(guardTextureSheet, 1, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    2 to selectFrame(guardTextureSheet, 2, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    3 to selectFrame(guardTextureSheet, 3, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    4 to selectFrame(guardTextureSheet, 4, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    5 to selectFrame(guardTextureSheet, 5, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    6 to selectFrame(guardTextureSheet, 6, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    7 to selectFrame(guardTextureSheet, 7, 0, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
)

private val guardWalking1 = mapOf(
    0 to selectFrame(guardTextureSheet, 0, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    1 to selectFrame(guardTextureSheet, 1, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    2 to selectFrame(guardTextureSheet, 2, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    3 to selectFrame(guardTextureSheet, 3, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    4 to selectFrame(guardTextureSheet, 4, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    5 to selectFrame(guardTextureSheet, 5, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    6 to selectFrame(guardTextureSheet, 6, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    7 to selectFrame(guardTextureSheet, 7, 1, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
)

private val guardWalking2 = mapOf(
    0 to selectFrame(guardTextureSheet, 0, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    1 to selectFrame(guardTextureSheet, 1, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    2 to selectFrame(guardTextureSheet, 2, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    3 to selectFrame(guardTextureSheet, 3, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    4 to selectFrame(guardTextureSheet, 4, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    5 to selectFrame(guardTextureSheet, 5, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    6 to selectFrame(guardTextureSheet, 6, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    7 to selectFrame(guardTextureSheet, 7, 2, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
)

private val guardWalking3 = mapOf(
    0 to selectFrame(guardTextureSheet, 0, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    1 to selectFrame(guardTextureSheet, 1, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    2 to selectFrame(guardTextureSheet, 2, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    3 to selectFrame(guardTextureSheet, 3, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    4 to selectFrame(guardTextureSheet, 4, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    5 to selectFrame(guardTextureSheet, 5, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    6 to selectFrame(guardTextureSheet, 6, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    7 to selectFrame(guardTextureSheet, 7, 3, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
)

private val guardWalking4 = mapOf(
    0 to selectFrame(guardTextureSheet, 0, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    1 to selectFrame(guardTextureSheet, 1, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    2 to selectFrame(guardTextureSheet, 2, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    3 to selectFrame(guardTextureSheet, 3, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    4 to selectFrame(guardTextureSheet, 4, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    5 to selectFrame(guardTextureSheet, 5, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    6 to selectFrame(guardTextureSheet, 6, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
    7 to selectFrame(guardTextureSheet, 7, 4, SPRITE_SIZE, 8 * SPRITE_SIZE, 1),
)

fun getGuardTexture(
    direction: Int,
    walking: Boolean,
    walkingFrame: Int
): IntArray {
    return when {
        !walking -> guardStill[direction]!!
        walking && walkingFrame == 0 -> guardWalking1[direction]!!
        walking && walkingFrame == 1 -> guardWalking2[direction]!!
        walking && walkingFrame == 2 -> guardWalking3[direction]!!
        walking && walkingFrame == 3 -> guardWalking4[direction]!!
        else -> guardStill[direction]!!
    }
}
