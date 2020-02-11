package com.liempo.furniture

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_furnitures.*

class FurnitureFragment: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_furnitures,
        container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup recycler views
        with(living_room_rv) {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false)
            adapter = FurnitureAdapter(activity as MainActivity)
        }
        with(bedroom_rv) {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false)
            adapter = FurnitureAdapter(activity as MainActivity)
        }

        buildAssetMap()
    }

    private fun buildAssetMap() {
        activity?.assets?.let { assets ->
            assets.list(MODELS_FOLDER)?.forEach { folder ->
                assets.list("$MODELS_FOLDER/$folder")?.forEach { file ->
                    // Generate key (trim)
                    val key = file.replace(
                        ".sfb", "")
                    // Add to hash map
                    Uri.parse("models/$folder/$file").let { uri ->
                        // Add to adapter
                        (when(folder) {
                            "living_room" -> (living_room_rv
                                .adapter as FurnitureAdapter)
                            "bedroom" -> (bedroom_rv
                                .adapter as FurnitureAdapter)
                            else -> null
                        })?.addItem(key, uri)
                    }
                }
            }
        }
    }

    companion object {
        private const val MODELS_FOLDER = "models"
    }
}