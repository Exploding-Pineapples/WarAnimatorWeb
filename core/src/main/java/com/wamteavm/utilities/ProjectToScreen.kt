package com.wamteavm.utilities

import com.wamteavm.WarAnimator
import com.wamteavm.models.Coordinate

fun projectToScreen(position: Coordinate, zoom: Float, cx: Float, cy: Float): Coordinate {
    return Coordinate(
        position.x * zoom - cx * (zoom - 1) + (WarAnimator.DISPLAY_WIDTH / 2 - cx),
        position.y * zoom - cy * (zoom - 1) + (WarAnimator.DISPLAY_HEIGHT / 2 - cy)
    )
}
