package com.liempo.furniture

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val models =
        hashMapOf<String, Uri>()
    private var selected: ModelRenderable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the instance of arFragment
        val ux = supportFragmentManager
            .findFragmentById(R.id.ux_fragment) as ArFragment

        // Check if device is support before initializing fragment
        if (isDeviceSupport().not())
            Toast.makeText(
                this,
                "OpenGL version must be above $MIN_OPENGL_VERSION.",
                Toast.LENGTH_LONG
            ).show()

        with(living_room_recycler_view) {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false)
            adapter = ModelsAdapter(this@MainActivity)
        }

        with(bedroom_recycler_view) {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false)
            adapter = ModelsAdapter(this@MainActivity)
        }

        buildModelsFromAssets()

        ux.setOnTapArPlaneListener { hit, _, _ ->
            if (selected == null)
                return@setOnTapArPlaneListener

            // Create the anchor
            val anchor = hit.createAnchor()
            val node = AnchorNode(anchor).apply {
                setParent(ux.arSceneView.scene)
            }

            // Create the transformable node
            TransformableNode(ux.transformationSystem).apply {
                setParent(node); renderable = selected; select()

                scaleController.minScale = 0.4f
                scaleController.maxScale = 1.0f
            }
        }

        val sheetBehavior = from(sheet)
        sheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    STATE_HIDDEN -> {}
                    STATE_EXPANDED -> {
                        sheet_icon.setImageResource(
                            R.drawable.ic_keyboard_arrow_down_black_24dp)
                    }
                    STATE_COLLAPSED -> {
                        sheet_icon.setImageResource(
                            R.drawable.ic_keyboard_arrow_up_black_24dp)
                    }
                    STATE_DRAGGING -> {}
                    STATE_SETTLING -> {}
                    STATE_HALF_EXPANDED -> {}
                }
            }

        })


        sheet_icon.setOnClickListener { toggleSheet() }
    }

    internal fun selectModel(key: String) {
        ModelRenderable.builder()
            .setSource(this, models[key])

        ModelRenderable.builder()
            .setSource(this, models[key])
            .build()
            .thenAccept {
                selected = it
                toggleSheet()
            }.exceptionally { error(it) }
    }

    private fun toggleSheet() {
        val behavior = from(sheet)

        if (behavior.state != STATE_EXPANDED)
            behavior.state = STATE_EXPANDED
        else
            behavior.state = STATE_COLLAPSED
    }

    private fun buildModelsFromAssets() {
        for (folder in assets.list("models")!!) {
            for (filename in assets.list("models/$folder")!!) {
                if (!filename.contains(".sfb"))
                    continue
                val key = filename.replace(
                    ".sfb", "")
                models[key] = Uri.parse("models/$folder/$filename")

                // Add to adapter
                val adapter = when(folder) {
                    "living_room" -> (living_room_recycler_view
                        .adapter as ModelsAdapter)
                    "bedroom" -> (bedroom_recycler_view
                        .adapter as ModelsAdapter)
                    else -> null
                }

                adapter?.apply {
                    Timber.i("Adding $key to adapter")
                    items.add(key); notifyItemInserted(items.lastIndex)
                }
            }
        }
    }

    private fun getOpenGLVersion(): Double =
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo.glEsVersion.toDouble()

    private fun isDeviceSupport(): Boolean =
        (ArCoreApk.getInstance().checkAvailability(this)
                != ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) &&
                (getOpenGLVersion() > MIN_OPENGL_VERSION)

    companion object {
        private const val MIN_OPENGL_VERSION = 3.0
    }
}
