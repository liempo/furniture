package com.liempo.furniture

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_color.*

class ColorFragment: DialogFragment() {

    private var uri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = arguments?.getString("uri")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_color,
        container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        original_button.setOnClickListener {
            activity.selectModel(Uri.parse(uri))
            dismiss()
        }

        red_button.setOnClickListener {
            activity.selectModel(Uri.parse(uri?.
                replace("original", "red")))
            dismiss()
        }

        red_button.setOnClickListener {
            activity.selectModel(Uri.parse(uri?.
                replace("original", "blue")))
            dismiss()
        }
    }

    companion object {
        fun newInstance(uri: String) = ColorFragment().apply {
             arguments = Bundle().apply { putString("uri", uri) }
        }
    }
}