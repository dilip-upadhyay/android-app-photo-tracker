package com.example.dilipupadhyay.phototracking

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_deleted_image.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class DeletedImageActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deleted_image)


        // Creates a vertical Layout Manager
        //rv_animal_list.layoutManager = LinearLayoutManager(this)

        // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
        image_list.layoutManager = GridLayoutManager(this, 3)

        // Access the RecyclerView Adapter and load the data into it

        if (!checkSelfPermission()) {
            requestPermission()
        } else {
            // if permission granted read images from storage.
            //  source code for this function can be found below.
            var cacheFile= cacheDir.absolutePath + File.separator+"phototracking.data"
            var imagesList = getAllShownImagesPath(this)

            var imagesListStr = getAllShownImagesPath(this, imagesList.get(0).folderNames, false)

            val file = File(cacheFile)
            val fileExists = file.exists()
            val finalList: ArrayList<Image> = ArrayList()
            if(fileExists) {
                val lastFileDetails: ArrayList<String> = file.readLines() as ArrayList<String>
                val currentFileList: ArrayList<String> = ArrayList<String>()
                imagesListStr.forEach { currentFileList.add(it.imagePath) }


                lastFileDetails.removeAll(currentFileList)
                lastFileDetails.forEach { finalList.add(Image(it, false)) }


            }
            file.printWriter().use { out ->

                imagesListStr.forEach {
                    out.println(it.imagePath)
                }
            }

            imagesListStr.forEach {
                val fileName = it.imagePath.substring(it.imagePath.lastIndexOf(File.separator)+1)
                val fileInputStream = FileInputStream(it.imagePath)
                fileInputStream.toFile(cacheDir.absolutePath + File.separator+fileName)
             }
            val finalListToShow : ArrayList<Image> = ArrayList()
            finalList.forEach{
                val fileName = it.imagePath.substring(it.imagePath.lastIndexOf(File.separator)+1)
                val imagePathToSHow=cacheDir.absolutePath + File.separator+fileName
                finalListToShow.add(Image(imagePathToSHow, false))
            }
            image_list.adapter = ImageAdapter(finalListToShow, this)

        }


    }
    fun InputStream.toFile(path: String) {
        use { input ->
            File(path).outputStream().use { input.copyTo(it) }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 6036)
    }


    private fun checkSelfPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        } else
            return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            6036 -> {
                if (grantResults.size > 0) {
                    var permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (permissionGranted) {

                        // Now we are ready to access device storage and read images stored on device.

                        loadAllImages()
                    } else {
                        Toast.makeText(this, "Permission Denied! Cannot load images.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun loadAllImages() {
        var imagesList = getAllShownImagesPath(this)
        var intent = Intent(this, ImageActivity::class.java)
        intent.putParcelableArrayListExtra("image_url_data", imagesList)
        startActivity(intent)
        finish()
    }

    private fun getAllShownImagesPath(activity: Activity): ArrayList<Albums> {

        val uri: Uri
        val cursor: Cursor
        var cursorBucket: Cursor
        val column_index_data: Int
        val column_index_folder_name: Int
        val listOfAllImages = ArrayList<String>()
        var absolutePathOfImage: String? = null
        var albumsList = ArrayList<Albums>()


        val BUCKET_GROUP_BY = "1) GROUP BY 1,(2"
        val BUCKET_ORDER_BY = "MAX(datetaken) DESC"

        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATA
        )

        cursor = activity.contentResolver.query(uri, projection, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY)

        if (cursor != null) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data)
                Log.d("title_apps", "bucket name:" + cursor.getString(column_index_data))

                val selectionArgs = arrayOf("%" + cursor.getString(column_index_folder_name) + "%")
                val selection = MediaStore.Images.Media.DATA + " like ? "
                val projectionOnlyBucket =
                    arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                cursorBucket = activity.contentResolver.query(uri, projectionOnlyBucket, selection, selectionArgs, null)
                Log.d("title_apps", "bucket size:" + cursorBucket.count)

                if (absolutePathOfImage != "" && absolutePathOfImage != null) {
                    listOfAllImages.add(absolutePathOfImage)
                    albumsList.add(
                        Albums(
                            cursor.getString(column_index_folder_name),
                            absolutePathOfImage,
                            cursorBucket.count,
                            false
                        )
                    )

                }
            }
        }
        return getListOfVideoFolders(albumsList)
    }

    // Read all images path from specified directory.

    private fun getAllShownImagesPath(activity: Activity, folderName: String?, isVideo: Boolean?): ArrayList<Image> {

        val uri: Uri
        val cursorBucket: Cursor
        val column_index_data: Int
        val listOfAllImages = ArrayList<Image>()
        var absolutePathOfImage: String? = null

        val selectionArgs = arrayOf("%" + folderName + "%")

        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Images.Media.DATA + " like ? "

        val projectionOnlyBucket = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        cursorBucket = activity.contentResolver.query(uri, projectionOnlyBucket, selection, selectionArgs, null)

        column_index_data = cursorBucket.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

        while (cursorBucket.moveToNext()) {
            absolutePathOfImage = cursorBucket.getString(column_index_data)
            if (absolutePathOfImage != "" && absolutePathOfImage != null)
                listOfAllImages.add(Image(absolutePathOfImage, false))
        }
        return listOfAllImages
    }

    // This function is resposible to read all videos from all folders.
    private fun getListOfVideoFolders(albumsList: ArrayList<Albums>): ArrayList<Albums> {

        var cursor: Cursor
        var cursorBucket: Cursor
        var uri: Uri
        val BUCKET_GROUP_BY = "1) GROUP BY 1,(2"
        val BUCKET_ORDER_BY = "MAX(datetaken) DESC"
        val column_index_album_name: Int
        val column_index_album_video: Int

        uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection1 = arrayOf(
            MediaStore.Video.VideoColumns.BUCKET_ID,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATE_TAKEN,
            MediaStore.Video.VideoColumns.DATA
        )

        cursor = this.contentResolver.query(uri, projection1, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY)

        if (cursor != null) {
            column_index_album_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            column_index_album_video = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            while (cursor.moveToNext()) {
                Log.d("title_apps", "bucket video:" + cursor.getString(column_index_album_name))
                Log.d("title_apps", "bucket video:" + cursor.getString(column_index_album_video))
                val selectionArgs = arrayOf("%" + cursor.getString(column_index_album_name) + "%")

                val selection = MediaStore.Video.Media.DATA + " like ? "
                val projectionOnlyBucket =
                    arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                cursorBucket = this.contentResolver.query(uri, projectionOnlyBucket, selection, selectionArgs, null)
                Log.d("title_apps", "bucket size:" + cursorBucket.count)

                albumsList.add(
                    Albums(
                        cursor.getString(column_index_album_name),
                        cursor.getString(column_index_album_video),
                        cursorBucket.count,
                        true
                    )
                )
            }
        }
        return albumsList
    }

}
