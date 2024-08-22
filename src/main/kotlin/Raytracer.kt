import engine.*
import engine.textures.readPpmImage
import maps.*
import maps.Map
import models.*
import sprites.GuardSprite
import sprites.PistolSprite
import kotlin.math.*


const val W = 640
const val H = 480


private val FOV_RAD = 60.toRadian()
private const val MOVE_STEP = 0.2f
private val ROTATION_STEP_RAD = 2f.toRadian()


private const val TEXTURE_SIZE = 64 //walls, floor and ceiling textures
private val floorTexture = readPpmImage("textures/floor.ppm")
private val cellingTexture = readPpmImage("textures/celling.ppm")


private const val CELL_SIZE = 2
private val currentMap: Map = Map1


private val wallDepths = FloatArray(W) // depth buffer

private val playerPosition =
    findPositionBasedOnMapIndex(
        mapX = currentMap.MAP_X,
        mapY = currentMap.MAP_Y,
        cellSize = CELL_SIZE,
        index = currentMap.MAP.indexOf(MapObject.player.id)
    )
private val player =
    Player(
        x = playerPosition[0],
        y = playerPosition[1],
        rotationRad = -90f.toRadian().normalizeAngle(),
        isMainPlayer = true
    )

private val enemies = currentMap.getEnemiesFromMap(cellSize = CELL_SIZE)


fun gameLoop(pressedKeys: Set<Long>, onFrame: (Screen) -> Unit) {
    enemies.forEach { enemy ->
        enemy.animate(map = currentMap, cellSize = CELL_SIZE)
    }

    movePlayer(pressedKeys)
    onFrame(generateFrame())
}


private fun generateFrame(): Screen {
    val screen = Screen(W, H)

    // draw 3d
    castRays(player, screen)

    // draw enemies based on distance
    enemies.sortedByDescending { it.distanceTo(player) }.forEach { enemy ->
        drawSprite(screen, player, enemy, wallDepths)
    }

    drawMap(
        screen = screen,
        map = currentMap,
        xOffset = 10,
        yOffset = H - CELL_SIZE * currentMap.MAP_Y - 10,
        player = player,
        enemies = enemies,
        cellSize = CELL_SIZE,
        playerSize = 5f
    )


    // draw pistol
    screen.drawBitmap(
        bitmap = PistolSprite.getFrame(player.shootingFrame)!!,
        x = 2 * screen.w / 3 + (20 * sin(player.x)).toInt(),
        y = (screen.h - 175 * 0.8f + 20 - 10 * sin(player.y)).toInt(),
        bitmapSizeX = 128,
        bitmapSizeY = 128,
        transparentColor = PistolSprite.TRANSPARENT_COLOR
    )

    // draw cross when enemy is in range
    enemies.forEach { enemy ->
        if (player.distanceTo(enemy) < 10 && player.inShotAngle(enemy)) {
            drawCross(screen)
        }
    }


    player.animate(map = currentMap, cellSize = CELL_SIZE)


//    drawText(bitmap, 10, 10, renderTime.toString(unit = DurationUnit.MILLISECONDS, decimals = 2), Color.White)


    return screen
}

private fun drawCross(screen: Screen) {
    val crossSize = 10
    val crossColor = Screen.Color(255, 255, 255)

    val x = W / 2 - crossSize / 2
    val y = H / 2 - crossSize / 2

    screen.drawLine(x, y, x + crossSize, y + crossSize, crossColor)
    screen.drawLine(x + crossSize, y, x, y + crossSize, crossColor)
}


// draws square in 3d world using 3d projection mapping
private fun drawSprite(screen: Screen, player: Player, enemy: Player, wallDepths: FloatArray) {
    // Calculate the angle from the enemy to the player
    val angleToPlayer = atan2(player.y - enemy.y, player.x - enemy.x)

    // Calculate the difference between the enemy's rotation and the angle to the player
    var diff = angleToPlayer - enemy.rotationRad

    // Normalize the angle difference to be between -PI and PI
    diff = (diff + engine.PI) % (2 * engine.PI) - engine.PI

    // Calculate the texture index, ensuring it falls within the valid range
    val numTextures = 8  // Assuming there are 8 textures in the textureSet
    val textureIndex = numTextures - ((diff / (2 * engine.PI) * numTextures) + numTextures) % numTextures

    // Fetch the correct texture for rendering
    val texture = GuardSprite.getTexture(
        direction = textureIndex.toInt(),
        state = enemy.state,
        walkingFrame = enemy.walkingFrame,
        dyingFrame = enemy.dyingFrame
    )

    val pointHeight = CELL_SIZE // Height of the square in world units

    // Calculate vector from player to enemy
    val dx = enemy.x - player.x
    val dy = enemy.y - player.y

    // Calculate distance to enemy
    val distance = sqrt(dx * dx + dy * dy)

    // Calculate angle to enemy relative to player's rotation
    var angle = atan2(dy, dx) - player.rotationRad

    // Normalize angle to be between -PI and PI
    angle = (angle + engine.PI) % (2 * engine.PI) - engine.PI

    // Check if enemy is within player's FOV
    if (abs(angle) < FOV_RAD / 2) {
        // Calculate screen x-coordinate
        val screenX = ((W / 2) * (1 + angle / (FOV_RAD / 2))).toInt()

        // Calculate perceived height of the square
        val perceivedHeight = (H / distance * pointHeight).toInt()

        // Calculate top and bottom y-coordinates
        val topY = (H / 2 - perceivedHeight / 2).coerceIn(0, H - 1)
        val bottomY = (H / 2 + perceivedHeight / 2).coerceIn(0, H - 1)

        // Calculate perceived width of the square
        val perceivedWidth = perceivedHeight

        // Draw sprite
        for (y in topY..bottomY) {
            for (x in (screenX - perceivedWidth / 2)..(screenX + perceivedWidth / 2)) {
                if (x >= 0 && x < W) {
                    // Only draw the sprite pixel if it's closer than the wall
                    if (wallDepths[x] - (distance / CELL_SIZE) > -0.1) { // -0.1 - padding to avoid wall clipping
                        // Calculate texture coordinates
                        val texX =
                            ((x - (screenX - perceivedWidth / 2)).toFloat() / perceivedWidth * GuardSprite.SPRITE_SIZE).toInt() % GuardSprite.SPRITE_SIZE
                        val texY =
                            ((y - topY).toFloat() / perceivedHeight * GuardSprite.SPRITE_SIZE).toInt() % GuardSprite.SPRITE_SIZE

                        val texIndex = (texY * GuardSprite.SPRITE_SIZE + texX) * 3

                        val texColor = Screen.Color(
                            texture[texIndex],
                            texture[texIndex + 1],
                            texture[texIndex + 2],
                        )

                        if (texColor != GuardSprite.TRANSPARENT_COLOR) {
                            // Apply distance-based shading
                            val intensity = (1.0f - (distance / 30.0f)).coerceIn(0.2f, 1f)
                            val shadedColor = darkenColor(texColor, intensity)

                            screen.setRGB(x, y, shadedColor)
                        }
                    }
                }
            }
        }
    }
}


// Ray casting using DDA algorithm. Cover walls with wall texture. Add fish-eye correction.
private fun castRays(player: Player, bitmap: Screen) {
    val rayCount = W
    val rayStep = FOV_RAD / rayCount

    for (x in 0 until rayCount) {
        val rayAngle = player.rotationRad - FOV_RAD / 2 + x * rayStep

        val rayDirX = cos(rayAngle)
        val rayDirY = sin(rayAngle)

        var mapX = floor(player.x / CELL_SIZE).toInt()
        var mapY = floor(player.y / CELL_SIZE).toInt()

        val deltaDistX = abs(1 / rayDirX)
        val deltaDistY = abs(1 / rayDirY)

        var sideDistX: Float
        var sideDistY: Float

        val stepX: Int
        val stepY: Int

        if (rayDirX < 0) {
            stepX = -1
            sideDistX = (player.x / CELL_SIZE - mapX) * deltaDistX
        } else {
            stepX = 1
            sideDistX = (mapX + 1.0f - player.x / CELL_SIZE) * deltaDistX
        }

        if (rayDirY < 0) {
            stepY = -1
            sideDistY = (player.y / CELL_SIZE - mapY) * deltaDistY
        } else {
            stepY = 1
            sideDistY = (mapY + 1.0f - player.y / CELL_SIZE) * deltaDistY
        }

        var hit = false
        var side = 0

        var wallTextureIndex: Int = 0

        while (!hit) {
            if (sideDistX < sideDistY) {
                sideDistX += deltaDistX
                mapX += stepX
                side = 0
            } else {
                sideDistY += deltaDistY
                mapY += stepY
                side = 1
            }


            if (mapX < 0 || mapX >= currentMap.MAP_X || mapY < 0 || mapY >= currentMap.MAP_Y) {
                hit = true
            } else
                if (currentMap.MAP[mapY * currentMap.MAP_X + mapX] > 0) {
                    hit = true
                    wallTextureIndex = currentMap.MAP[mapY * currentMap.MAP_X + mapX]
                }
        }

        var perpWallDist: Float
        if (side == 0) {
            perpWallDist = (mapX - player.x / CELL_SIZE + (1 - stepX) / 2) / rayDirX
        } else {
            perpWallDist = (mapY - player.y / CELL_SIZE + (1 - stepY) / 2) / rayDirY
        }

        wallDepths[x] = perpWallDist

        // Fish-eye correction
        perpWallDist *= cos(player.rotationRad - rayAngle)

        val lineHeight = (H / perpWallDist).toInt()

        val drawStart = (-lineHeight / 2 + H / 2).coerceAtLeast(0)
        val drawEnd = (lineHeight / 2 + H / 2).coerceAtMost(H - 1)

        // Texture mapping for walls
        var wallX: Float
        if (side == 0) {
            wallX = player.y / CELL_SIZE + perpWallDist * rayDirY
        } else {
            wallX = player.x / CELL_SIZE + perpWallDist * rayDirX
        }
        wallX -= floor(wallX)

        var texX = (wallX * TEXTURE_SIZE).toInt()
        if ((side == 0 && rayDirX > 0) || (side == 1 && rayDirY < 0)) {
            texX = TEXTURE_SIZE - texX - 1
        }

        val step = TEXTURE_SIZE.toFloat() / lineHeight
        var texPos = (drawStart - H / 2 + lineHeight / 2) * step


        val wallTexture = Walls.wallTextures[wallTextureIndex] ?: error("Wall texture not found $wallTextureIndex")

        for (y in drawStart until drawEnd) {
            val texY = (texPos.toInt() and (TEXTURE_SIZE - 1))
            texPos += step

            val texIndex = (texY * TEXTURE_SIZE + texX) * 3
            val texColor = Screen.Color(
                wallTexture[texIndex],
                wallTexture[texIndex + 1],
                wallTexture[texIndex + 2],
            )

            val intensity = 1.0f - ((perpWallDist / 20.0f) + 0.4f * side).coerceAtMost(1f)
            val color = darkenColor(texColor, intensity)

            bitmap.setRGB(x, y, color)
        }

        // Ceiling casting
        if (drawStart > 0) {
            for (y in 0 until drawStart) {
                val ceilingDistance = H.toFloat() / (H - 2.0f * y)

                // Reversed direction for the ceiling
                val ceilingX = player.x / CELL_SIZE + ceilingDistance * rayDirX
                val ceilingY = player.y / CELL_SIZE + ceilingDistance * rayDirY

                val ceilingTexX = ((ceilingX - floor(ceilingX)) * TEXTURE_SIZE).toInt()
                val ceilingTexY = ((ceilingY - floor(ceilingY)) * TEXTURE_SIZE).toInt()

                val texIndex = (ceilingTexY * TEXTURE_SIZE + ceilingTexX) * 3
                if (texIndex >= 0) {
                    val texColor = Screen.Color(
                        cellingTexture[texIndex],
                        cellingTexture[texIndex + 1],
                        cellingTexture[texIndex + 2],
                    )

                    val color = darkenColor(texColor, 0.5f) // Apply some darkness to the ceiling
                    bitmap.setRGB(x, y, color)
                }
            }
        }

        // Floor casting
        if (drawEnd < H) {
            for (y in drawEnd until H) {
                val floorDistance = H.toFloat() / (2.0f * y - H)

                val floorX = player.x / CELL_SIZE + floorDistance * rayDirX
                val floorY = player.y / CELL_SIZE + floorDistance * rayDirY

                val floorTexX = (floorX * TEXTURE_SIZE % TEXTURE_SIZE).toInt()
                val floorTexY = (floorY * TEXTURE_SIZE % TEXTURE_SIZE).toInt()

                val texIndex = (floorTexY * TEXTURE_SIZE + floorTexX) * 3
                if (texIndex >= 0) {
                    val texColor = Screen.Color(
                        floorTexture[texIndex],
                        floorTexture[texIndex + 1],
                        floorTexture[texIndex + 2],
                    )

                    val color = darkenColor(texColor, 0.5f) // Apply some darkness to the floor
                    bitmap.setRGB(x, y, color)
                }
            }
        }
    }
}


// Helper function to darken a color based on intensity used for shading
private fun darkenColor(color: Screen.Color, intensity: Float): Screen.Color =
    Screen.Color(
        (color.red * intensity).toInt(),
        (color.green * intensity).toInt(),
        (color.blue * intensity).toInt(),
    )


fun movePlayer(pressedKeys: Set<Long>) {
//    println("pressedKeys: $pressedKeys")
    var dx = 0f
    var dy = 0f
    var dr = 0f

    if (374199025664 in pressedKeys) { // Right arrow key
        dx += MOVE_STEP * cos(player.rotationRad)
        dy += MOVE_STEP * sin(player.rotationRad)
    }
    if (357019156480 in pressedKeys) { // Left arrow key
        dx -= MOVE_STEP * cos(player.rotationRad)
        dy -= MOVE_STEP * sin(player.rotationRad)
    }

    if (348429221888 in pressedKeys) { // A key
        dx -= MOVE_STEP * cos(player.rotationRad + 90.toRadian())
        dy -= MOVE_STEP * sin(player.rotationRad + 90.toRadian())
    }

    if (296889614336 in pressedKeys) { // D key
        dx += MOVE_STEP * cos(player.rotationRad + 90.toRadian())
        dy += MOVE_STEP * sin(player.rotationRad + 90.toRadian())
    }

    if (279709745152 in pressedKeys) { // Up arrow key
        dr -= ROTATION_STEP_RAD
    }
    if (292594647040 in pressedKeys) { // Down arrow key
        dr += ROTATION_STEP_RAD
    }

    if (137975824384 in pressedKeys) { // Space key
        player.animate(state = PlayerState.SHOOTING, map = currentMap, cellSize = CELL_SIZE)
        playSound("sound/gunshot1.mp3")

        enemies.forEach { enemy ->
            if (player.distanceTo(enemy) < 10 && player.inShotAngle(enemy)) {
                enemy.state = PlayerState.DYING
                playSound("sound/mandeathscream.mp3")
            }
        }
    }

    // Apply rotation
    val newRotation = player.rotationRad + dr
    player.rotationRad = newRotation.normalizeAngle()

    // Apply movement with collision detection
    val newX = player.x + dx
    val newY = player.y + dy

    if (isWall(newX, player.y, currentMap.MAP, currentMap.MAP_X, currentMap.MAP_Y, CELL_SIZE) == WallType.NONE) {
        player.x = newX
    }

    if (isWall(player.x, newY, currentMap.MAP, currentMap.MAP_X, currentMap.MAP_Y, CELL_SIZE) == WallType.NONE) {
        player.y = newY
    }

    // Open door

    if (isWall(newX, player.y, currentMap.MAP, currentMap.MAP_X, currentMap.MAP_Y, CELL_SIZE) == WallType.DOOR) {
        // open door
        currentMap.MAP[currentMap.MAP_X * (player.y.toInt() / CELL_SIZE) + (newX.toInt() / CELL_SIZE)] = 0
    }

    if (isWall(player.x, newY, currentMap.MAP, currentMap.MAP_X, currentMap.MAP_Y, CELL_SIZE) == WallType.DOOR) {
        // open door
        currentMap.MAP[currentMap.MAP_X * (newY.toInt() / CELL_SIZE) + (player.x.toInt() / CELL_SIZE)] = 0
    }

    // Exit
    if (isWall(newX, player.y, currentMap.MAP, currentMap.MAP_X, currentMap.MAP_Y, CELL_SIZE) == WallType.EXIT) {
        error("You win!")
    }


}






