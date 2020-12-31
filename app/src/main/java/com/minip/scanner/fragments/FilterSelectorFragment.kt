package com.minip.scanner.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minip.scanner.R
import com.minip.scanner.recyclerview.FilterImageAdapter

class FilterSelectorFragment  : Fragment(R.layout.filter_selector){

    var imageBitmap : ArrayList<Bitmap> = ArrayList()
    var filteredImageBitmap : ArrayList<Bitmap> = ArrayList()
    lateinit var filterImageRecycler : RecyclerView
    lateinit var filterImageAdapter: FilterImageAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.filterImageRecycler  = requireActivity().findViewById<RecyclerView>(R.id.filterImageRecycler)
        filterImageRecycler.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        filterImageAdapter =  FilterImageAdapter();
        filterImageAdapter.setBitmap(imageBitmap)

        filterImageRecycler.adapter = filterImageAdapter;

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


}