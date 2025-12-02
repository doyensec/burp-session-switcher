package sessionswitcher.rules.conditions.type.types

import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.type.ConditionType

abstract class StringConditionType(matchOn: String, matchesOnResponse: Boolean) :
    ConditionType(
        matchOn,
        matchesOnResponse,
        availableOperations = OPERATORS.entries.map { it.description },
        extraFields = listOf(ConditionField.makeTextField("Pattern"))
    ) {
    enum class OPERATORS(val description: String) {
        EXACT_MATCH("Matches exactly"),
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        REGEX_MATCH("Matches Regex");

        companion object {
            fun fromDescription(description: String): OPERATORS {
                return entries.find { it.description == description } ?: throw IllegalArgumentException("Unknown operation: $description")
            }
        }
    }

    companion object {
        fun stringMatches(
            pattern: String,
            operation: OPERATORS,
            value: String,
            negativeMatch: Boolean
        ): Boolean {
            val match = when (operation) {
                OPERATORS.STARTS_WITH -> {
                    value.lowercase().startsWith(pattern.lowercase())
                }

                OPERATORS.CONTAINS -> {
                    value.lowercase().contains(pattern.lowercase())
                }

                OPERATORS.ENDS_WITH -> {
                    value.lowercase().endsWith(pattern.lowercase())
                }

                OPERATORS.EXACT_MATCH -> {
                    value.equals(pattern, ignoreCase = true)
                }

                OPERATORS.REGEX_MATCH -> {
                    value.matches(pattern.toRegex())
                }
            }
            return match xor negativeMatch
        }
    }

    fun stringMatches(configuration: ConditionConfig, value: String, negativeMatch: Boolean = false): Boolean {
        val operation = OPERATORS.fromDescription(configuration.operation)
        val pattern = configuration.extraFields["Pattern"] ?: throw IllegalArgumentException("Pattern is null!")

        return StringConditionType.stringMatches(pattern, operation, value, negativeMatch)
    }

    override fun validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String> {
        val pattern = configuration.extraFields["Pattern"]

        if (pattern == null || pattern.isBlank()) {
            return Pair(true, "Pattern is empty!")
        }
        if (!availableOperations.contains(configuration.operation)) {
            return Pair(false, "Unknown operation! Send bug report.")
        }
        if (configuration.operation == OPERATORS.REGEX_MATCH.description) {
            try {
                Regex(pattern)
            } catch (e: Exception) {
                return Pair(false, e.message ?: "Invalid Regex")
            }
        }
        return Pair(true, "")
    }

    override fun describe(configuration: ConditionConfig): String {
        val pattern = configuration.extraFields["Pattern"] ?: throw IllegalArgumentException("Pattern is null!")

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
        return "${this.matchOn} $operation \"$pattern\""
    }
}