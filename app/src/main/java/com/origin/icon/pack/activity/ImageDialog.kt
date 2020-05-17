package com.origin.icon.pack.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.origin.icon.pack.R
import kotlinx.android.synthetic.main.activity_image_dialog.*

class ImageDialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_dialog)
        /*
        val m = windowManager
        val d = m.defaultDisplay //为获取屏幕宽高
        val p = window.attributes
//        p.height = (int) (d.getHeight()*0.9); //高度设置为屏幕的0.8
        p.width = d.width * 1 //宽度设置为屏幕的0.8
        window.attributes = p
        */
        initView()
    }

    private fun initView(){
        val image = intent.getIntExtra("icon",R.mipmap.ic_launcher)
        val iconName = intent.getStringExtra("name")
        Glide.with(this).load(image).into(icon)
        name.text = iconName

        close.setOnClickListener {
            finish()
        }
    }
}
