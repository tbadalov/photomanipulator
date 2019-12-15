package ee.ut.photomanipulation.history

import android.net.Uri
import java.lang.RuntimeException

class EventHistory(baseImageLocation:Uri) {

    private val baseUri = baseImageLocation
    private val history = arrayListOf<Uri>(baseUri)
    private var historyPosition = 0

    fun canUndo() : Boolean { return historyPosition > 0 }

    fun undo() : Uri {
        if (historyPosition == 0) throw RuntimeException("Can't undo")
        return history[--historyPosition]
    }

    fun canRedo() : Boolean { return historyPosition < history.size-1 }

    fun redo() : Uri {
        if (historyPosition == history.size-1 ) throw RuntimeException("Can't redo")
        return history[++historyPosition]
    }

    fun perform(uri:Uri) {
        if (canRedo()) { history.subList(historyPosition+1, history.size).clear() }
        history.add(uri)
        historyPosition++
    }

}