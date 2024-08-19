package models

import isWall
import normalizeAngle
import toRadian
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

enum class PlayerState {
    IDLE,
    WALKING,
    SHOOTING
}

data class Player(
    var x: Float,
    var y: Float,
    var z: Float,
    var rotationRad: Float = 0f,
    var walkingFrame: Int = 0,
    var shootingFrame: Int = 0,
    var state: PlayerState = PlayerState.IDLE,
    var timer: Int = 0
)

fun Player.animate(state: PlayerState? = null) {
    state?.let {
        this.state = it
    }

    if (this.state == PlayerState.WALKING) {
        this.walk()
    } else if (this.state == PlayerState.SHOOTING) {
        this.shoot()
    }
}

private fun Player.walk() {

    if (this.timer % 7 == 0) {
        this.walkingFrame = (this.walkingFrame + 1) % 4
    }
    this.timer++
}

private fun Player.shoot(frameCount: Int = 6) {


    if (this.shootingFrame >= frameCount - 1) {
        this.state = PlayerState.WALKING
        shootingFrame = 0
        return
    }
    if (this.timer % 7 == 0) {
        this.shootingFrame = (this.shootingFrame + 1)
    }
    this.timer++
}

fun Player.distanceTo(player: Player): Float {
    return kotlin.math.sqrt((this.x - player.x) * (this.x - player.x) + (this.y - player.y) * (this.y - player.y))
}


fun Player.walkRandom(map: IntArray, mapW: Int, mapH: Int, cellSize: Int, buffer: Float = 0.2f) {
    // Calculate movement deltas based on current rotation
    val dx = 0.1f * cos(this.rotationRad)
    val dy = 0.1f * sin(this.rotationRad)

    // Calculate new positions with buffer applied
    val newX = this.x + dx
    val newY = this.y + dy

    // Initialize rotation to current rotation
    var rotation = this.rotationRad

    // Check for wall collisions and apply buffer zone
    if (isWall(newX + dx.sign * buffer, this.y, map, mapW, mapH, cellSize) == WallType.NONE) {
        this.x = newX
    } else {
        rotation = (rotation + 10.toRadian()).normalizeAngle()
    }

    if (isWall(this.x, newY + dy.sign * buffer, map, mapW, mapH, cellSize) == WallType.NONE) {
        this.y = newY
    } else {
        rotation = (rotation + 10.toRadian()).normalizeAngle()
    }

    // Update rotation
    this.rotationRad = rotation
}

