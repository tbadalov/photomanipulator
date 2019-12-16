package ee.ut.photomanipulation.operations

import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import ee.ut.photomanipulation.FileUtil
import ee.ut.photomanipulation.LoadingFeedback
import ee.ut.photomanipulation.history.EventHistory

class SaveOperation(loadingFeedback: LoadingFeedback, history: EventHistory, context: Context) : Operation(loadingFeedback, history, context) {

    override fun perform(src: Bitmap): Bitmap {
        MediaStore.Images.Media.insertImage(
            context.contentResolver,
            src,
            FileUtil.tmpFile(context).name,
            "")
        return src
    }

    override fun onPostExecute(resultPath: String) {
        loadingFeedback.finish()
    }
}