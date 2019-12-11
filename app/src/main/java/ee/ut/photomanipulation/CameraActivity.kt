package ee.ut.photomanipulation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {

    lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        surfaceView.holder.addCallback(surfaceReadyCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startCameraSession() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        if (cameraManager.cameraIdList.isEmpty()) { return } // no cameras

        val firstCamera = cameraManager.cameraIdList[0] // check for front facing camera

        cameraManager.openCamera(firstCamera, cameraStateCallback, Handler { true })
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(camera.id)
            cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
                streamConfigurationMap.getOutputSizes(ImageFormat.JPEG)?.let { sizes ->
                    val previewSize = sizes[0]

                    val displayRotation = windowManager.defaultDisplay.rotation
                    val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)

                    // swap width and height if needed
                    val rotatedPreviewWidth = if (swappedDimensions) previewSize.height else previewSize.width
                    val rotatedPreviewHeight = if (swappedDimensions) previewSize.width else previewSize.height
                    surfaceView.holder.setFixedSize(rotatedPreviewWidth, rotatedPreviewHeight)

                    val previewSurface = surfaceView.holder.surface

                    val captureCallback = object : CameraCaptureSession.StateCallback()
                    {
                        override fun onConfigureFailed(session: CameraCaptureSession) {}

                        override fun onConfigured(session: CameraCaptureSession) {
                            // session configured
                            val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                .apply {
                                    addTarget(previewSurface)
                                }
                            session.setRepeatingRequest(
                                previewRequestBuilder.build(),
                                object: CameraCaptureSession.CaptureCallback() {},
                                Handler { true }
                            )
                        }
                    }

                    camera.createCaptureSession(mutableListOf(previewSurface), captureCallback, Handler { true })
                }
            }
        }
        override fun onClosed(camera: CameraDevice) { }
        override fun onDisconnected(camera: CameraDevice) { }
        override fun onError(camera: CameraDevice, error: Int) { }
    }

    val surfaceReadyCallback = object: SurfaceHolder.Callback {
        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) { }
        override fun surfaceDestroyed(p0: SurfaceHolder?) { }

        override fun surfaceCreated(p0: SurfaceHolder?) {
            startCameraSession()
        }
    }

    private fun areDimensionsSwapped(displayRotation: Int, cameraCharacteristics: CameraCharacteristics): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                // invalid display rotation
            }
        }
        return swappedDimensions
    }
}
