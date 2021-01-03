package com.minip.scanner.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.TextureViewMeteringPointFactory
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.minip.scanner.R
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class CameraCaptureFragment : Fragment(R.layout.camera_capture) {

    private var imageCapture: ImageCapture? = null
    private lateinit var camera : Camera
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    lateinit var previewView : PreviewView
    lateinit var camera_capture_button : Button
    lateinit var capturePreview : ImageView
    lateinit var imageCountView  : TextView
    lateinit var loadingView : View
    var imageCount = 0
    var  coordinates  : ArrayList<ImageSelectedCoordinates> = ArrayList()
    var capturedImages : ArrayList<Uri> = ArrayList()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity() , REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            startCamera()
        }
        loadingView = view.findViewById(R.id.loadingPanel)
        loadingView.setVisibility(View.GONE);
        // Set up the listener for take photo button
        previewView = view.findViewById(R.id.viewFinder)
        camera_capture_button = view.findViewById(R.id.camera_capture_button)
        capturePreview = view.findViewById(R.id.capture_preview)
        imageCountView = view.findViewById(R.id.image_count)


        camera_capture_button.setOnClickListener { takePhoto() }
        capturePreview.setOnClickListener { if(imageCount  > 0) { finish() } }
        cameraExecutor = Executors.newSingleThreadExecutor()

        if(capturedImages.size  > 0) {

            capturePreview.setImageURI(capturedImages[capturedImages.size-1])
            imageCountView.text = imageCount.toString();
            coordinates = ArrayList()
        }


        previewView.setOnTouchListener(View.OnTouchListener { view: View, event: MotionEvent ->

            if (event.action != MotionEvent.ACTION_UP) {
                return@OnTouchListener false
            }

            val factory : MeteringPointFactory = previewView.createMeteringPointFactory(CameraSelector.DEFAULT_BACK_CAMERA);
            val point : MeteringPoint = factory.createPoint(event.getX(), event.getY());
            val action : FocusMeteringAction =  FocusMeteringAction.Builder(point).build();
            camera.cameraControl.startFocusAndMetering(action)
            return@OnTouchListener true

        } )

    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        outputDirectory = requireContext().cacheDir
        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                UUID.randomUUID().toString() + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                imageCount++
                imageCountView.setText(imageCount.toString());
                capturedImages.add(savedUri)
                capturePreview.setImageURI(savedUri)


            }
        })


    }
    private fun finish() {
        loadingView.visibility = View.VISIBLE;
        var converterfragment : ConverterFragment = ConverterFragment();
        val images = capturedImages
        var runnable : Runnable  = object : Runnable {


            override fun run() {
                try {
                    Log.d("R UN START " ,  "!!!!!!!!!!!!!!!!!!!$$$$$$$$$$$$$$$$$$$$$$$")


                    val client =  OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();

                    var i = 0
                    for (i in 0..images.size - 1) {
                        Log.d("RAN TIMES ", images.size.toString())
                        val bmpfile = BitmapFactory.decodeFile(capturedImages[i].path)

                        val resized: Bitmap = Bitmap.createScaledBitmap(bmpfile, 299, 299, true)

                        val file = bitmapToFile(resized)

                        val requestBody: RequestBody = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.name, file.asRequestBody())
                            .build()

                        val request = Request.Builder()
                            .url("http://scarb.zapto.org:5000/detect").header("Accept-Encoding", "identity").addHeader("Connection","close")
                            .post(requestBody)
                            .build()

                        var response: Response = client.newCall(request).execute()


                        var imageSelectedCoordinates = ImageSelectedCoordinates()
                        if (response.isSuccessful) {
                            val resStr = response.body!!.string()
                            Log.d("RESPONSE IS ", resStr)
                            response.close()
                            val json = JSONObject(resStr)
                            val arr1: JSONArray = json.getJSONArray("points").getJSONArray(0)
                            val data = fillData(arr1)

                            if(data != null){
                                val x1 = data.get(0)
                                val y1 = data.get(1)
                                val x2 = data.get(2)
                                val y2 = data.get(3)
                                val x3 = data.get(4)
                                val y3 = data.get(5)
                                val x4 = data.get(6)
                                val y4 = data.get(7)
                                Log.d("X1", x1.toString())
                                val fa = arrayListOf<FloatArray>(floatArrayOf(x1, y1), floatArrayOf(x2, y2), floatArrayOf(x3, y3), floatArrayOf(x4, y4))
                                var ysorted = arrayListOf<FloatArray>()
                                var top = arrayListOf<FloatArray>()
                                var bottom = arrayListOf<FloatArray>()
                                val i = 0

                                ysorted.add(fa[0])
//                                Log.d("ysorted[j][1]")
                                for(i in 1..fa.size-1) {

                                    val pointtocompare = fa[i]
                                    var foundPos = false
                                    for (j in 0..ysorted.size-1) {

                                        if(pointtocompare[1] < ysorted[j][1]) {
                                            ysorted.add(j, pointtocompare)
                                            foundPos = true
                                            break
                                        }

                                    }
                                    if(!foundPos) {
                                        ysorted.add(pointtocompare)
                                    }

                                }
                                top.add(ysorted[0])
                                top.add(ysorted[1])

                                bottom.add(ysorted[2])
                                bottom.add(ysorted[3])

                                var xsortedTop = arrayListOf<FloatArray>()

                                xsortedTop.add(top[0])
                                for(i in 1..top.size-1) {

                                    val pointtocompare = top[i]
                                    var foundPos = false
                                    for (j in 0..xsortedTop.size-1) {

                                        if(pointtocompare[0] < xsortedTop[j][0]) {
                                            xsortedTop.add(j, pointtocompare)
                                            foundPos = true
                                            break
                                        }

                                    }
                                    if(!foundPos) {
                                        xsortedTop.add(pointtocompare)
                                    }

                                }
                                var xsortedBottom = arrayListOf<FloatArray>()

                                xsortedBottom.add(bottom[0])
                                for(i in 1..bottom.size-1) {

                                    val pointtocompare = bottom[i]
                                    var foundPos = false
                                    for (j in 0..xsortedBottom.size-1) {

                                        if(pointtocompare[0] < xsortedBottom[j][0]) {
                                            xsortedBottom.add(j, pointtocompare)
                                            foundPos = true
                                            break
                                        }

                                    }
                                    if(!foundPos) {
                                        xsortedBottom.add(pointtocompare)
                                    }

                                }

                                imageSelectedCoordinates.x1 = xsortedTop[0][0]
                                imageSelectedCoordinates.y1 = xsortedTop[0][1]
                                imageSelectedCoordinates.x2 = xsortedTop[1][0]
                                imageSelectedCoordinates.y2 = xsortedTop[1][1]

                                Log.d("xSortedBottom", xsortedBottom.size.toString())
                                Log.d("xSortedBottom", xsortedBottom[1].size.toString())


                                imageSelectedCoordinates.x3 = xsortedBottom[0][0]
                                imageSelectedCoordinates.y3 = xsortedBottom[0][1]
                                imageSelectedCoordinates.x4 = xsortedBottom[1][0]
                                imageSelectedCoordinates.y4 = xsortedBottom[1][1]
                                coordinates.add(imageSelectedCoordinates)

                            }


                        } else {
                            Log.d("STRING -=========-", response.message)
                            Log.d("AAAAAAAREEEEEEEEEEEE", " NAMAN")
                        }


                    }
                    converterfragment.multiImage(capturedImages, coordinates)

                    requireActivity().supportFragmentManager.beginTransaction()/*.setCustomAnimations(
                        R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right
                    )*/.replace(R.id.fragment_container, converterfragment).addToBackStack(null)
                        .commit()
                } catch (e : java.lang.Exception) {
                    e.printStackTrace()
                    for(i in 0 until capturedImages.size) {
                        coordinates.add(ImageSelectedCoordinates())
                    }
                    converterfragment.multiImage(capturedImages, coordinates)
                    requireActivity().runOnUiThread {
                        val toast : Toast = Toast.makeText(requireActivity(),"Some Error Occured, Please try again",Toast.LENGTH_SHORT )
                        toast.show()

                    }

                    requireActivity().supportFragmentManager.beginTransaction()/*.setCustomAnimations(
                        R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right
                    )*/.replace(R.id.fragment_container, converterfragment).addToBackStack(null)
                            .commit()
                }

            }
        }

        var thread : Thread = Thread(runnable)
        thread.start()



    }
    private fun bitmapToFile(bitmap : Bitmap) : File {

        val f = File(requireContext().cacheDir, "image.jpg")
        f.createNewFile()
        val bos  : ByteArrayOutputStream =  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , bos);
        val bitmapdata = bos.toByteArray();

        var fos : FileOutputStream? = null;
        try {
            fos = FileOutputStream(f);
        } catch ( e : FileNotFoundException) {
            e.printStackTrace();
            Log.d(" TEST ", "  IO EXCPETION ")
        }
        try {
            fos!!.write(bitmapdata);
            fos!!.flush();
            fos!!.close();

        } catch (e : IOException) {
            Log.d(" TEST ", "  IO EXCPETION 2")
            e.printStackTrace();
        }
        return f
    }
    private fun fillData(jsonArray: JSONArray): FloatArray? {
        val fData = FloatArray(jsonArray.length())
        for (i in 0 until jsonArray.length()) {
            try {
                Log.d("VALUE  ISSSSSSSS  ", jsonArray.getString(i))
                fData[i] = jsonArray.getString(i).toFloat()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return fData
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.createSurfaceProvider())
                    }
            imageCapture = ImageCapture.Builder()
                    .build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture)


            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(activity,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
               requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }


//    override fun onResume() {
//        super.onResume()
//        requireActivity().supportFragmentManager.beginTransaction().detach(this).attach(this).commit();
//    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    companion object {
        private const val TAG = "MiniPScanner"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }


}