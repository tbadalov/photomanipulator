package ee.ut.photomanipulation

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import java.io.File

class PictureAdapter(context: Context, cursor: Cursor) : CursorAdapter(
    context, cursor, true
) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.picture_item, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val columnIndexData = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        val pictureView = view?.findViewById<ImageView>(R.id.picture)

        val imagePath = cursor?.getString(columnIndexData!!)
        val imageUri = Uri.fromFile(File(imagePath))
        var bitmap = MediaStore.Images.Media.getBitmap(
            context?.contentResolver,
            Uri.fromFile(File(imagePath))
        )
        pictureView?.setImageBitmap(bitmap)

        pictureView?.setOnClickListener { view ->
            val intent = Intent(context, PictureActivity::class.java)
            intent.putExtra("imagePath", imagePath)
            context?.startActivity(intent)
        }
    }
}
