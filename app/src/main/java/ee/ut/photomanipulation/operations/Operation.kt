package ee.ut.photomanipulation.operations

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import ee.ut.photomanipulation.LoadingFeedback
import ee.ut.photomanipulation.history.EventHistory
import java.io.File


abstract class Operation(loadingFeedback:LoadingFeedback, history:EventHistory) : AsyncTask<String, Void, String>() {

    val loadingFeedback = loadingFeedback
    val history = history

    override fun onPreExecute() {
        loadingFeedback.showLoading()
    }

    override fun doInBackground(vararg params: String?): String {
        val src = BitmapFactory.decodeFile(history.current().path.toString())
        val dst = perform(src)
        return saveToFile(dst)
    }

    override fun onPostExecute(resultPath: String?) {
        val resultUri = Uri.fromFile(File(resultPath))
        history.perform(resultUri)
        loadingFeedback.drawPicture(resultUri)
        loadingFeedback.onOperationFinished()
        loadingFeedback.hideLoading()
    }

    private fun saveToFile(bitmap: Bitmap) : String {
        return "/storage/emulated/0/output.jpeg" //TODO to real saving to temp file with random name
    }

    abstract fun perform(src: Bitmap) : Bitmap
}