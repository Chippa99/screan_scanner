package processor.actions

class ActionFactory {
    //replace location on ActionContext
    fun getAction(action: ActionEnum): Action {
        return when (action) {
            ActionEnum.CLICK -> ClickAction()
            ActionEnum.NOTHING -> NothingAction()
        }
    }
}