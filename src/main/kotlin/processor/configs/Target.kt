package processor.configs

import com.fasterxml.jackson.annotation.*
import processor.actions.ActionEnum
import java.awt.Point
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage

class Target @JsonCreator constructor(
    @JsonProperty("action") val action: CustomAction,
    @JsonProperty("areaX") var areaX: Int,
    @JsonProperty("areaY") var areaY: Int,
    @JsonProperty("areaWidth") val areaWidth: Int,
    @JsonProperty("areaHeight") val areaHeight: Int,
) {
    @JsonIgnore
    fun getBufferedImage(): BufferedImage {
        return Robot().createScreenCapture(Rectangle(areaX, areaY, areaWidth, areaHeight))
    }
}

class ListCustomActions @JsonCreator constructor(
    @JsonProperty("delayBetweenActions")
    val delayBetweenActions: Long = 0,
    @JsonProperty("listActions")
    val listActions: List<CustomAction> = emptyList(),
)

class CustomAction @JsonCreator constructor(
    @JsonProperty("actionType") val actionType: ActionEnum,
    @JsonProperty("targetText") val targetText: String,
    @JsonProperty("locationX") var locationX: Int,
    @JsonProperty("locationY") var locationY: Int,
)

class ListTargets @JsonCreator constructor(
    @JsonProperty("delay")
    val delay: Long,
    @JsonProperty(value = "beforeActionsList")
    val beforeActionsList: ListCustomActions = ListCustomActions(),
    @JsonProperty(value = "afterActionsList")
    val afterActionsList: ListCustomActions = ListCustomActions(),
    @JsonProperty("targets")
    val targets: ArrayList<Target> = arrayListOf()
) {
    @JsonIgnore
    fun foundTarget(coords: Point): Target? {
        return targets.find { target: Target ->
            target.areaX == coords.x && target.areaY == coords.y
        }
    }
    @JsonIgnore
    fun foundActions(coords: Point): CustomAction? {
        val action = targets.find { action: Target ->
            action.action.locationX == coords.x &&  action.action.locationY == coords.y
        }?.action
        if (action != null) { return action }

        val beforeAction = beforeActionsList.listActions.find { beforeAction: CustomAction ->
            beforeAction.locationX == coords.x && beforeAction.locationY == coords.y
        }
        if (beforeAction != null) { return beforeAction }

        val afterAction = afterActionsList.listActions.find { afterAction: CustomAction ->
            afterAction.locationX == coords.x && afterAction.locationY == coords.y
        }
        if (afterAction != null) { return afterAction }

        return null
    }
}