package com.origin.icon.pack.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.origin.icon.pack.R
import com.origin.icon.pack.fragment.*
import com.origin.icon.pack.listener.AppBarStateChangeListener

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_box.*
import com.origin.icon.pack.bean.AdaptionBean
import com.origin.icon.pack.util.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.io.*
import java.lang.StringBuilder
import java.util.ArrayList
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SectionsPagerAdapter
    private var fabStatus = 1
    private lateinit var requestsFragment: RequestFragment
    private var homeFragment: HomeFragment? = null
    private lateinit var iconsFragment: IconsFragment
    private val icons = ArrayList<Int>()
    private var tabTextColor = 0xffffff
    private lateinit var saveThread: Thread

    /**
     * 获取未授权的权限
     */
    private lateinit var permissionList:MutableList<String>

    /**
     * 请求权限的返回值
     */
    private val permissionCode = 1
    /**
     * 已适配列表
     */
    private var adaptations: ArrayList<AdaptionBean> = ArrayList()
    /**
     * 旧的已适配列表
     */
    private var adaptationsOld: ArrayList<AdaptionBean> = ArrayList()

    /**
     * 旧的已适配列表
     */
    private var adaptationsNew: ArrayList<AdaptionBean> = ArrayList()

    private var mHandler= @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            when(msg!!.what){
                1 -> {
                    explore()
                }
                2 ->{
                    updateContent()
                }
                3 ->{

                    val data = msg.obj.toString()
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("出现错误")
                        .setMessage("appfilter.xml 很可能出错，请检查：\n${data}")
                        .setPositiveButton("点击复制") { _, _ ->
                            Utils.copy(data, this@MainActivity)
                        }.setNeutralButton(resources.getString(R.string.cancel)){_,_->

                        }.show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.backgroundColor)
        }else{
            window.navigationBarColor = Color.BLACK
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        tabTextColor = ContextCompat.getColor(this@MainActivity,R.color.white)
        checkVersion()
        getPermission()
        initView()

    }

    private fun initView(){
//        val wallpaperManager = WallpaperManager.getInstance(this)
//        Glide.with(this).load(wallpaperManager.drawable).into(headImg)

        loadIcons()
        collapsingToolbar.setExpandedTitleColor(tabTextColor) //设置还没收缩时状态下字体颜色
        collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this@MainActivity,R.color.black_text))//设置收缩后Toolbar上字体的颜色
        collapsingToolbar.title = resources.getString(R.string.app_name)
        collapsingToolbar.expandedTitleMarginBottom = DisplayUtil.dip2px(this,60f)

        adapter = SectionsPagerAdapter(supportFragmentManager)
        setupViewPager(pager)
        pager.offscreenPageLimit = 5
        tab.setupWithViewPager(pager)
        tab.setSelectedTabIndicatorColor(tabTextColor)

        appBar.addOnOffsetChangedListener(object : AppBarStateChangeListener(){
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?, i: Int, max: Int) {
                tab.tabTextColors = ColorStateList.valueOf(ColorUtil.getColor(tabTextColor, ContextCompat.getColor(this@MainActivity,R.color.text_color),i.toFloat(),max.toFloat()))
                tab.setSelectedTabIndicatorColor(ColorUtil.getColor(tabTextColor, ContextCompat.getColor(this@MainActivity,R.color.text_color),i.toFloat(),max.toFloat()))
            }
        })

        closeSearch.setOnClickListener {
            if (searchInput.text.isNotEmpty()){
                searchInput.setText("")
            }else{
                closeKeyboard()
                search()
                iconsFragment.reloadIcons()
            }
        }

        actionSearch.setOnClickListener {
            closeKeyboard()
            iconsFragment.search(searchInput.text.toString())
        }

        searchInput.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                iconsFragment.search(p0.toString())
                Log.e("SearchTextChange1", p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.e("SearchTextChange2", p0.toString())
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.e("SearchTextChange3", p0.toString())
            }

        })

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageSelected(p0: Int) {
                when(p0){
                    0 ->{
                        if (fabStatus!=1){
                            fabStatus = 1
                            changeFabIcon(R.drawable.ic_search_white_24dp)
                        }
                        closeSearch()
                    }
                    1 ->{
                        iconsFragment = adapter.getFragment(1) as IconsFragment
                        if (fabStatus!=1){
                            fabStatus = 1
                            changeFabIcon(R.drawable.ic_search_white_24dp)
                        }
                    }

                    2 ->{
                        fabStatus = 2
                        changeFabIcon(R.drawable.ic_send_white_24dp)
                        requestsFragment = adapter.getFragment(2) as RequestFragment
                        requestsFragment.setCallbackListener(object : RequestFragment.Callbacks{
                            override fun callback(position: Int) {
                                when (position) {
                                    0 -> hide(fabSend)
                                    1 -> show(fabSend)
                                    2 -> SnackbarUtil().SnackbarUtil(this@MainActivity, fabSend ,resources.getString(R.string.no_choose_app))
                                }
                            }
                        })
                        closeSearch()
                    }
                    3 ->{
                        if (fabStatus!=1){
                            fabStatus = 1
                            changeFabIcon(R.drawable.ic_search_white_24dp)
                        }
                        closeSearch()
                    }
                    4 ->{
                        if (fabStatus!=1){
                            fabStatus = 1
                            changeFabIcon(R.drawable.ic_search_white_24dp)
                        }
                        closeSearch()
                    }
                }
            }

        })

        refresh.setOnClickListener {
            loadIcons()
        }


        fabSend.setOnClickListener {

            if (fabStatus==2){
                if (permissionList.isEmpty()){

                    requestsFragment.send()

                }else{
                    Snackbar.make(fabSend,resources.getString(R.string.get_permission),
                        Snackbar.LENGTH_SHORT).show()
                }
            }else {
                pager.currentItem = 1
                search()
            }
        }
    }

    private fun closeSearch() {
        if (searchBox.visibility == View.VISIBLE){
            searchInput.setText("")
            closeKeyboard()
            search()
            iconsFragment.reloadIcons()
        }
    }

    private fun search() {

        if (searchBox.visibility != View.VISIBLE){

            appBar.setExpanded(true)

            ObjectAnimator.ofFloat(icon1, "alpha",1f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon1, "translationY", 0f, 200f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon2, "alpha",1f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon2, "translationY", 0f, 200f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon3, "alpha",1f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon3, "translationY", 0f, 200f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon4, "alpha",1f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon4, "translationY", 0f, 200f).setDuration(300).start()

            val searchBoxAnimation = ObjectAnimator.ofFloat(searchBox, "translationY", -200f, 0f)
            ObjectAnimator.ofFloat(searchBox, "alpha",0f, 1f).setDuration(300).start()
            searchBoxAnimation.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    searchInput.isFocusable = true
                    searchInput.isFocusableInTouchMode = true
                    searchInput.requestFocus()

                    val inputManager =
                        searchInput.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.showSoftInput(searchInput, 0)

                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationStart(p0: Animator?) {
                    searchBox.visibility = View.VISIBLE
                }
            })
            searchBoxAnimation.duration = 300
            searchBoxAnimation.start()
        }else {

            ObjectAnimator.ofFloat(icon1, "translationY", 200f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon1, "alpha",0f, 1f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon2, "translationY", 200f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon2, "alpha",0f, 1f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon3, "translationY", 200f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon3, "alpha",0f, 1f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon4, "translationY", 200f, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(icon4, "alpha",0f, 1f).setDuration(300).start()

            val searchBoxAnimation = ObjectAnimator.ofFloat(searchBox, "translationY", 0f, -200f)
            ObjectAnimator.ofFloat(searchBox, "alpha",1f, 0f).setDuration(300).start()
            searchBoxAnimation.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {

                    searchBox.visibility = View.GONE
                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationStart(p0: Animator?) {
                }
            })
            searchBoxAnimation.duration = 300
            searchBoxAnimation.start()
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (homeFragment == null){
            homeFragment = adapter.getFragment(0) as HomeFragment
            homeFragment!!.setCallbackListener(object : HomeFragment.Callbacks{
                override fun callback(position: Int) {
                    when(position) {
                        1 -> pager.currentItem = 1
                        2 -> updateContent()
                    }
                }
            })
            showUpdateIcons()
        }
    }

    private var threadIndex = 0

    private fun showUpdateIcons(){

        threadIndex++
        if (threadIndex < 2){
            return
        }
        check()
    }

    private fun closeKeyboard(){
        searchInput.clearFocus()
        val `in` = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        `in`.hideSoftInputFromWindow(searchInput.windowToken, 0)

    }

    /**
     * 检索壁纸颜色
     */
    private fun checkWallpaper() {

        val wallpaperManager = WallpaperManager.getInstance(this)

        Glide.with(this@MainActivity).load(drawableToBitmap(wallpaperManager.drawable)).into(headImg)

        doAsync{

            tabTextColor = if (getBright(Bitmap.createScaledBitmap(drawableToBitmap(wallpaperManager.drawable), 500, 300, false)) > 220){
                ContextCompat.getColor(this@MainActivity, R.color.text_color)
            }else{
                ContextCompat.getColor(this@MainActivity, R.color.white)
            }

            uiThread {

                collapsingToolbar.setExpandedTitleColor(tabTextColor) //设置还没收缩时状态下字体颜色
                tab.tabTextColors = ColorStateList.valueOf(tabTextColor)
                tab.setSelectedTabIndicatorColor(tabTextColor)

            }
        }
    }

    private fun getBright(bm: Bitmap): Int {
        val width = bm.width
        val height = bm.height
        var r: Int
        var g: Int
        var b: Int
        var count = 0
        var bright = 0
        for (i: Int in 0 until width) {
            for (j: Int in 0 until height) {
                count++
                val localTemp = bm.getPixel(i, j)
                r = localTemp.or(0xff00ffff.toInt()).shr(8).and(0x00ff)
                g = localTemp.or(0xffff00ff.toInt()).shr(8).and(0x0000ff)
                b = localTemp.or(0xffffff00.toInt()).and(0x0000ff)
                bright = (bright + 0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }
        }
        return bright/count
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap: Bitmap

        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if(bitmapDrawable.bitmap != null) {
                //压缩bitmap以便更快识别图像
                return bitmapDrawable.bitmap
            }
        }

        if(drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * 探索App 特性与功能
     */
    private fun explore() {
        val accentColor = ContextCompat.getColor(this, R.color.primary)
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.fabSend)
            .setPrimaryText("探索图标")
            .setSecondaryText("点击此处搜索图标或应用至启动器!")
            .setBackgroundColour(Color.argb(244, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)))
            .setPromptStateChangeListener { prompt, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED){
                    updateContent()
                }
            }.show()
    }

    /**
     * 检查版本是否与上次一致
     */
    private fun checkVersion() {

        saveThread = object : Thread(){
            override fun run() {
                super.run()

                //判断是否首次打开软件
                if (!PreferencesUtil.get(this@MainActivity, "first", false)){
                    PreferencesUtil.put(this@MainActivity, "first", true)
                    PreferencesUtil.put(this@MainActivity, "versionCode", Utils.getAppVersion(this@MainActivity))

                    //保存到新文件
                    saveAppFilter(parser())

                    val file = File(filesDir, "appfilter.xml")
                    val files = File(file.parent)
                    files.mkdirs()
                    file.createNewFile()

                    val msg = Message()
                    msg.what = 1
                    mHandler.sendMessage(msg)

                }

                //判断是否首次更新软件
                if (PreferencesUtil.get(this@MainActivity, "versionCode", -1) != -1 && PreferencesUtil.get(this@MainActivity, "versionCode", -1) != Utils.getAppVersion(this@MainActivity)){

                    //不是首次更新，将旧版更改为上个版本的数据
                    try {
                        if (File(filesDir, "appfilter.xml").delete()){
                            val files = File(filesDir, "appfilter.xml")
                            File(filesDir, "appfilter-new.xml").renameTo(files)
                        }
                    }catch (e: IOException){

                    }
                    //新版本保存到新文件(获取新版数据)
                    saveAppFilter(parser())
                    val msg = Message()
                    msg.what = 2
                    mHandler.sendMessage(msg)
                    PreferencesUtil.put(this@MainActivity, "versionCode", Utils.getAppVersion(this@MainActivity))
                }else{
                    parser()
                }
                showUpdateIcons()
            }
        }

        saveThread.start()
    }

    private fun check(){

        if (File(filesDir, "appfilter.xml").exists()){
            //获取旧版数据
            if (parser2().isNotEmpty()){
                for (v in adaptations) {
                    if (checkIndexOf(adaptationsOld, v) < 0){
                        adaptationsNew.add(v)
                    }
                }
                homeFragment!!.getNewIcons(adaptationsNew)
            }else{
                homeFragment!!.showAll(adaptations)
            }
        }
    }

    private fun checkIndexOf(list: ArrayList<AdaptionBean>, item: AdaptionBean): Int{
        for (index in list.indices){
            if (list[index].pagName == item.pagName || list[index].icon == item.icon ){
                return index
            }
        }
        return -1
    }

    /**
     * 此版本更新信息
     */
    private fun updateContent() {
        UpdateFragment().show(supportFragmentManager,"UpdateDialog")
    }

    /**
     * 获取必要权限
     */
    private fun getPermission(){
        val permission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionList = mutableListOf()
        permissionList.clear()

        //获取未授权的权限
        for (permiss:String in permission){
            if (ContextCompat.checkSelfPermission(this@MainActivity, permiss) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permiss)
            }
        }

        if (permissionList.isNotEmpty()){
            //请求权限方法
            val permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(this@MainActivity, permissions, permissionCode)
        }else{
            checkWallpaper()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            permissionCode ->{
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,resources.getString(R.string.need_permission), Toast.LENGTH_SHORT).show()
                }else{
                    permissionList.clear()
                    checkWallpaper()
                }
            }
            else ->{}
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    private fun setupViewPager(viewPager: ViewPager) {
        adapter.addFragment(HomeFragment(),resources.getString(R.string.home))
        adapter.addFragment(IconsFragment(),resources.getString(R.string.icons))
        adapter.addFragment(RequestFragment(),resources.getString(R.string.icon_adapter))
        adapter.addFragment(ApplyFragment(),resources.getString(R.string.apply))
        adapter.addFragment(AboutFragment(),resources.getString(R.string.about))
        viewPager.adapter = adapter
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter (fm: FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {

        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }

        fun getFragment(position: Int): Fragment {
            return mFragmentList[position]
        }
    }


    /**
     * 显示的动画
     */
    private fun show(view: View) {
        view.animate().cancel()

        // If the view isn't visible currently, we'll animate it from a single pixel
        view.alpha = 0f
        view.scaleY = 0f
        view.scaleX = 0f

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(200)
            .setInterpolator(LinearOutSlowInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {

                }
            })
    }

    /**
     * 隐藏的动画
     */
    private fun hide(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(FastOutLinearInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                private var mCancelled: Boolean = false

                override fun onAnimationStart(animation: Animator) {
                    view.visibility = View.VISIBLE
                    mCancelled = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    mCancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!mCancelled) {
                        view.visibility = View.INVISIBLE
                    }
                }
            })
    }

    private fun changeFabIcon(icon: Int){
        val animator = ObjectAnimator.ofFloat(fabSend, "rotation", 0f, 360f)
        animator.duration = 360
        animator.interpolator = DecelerateInterpolator()
        animator.start()

        fabSend.setImageResource(icon)
    }

    private fun loadIcons(){
        val rs =  ArrayList<Int>()

        doAsync {
            if (icons.size == 0){
                val xml = resources.getXml(R.xml.drawable)
                var type = xml.eventType
                try {
                    while (type != XmlPullParser.END_DOCUMENT){
                        when(type){
                            XmlPullParser.START_TAG ->{
                                if (xml.name == "item"){
//                            Log.e("6666666",xml.getAttributeValue(0))
//                            Log.e("6666666",xml.getAttributeValue(1))

                                    val drawableString = xml.getAttributeValue(0)
                                    val drawableId = resources.getIdentifier(drawableString,"drawable",this@MainActivity.packageName)
                                    icons.add(drawableId)
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

            var b = true
            while (b){
                var bo = true
                val r = Random.nextInt(icons.size)%(icons.size+1)
                for (j: Int in rs){
                    if (j == r){
                        //如果在数组里找到这个随机数则跳出循环
                        if (rs.size >= 4){
                            b = false
                        }
                        bo = false
                        break
                    }
                }

                if (bo){
                    if (rs.size < 4){
                        rs.add(r)
                    }
                }

            }

            uiThread {
                iconAnimator(icon1)
                iconAnimator(icon2)
                iconAnimator(icon3)
                iconAnimator(icon4)
                Glide.with(this@MainActivity).load(icons[rs[0]]).into(icon1)
                Glide.with(this@MainActivity).load(icons[rs[1]]).into(icon2)
                Glide.with(this@MainActivity).load(icons[rs[2]]).into(icon3)
                Glide.with(this@MainActivity).load(icons[rs[3]]).into(icon4)

            }
        }

    }

    private fun iconAnimator(v: View){

        val animator1 = ObjectAnimator.ofFloat(v, "scaleX", 0f, 1f)
        animator1.duration = 600
        animator1.interpolator = DecelerateInterpolator()
        animator1.start()

        val animator2 = ObjectAnimator.ofFloat(v, "scaleY", 0f, 1f)
        animator2.duration = 600
        animator2.interpolator = DecelerateInterpolator()
        animator2.start()

    }

    /**
     * 获取当前版本appfilter
     */
    private fun parser(): String{
        adaptations.clear()
        val err = StringBuilder()
        val sb = StringBuilder()
        val xml = resources.getXml(R.xml.appfilter)
        var type = xml.eventType
        try {
            while (type != XmlPullParser.END_DOCUMENT){
                when(type){
                    XmlPullParser.START_TAG ->{
                        if (xml.name == "item"){
                            val pkgActivity = xml.getAttributeValue(0)
                            val drawable = xml.getAttributeValue(1)
                            if (pkgActivity.indexOf("{") > 0 && pkgActivity.indexOf("{")+1 < pkgActivity.indexOf("/") && pkgActivity.indexOf("/")+1 < pkgActivity.indexOf("}")){
                                adaptations.add(AdaptionBean(pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/")), pkgActivity.substring(pkgActivity.indexOf("/")+1,pkgActivity.indexOf("}")),drawable))

                                sb.append("<item component=\"$pkgActivity\" drawable=\"${drawable}\" />\r\n")
                            }else{
                                err.append("在${pkgActivity} 附近处有一处错误\r\n")
                                Log.e("SeeErr",pkgActivity)
                            }
                        }
                    }
                    XmlPullParser.TEXT ->{

                    }
                }
                type = xml.next()
            }
            if (err.isNotEmpty()){

                val msg = Message()
                msg.what = 3
                msg.obj = err.toString()
                mHandler.sendMessage(msg)
            }

        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    /**
     * 获取旧版appfilter
     */
    private fun parser2(): String{

        val appFilter = File(filesDir, "appfilter.xml")
        val content = StringBuilder()
        appFilter.forEachLine { line ->
            content.append(line)
            content.append("\r\n")
        }

        if (content.isNotEmpty()){

            val xml = XmlPullParserFactory.newInstance().newPullParser()
            xml.setInput(StringReader(content.toString()))
            var type = xml.eventType
            try {
                while (type != XmlPullParser.END_DOCUMENT){
                    when(type){
                        XmlPullParser.START_TAG ->{
                            if (xml.name == "item"){
                                val pkgActivity = xml.getAttributeValue(0)
                                if (pkgActivity.indexOf("{")+1 < pkgActivity.indexOf("/") && pkgActivity.indexOf("/")+1 < pkgActivity.indexOf("}")){
                                    adaptationsOld.add(AdaptionBean(pkgActivity.substring(pkgActivity.indexOf("{")+1,pkgActivity.indexOf("/")), pkgActivity.substring(pkgActivity.indexOf("/")+1,pkgActivity.indexOf("}")),xml.getAttributeValue(0)))
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
        return content.toString()
    }

    /**
     * 保存当前版本appfilter
     */
    private fun saveAppFilter(appFilter: String) {

        val file = File(filesDir, "appfilter-new.xml")
        var out: FileOutputStream? = null
        try {
            if (!file.exists()) {
                val files = File(file.parent)
                files.mkdirs()
                file.createNewFile()
            }

            out = FileOutputStream(file,false)
            out.write(appFilter.toByteArray())

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                out?.flush()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    override fun onBackPressed() {
        if (searchBox.visibility == View.VISIBLE){
            if (searchInput.text.isNotEmpty()){
                searchInput.setText("")
            }else{
                closeKeyboard()
                search()
                iconsFragment.reloadIcons()
            }
        }else if (pager.currentItem != 0){
            pager.currentItem = 0
        }else{
            super.onBackPressed()
        }
    }
}
