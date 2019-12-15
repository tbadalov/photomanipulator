package ee.ut.photomanipulation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_photo_edit.*
import java.io.File


class PhotoEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val imagePath = intent.getStringExtra("imagePath")
        val imageUri = Uri.fromFile(File(imagePath))

        val outUri = Uri.parse("file:///storage/emulated/0/output.jpeg")
        drawPicture(imageUri)

        btn_crop.setOnClickListener{ view -> Crop.of(imageUri, outUri).start(this)}
        btn_mirror_vertical.setOnClickListener{ view -> img_edit.scaleY = -img_edit.scaleY}
        btn_mirror_horizontal.setOnClickListener{ view -> img_edit.scaleX = -img_edit.scaleX}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            //show cropped pic
        }
    }

    private fun drawPicture(imgUri:Uri) {
        Picasso.with(this).load(imgUri.toString()).into(img_edit)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_activity_menu, menu)
        return true
    }
}
