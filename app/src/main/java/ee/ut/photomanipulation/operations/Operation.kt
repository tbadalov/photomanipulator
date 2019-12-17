package ee.ut.photomanipulation.operations

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import ee.ut.photomanipulation.FileUtil.Companion.tmpFile
import ee.ut.photomanipulation.LoadingFeedback
import ee.ut.photomanipulation.history.EventHistory
import java.io.File


abstract class Operation(loadingFeedback:LoadingFeedback, history:EventHistory, context: Context) : AsyncTask<String, Void, String>() {

    val loadingFeedback = loadingFeedback
    val history = history
    val context = context

    override fun onPreExecute() {
        loadingFeedback.showLoading()
    }

    override fun doInBackground(vararg params: String?): String {
        val src = BitmapFactory.decodeFile(history.current().path.toString())
        val dst = perform(src)
        return saveToFile(dst)
    }

    override fun onPostExecute(resultPath: String) {
        val resultUri = Uri.fromFile(File(resultPath))
        history.perform(resultUri)
        loadingFeedback.drawPicture(resultUri)
        loadingFeedback.onOperationFinished()
        loadingFeedback.hideLoading()
    }

    private fun saveToFile(bitmap: Bitmap) : String {
        val outputFile = tmpFile(context)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputFile.outputStream())
        return outputFile.path.toString()
    }

    abstract fun perform(src: Bitmap) : Bitmap
}