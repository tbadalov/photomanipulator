package ee.ut.photomanipulation

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.BUCKET_DISPLAY_NAME
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val MAIN_ACTIVITY = "mainActivity"
    val REQUEST_IMAGE_CAPTURE = 1
    var pictureAdapter: PictureAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val uri = EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaColumns.DATA, BUCKET_DISPLAY_NAME, MediaColumns._ID)
        val cursor = contentResolver.query(uri, projection, null, null, MediaColumns.DATE_ADDED + " DESC")!!
        initPictureAdapter(cursor)
        fab.setOnClickListener {
            Log.v(MAIN_ACTIVITY, "fab")
            dispatchTakePictureIntent()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    private fun initPictureAdapter(cursor: Cursor) {
        pictureAdapter = PictureAdapter(this, cursor)
        pictures_listview.adapter = pictureAdapter
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            MediaStore.Images.Media.insertImage(contentResolver ,imageBitmap ,"" , "")
        }
    }

    fun startSettingsActivity(menu: MenuItem){
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }

}
