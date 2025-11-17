package sessionswitcher.rules.refresher

import sessionswitcher.rules.conditions.Condition
import sessionswitcher.sessions.Session

class RefreshRule(val conditions: Array<Condition>, val session: Session, val config: RefreshConfig) {

}