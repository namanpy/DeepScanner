package com.minip.scanner.recyclerview

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.minip.scanner.R
import java.net.URI



class ImageAdapter(onImgClickListener: OnImageClickListener) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    var images: List<Uri> = ArrayList<Uri>();
    var onImageClickListener : OnImageClickListener

    init {
        onImageClickListener = onImgClickListener
    }

    class ImageViewHolder(itemView: View, onImgClickListener: OnImageClickListener) : RecyclerView.ViewHolder(itemView) , View.OnClickListener{
        var imageView : ImageView = itemView.findViewById(R.id.image);
        var onImageClickListener : OnImageClickListener
        init {

            onImageClickListener = onImgClickListener;
            imageView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
           onImageClickListener.onImageClick(adapterPosition)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        var view : View = LayoutInflater.from(parent.context).inflate(R.layout.imagedisplay, parent, false)
        return ImageViewHolder(view, onImageClickListener);
    }

    override fun getItemCount(): Int {
        return images.size
    }
    fun setUri(urilist  : List<Uri>){
        images = urilist
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        var uri : Uri = images.get(position);
        holder.imageView.setImageURI(uri);
    }

    interface OnImageClickListener {
        fun onImageClick(position : Int)
    }

}