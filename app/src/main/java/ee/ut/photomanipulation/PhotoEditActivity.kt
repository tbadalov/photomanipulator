package ee.ut.photomanipulation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_photo_edit.*


class PhotoEditActivity : AppCompatActivity() {

    private lateinit var imgUri: Uri

//    private val listenerMapping = mapOf<Int, View.OnClickListener>(
//        R.id.btn_crop to ((v) -> {})
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)
        imgUri = Uri.parse("file:///storage/emulated/0/WhatsApp/Media/WhatsApp%20Images/IMG-20191208-WA0021.jpeg")
        val outUri = Uri.parse("file:///storage/emulated/0/output.jpeg")
        drawPicture(imgUri)
        btn_crop.setOnClickListener{view -> Crop.of(imgUri, outUri).start(this)}
        btn_mirror_vertical.setOnClickListener{view -> img_edit.scaleY = -img_edit.scaleY}
        btn_mirror_horizontal.setOnClickListener{view -> img_edit.scaleX = -img_edit.scaleX}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            //show cropped pic
        }
    }

    private fun drawPicture(imgUri:Uri) {
        Picasso.with(this).load(imgUri.toString()).into(img_edit)
    }
}
