package pl.misiewicz.wpam.textrecognition

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.misiewicz.wpam.classification.SentimentClassificationRepository
import pl.misiewicz.wpam.classification.SentimentClassificationResult
import pl.misiewicz.wpam.classification.SentimentClassifierService


class TextRecognitionViewModel(private val sentimentClassifierService: SentimentClassifierService) : ViewModel() {

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val sentimentClassificationRepository = SentimentClassificationRepository.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    var imageUri: MutableLiveData<Uri> = MutableLiveData()
    var recognizedText: MutableLiveData<String> = MutableLiveData()
    var predictedSentiment: MutableLiveData<SentimentClassificationResult> = MutableLiveData()
    var isClassificationRunning: MutableLiveData<Boolean> = MutableLiveData()

    init {
        isClassificationRunning.postValue(false)
    }

    fun classifyText(userId: String, text: String) {
        coroutineScope.launch(Dispatchers.Main) {
            isClassificationRunning.postValue(true)
            val classificationResult = sentimentClassifierService.classifyText(text)
            predictedSentiment.postValue(classificationResult)
            isClassificationRunning.postValue(false)
            val itemKey = sentimentClassificationRepository.add(userId, classificationResult)
            val uploadRef = storage.child("images/").child("${System.currentTimeMillis()}.jpeg")
            uploadRef.putFile(imageUri.value!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    uploadRef.downloadUrl
                }.addOnSuccessListener { downloadUri ->
                    sentimentClassificationRepository.addImageUri(userId, itemKey, downloadUri)
                }
        }
    }
}