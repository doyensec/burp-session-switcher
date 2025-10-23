package sessionswitcher.rules.conditions

import sessionswitcher.rules.Condition
import java.util.*

abstract class StringCondition(matchOn: String, needsResponse: Boolean, pattern: String, val operator: OPERATORS, negative: Boolean = false):
    Condition(
        Properties(matchOn = matchOn, needsResponse = needsResponse, availableOperations = OPERATORS.entries.map { it.description }, canSetPattern = true),
        Configuration(operation = operator.description, negativeMatch = negative, pattern = Optional.of(pattern))
    ) {
    public enum class OPERATORS(val description: String) {
        EXACT_MATCH("Matches exactly"),
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        REGEX_MATCH("Matches Regex"),
    }

    protected fun stringMatches(value: String): Boolean {
       return _stringMatches(value) xor configuration.negativeMatch
    }

    protected fun _stringMatches(value: String): Boolean {
        when (operator) {
            OPERATORS.STARTS_WITH -> {
                return value.lowercase().startsWith(configuration.pattern.get().lowercase())
            }
            OPERATORS.CONTAINS -> {
                return value.lowercase().contains(configuration.pattern.get().lowercase())
            }
            OPERATORS.ENDS_WITH -> {
                return value.lowercase().endsWith(configuration.pattern.get().lowercase())
            }
            OPERATORS.EXACT_MATCH -> {
                return value.equals(configuration.pattern.get(), ignoreCase = true)
            }
            OPERATORS.REGEX_MATCH -> {
                return value.matches(configuration.pattern.get().toRegex())
            }
        }
    }

    override fun validateConfiguration(): Pair<Boolean, String> {
        if (!configuration.pattern.isPresent || configuration.pattern.get().isBlank()) {
            return Pair(false, "Cannot leave pattern empty")
        }
        return Pair(true, "")
    }

    override fun describe(): String {
        return "Rule: ${this.properties.matchOn} ${this.operator.description} \"${this.configuration.pattern.get()}\""
    }
}