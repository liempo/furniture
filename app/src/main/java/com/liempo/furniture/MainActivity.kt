package com.liempo.furniture

import android.app.ActivityManager
import android.content.Context
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var model: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if device is support before initializing fragment
        if (isDeviceSupport().not())
            Toast.makeText(
                this,
                "OpenGL version must be above $MIN_OPENGL_VERSION.",
                Toast.LENGTH_LONG
            ).show()

        // Get the instance of arFragment
        val ux = supportFragmentManager
            .findFragmentById(R.id.ux_fragment) as ArFragment

        ux.setOnTapArPlaneListener { hit, _, _ ->
            if (model == null) {
                Toast.makeText(
                    this,
                    "No furniture selected.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnTapArPlaneListener
            }

            // Create the anchor
            val anchor = hit.createAnchor()
            val node = AnchorNode(anchor).apply {
                setParent(ux.arSceneView.scene)
            }

            // Create the transformable node
            TransformableNode(ux.transformationSystem).apply {
                setParent(node); renderable = model; select()
                scaleController.minScale = 0.2f
                scaleController.maxScale = 1.5f
            }
        }

        // Set up dialog fragment
        val dialog = FurnitureFragment().apply {
            show(supportFragmentManager, "Dialog") }
        select_fab.setOnClickListener {
            dialog.show(supportFragmentManager, "Dialog")
        }
    }

    internal fun selectModel(uri: Uri) {
        ModelRenderable.builder()
            .setSource(this, uri)
            .build()
            .thenAccept {
                model = it
            }.exceptionally { error(it) }
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
