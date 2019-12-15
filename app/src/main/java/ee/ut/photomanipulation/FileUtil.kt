package ee.ut.photomanipulation

import android.content.Context
import java.io.File

class FileUtil {

    companion object {
        fun tmpFile(context: Context) : File {
            return File.createTempFile("tmp", ".jpg", context.cacheDir)
        }
    }

}