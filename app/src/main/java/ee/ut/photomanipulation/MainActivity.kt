package ee.ut.photomanipulation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val MAIN_ACTIVITY = "mainActivity"
    val REQUEST_IMAGE_CAPTURE = 1
    var pictureAdapter: PictureAdapter? = null
    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, 10)
        } else {
            initApplication()
        }
    }

    private fun initApplication(){
        val uri = EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaColumns.DATA, BUCKET_DISPLAY_NAME, MediaColumns._ID)
        val cursor = contentResolver.query(uri, projection, null, null, MediaColumns.DATE_ADDED + " DESC")!!
        initPictureAdapter(cursor)

        fab.setOnClickListener {
            Log.v(MAIN_ACTIVITY, "fab")
            dispatchTakePictureIntent()
        }

        fab2.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hasPermissions(): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == -1) {
                return false
            }
        }
        return true
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            10 -> {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(
                            this,
                            "Permission denied, application will not work as expected!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return
                    }
                }
                initApplication()
            }
        }
    }

    fun startSettingsActivity(menu: MenuItem){
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }

}
