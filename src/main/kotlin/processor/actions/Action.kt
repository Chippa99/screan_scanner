package processor.actions

import java.awt.Point

interface Action {
    fun make(location: Point): Any
}

enum class ActionEnum {
    CLICK,
    NOTHING
}