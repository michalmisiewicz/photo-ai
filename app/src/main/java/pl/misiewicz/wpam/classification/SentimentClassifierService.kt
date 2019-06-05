package pl.misiewicz.wpam.classification

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.language.v1.AnalyzeSentimentRequest
import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.LanguageServiceClient
import com.google.cloud.language.v1.LanguageServiceSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.misiewicz.wpam.R

class SentimentClassifierService(context: Context) {

    private val languageServiceClient: LanguageServiceClient = let {
        LanguageServiceClient.create(
            LanguageServiceSettings
                .newBuilder()
                .setCredentialsProvider {
                    GoogleCredentials.fromStream(context.resources.openRawResource(R.raw.credential))
                }
                .build())
    }


    suspend fun classifyText(text: String) = run {
        withContext(Dispatchers.IO) {
            val request = AnalyzeSentimentRequest
                .newBuilder()
                .setDocument(
                    Document.newBuilder()
                        .setContent(text)
                        .setType(Document.Type.PLAIN_TEXT)
                        .build()
                )
                .build()
            val response = languageServiceClient.analyzeSentiment(request)
            val sentiment = interpretResults(response.documentSentiment.score, response.documentSentiment.magnitude)
            return@withContext SentimentClassificationResult(
                text,
                sentiment,
                response.language
            )
        }
    }

    private fun interpretResults(score: Float, magnitude: Float): Sentiment {
        return when {
            score >= 0.15 -> Sentiment.POSITIVE
            score <= -0.15 -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
    }
}