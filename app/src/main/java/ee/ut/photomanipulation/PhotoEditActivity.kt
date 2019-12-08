package ee.ut.photomanipulation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PhotoEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)
        val bytes = intent.getByteArrayExtra("data")
        //val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
