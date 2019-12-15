package ee.ut.photomanipulation

import android.net.Uri
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_photo_edit.*
import java.lang.ref.WeakReference

class LoadingFeedback(progressBar:ProgressBar, activity: AppCompatActivity) {

    val progressBar = WeakReference<ProgressBar>(progressBar)
    val activity = WeakReference<AppCompatActivity>(activity)
    lateinit var menu:WeakReference<Menu>

    fun showLoading() {
        progressBar.get()?.visibility = View.VISIBLE
        activity.get()?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun hideLoading() {
        progressBar.get()?.visibility = View.GONE
        activity.get()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun drawPicture(imgUri: Uri) {
        val imgView = activity.get()?.img_edit
        Picasso.with(activity.get()).load(imgUri.toString()).into(imgView)
    }

    fun onOperationFinished() {
        menu.get()?.findItem(R.id.undo)?.isEnabled = true
        menu.get()?.findItem(R.id.redo)?.isEnabled = false
    }

}