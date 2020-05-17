package com.origin.icon.pack.util

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.origin.icon.pack.R


class SnackbarUtil {

    fun SnackbarUtil(context: Context, views: View, content: String) {

        val snackbar = Snackbar.make(views, content, Snackbar.LENGTH_SHORT)
        val view = snackbar.view
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        snackbar.show()

    }
}