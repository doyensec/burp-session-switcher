package sessionswitcher.automode

data class MatchInfo(public val matchedCookies: List<Pair<String, String>> = listOf(), public val matchedHeaders: List<Pair<String, String>> = listOf())