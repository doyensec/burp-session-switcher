package sessionswitcher.rules.conditions

import java.util.*

data class ConditionConfig(public val operation: String, public val pattern: Optional<String>, public val negativeMatch: Boolean)