package com.liempo.furniture

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_model.view.*

class ModelsAdapter(private val activity: MainActivity):
    RecyclerView.Adapter<ModelsAdapter.ViewHolder>() {

    // List of model names (key)
    val items = arrayListOf<String>()

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val preview: ImageView = view.preview
        val name: TextView = view.model_name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_model, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val previewImageUri = Uri.parse(
            "file:///android_asset/previews/${items[position]}.png")

        Glide.with(holder.preview)
            .load(previewImageUri)
            .centerCrop()
            .into(holder.preview)

        holder.itemView.setOnClickListener {
            activity.selectModel(items[position])
        }

        @SuppressLint("DefaultLocale")
        holder.name.text = items[position].capitalize()
    }
}