package pl.misiewicz.wpam.classificationhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import pl.misiewicz.wpam.classification.SentimentClassificationRepository
import pl.misiewicz.wpam.classification.SentimentClassificationResult

class ClassificationHistoryViewModel : ViewModel() {

    private val sentimentClassificationRepository = SentimentClassificationRepository.getInstance()

    fun classificationHistory(userId: String): LiveData<List<SentimentClassificationResult>> {
        return sentimentClassificationRepository.getAllTask(userId)
    }
}