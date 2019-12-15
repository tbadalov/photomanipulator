package ee.ut.photomanipulation.operations

import android.graphics.Bitmap
import android.graphics.Matrix
import ee.ut.photomanipulation.LoadingFeedback
import ee.ut.photomanipulation.history.EventHistory

class MirrorOperation(loadingFeedback: LoadingFeedback, history: EventHistory, isHorizontal:Boolean) : Operation(loadingFeedback, history) {

    val isHorizontal = isHorizontal

    override fun perform(src: Bitmap): Bitmap {
        val coefficient = if (isHorizontal) -1f else 1f
        val m = Matrix()
        m.preScale(coefficient, -coefficient)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, false)
    }
}