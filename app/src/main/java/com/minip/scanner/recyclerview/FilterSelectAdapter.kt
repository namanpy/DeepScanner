package com.minip.scanner.recyclerview

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minip.scanner.R
import java.net.URI



class FilterSelectAdapter(onImgClickListener: OnImageClickListener) : RecyclerView.Adapter<FilterSelectAdapter.FilterSelectHolder>() {
    var images: List<Int> = ArrayList<Int>();
    var name : ArrayList<String> = ArrayList();

    var onImageClickListener : OnImageClickListener

    init {
        onImageClickListener = onImgClickListener

    }

    class FilterSelectHolder(itemView: View, onImgClickListener: OnImageClickListener) : RecyclerView.ViewHolder(itemView) , View.OnClickListener{
        var filterDisplayImage : ImageView = itemView.findViewById(R.id.filterDisplayImage);
        var filterName : TextView = itemView.findViewById(R.id.filterName);
        var onImageClickListener : OnImageClickListener
        init {

            onImageClickListener = onImgClickListener;
            filterDisplayImage.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            onImageClickListener.onImageClick(adapterPosition)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterSelectHolder {
        var view : View = LayoutInflater.from(parent.context).inflate(R.layout.filter, parent, false)
        return FilterSelectHolder(view, onImageClickListener);
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun content(images : ArrayList<Int>, names : ArrayList<String>) {
        this.images = images
        this.name = names

    }

    override fun onBindViewHolder(holder: FilterSelectHolder, position: Int) {
        var image : Int = images.get(position);
        holder.filterDisplayImage.setImageResource(image)
        holder.filterName.setText(name.get(position));
    }

    interface OnImageClickListener {
        fun onImageClick(position : Int)
    }

}