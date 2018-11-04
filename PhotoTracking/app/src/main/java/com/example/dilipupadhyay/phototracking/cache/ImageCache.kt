package com.example.dilipupadhyay.phototracking.cache

import android.media.Image

interface ImageCache<Key : String, Value : Image> {
    fun get(key: String): Image
    fun set(key: Key, value: Image): Void
}