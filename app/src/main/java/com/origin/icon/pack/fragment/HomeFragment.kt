package com.origin.icon.pack.fragment


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide

import com.origin.icon.pack.R
import kotlinx.android.synthetic.main.fragment_home.*
import android.widget.Toast
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.origin.icon.pack.activity.MainActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URISyntaxException
import android.os.Build.VERSION_CODES.HONEYCOMB
import android.util.Log
import android.view.animation.BounceInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.origin.icon.pack.activity.ImageDialog
import com.origin.icon.pack.adapter.HomeIconsAdapter
import com.origin.icon.pack.adapter.IconsAdapter
import com.origin.icon.pack.bean.AdaptionBean
import java.util.*
import kotlin.math.abs


/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : androidx.fragment.app.Fragment() {

    private var adaptations = 0

    private val allAdaptions = ArrayList<AdaptionBean>()
    private val newAdaptions = ArrayList<AdaptionBean>()

    private val cardItemList = ArrayList<View>()

    companion object{
        private const val ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone"

        private const val INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{payCode}%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"

        private const val MIN_SCALE = 0.82f
        private const val MIN_ALPHA = 0.6f

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        title.text = resources.getString(R.string.home_title)
        introduction.text = resources.getString(R.string.home_subtitle)

        newNumber.text = resources.getString(R.string.new_icons)

        cardPager.clipToPadding = false
        cardPager.setPadding(160, 0, 160, 0)
        cardPager.pageMargin = 60
        cardPager.setPageTransformer(false, DepthPageTransformer())

        iconsPage.setOnClickListener {
            callbacks.callback(1)
        }

        newDialog.setOnClickListener {
            callbacks.callback(2)
        }

        openResource.setOnClickListener {
            startHttp(resources.getString(R.string.home_link))
        }

        for (card in 0..2){

            val cardItem = LayoutInflater.from(context).inflate(R.layout.home_card, null)

            when (card){
                0 -> {
                    Glide.with(context!!).load(R.drawable.ic_alipay).into(cardItem.findViewById(R.id.cardImage))
                    cardItem.findViewById<TextView>(R.id.cardText).text = "如果您能够捐赠我，我将非常感谢"
                    cardItem.findViewById<CardView>(R.id.card).setOnClickListener {

                        cardPager.currentItem = 0
                        startIntentUrl(INTENT_URL_FORMAT.replace("{payCode}", resources.getString(R.string.alipay_code)))
                    }
                }
                1 -> {
                    Glide.with(context!!).load(R.drawable.ic_origin).into(cardItem.findViewById(R.id.cardImage))
                    cardItem.findViewById<TextView>(R.id.cardText).text = "Origin 图标包"
                    cardItem.findViewById<CardView>(R.id.card).setOnClickListener {

                        cardPager.currentItem = 1
                        val x = ObjectAnimator.ofFloat(cardItem, "scaleX", 1f, 0.76f, 1f).setDuration(600)
                        x.interpolator = BounceInterpolator()
                        val y = ObjectAnimator.ofFloat(cardItem, "scaleY", 1f, 0.76f, 1f).setDuration(600)
                        y.interpolator = BounceInterpolator()
                        x.start()
                        y.start()
                    }
                }
                2 -> {
                    Glide.with(context!!).load(R.drawable.ic_stars).into(cardItem.findViewById(R.id.cardImage))
                    cardItem.findViewById<TextView>(R.id.cardText).text = "请为这个作品进行评价"
                    cardItem.findViewById<CardView>(R.id.card).setOnClickListener {

                        cardPager.currentItem = 2
                        openAppStore(packageName = context!!.packageName)
                    }
                }
            }

            cardItemList.add(cardItem)
        }

        cardPager.adapter = SectionsPagerAdapter(cardItemList)
        cardPager.offscreenPageLimit = 3
        cardPager.currentItem = 1

        doAsync{
            parser()
            uiThread {
                iconNumber.text = adaptations.toString()
            }
        }

        contributeClose.setOnClickListener {
            val closeAnimation = ObjectAnimator.ofFloat(contribute, "alpha", 1f, 0f).setDuration(600)
            closeAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    contribute.visibility = View.GONE
                }
            })
            closeAnimation.start()
        }

        contribute.setOnClickListener {
            startIntentUrl(INTENT_URL_FORMAT.replace("{payCode}", resources.getString(R.string.alipay_code)))
        }
    }

    fun showAll(iconsList: ArrayList<AdaptionBean>){
        if (allAdaptions.size < 1) {
            parser()
        }
        if (iconsList.size > 0){
            whatsNewAdaption.visibility = View.VISIBLE
            updateIcons.visibility = View.VISIBLE
        }else{
            return
        }
        newNumber.text = allAdaptions.size.toString()
        updateIcons.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = HomeIconsAdapter(context!!, allAdaptions)
        updateIcons.adapter = adapter
        adapter.setClickListener(object : HomeIconsAdapter.OnItemClickListener{
            override fun onClick(icon: Int, name: String) {
                val intent = Intent(context!!, ImageDialog::class.java)
                intent.putExtra("icon",icon)
                intent.putExtra("name",name)
                startActivity(intent)
            }
        })
        showAdaptions(iconsList)
    }

    fun getNewIcons(iconsList: ArrayList<AdaptionBean>){
        newNumber.text = iconsList.size.toString()
        if (iconsList.size > 0){
            whatsNewAdaption.visibility = View.VISIBLE
            updateIcons.visibility = View.VISIBLE
        }else{
            return
        }
        updateIcons.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = HomeIconsAdapter(context!!, iconsList)
        updateIcons.adapter = adapter
        adapter.setClickListener(object : HomeIconsAdapter.OnItemClickListener{
            override fun onClick(icon: Int, name: String) {
                val intent = Intent(context!!, ImageDialog::class.java)
                intent.putExtra("icon",icon)
                intent.putExtra("name",name)
                startActivity(intent)
            }
        })

        showAdaptions(iconsList)
    }

    private fun showAdaptions(iconsList: ArrayList<AdaptionBean>){
        newAdaptions.clear()
        val pm = context!!.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN,null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(mainIntent,0)
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
        for (reInfo: ResolveInfo in resolveInfos){
            val pkgName = reInfo.activityInfo.packageName // 获得应用程序的包名
            for (adaptionBean: AdaptionBean in iconsList){
                if (adaptionBean.pagName == pkgName && reInfo.activityInfo.name == adaptionBean.activityName){
                    newAdaptions.add(adaptionBean)
                    //包名和activity名一样才算适配
                    break
                }
            }
        }

        if (newAdaptions.size > 0){
            whatsAdaption.visibility = View.VISIBLE
            adaptationIcons.visibility = View.VISIBLE
            contribute.visibility = View.VISIBLE

            adaptationIcons.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val adapter = HomeIconsAdapter(context!!, newAdaptions)
            adaptationIcons.adapter = adapter
            adapter.setClickListener(object : HomeIconsAdapter.OnItemClickListener{
                override fun onClick(icon: Int, name: String) {
                    val intent = Intent(context!!, ImageDialog::class.java)
                    intent.putExtra("icon",icon)
                    intent.putExtra("name",name)
                    startActivity(intent)
                }
            })

            adaptionNumber.text = "对本设备新适配了${newAdaptions.size}应用"
        }
    }

    private fun parser(){
        adaptations = 0
        val xml = context!!.resources.getXml(R.xml.drawable)
        var type = xml.eventType
        try {
            while (type != XmlPullParser.END_DOCUMENT){
                when(type){
                    XmlPullParser.START_TAG ->{
                        if (xml.name == "item"){

                            val drawableString = xml.getAttributeValue(0)
                            allAdaptions.add(AdaptionBean("", "", drawableString))

                            adaptations++
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


    private fun startHttp(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uri)
        context!!.startActivity(intent)
    }


    private fun openAppStore(packageName: String) {
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, resources.getString(R.string.no_store), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    interface Callbacks {
        fun callback(position: Int)
    }

    private lateinit var callbacks: Callbacks

    fun setCallbackListener(callbacks: Callbacks) {
        this.callbacks = callbacks
    }


    private fun startIntentUrl(intentFullUrl: String): Boolean {

        if (hasInstalledAlipayClient(context!!)){
            try {
                val intent = Intent.parseUri(
                    intentFullUrl,
                    Intent.URI_INTENT_SCHEME
                )
                startActivity(intent)
                return true
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                return false
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                return false
            }
        }else{
            Toast.makeText(context, "未安装支付宝", Toast.LENGTH_SHORT).show()
        }

        return false
    }

    /**
     * 判断支付宝客户端是否已安装，建议调用转账前检查
     * @param context Context
     * @return 支付宝客户端是否已安装
     */
    private fun hasInstalledAlipayClient(context: Context): Boolean{
        val pm = context.packageManager
        try {
            val info = pm.getPackageInfo(ALIPAY_PACKAGE_NAME, 0)
            return info != null
        } catch (e: PackageManager.NameNotFoundException ) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter (private val listView: List<View>) : PagerAdapter() {

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getCount(): Int {
            return listView.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(listView[position])
            return listView[position]
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(listView[position])
        }
    }

    @TargetApi(HONEYCOMB)
    inner class DepthPageTransformer : ViewPager.PageTransformer {

        @TargetApi(HONEYCOMB)
        @SuppressLint("NewApi")
        override fun transformPage(view: View, position: Float) {
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.scaleY = MIN_SCALE
                view.alpha = MIN_ALPHA
            } else if (position == 0f) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.scaleY = MIN_SCALE
                view.alpha = MIN_ALPHA
            } else if (position <= 1) { // (0,1]
                // Fade the page out.

                // Counteract the default slide transition

                // Scale the page down (between MIN_SCALE and 1)
                val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                val alphaFactor = MIN_ALPHA + (1 - MIN_ALPHA) * (1 - abs(position))
                view.scaleY = scaleFactor
                view.alpha = alphaFactor
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.scaleY = MIN_SCALE
                view.alpha = MIN_ALPHA
            }
        }
    }
}
