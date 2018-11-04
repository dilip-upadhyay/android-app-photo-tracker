package com.example.dilipupadhyay.phototracking

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import com.example.dilipupadhyay.phototracking.R.id.parent
import kotlinx.android.synthetic.main.list_item.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class ImageAdapter(val items: ArrayList<Image>, val context: Context) : RecyclerView.Adapter<ImageViewHolder>() {
    override fun onBindViewHolder(holder: ImageViewHolder, p1: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        println(" ======Item Count =========${items.size}")
        return items.size
    }


    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int, payloads: MutableList<Any>) {
        holder?.imgPath?.text = items.get(position).imagePath
        val imageFile = File(items.get(position).imagePath)
        val fileInputStream = FileInputStream(imageFile)
        holder?.tvImage?.setImageBitmap(BitmapFactory.decodeStream(fileInputStream))
    }
}

class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val tvImage = view.tv_image
    val imgPath = view.imgPath
}