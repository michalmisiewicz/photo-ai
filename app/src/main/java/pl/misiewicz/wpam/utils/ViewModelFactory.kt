package pl.misiewicz.wpam.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.misiewicz.wpam.classification.SentimentClassifierService
import pl.misiewicz.wpam.textrecognition.TextRecognitionViewModel


class ViewModelFactory private constructor(private val sentimentClassifierService: SentimentClassifierService): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(TextRecognitionViewModel::class.java) ->
                    TextRecognitionViewModel(sentimentClassifierService)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T


    companion object {

        @Volatile private var INSTANCE: ViewModelFactory? = null

        fun getInstance(application: Application) =
            INSTANCE
                ?: synchronized(ViewModelFactory::class.java) {
                INSTANCE ?: ViewModelFactory(
                    SentimentClassifierService(application)
                )
                    .also { INSTANCE = it }
            }


        fun destroyInstance() {
            INSTANCE = null
        }
    }
}