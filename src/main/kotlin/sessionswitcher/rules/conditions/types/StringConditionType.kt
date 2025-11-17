package sessionswitcher.rules.conditions.types

import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.ConditionType

abstract class StringConditionType(matchOn: String, needsResponse: Boolean):
    ConditionType(matchOn, needsResponse, availableOperations = OPERATORS.entries.map { it.description }, canSetPattern = true)
    {
    public enum class OPERATORS(val description: String) {
        EXACT_MATCH("Matches exactly"),
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        REGEX_MATCH("Matches Regex"),
    }

    protected fun stringMatches(configuration: ConditionConfiguration, value: String): Boolean {
        val match = when (configuration.operation) {
            OPERATORS.STARTS_WITH.description -> {
                value.lowercase().startsWith(configuration.pattern.get().lowercase())
            }
            OPERATORS.CONTAINS.description -> {
                value.lowercase().contains(configuration.pattern.get().lowercase())
            }
            OPERATORS.ENDS_WITH.description -> {
                value.lowercase().endsWith(configuration.pattern.get().lowercase())
            }
            OPERATORS.EXACT_MATCH.description -> {
                value.equals(configuration.pattern.get(), ignoreCase = true)
            }
            OPERATORS.REGEX_MATCH.description -> {
                value.matches(configuration.pattern.get().toRegex())
            }
            else -> throw IllegalArgumentException("Unknown operation: ${configuration.operation}")
        }
       return match xor configuration.negativeMatch
    }

    override fun validateConfiguration(configuration: ConditionConfiguration): Pair<Boolean, String> {
        if (!configuration.pattern.isPresent || configuration.pattern.get().isBlank()) {
            return Pair(true, "Pattern is empty!")
        }
        if (!availableOperations.contains(configuration.operation)) {
            return Pair(false, "Unknown operation! Send bug report.")
        }
        return Pair(true, "")
    }

    override fun describe(configuration: ConditionConfiguration): String {
        return "${this.matchOn} ${configuration.operation} \"${configuration.pattern.get()}\""
    }
}