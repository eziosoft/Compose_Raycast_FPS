package models

import CELL_SIZE
import MAP
import MAP_X
import MAP_Y
import PI
import WallType
import isWall
import normalizeAngle
import toRadian
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

enum class PlayerState {
    IDLE,
    WALKING,
    SHOOTING,
    DYING,
    DEAD
}

data class Player(
    var x: Float,
    var y: Float,
    var rotationRad: Float = 0f,
    var walkingFrame: Int = 0,
    var shootingFrame: Int = 0,
    var dyingFrame: Int = 0,
    var state: PlayerState = PlayerState.IDLE,
    var timer: Int = 0,
    val isMainPlayer: Boolean
)

fun Player.animate(state: PlayerState? = null) {
    state?.let {
        this.state = it
    }

    when (this.state) {
        PlayerState.WALKING -> {
            this.walk()
            if (!isMainPlayer) {
                this.walkRandom(MAP, MAP_X, MAP_Y, CELL_SIZE)
            }
        }
        PlayerState.SHOOTING -> {
            this.shoot()
        }
        PlayerState.DYING -> {
            this.dying(5)
        }

        PlayerState.DEAD -> this.dead()

        PlayerState.IDLE -> {}//TODO()
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

private fun Player.dying(frameCount: Int) {

    if (this.dyingFrame >= frameCount - 1) {
        this.dead()
        return
    }
    if (this.timer % 7 == 0) {
        this.dyingFrame = (this.dyingFrame + 1)
    }
    this.timer++

}

private fun Player.dead(){
    this.state = PlayerState.DEAD
    dyingFrame = 4
}

fun Player.distanceTo(player: Player): Float {
    return kotlin.math.sqrt((this.x - player.x) * (this.x - player.x) + (this.y - player.y) * (this.y - player.y))
}

fun Player.angleTo(player: Player): Float {
    return kotlin.math.atan2(player.y - this.y, player.x - this.x)
}

fun Player.inShotAngle(player: Player): Boolean {
    val angle = this.angleTo(player)
    var diff = this.rotationRad - angle

    // Normalize the difference to the range [-π, π]
    diff = (diff + PI).rem(2 * PI) - PI

    return kotlin.math.abs(diff) < 10.toRadian()
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

