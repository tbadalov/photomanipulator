package ee.ut.photomanipulation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.soundcloud.android.crop.Crop
import ee.ut.photomanipulation.FileUtil.Companion.tmpFile
import ee.ut.photomanipulation.history.EventHistory
import ee.ut.photomanipulation.operations.MirrorOperation
import ee.ut.photomanipulation.operations.RotateOperation
import ee.ut.photomanipulation.operations.SaveOperation
import kotlinx.android.synthetic.main.activity_photo_edit.*
import java.io.File
import java.lang.ref.WeakReference


class PhotoEditActivity : AppCompatActivity() {

    private lateinit var history:EventHistory
    private lateinit var undo:MenuItem
    private lateinit var redo:MenuItem
    private lateinit var loadingFeedback: LoadingFeedback
    private var cropOutput:Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)
        loadingFeedback = LoadingFeedback(progressbar, this)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val imagePath = intent.getStringExtra("imagePath")
        val imageUri = Uri.fromFile(File(imagePath))
        history = EventHistory(imageUri)

        loadingFeedback.drawPicture(imageUri)

        btn_crop.setOnClickListener{ view ->
            cropOutput = tmpFile(this).toUri()
            Crop.of(history.current(), cropOutput).start(this)
        }
        btn_rotate_left.setOnClickListener{ view -> RotateOperation(loadingFeedback, history, this, true).execute() }
        btn_rotate_right.setOnClickListener{ view -> RotateOperation(loadingFeedback, history, this, false).execute() }
        btn_mirror_v.setOnClickListener{ view -> MirrorOperation(loadingFeedback, history, this, false).execute() }
        btn_mirror_h.setOnClickListener{ view -> MirrorOperation(loadingFeedback, history, this, true).execute() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            history.perform(cropOutput)
            loadingFeedback.drawPicture(cropOutput)
            loadingFeedback.onOperationFinished()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_activity_menu, menu)
        undo = menu?.findItem(R.id.undo)!!
        redo = menu.findItem(R.id.redo)!!
        loadingFeedback.menu = WeakReference(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.undo -> {
                loadingFeedback.drawPicture(history.undo())
                redo.isEnabled = true
                if (!history.canUndo()) { item.isEnabled = false }
                return true
            }
            R.id.redo -> {
                loadingFeedback.drawPicture(history.redo())
                undo.isEnabled = true
                if (!history.canRedo()) { item.isEnabled = false }
                return true
            }
            R.id.save -> { SaveOperation(loadingFeedback, history, this).execute() }
        }

        return false
    }
}
