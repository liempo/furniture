package com.liempo.furniture

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var ux: CaptureArFragment
    private var model: ModelRenderable? = null
    private var selected: AnchorNode? = null

    // Dialogs

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
        ux = supportFragmentManager
            .findFragmentById(R.id.ux_fragment) as CaptureArFragment

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
                selected = this
            }

            // Create the transformable node
            TransformableNode(ux.transformationSystem).apply {
                setParent(node); renderable = model; select()
                scaleController.minScale = 0.2f
                scaleController.maxScale = 1.5f
            }
        }

        // Set up dialog fragment
        val furnitureDialog = FurnitureFragment().apply {
            show(supportFragmentManager, "FurnitureDialog") }

        select_fab.setOnClickListener {
            furnitureDialog.show(supportFragmentManager, "Dialog")
        }

        delete_fab.setOnClickListener {
            ux.arSceneView.scene.removeChild(selected)
            selected?.apply { anchor?.detach(); setParent(null) }
            Toast.makeText(this,
                "Item is deleted",
                Toast.LENGTH_SHORT).show()
        }

        capture_fab.setOnClickListener {
            takePhoto()
        }
    }

    internal fun selectColor(uri: Uri) {
        ColorFragment.newInstance(uri.toString()).apply {
            show(supportFragmentManager, "ColorDialog")
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

    private fun takePhoto() {
        val filename = generateFilename()

        val bitmap = Bitmap.createBitmap(
            ux.arSceneView.width,
            ux.arSceneView.height,
            Bitmap.Config.ARGB_8888
        )

        val thread = HandlerThread("PixelCopier")
            .apply { start() }

        PixelCopy.request(ux.arSceneView, bitmap, {  result ->
            if (result == PixelCopy.SUCCESS) {
                saveBitmapToDisk(bitmap, filename)

                // Notify gallery
                val file = File(filename)
                val uri = FileProvider.getUriForFile(this,
                    "$packageName.provider", file)
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(this)
                }

                Toast.makeText(
                    this,
                    "Photo saved", Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Failed to save photo", Toast.LENGTH_LONG
                ).show()
            }

        }, Handler(thread.looper))
    }

    @Suppress("DEPRECATION")
    private fun generateFilename(): String {
        val format = SimpleDateFormat("yyyyMMddHHmmss",
            Locale.getDefault())
        val date = format.format(Date())
        return Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).path + File.separator +
                ALBUM_NAME + File.separator + date + SUFFIX
    }

    private fun saveBitmapToDisk(bitmap: Bitmap, filename: String) {
        val out = File(filename)
        if (!out.parentFile?.exists()!!)
            out.parentFile?.mkdirs()

        out.outputStream().use {
            val data = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,
                100, data)
            data.writeTo(it)
            it.flush(); it.close()
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
        private const val ALBUM_NAME = "Furniture"
        private const val SUFFIX = "_screenshot.jpg"
    }
}