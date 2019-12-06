package ee.ut.photomanipulation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PictureActivity : AppCompatActivity() {
    val TAG = "pictureActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        val imagePath = intent.getStringExtra("imagePath")
        val imageUri = Uri.fromFile(File(imagePath))

        val bitmap = BitmapFactory.decodeFile(imagePath)

        findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)
    }
}