package ee.ut.photomanipulation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraActivity : AppCompatActivity() {
    // Most of the code in this class is inspired by
    // https://android.jlelse.eu/the-least-you-can-do-with-camera2-api-2971c8c81b8b

    val TAG = "CameraActivity"

    lateinit var cameraManager: CameraManager
    var cameraFacing = CameraCharacteristics.LENS_FACING_BACK
    lateinit var galleryFolder: File
    lateinit var previewSize : Size
    lateinit var cameraId : String
    var mCameraDevice : CameraDevice? = null
    var mCameraCaptureSession : CameraCaptureSession? = null
    var mCaptureRequestBuilder : CaptureRequest.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.i(TAG, "onSurfaceTextureSizeChanged")
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            val matrix = configureTransformMatrix(textureView.width, textureView.height)
            textureView.setTransform(matrix)
        }

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
            return false
        }

        override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
            setUpCamera()
            openCamera()
        }
    }

    val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice;
            createPreviewSession();
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    }

    private fun setUpCamera() {
        try {
            for (id in cameraManager.cameraIdList) {
                val cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(id);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        cameraFacing) {
                    val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    streamConfigurationMap?.getOutputSizes(SurfaceTexture::class.java).let { sizes ->
                        previewSize = sizes!![0]
                    }
                    cameraId = id;
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                cameraManager.openCamera(cameraId, stateCallback, Handler { true })
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createPreviewSession() {
        val surfaceTexture = textureView.surfaceTexture
        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val previewSurface = Surface(surfaceTexture)
        mCaptureRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mCaptureRequestBuilder?.addTarget(previewSurface)

        mCameraDevice?.createCaptureSession(
            mutableListOf(previewSurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(p0: CameraCaptureSession) {
                }

                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    if (mCameraDevice == null) return

                    val captureRequest = mCaptureRequestBuilder?.build()
                    mCameraCaptureSession = cameraCaptureSession
                    cameraCaptureSession.setRepeatingRequest(captureRequest!!, null, Handler { true });
                }

            },
            Handler { true }
        )
    }

    override fun onResume() {
        super.onResume()
        if (textureView.isAvailable()) {
            setUpCamera()
            openCamera()
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    override fun onStop() {
        super.onStop()
        closeCamera()
    }

    private fun closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession!!.close()
            mCameraCaptureSession = null
        }
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
    }

    private fun createImageName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "image_" + timeStamp + "_"
        return imageFileName
    }

    private fun lock() {
        try {
            mCameraCaptureSession?.capture(
                mCaptureRequestBuilder!!.build(),
                null, Handler { true }
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun unlock() {
        try {
            mCameraCaptureSession?.setRepeatingRequest(
                mCaptureRequestBuilder!!.build(),
                null, Handler { true }
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun configureTransformMatrix(viewWidth: Int, viewHeight: Int): Matrix {
        val rotation = windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, textureView.height.toFloat(), textureView.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale: Float = Math.max(
                viewHeight.toFloat() / textureView.getHeight(),
                viewWidth.toFloat() / textureView.getWidth()
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate(90f * (rotation - 2), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        return matrix
    }

    fun onCaptureButtonClicked(view: View?) {
        Log.i(TAG, "Photo taken")
        lock()
        try {
            var bitmap = textureView.getBitmap()
            val matrix = configureTransformMatrix(textureView.width, textureView.height)
            // rotate matrix based on rotation
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            val insertImage = MediaStore.Images.Media.insertImage(contentResolver, bitmap, createImageName(), "")
            val resultIntent = Intent()
            resultIntent.putExtra("imageUri", insertImage)
            setResult(3, resultIntent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            unlock()
        }
    }
}
