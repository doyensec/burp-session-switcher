package sessionswitcher.rules.conditions.types

import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.ConditionTypeInstance

abstract class StringConditionType(matchOn: String, matchesOnResponse: Boolean) :
    ConditionTypeInstance(
        matchOn,
        matchesOnResponse,
        availableOperations = OPERATORS.entries.map { it.description },
        canSetPattern = true
    ) {
    enum class OPERATORS(val description: String) {
        EXACT_MATCH("Matches exactly"),
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        REGEX_MATCH("Matches Regex"),
    }

    protected fun stringMatches(
        configuration: ConditionConfig,
        value: String,
        negativeMatch: Boolean = configuration.negativeMatch
    ): Boolean {
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
        return match xor negativeMatch
    }

    override fun validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String> {
        if (!configuration.pattern.isPresent || configuration.pattern.get().isBlank()) {
            return Pair(true, "Pattern is empty!")
        }
        if (!availableOperations.contains(configuration.operation)) {
            return Pair(false, "Unknown operation! Send bug report.")
        }
        if (configuration.operation == OPERATORS.REGEX_MATCH.description) {
            try {
                Regex(configuration.pattern.get())
            } catch (e: Exception) {
                return Pair(false, e.message ?: "Invalid Regex")
            }
        }
        return Pair(true, "")
    }

    override fun describe(configuration: ConditionConfig): String {
        val operation = if (configuration.negativeMatch) {
            when (configuration.operation) {
                OPERATORS.EXACT_MATCH.description -> "does NOT match exactly"
                OPERATORS.CONTAINS.description -> "does NOT contain"
                OPERATORS.STARTS_WITH.description -> "does NOT start with"
                OPERATORS.ENDS_WITH.description -> "does NOT end with"
                OPERATORS.REGEX_MATCH.description -> "does NOT match Regex"
                else -> {
                    configuration.operation
                }
            }
        } else {
            configuration.operation.lowercase()
        }
        return "${this.matchOn} $operation \"${configuration.pattern.get()}\""
    }
}