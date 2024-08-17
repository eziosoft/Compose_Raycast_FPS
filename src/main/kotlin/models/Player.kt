package models

import isWall
import normalizeAngle
import kotlin.math.cos
import kotlin.math.sin

enum class PlayerState {
    IDLE,
    WALKING
}

data class Player(
    var x: Float,
    var y: Float,
    var z: Float,
    var rotationRad: Float = 0f,
    var walkingFrame: Int = 0,
    var state: PlayerState = PlayerState.IDLE,
    var timer:Int = 0
)

fun Player.animate(){
    if (this.timer % 7 == 0) {
        this.walkingFrame = (this.walkingFrame + 1) % 4
    }
    this.timer++
}


 fun Player.walkRandom( map:IntArray, mapW:Int, mapH:Int, cellSize:Int) {
    // move, avoid walls
    val dx = 0.1f * cos(this.rotationRad)
    val dy = 0.1f * sin(this.rotationRad)

    var rotation = this.rotationRad

    val newX = this.x + dx
    val newY = this.y + dy

    if (!isWall(newX, this.y, map, mapW, mapH, cellSize)) {
        this.x = newX

    }else{
        rotation = this.rotationRad + 0.05f
    }

    if (!isWall(this.x, newY, map, mapW, mapH, cellSize)) {
        this.y = newY
    }else{
        rotation = this.rotationRad + 0.05f
    }

    // rotate
    this.rotationRad = rotation.normalizeAngle()
}