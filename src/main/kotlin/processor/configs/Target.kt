package processor.configs

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import processor.actions.ActionEnum
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage

class Target @JsonCreator constructor(
    @JsonProperty("action") val action: CustomAction,
    @JsonProperty("areaX") val areaX: Int,
    @JsonProperty("areaY") val areaY: Int,
    @JsonProperty("areaWidth") val areaWidth: Int,
    @JsonProperty("areaHeight") val areaHeight: Int,
) {

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
    @JsonProperty("targetText")  val targetText: String,
    @JsonProperty("locationX") val locationX: Int,
    @JsonProperty("locationY") val locationY: Int,
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
)