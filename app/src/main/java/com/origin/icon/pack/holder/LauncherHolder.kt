package com.origin.icon.pack.holder

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.origin.icon.pack.R
import com.origin.icon.pack.bean.LauncherItem
import com.origin.icon.pack.util.Utils

class LauncherHolder(itemView: View, private var listener: OnLauncherClickListener) : RecyclerView.ViewHolder(itemView) {

    private lateinit var launcher: LauncherItem
    private val launcherLogo = itemView.findViewById<ImageView>(R.id.launcherLogo)
    private val launcherName = itemView.findViewById<TextView>(R.id.launcherName)

    init {
        itemView.findViewById<CardView>(R.id.launcherCard).setOnClickListener {

            Log.e("HolderClick", "HolderClick")
            this.listener.onLauncherClick(launcher)
        }
    }

    fun setItem(context: Context,item: LauncherItem){

        launcher = item

        val iconName = "ic_" + launcher.name.toLowerCase().replace(" ", "_")
        val iconResource = Utils.getIconResId(context.resources,context.packageName,iconName)

        val option = RequestOptions().priority(Priority.IMMEDIATE)
        Glide.with(context)
            .load(
                if (iconResource != 0)
                    iconResource
                else
                    Utils.getIconResId(
                        context
                            .resources, context.packageName, "ic_na_launcher"
                    )
            ).apply(option)
            .into(launcherLogo)
        launcherName.text = launcher.name

        if (isInstalled(context,launcher.isInstalled,launcher.packageName)){
            launcherLogo.colorFilter = null
            launcherName.setBackgroundColor(launcher.launcherColor)
            launcherName.setTextColor(ContextCompat.getColor(context, R.color.white))
        }else{
            launcherLogo.colorFilter = bnwFilter()
            launcherName.setBackgroundColor(Color.TRANSPARENT)
            launcherName.setTextColor(ContextCompat.getColor(context, R.color.text_color))
        }
    }

    private fun isInstalled(context: Context, isInstall: Int, packageName: String): Boolean {
        var endData = false
        if (isInstall == -1) {
            if ("org.cyanogenmod.theme.chooser" == packageName) {
                if (Utils.isAppInstalled(context, "org.cyanogenmod.theme.chooser") || Utils.isAppInstalled(
                        context,
                        "com.cyngn.theme.chooser"
                    )
                ) {
                    return true
                }
            } else {
                endData = Utils.isAppInstalled(context, packageName)
            }
        }
        // Caches this value, checking if a launcher is installed is intensive on processing
        return endData
    }

    private fun bnwFilter(): ColorFilter {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        return ColorMatrixColorFilter(matrix)
    }

    interface OnLauncherClickListener {
        fun onLauncherClick(item: LauncherItem)
    }

}