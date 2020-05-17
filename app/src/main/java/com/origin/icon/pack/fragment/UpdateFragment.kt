package com.origin.icon.pack.fragment


import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_update.*

import com.origin.icon.pack.R
import com.origin.icon.pack.util.Utils

/**
 * A simple [Fragment] subclass.
 *
 */
class UpdateFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init(){
        close.setOnClickListener {
            dismiss()
        }

        appVersion.text = "${context!!.resources.getString(R.string.official)} ${Utils.getAppVersionName(context!!)}"
    }

    override fun onStart() {
        super.onStart()
        val window = dialog!!.window
        val windowParams = window!!.attributes
        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics) //为获取屏幕宽高
        windowParams.width = (displayMetrics.widthPixels * 0.9).toInt()
        window.attributes = windowParams
    }
}
