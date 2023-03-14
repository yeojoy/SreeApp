package com.livebarn.android.sree.util

import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

object ConfettiManager {
    fun rain(): List<Party> {
        return listOf(
            Party(
                speed = 0f,
                maxSpeed = 15f,
                damping = 0.9f,
                angle = Angle.BOTTOM,
                spread = Spread.ROUND,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(100),
                position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
            )
        )
    }

    /**
     * x: x-coord. Percent of width. 0.0 is left edge, and 1.0 is right edge
     * y: y-coord. Percent of height. 0.0 is top edge, and 1.0 is bottom edge.
     */
    fun explode(x: Double, y: Double): List<Party> {
        val xCoord = if (x > 1.0 || x < 0.0) 0.5 else x
        val yCoord = if (y > 1.0 || x < 0.0) 0.5 else y

        return listOf(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(xCoord, yCoord)
            )
        )
    }
}