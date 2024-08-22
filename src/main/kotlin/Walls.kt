import engine.textures.readPpmImage

object Walls {
    private val wallTexture1 = readPpmImage("textures/wall1.ppm")
    private val wallTexture2 = readPpmImage("textures/wall2.ppm")
    private val wallTexture3 = readPpmImage("textures/wall3.ppm")
    private val doorTexture = readPpmImage("textures/door.ppm")
    private val exitTexture = readPpmImage("textures/exit.ppm")

    val wallTextures = mapOf(
        1 to wallTexture1,
        2 to wallTexture2,
        3 to wallTexture3,
        8 to exitTexture,
        9 to doorTexture
    )

}