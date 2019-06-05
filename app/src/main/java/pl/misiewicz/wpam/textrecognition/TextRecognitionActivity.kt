package pl.misiewicz.wpam.textrecognition

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_text_recognition.*
import kotlinx.android.synthetic.main.text_recognition_content.*
import pl.misiewicz.wpam.LoginActivity
import pl.misiewicz.wpam.R
import pl.misiewicz.wpam.classification.Sentiment
import pl.misiewicz.wpam.classification.SentimentClassificationResult
import pl.misiewicz.wpam.utils.ViewModelFactory
import pl.misiewicz.wpam.utils.saveInLocalCache

class TextRecognitionActivity : AppCompatActivity() {

    private lateinit var viewModel: TextRecognitionViewModel
    private lateinit var userId: String
    private lateinit var textRecognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_recognition)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(application)).get(TextRecognitionViewModel::class.java)

        textRecognizer = TextRecognizer()

        classification_fab.setOnClickListener {
            CropImage.activity().start(this)
        }

        viewModel.recognizedText.observe(this, Observer { recognizedText ->
            recognizedText?.let { recognizedTextField.text = it }
        })

        viewModel.imageUri.observe(this, Observer { imageUri ->
            imageUri?. let { uri ->
                Picasso.get().load(imageUri).into(textImageView)

                textImageView.setOnClickListener {
                    StfalconImageViewer.Builder<Uri>(this, arrayOf(uri)) { view, imageUri ->
                        Picasso.get().load(imageUri).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .into(view)
                    }.show()
                }
            }
        })

        viewModel.predictedSentiment.observe(this, Observer { classificationResult ->
            language_text_view.text = classificationResult.language
            sentiment_text_view.text = classificationResult.sentiment.sentiment

            when (classificationResult.sentiment){
                Sentiment.POSITIVE -> sentiment_text_view.setTextColor(getColor(R.color.green))
                Sentiment.NEGATIVE -> sentiment_text_view.setTextColor(getColor(R.color.red))
                Sentiment.NEUTRAL -> sentiment_text_view.setTextColor(getColor(R.color.blue))
                else -> throw RuntimeException("This never should happened")
            }
        })

        viewModel.isClassificationRunning.observe(this, Observer { isRunning ->
            if (isRunning) {
                classificationProgressBar.visibility = View.VISIBLE
                language_text_view.visibility = View.INVISIBLE
                language_title.visibility = View.INVISIBLE
                sentiment_text_view.visibility = View.INVISIBLE
                sentiment_title.visibility = View.INVISIBLE
            } else {
                classificationProgressBar.visibility = View.GONE
                language_text_view.visibility = View.VISIBLE
                language_title.visibility = View.VISIBLE
                sentiment_text_view.visibility = View.VISIBLE
                sentiment_title.visibility = View.VISIBLE
            }
        })

        if (intent.hasExtra("rawImageUri") && viewModel.imageUri.value == null && viewModel.recognizedText.value == null) {
            analyzeImage(Uri.parse(intent.getStringExtra("rawImageUri")))
        }

        if (intent.hasExtra("classificationResult")) {
            val classificationResult = intent.getSerializableExtra("classificationResult") as SentimentClassificationResult
            viewModel.imageUri.value = Uri.parse(classificationResult.imageUri)
            viewModel.predictedSentiment.value = classificationResult
            viewModel.recognizedText.value = classificationResult.text
        }

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            userId = auth.currentUser!!.uid
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val imageUri = result.uri
                viewModel.imageUri.value = imageUri
                textImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))

                analyzeImage(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "There was some error : ${result.error.message}", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }

    private fun analyzeImage(imageUri: Uri?) {
        if (imageUri == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        textRecognizer
            .analyze(bitmap)
            .addOnSuccessListener { result ->
                val mutableImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val recognizedText = textRecognizer.getText(result)
                textRecognizer.drawTextBoundaryOnBitmap(result, mutableImage)

                if (recognizedText.isNotEmpty()) {
                    viewModel.recognizedText.value = recognizedText
                    val outputFileUri = saveInLocalCache(
                        this,
                        mutableImage,
                        filename = "${System.currentTimeMillis()}.jpeg"
                    )
                    outputFileUri.let { uri -> viewModel.imageUri.value = uri }
                    viewModel.classifyText(userId, recognizedText)
                } else {
                    viewModel.recognizedText.value = "Nie odnaleziono tekstu na obrazie"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
                viewModel.isClassificationRunning.value = false
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            viewModel.recognizedText.postValue(null)
            viewModel.imageUri.postValue(null)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewModel.recognizedText.postValue(null)
            viewModel.imageUri.postValue(null)
        }
        return super.onKeyDown(keyCode, event)
    }
}
