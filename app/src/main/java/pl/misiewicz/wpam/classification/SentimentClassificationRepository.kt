package pl.misiewicz.wpam.classification

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*

class SentimentClassificationRepository private constructor() {

    private val remoteDB = FirebaseDatabase.getInstance()
    private var database: DatabaseReference
    private val TAG = "SentimentClassificationRepository"

    init {
        remoteDB.setPersistenceEnabled(true)
        database = remoteDB.reference
    }

    companion object {
        @Volatile private var INSTANCE: SentimentClassificationRepository? = null

        fun getInstance() = INSTANCE
            ?: SentimentClassificationRepository().also { INSTANCE = it }
    }

    fun getAllTask(userId: String): LiveData<List<SentimentClassificationResult>> {
        val result = MutableLiveData<List<SentimentClassificationResult>>()

        database.child("classification/$userId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val res = snapshot.children.map { classificationSnapshot -> classificationSnapshot.getValue(SentimentClassificationResult::class.java)!! }
                    result.postValue(res)
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.e(TAG, p0.message)
                }
            })

        return result
    }

    fun add(userId: String, classificationResult: SentimentClassificationResult): String {
        val key = database.child("classification").push().key

        database.child("classification/$userId/${key!!}").setValue(classificationResult.toMap())
        return key
    }

    fun addImageUri(userId: String, key: String, uri: Uri) {
        database.child("classification/$userId/$key/imageUri").setValue(uri.toString()).addOnFailureListener { e ->
            Log.e("SentimentClassificationRepository", e.message)
        }
    }
}