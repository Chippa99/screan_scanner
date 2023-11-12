package processor.actions

import java.awt.Point
import java.awt.Robot

class NothingAction: Action {
    companion object {
        val robot = Robot()
    }

    override fun make(location: Point): Any {
        robot.mouseMove(location.x, location.y)
        return "clicked"
    }
}