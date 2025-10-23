package sessionswitcher.rules.refresher

import sessionswitcher.rules.conditions.Condition

class RefresherRule {
    private val conditions = ArrayList<Condition>()
    private val actions = ArrayList<RefreshAction>()
}