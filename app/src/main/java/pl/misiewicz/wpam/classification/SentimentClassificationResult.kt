package pl.misiewicz.wpam.classification

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class SentimentClassificationResult(
    val text: String = "",
    val sentiment: Sentiment = Sentiment.PLACEHOLDER,
    val language: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var imageUri: String? = null) : Serializable {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "text" to text,
            "sentiment" to sentiment.name,
            "language" to language,
            "imageUri" to imageUri,
            "timestamp" to timestamp
        )
    }
}