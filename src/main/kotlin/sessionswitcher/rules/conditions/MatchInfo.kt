package sessionswitcher.rules.conditions

data class MatchInfo(
    val matchedCookies: List<Pair<String, String>> = listOf(),
    val matchedHeaders: List<Pair<String, String>> = listOf(),
)
