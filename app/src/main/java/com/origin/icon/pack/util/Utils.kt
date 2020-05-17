package com.origin.icon.pack.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import com.origin.icon.pack.R

object Utils {

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val installed: Boolean
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        return installed
    }

    fun getIconResId(r: Resources, p: String, name: String): Int {
        val res = r.getIdentifier(name, "drawable", p)
        return if (res != 0) {
            res
        } else {
            0
        }
    }

    /**
     * 获取App版本号
     */
    fun getAppVersion(context: Context): Int{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        }
    }

    /**
     * 获取App版本号名称
     */
    fun getAppVersionName(context: Context): String{
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName

    }

    /**
     * 获取应用包名
     */
    fun getAppPackName(context: Context): String{
        return context.packageManager.getPackageInfo(context.packageName, 0).packageName

    }

    /**
     * 实现文本复制功能
     * @param content
     */
    fun copy(content: String, context: Context) {
        // 得到剪贴板管理器
        val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = ClipData.newPlainText(context.getString(R.string.app_name),content.trim())
        cmb.setPrimaryClip(data)
    }
}