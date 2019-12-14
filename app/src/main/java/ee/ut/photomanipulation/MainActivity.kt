package ee.ut.photomanipulation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.BUCKET_DISPLAY_NAME
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    val MAIN_ACTIVITY = "mainActivity"
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_CAMERA_API = 2
    var pictureAdapter: PictureAdapter? = null
    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA)
    private var mStorageRef: FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, 10)
        } else {
            initApplication()
            mStorageRef = FirebaseStorage.getInstance()
        }
    }

    override fun onResume() {
        super.onResume()
        initCamera()
    }

    private fun initApplication() {
        val uri = EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaColumns.DATA, BUCKET_DISPLAY_NAME, MediaColumns._ID)
        val cursor = contentResolver.query(uri, projection, null, null, MediaColumns.DATE_ADDED + " DESC")!!
        initPictureAdapter(cursor)
        initCamera()

    }

    private fun initCamera() {
        val settings = getSharedPreferences("settings", 0)
        val useCustomCamera = settings.getBoolean("useCustomCamera", false)

        if (useCustomCamera) {
            fab.setOnClickListener {
                val intent = Intent(this, CameraActivity::class.java)
                startActivityForResult(intent, REQUEST_CAMERA_API)
            }
        } else {
            fab.setOnClickListener {
                dispatchTakePictureIntent()
            }
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val insertImage =
                MediaStore.Images.Media.insertImage(contentResolver, imageBitmap, "", "")
            val settings = getSharedPreferences("settings", 0)
            if(settings.getBoolean("cloudSync", false)) uploadToFireStorage(insertImage)

        }
        else if (requestCode == REQUEST_CAMERA_API && resultCode == RESULT_OK) {
            val imageUri = data?.extras?.get("imageUri") as String
            val settings = getSharedPreferences("settings", 0)
            if(settings.getBoolean("cloudSync", false)) uploadToFireStorage(imageUri)
        }
    }

    private fun uploadToFireStorage(insertImage: String?) {
        val path = getPath(Uri.parse(insertImage))
        val file = Uri.fromFile(File(path!!))
        val riversRef = mStorageRef?.reference?.child(path)

        riversRef?.putFile(file)?.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(this, R.string.picture_upload_success, Toast.LENGTH_LONG).show()
        }?.addOnFailureListener(OnFailureListener {
            Toast.makeText(this, R.string.picture_upload_fail, Toast.LENGTH_LONG)
                .show()
        })
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
                            R.string.no_permission,
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

    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        if (cursor != null) {
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        }
        return uri?.path
    }

    fun startSettingsActivity(menu: MenuItem){
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }
}
