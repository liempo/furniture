package com.liempo.furniture

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val models =
        hashMapOf<String, ModelRenderable>()

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

        with(models_recycler_view) {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false)
            adapter = ModelsAdapter()
        }

        buildModelsFromAssets()
    }

    private fun buildModelsFromAssets() {
        for (filename in assets.list("")!!) {
            if (!filename.contains(".sfb"))
                continue

            val key = filename.replace(
                ".sfb", "")
            ModelRenderable.builder()
                .setSource(this,
                    Uri.parse(filename))
                .build()
                .thenAccept {
                    models[key] = it

                    // Add to adapter
                    (models_recycler_view.adapter as ModelsAdapter).apply {
                        Timber.i("Adding $key to adapter")
                        items.add(key); notifyItemInserted(items.lastIndex)
                    }

                    Timber.d("Models loaded: ${models.size}")
                }.exceptionally { error(it) }
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
