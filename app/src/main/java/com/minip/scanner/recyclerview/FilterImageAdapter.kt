package com.minip.scanner.recyclerview

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.minip.scanner.R

class FilterImageAdapter : RecyclerView.Adapter<FilterImageAdapter.FilterImageViewHolder>() {
    var images: List<Bitmap> = ArrayList<Bitmap>();


    class FilterImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView : ImageView = itemView.findViewById(R.id.image);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterImageViewHolder {
        var view : View = LayoutInflater.from(parent.context).inflate(R.layout.filter_image_display, parent, false)
        return FilterImageViewHolder(view);
    }

    override fun getItemCount(): Int {
        return images.size
    }
    fun setBitmap(bitmaplist  : List<Bitmap>){
        images = bitmaplist
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: FilterImageViewHolder, position: Int) {
        var bitmap : Bitmap = images.get(position);
        holder.imageView.setImageBitmap(bitmap);
    }

}