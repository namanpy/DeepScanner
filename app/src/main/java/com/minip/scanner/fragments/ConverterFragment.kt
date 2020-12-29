package com.minip.scanner.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minip.scanner.ImageProcessing.CalculateAspectRatio
import com.minip.scanner.ImageProcessing.GenerateWarpedImage
import com.minip.scanner.R
import com.minip.scanner.customview.SelectView
import com.minip.scanner.recyclerview.ImageAdapter
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat


class ImageSelectedCoordinates {
    var x1 = 0f
    var y1 =0f

    var x2 = 0f
    var y2 = 0f

    var x3 = 0f
    var y3 = 0f

    var x4 = 0f
    var y4 = 0f
}
class ConverterFragment : Fragment(R.layout.converter_fragment) , ImageAdapter.OnImageClickListener, SelectView.OnSelectCoordinatesListener {
    var  images : ArrayList<Uri> = ArrayList()
    var  coordinates  : ArrayList<ImageSelectedCoordinates> = ArrayList()
    var  currentSelectedImage : Int = 0
    lateinit var selectedImageView : ImageView
    lateinit var selectView: SelectView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var image_recycler_view :RecyclerView = requireActivity().findViewById(R.id.recycler)
        image_recycler_view.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false );
        image_recycler_view.setHasFixedSize(true)
        var image_adapter  : ImageAdapter = ImageAdapter(this)
        image_recycler_view.adapter = image_adapter
        image_adapter.setUri(images)

        selectView = requireActivity().findViewById(R.id.selectview)

        selectView._setOnSelectCoordinatesListener(this)

        selectedImageView = requireActivity().findViewById(R.id.image_container)

        selectedImageView.setImageURI(images.get(currentSelectedImage))

        var button : Button = requireActivity().findViewById(R.id.process_button)

        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var i : Int = 0
                for(i in 0..images.size-1) {

                    var image = images.get(i)
                    var coords = coordinates.get(i)

                    var bitmap : Bitmap = getCapturedImage(image);
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//
//
//                    Log.d("imgRatio", imgRatio.toString())
//                    var imageViewHeight = selectView.height;
//                    var imageViewWidth = selectView.width;
//                    var imgRatio : Float = (imageViewWidth.toFloat()/imageViewHeight.toFloat()).toFloat();
//                    var displayedImageHeight = 0f
//                    var displayedImageWidth = 0f
//
//                    if(imgRatio <= 1) {
//                        displayedImageHeight = imageViewHeight.toFloat()
//                        displayedImageWidth = displayedImageHeight * imgRatio
//
//                    } else {
//                        displayedImageWidth = imageViewWidth.toFloat()
//                        displayedImageHeight = displayedImageWidth * imgRatio
//                    }
//


                    var actualHeight = 0
                    var actualWidth  = 0
                    var imageViewHeight = selectedImageView.getHeight()
                    var imageViewWidth = selectedImageView.getWidth();
                    var bitmapHeight = bitmap.height;
                    var bitmapWidth = bitmap.width;

                    if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                        actualWidth = bitmapWidth * imageViewHeight / bitmapHeight;
                        actualHeight = imageViewHeight;
                    } else {
                        actualHeight = bitmapHeight * imageViewWidth / bitmapWidth;
                        actualWidth = imageViewWidth;
                    }

                    Log.d("displayedImageHeight", actualHeight.toString())
                    Log.d("imageViewHeight", imageViewHeight.toString())
                    Log.d("bitmapHeight", bitmap.height.toString())
                    Log.d("bitmapwidth", bitmap.height.toString())
                    var displayedImageHeight = actualHeight
                    var displayedImageWidth = actualWidth
                    if(displayedImageWidth < imageViewWidth) {
                        coords.x1 = ((coords.x1 - ((imageViewWidth - displayedImageWidth) / 2f))/displayedImageWidth) * bitmap.width
                        coords.x2 = ((coords.x2 - ((imageViewWidth - displayedImageWidth) / 2f))/displayedImageWidth) * bitmap.width
                        coords.x3 = ((coords.x3 - ((imageViewWidth - displayedImageWidth) / 2f))/displayedImageWidth) * bitmap.width
                        coords.x4 = ((coords.x4 - ((imageViewWidth - displayedImageWidth) / 2f))/displayedImageWidth) * bitmap.width

                        coords.y1 = coords.y1/displayedImageHeight * bitmap.height
                        coords.y2 = coords.y2/displayedImageHeight * bitmap.height
                        coords.y3 = coords.y3/displayedImageHeight * bitmap.height
                        coords.y4 = coords.y4/displayedImageHeight * bitmap.height
                    }
                    if(displayedImageHeight < imageViewHeight) {
                        coords.y1 = ((coords.y1 - ((imageViewHeight - displayedImageHeight) / 2f))/displayedImageHeight) * bitmap.height
                        coords.y2 = ((coords.y2 - ((imageViewHeight - displayedImageHeight) / 2f))/displayedImageHeight) * bitmap.height
                        coords.y3 = ((coords.y3 - ((imageViewHeight - displayedImageHeight) / 2f))/displayedImageHeight) * bitmap.height
                        coords.y4 = ((coords.y4 - ((imageViewHeight - displayedImageHeight) / 2f))/displayedImageHeight) * bitmap.height

                        coords.x1 = coords.x1/displayedImageWidth * bitmap.width
                        coords.x2 = coords.x2/displayedImageWidth * bitmap.width
                        coords.x3 = coords.x3/displayedImageWidth * bitmap.width
                        coords.x4 = coords.x4/displayedImageWidth * bitmap.width
                    }





                    var imgMAT = Mat(bitmap.width, bitmap.height, CvType.CV_8UC1)
                    Utils.bitmapToMat(bitmap , imgMAT)

                    Log.d("image is ", imgMAT.toString())

                    val imageHeight = bitmap.height
                    val imageWidth = bitmap.width

                    var calculated_dims = CalculateAspectRatio(coords.x1, coords.y1, coords.x2, coords.y2, coords.x3, coords.y3, coords.x4, coords.y4, actualWidth, actualHeight);

                    var newbitmap = GenerateWarpedImage("magicColor" ,imgMAT, calculated_dims.W, calculated_dims.H ,coords.x1, coords.y1, coords.x2, coords.y2, coords.x3, coords.y3, coords.x4, coords.y4).image;

                    selectedImageView.setImageBitmap(newbitmap)
                    Log.d("NEW BITMAP WIDTH ", newbitmap.width.toString())
                    Log.d("NEW BITMAP HEIGHT ", newbitmap.height.toString())

                }
            }

        })


    }


    fun singleImage(imageUri : Uri) {
        images.add(imageUri)
        coordinates.add(ImageSelectedCoordinates())

    }

    override fun onImageClick(position: Int) {
        currentSelectedImage = position
        selectedImageView.setImageURI(images.get(currentSelectedImage))
        selectView.setCurrentImageIndex(currentSelectedImage)

    }

    override fun onSelectCoordinates(imageindex: Int, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {

        var coords  : ImageSelectedCoordinates = coordinates.get(currentSelectedImage)

        coords.x1 = x1
        coords.x2 = x2
        coords.x3 = x3
        coords.x4 = x4

        coords.y1 = y1
        coords.y2 = y2
        coords.y3 = y3
        coords.y4 = y4
        Log.d("TEST : ", coordinates.get(currentSelectedImage).x1.toString())
        Log.d("TEST : ", coordinates.get(currentSelectedImage).x4.toString())
    }


    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.createSource(requireActivity().contentResolver, selectedPhotoUri)
                } else {
                    TODO("VERSION.SDK_INT < P")
                }
                ImageDecoder.decodeBitmap(source)
            }
        }

        return bitmap
    }

}