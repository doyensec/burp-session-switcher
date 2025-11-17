package sessionswitcher.rules.refresher

import sessionswitcher.rules.conditions.Condition

class RefreshRule {
    private val conditions = ArrayList<Condition>()
    private lateinit var actions: RefreshAction
}