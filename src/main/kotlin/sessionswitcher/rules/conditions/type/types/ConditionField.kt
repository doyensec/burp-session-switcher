package sessionswitcher.rules.conditions.type.types

class ConditionField private constructor(
    val name: String,
    val type: FieldType,
) {
    var choices: Array<String>? = null
        private set
    var defaultText: String? = null
        private set
    var defaultBoolean: Boolean = false
        private set
    var defaultChoice: String? = null
        private set

    // Multiple choice constructor
    private constructor(name: String, choices: Array<String>, defaultChoice: String? = null) : this(name, FieldType.MULTIPLE_CHOICE) {
        if (choices.isEmpty()) throw IllegalArgumentException("Choices cannot be empty")
        this.choices = choices.copyOf()
        if (defaultChoice == null) return
        if (defaultChoice !in choices) throw IllegalArgumentException("Default choice $defaultChoice is not in choices $choices")
        this.defaultChoice = defaultChoice
    }

    // Text field constructor
    private constructor(name: String, defaultText: String? = null) : this(name, FieldType.TEXT) {
        this.defaultText = defaultText
    }

    // Boolean field constructor
    private constructor(name: String, defaultBoolean: Boolean?) : this(name, FieldType.BOOLEAN) {
        this.defaultBoolean = defaultBoolean ?: false
    }

    companion object {
        fun makeTextField(
            name: String,
            defaultText: String? = null,
        ) = ConditionField(name, defaultText)

        fun makeMultipleChoiceField(
            name: String,
            choices: Array<String>,
            defaultChoice: String? = null,
        ) = ConditionField(name, choices, defaultChoice)

        fun makeBooleanField(
            name: String,
            defaultBoolean: Boolean? = null,
        ) = ConditionField(name, defaultBoolean)
    }

    public enum class FieldType {
        TEXT,
        MULTIPLE_CHOICE,
        BOOLEAN,
    }
}
