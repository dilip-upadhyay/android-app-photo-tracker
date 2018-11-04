package com.example.dilipupadhyay.phototracking

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File

class Home : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText("All Photos")
                // return@OnNavigationItemSelectedListener true
                val intent = Intent(this, ImageActivity::class.java)
                // start your next activity
                startActivity(intent)
            }
            R.id.navigation_dashboard -> {
                message.setText("Deleted Photos")
                val intent = Intent(this, DeletedImageActivity::class.java)
                // start your next activity
                startActivity(intent)
            }

        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }


}
