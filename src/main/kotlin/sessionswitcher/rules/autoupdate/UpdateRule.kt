package sessionswitcher.rules.autoupdate

import sessionswitcher.rules.conditions.Condition
import sessionswitcher.sessions.Session

class UpdateRule(val conditions: Array<Condition>, val session: Session, val config: UpdateConfig) {

}