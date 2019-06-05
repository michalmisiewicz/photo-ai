package pl.misiewicz.wpam.classificationhistory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.classification_history_activity.*
import pl.misiewicz.wpam.LoginActivity
import pl.misiewicz.wpam.R
import pl.misiewicz.wpam.classification.SentimentClassificationResult
import pl.misiewicz.wpam.textrecognition.TextRecognitionActivity

class ClassificationHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: ClassificationHistoryAdapter
    private lateinit var classificationHistory: LiveData<List<SentimentClassificationResult>>
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_bar_name)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val viewModel = ViewModelProviders.of(this).get(ClassificationHistoryViewModel::class.java)

        initRecyclerView()

        classification_fab.setOnClickListener {
            CropImage.activity().start(this)
        }

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            userId = auth.currentUser!!.uid
        }

        classificationHistory = viewModel.classificationHistory(userId)

        classificationHistory.observe(this, Observer { history -> run {
            if (history.isNotEmpty()) {
                button_arrow.visibility = View.GONE
                first_visit_message.visibility = View.GONE
                click_button_message.visibility = View.GONE
            }
            adapter.setData(history)
        } })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu_item -> {
                AuthUI.getInstance().signOut(this).addOnSuccessListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val imageUri = result.uri
                val intent = Intent(this, TextRecognitionActivity::class.java).putExtra("rawImageUri", imageUri.toString())
                startActivity(intent)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "There was some error : ${result.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    private fun initRecyclerView() {
        adapter = ClassificationHistoryAdapter(this)
        main_activity_recycler_view.layoutManager = LinearLayoutManager(this)
        main_activity_recycler_view.adapter = adapter
    }
}
