package ee.ut.photomanipulation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Environment
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
        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) { }

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) { }

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

    fun onCaptureButtonClicked(view: View?) {
        Log.i(TAG, "Photo taken")
        lock()
        var outputPhoto: FileOutputStream? = null
        try {
            //outputPhoto = FileOutputStream(createImageFile())
            val bitmap = textureView.getBitmap()
            MediaStore.Images.Media.insertImage(contentResolver, bitmap, createImageName(), "")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            unlock()
            try {
                if (outputPhoto != null) {
                    outputPhoto.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
