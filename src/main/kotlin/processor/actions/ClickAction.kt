package processor.actions

import java.awt.Point
import java.awt.Robot
import java.awt.event.InputEvent
import kotlin.random.Random

class ClickAction : Action {
    companion object {
        val robot = Robot()
    }

    override fun make(location: Point): Any {
        robot.mouseMove(location.x, location.y)

        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        Thread.sleep(Random.nextLong(100,200))
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        return "clicked"
    }
}