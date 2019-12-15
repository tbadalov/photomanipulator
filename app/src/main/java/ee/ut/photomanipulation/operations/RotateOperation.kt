package ee.ut.photomanipulation.operations

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import ee.ut.photomanipulation.LoadingFeedback
import ee.ut.photomanipulation.history.EventHistory


class RotateOperation(loadingFeedback: LoadingFeedback, history: EventHistory, context: Context, isLeft:Boolean) : Operation(loadingFeedback, history, context) {

    val isLeft = isLeft

    override fun perform(src: Bitmap): Bitmap {

        val angle = if (isLeft) -90f else 90f

        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }
}