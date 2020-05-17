package com.origin.icon.pack.fragment


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.origin.icon.pack.R
import com.origin.icon.pack.activity.ImageDialog
import com.origin.icon.pack.adapter.IconsAdapter
import com.origin.icon.pack.bean.IconsBean
import kotlinx.android.synthetic.main.fragment_icons.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 *
 */
class IconsFragment : androidx.fragment.app.Fragment() {

    private val iconsList = ArrayList<IconsBean>()
    private var searchIconsList = ArrayList<IconsBean>()
    private lateinit var adapter: IconsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_icons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        recyclerIcons.layoutManager = GridLayoutManager(context, 4)
        adapter = IconsAdapter(context!!,iconsList)
        recyclerIcons.adapter = adapter

        adapter.setClickListener(object : IconsAdapter.OnItemClickListener{
            override fun onClick(icon: Int, name: String) {
                val intent = Intent(context!!, ImageDialog::class.java)
                intent.putExtra("icon",icon)
                intent.putExtra("name",name)
                startActivity(intent)
            }
        })
        reloadIcons()
    }

    private fun loadIcons(){
        iconsList.clear()
        val xml = context!!.resources.getXml(R.xml.drawable)
        var type = xml.eventType
        var category = "All"
        try {
            while (type != XmlPullParser.END_DOCUMENT){
                when(type){
                    XmlPullParser.START_TAG ->{

                        if (xml.name == "category" && xml.attributeCount == 1){
                            category = xml.getAttributeValue(0)
                        }

                        if (xml.name == "item"){
                            if (xml.attributeCount == 1){
                                val drawableString = xml.getAttributeValue(0)
                                val drawableId = context!!.resources.getIdentifier(drawableString,"drawable",context!!.packageName)
                                iconsList.add(IconsBean(category, drawableId,drawableString))
                            }else{
                                val drawableString = xml.getAttributeValue(0)
                                val drawableName = xml.getAttributeValue(1)
                                val drawableId = context!!.resources.getIdentifier(drawableString,"drawable",context!!.packageName)
                                iconsList.add(IconsBean(category, drawableId,drawableName))
                            }
                        }
                    }
                    XmlPullParser.TEXT ->{

                    }
                }
                type = xml.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun search(name: String) {
        if (name.isNotEmpty()){

            doAsync {
                iconsList.clear()
                for (iconName in searchIconsList) {
                    if (iconName.name.contains(name, true)) {
                        iconsList.add(iconName)
                    }
                }

                uiThread {
                    adapter.notifyDataSetChanged()
                }
            }
        }else{
            reloadIcons()
        }
    }

    fun reloadIcons() {

        doAsync {
            loadIcons()
            uiThread {
                if (loading.visibility == View.VISIBLE){
                    loading.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
                searchIconsList.clear()
                searchIconsList = iconsList.clone() as ArrayList<IconsBean>
            }
        }

    }
}
