package com.example.knumap.ui.theme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.knumap.R
import com.example.knumap.model.Post

class PostWriteActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_write)

        imageUri = intent.getParcelableExtra("imageUri") ?: return

        findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val description = findViewById<EditText>(R.id.descriptionEdit).text.toString()
            val locationName = findViewById<TextView>(R.id.locationTitle).text.toString()
            val locationDetail = findViewById<TextView>(R.id.locationDetail).text.toString()

            val post = Post(
                imageUri = imageUri,
                locationName = locationName,
                locationDetail = locationDetail,
                description = description,
                hashtags = extractHashtags(description)
            )

            val intent = Intent().apply {
                putExtra("newPost", post)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun extractHashtags(text: String): List<String> {
        return Regex("#(\\w+)").findAll(text).map { it.value }.toList()
    }
}
