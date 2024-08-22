package engine

const val PI = 3.1415927f

fun Float.normalizeAngle(): Float = (this + PI) % (2 * PI) - PI
fun Float.toRadian(): Float = this * PI / 180f
fun Int.toRadian(): Float = this.toFloat() * PI / 180f
