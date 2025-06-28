package sessionswitcher.rules.conditions

import sessionswitcher.rules.Condition

abstract class StringCondition(val pattern: String, val operator: OPERATORS, val negative: Boolean = false): Condition() {
    public enum class OPERATORS(val description: String) {
        EXACT_MATCH("Matches exactly"),
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        REGEX_MATCH("Matches Regex"),
    }

    protected fun stringMatches(value: String): Boolean {
       return _stringMatches(value) xor negative
    }

    protected fun _stringMatches(value: String): Boolean {
        when (operator) {
            OPERATORS.STARTS_WITH -> {
                return value.lowercase().startsWith(pattern.lowercase())
            }
            OPERATORS.CONTAINS -> {
                return value.lowercase().contains(pattern.lowercase())
            }
            OPERATORS.ENDS_WITH -> {
                return value.lowercase().endsWith(pattern.lowercase())
            }
            OPERATORS.EXACT_MATCH -> {
                return value.lowercase() == pattern.lowercase()
            }
            OPERATORS.REGEX_MATCH -> {
                return value.matches(pattern.toRegex())
            }
        }
    }

    override fun getAvailableOperations(): List<String> = OPERATORS.entries.map { it.description }

    override fun matchOperation(): String = operator.description

    override fun canSetPattern(): Boolean = true

    override fun matchPattern(): String = pattern

    override fun validateConfiguration(): Pair<Boolean, String> {
        if (pattern.isEmpty()) {
            return Pair(false, "Cannot leave pattern empty")
        }
        return Pair(true, "")
    }

    override fun isNegativeMatch(): Boolean = negative

    override fun describe(): String {
        return "Rule: ${this.matchOn()} ${this.operator.description} \"${this.pattern}\""
    }
}