package sessionswitcher.rules.conditions

import java.util.*

data class ConditionConfiguration(public val operation: String, public val pattern: Optional<String>, public val negativeMatch: Boolean)