package ee.ut.photomanipulation

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

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
        Glide
            .with(context!!)
            .load(imagePath)
            .centerCrop()
            .into(pictureView!!)

        pictureView.setOnClickListener {
            val intent = Intent(context, PhotoEditActivity::class.java)
            intent.putExtra("imagePath", imagePath)
            context.startActivity(intent)
        }
    }
}
