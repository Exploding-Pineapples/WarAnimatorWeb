package com.wamteavm.utilities

import com.wamteavm.models.Coordinate
import kotlin.math.hypot

fun distanceFromPointToSegment(p: Coordinate, a: Coordinate, b: Coordinate): Float {
    val abX = b.x - a.x
    val abY = b.y - a.y
    val apX = p.x - a.x
    val apY = p.y - a.y

    val abLengthSquared = abX * abX + abY * abY
    if (abLengthSquared == 0f) {
        // a and b are the same point
        return hypot(p.x - a.x, p.y - a.y)
    }

    // Project point p onto line ab, computing parameterized position t
    val t = ((apX * abX) + (apY * abY)) / abLengthSquared

    // Clamp t from 0 to 1 to stay within the segment
    val tClamped = t.coerceIn(0f, 1f)

    // Compute closest point on segment
    val closestX = a.x + tClamped * abX
    val closestY = a.y + tClamped * abY

    // Return distance from p to the closest point
    return hypot(p.x - closestX, p.y - closestY)
}

fun clickedCoordinates(x: Float, y: Float, coordinates: Array<Coordinate>): Boolean {
    if (coordinates.isNotEmpty()) {
        for (i in 1..coordinates.lastIndex) {
            val dist = distanceFromPointToSegment(
                Coordinate(x, y),
                Coordinate(coordinates[i - 1].x, coordinates[i - 1].y),
                Coordinate(coordinates[i].x, coordinates[i].y),
            )
            if (dist <= 10) {
                return true
            }
        }
    }
    return false
}

fun clickedCoordinates(x: Float, y: Float, coordinates: Array<Float>): Boolean {
    if (coordinates.isNotEmpty()) {
        for (i in 4..coordinates.lastIndex step 2) {
            val dist = distanceFromPointToSegment(
                Coordinate(x, y),
                Coordinate(coordinates[i - 3], coordinates[i - 2]),
                Coordinate(coordinates[i - 1], coordinates[i]),
            )
            if (dist <= 10) {
                return true
            }
        }
    }
    return false
}
