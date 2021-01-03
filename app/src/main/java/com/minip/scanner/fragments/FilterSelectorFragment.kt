package com.minip.scanner.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minip.scanner.R
import com.minip.scanner.recyclerview.FilterImageAdapter
import com.minip.scanner.recyclerview.FilterSelectAdapter
import com.minip.scanner.recyclerview.ImageAdapter
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo.fastNlMeansDenoising
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class Filter {
    val filterNames : ArrayList<String> = arrayListOf("Magic Color", "B&W" , "Cyan")
    val filterDisplayImages  : ArrayList<Int> = arrayListOf(R.drawable.magic_color, R.drawable.cyan, R.drawable.bnw)

    fun applyFilter(filter : Int, images : ArrayList<Bitmap>) : ArrayList<Bitmap> {
        var filteredImages : ArrayList<Bitmap> = ArrayList()
        if(filter == 0) {
            //Magic Color

            val i = 0
            for(i in 0..images.size-1)  {

                var mat : Mat = Mat(images.get(i).width, images.get(i).height, CvType.CV_8UC1)
                var mat2 : Mat = Mat(images.get(i).width, images.get(i).height, CvType.CV_8UC1)

                Utils.bitmapToMat(images.get(i), mat)
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
                fastNlMeansDenoising(mat, mat2, 11f, 31, 9)
                Imgproc.adaptiveThreshold(mat2, mat2, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)

                var bitmap : Bitmap = Bitmap.createBitmap(images.get(i).width, images.get(i).height, Bitmap.Config.ARGB_8888)

                Utils.matToBitmap(mat2, bitmap)
                filteredImages.add(bitmap)
            }

        } else if(filter == 1) {
            //B&W
            val i = 0
            for(i in 0..images.size-1)  {

                var mat : Mat = Mat(images.get(i).width, images[i].height, CvType.CV_8UC1)
                var mat2 : Mat = Mat(images.get(i).width, images[i].height, CvType.CV_8UC1)

                Utils.bitmapToMat(images[i], mat)
                Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGR2GRAY)

                var bitmap : Bitmap = Bitmap.createBitmap(images.get(i).width, images.get(i).height, Bitmap.Config.ARGB_8888)

                Utils.matToBitmap(mat2, bitmap)
                filteredImages.add(bitmap)
            }

        } else if(filter == 2) {

            val i = 0
            for(i in 0..images.size-1)  {

                var mat : Mat = Mat(images.get(i).width, images.get(i).height, CvType.CV_8UC1)
                var mat2 : Mat = Mat(images.get(i).width, images.get(i).height, CvType.CV_8UC1)

                Utils.bitmapToMat(images.get(i), mat)

                Imgproc.medianBlur(mat, mat2,11)

                Core.multiply(mat, Core.mean(mat2) , mat)
                Core.divide(mat, mat2, mat)
                mat.convertTo(mat, -1, 6.0)
                Imgproc.threshold(mat,mat,140.0, 255.0,Imgproc.THRESH_BINARY)

                var bitmap : Bitmap = Bitmap.createBitmap(images.get(i).width, images.get(i).height, Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(mat, bitmap)
                filteredImages.add(bitmap)

            }

        }
        return filteredImages
    }


}

class FilterSelectorFragment  : Fragment(R.layout.filter_selector), FilterSelectAdapter.OnImageClickListener{

    var filter : Filter = Filter()
    var imageBitmap : ArrayList<Bitmap> = ArrayList()
    var filteredImageBitmap : ArrayList<Bitmap> = ArrayList()
    lateinit var filterImageRecycler : RecyclerView
    lateinit var filterRecycler : RecyclerView
    lateinit var filterImageAdapter: FilterImageAdapter
    lateinit var filterSelectAdapter: FilterSelectAdapter
    lateinit var createPdf : Button
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        this.filterImageRecycler  = requireActivity().findViewById<RecyclerView>(R.id.filterImageRecycler)
        filterImageRecycler.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        filterImageAdapter =  FilterImageAdapter();
        filterImageAdapter.setBitmap(imageBitmap)

        filterImageRecycler.adapter = filterImageAdapter;

        this.filterRecycler  = requireActivity().findViewById<RecyclerView>(R.id.filterRecycler)
        filterRecycler.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        filterSelectAdapter =  FilterSelectAdapter(this)
        filterSelectAdapter.content(filter.filterDisplayImages, filter.filterNames)

        filterRecycler.adapter = filterSelectAdapter;


        createPdf = view.findViewById(R.id.createPdf)
        createPdf.setOnClickListener(View.OnClickListener {

            var pdfuri : Uri = createPdf()
            var intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(pdfuri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent = Intent.createChooser(intent, "Open File");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        })
    }
    fun createPdf() : Uri {
        var outputDir  = requireContext().filesDir
        var filename =  UUID.randomUUID().toString() + ".pdf"
        var file  : File = File(outputDir,filename)
        if (!file.exists()) {
            file.createNewFile();
        }

        var fos : FileOutputStream = FileOutputStream(file)

        val document : PdfDocument = PdfDocument()

        for(i in 0 until filteredImageBitmap.size) {
            var pageInfo: PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(filteredImageBitmap.get(i).width, filteredImageBitmap.get(i).height, i + 1).create()
            var page: PdfDocument.Page = document.startPage(pageInfo)
            page.canvas.drawBitmap(filteredImageBitmap.get(i), 0f, 0f, null);
            document.finishPage(page)
            document.writeTo(fos)
        }
        document.close()

        fos.flush()
        fos.close()

        return FileProvider.getUriForFile(requireContext(),requireContext().applicationContext.packageName+ ".provider", file)
    }
    fun content( bitmaplist : ArrayList<Bitmap>) {

        this.imageBitmap = bitmaplist

    }

    @SuppressLint("NewApi")
    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }

        return bitmap
    }

    override fun onImageClick(position: Int) {
        //Filter is clicked
        var _this = this
        var runnable : Runnable = object : Runnable {
            override fun run() {
                var imagelist : ArrayList<Bitmap> = filter.applyFilter(position, _this.imageBitmap)

                _this.requireActivity().runOnUiThread {
                    _this.filteredImageBitmap = imagelist

                    _this.filterImageAdapter.setBitmap(filteredImageBitmap)
                }
            }

        }
        var thread : Thread= Thread(runnable)
        thread.start()
    }



}