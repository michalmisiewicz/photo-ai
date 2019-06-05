package pl.misiewicz.wpam.classificationhistory

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.joda.time.DateTime
import pl.misiewicz.wpam.R
import pl.misiewicz.wpam.classification.Sentiment
import pl.misiewicz.wpam.classification.SentimentClassificationResult
import pl.misiewicz.wpam.textrecognition.TextRecognitionActivity
import pl.misiewicz.wpam.utils.dpToPx


class ClassificationHistoryAdapter(private val context: Context) : RecyclerView.Adapter<ClassificationHistoryAdapter.ClassificationResultHolder>() {

    private var classificationHistory: List<SentimentClassificationResult> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassificationResultHolder {
        return ClassificationResultHolder(
            LayoutInflater.from(context).inflate(R.layout.classification_history_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ClassificationResultHolder, position: Int) {
        holder.image.setImageResource(R.mipmap.text_recognition)
        holder.sentiment.text = classificationHistory[position].sentiment.sentiment
        holder.language.text = classificationHistory[position].language
        holder.datetime.text = DateTime(classificationHistory[position].timestamp).toString("yyyy-MM-dd HH:mm:ss")
        classificationHistory[position].imageUri?.let { imageUri ->
            val thumbnailSize = dpToPx(context, 100)
            Picasso.get().load(imageUri).resize(thumbnailSize, thumbnailSize).centerCrop().into(holder.image)
        }

        when (classificationHistory[position].sentiment){
            Sentiment.POSITIVE -> holder.sentiment.setTextColor(getColor(context, R.color.green))
            Sentiment.NEGATIVE -> holder.sentiment.setTextColor(getColor(context, R.color.red))
            Sentiment.NEUTRAL -> holder.sentiment.setTextColor(getColor(context, R.color.blue))
            else -> throw RuntimeException("This never should happened")
        }

        holder.view.setOnClickListener {
            val intent = Intent(context, TextRecognitionActivity::class.java).putExtra("classificationResult", classificationHistory[position])
            context.startActivity(intent)
        }
    }

    fun setData(newData: List<SentimentClassificationResult>) {
        this.classificationHistory = newData
        notifyDataSetChanged()
    }

    override fun getItemCount() = classificationHistory.size

    class ClassificationResultHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view = itemView
        val image = itemView.findViewById<ImageView>(R.id.textImageView)!!
        val sentiment = itemView.findViewById<TextView>(R.id.sentiment_text_view)!!
        val language = itemView.findViewById<TextView>(R.id.language_text_view)!!
        val datetime = itemView.findViewById<TextView>(R.id.date_field)!!
    }
}