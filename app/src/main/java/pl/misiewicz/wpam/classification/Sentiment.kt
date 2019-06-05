package pl.misiewicz.wpam.classification

enum class Sentiment(val sentiment: String) {
    POSITIVE("pozytywny"),
    NEGATIVE("negatywny"),
    NEUTRAL("neutralny"),
    PLACEHOLDER("placeholder")
}