package com.liempo.furniture

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_furniture.view.*

class FurnitureAdapter(private val fragment: DialogFragment):
    RecyclerView.Adapter<FurnitureAdapter.ViewHolder>() {

    // List of model names (key)
    private val items = arrayListOf<Furniture>()

    internal fun addItem(key: String, uri: Uri) {
        items.add(Furniture(key, uri))
        notifyItemInserted(items.lastIndex)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val preview: ImageView = view.preview
        val name: TextView = view.model_name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_furniture, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val previewImageUri = Uri.parse(
            "file:///android_asset/" +
                    "previews/${item.key}.png")

        Glide.with(holder.preview)
            .load(previewImageUri)
            .centerCrop()
            .into(holder.preview)

        holder.itemView.setOnClickListener {
            (fragment.activity as MainActivity)
                .selectColor(item.uri)
            fragment.dismiss()
        }

        @SuppressLint("DefaultLocale")
        holder.name.text = item.key.capitalize()
            .replace("_", " ")
    }

    data class Furniture(
        val key: String,
        val uri: Uri
    )
}