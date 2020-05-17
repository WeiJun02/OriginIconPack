package com.origin.icon.pack.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.io.*;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Lollipop
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    //用于格式化日期,作为日志文件名的一部分
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINESE);

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        CrashHandler crashHandler = getInstance();
        crashHandler.setContext(context);
        //获取系统默认的UncaughtException处理器
        crashHandler.setDefaultHandler(Thread.getDefaultUncaughtExceptionHandler());
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(crashHandler);
    }

    private void setDefaultHandler(Thread.UncaughtExceptionHandler mDefaultHandler) {
        this.mDefaultHandler = mDefaultHandler;
    }

    private void setContext(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.d("error",ex.getMessage()+";"+ex.getLocalizedMessage());
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }

            //退出程序
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(1);
        }
    }

    public static String outputTextFile(String value){
        return outputTextFile(value,formatter.format(new Date()));
    }

    public static String outputTextFile(String value,String name){
        try{
            String file = OtherUtil.getSDLogPath()+"/OpenLog/"+name+".txt";
            File path = new File(OtherUtil.getSDLogPath()+"/OpenLog");
            if(!path.exists()){
                path.mkdirs();
            }
            FileOutputStream outStream = new FileOutputStream(file,true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream,"gbk");
            writer.write(value);
            writer.flush();
            writer.close();//记得关闭
            return file;
        }catch (Exception e){}
        return null;
    }

    public static void removeLogFile(String name){
        try{
            String file = OtherUtil.getSDLogPath()+"/"+name+".log";
            File path = new File(file);
            if(path.exists()){
                path.delete();
            }
        }catch (Exception e){}
    }

    public static void removeLog(){
        try{
            String file = OtherUtil.getSDLogPath();
            File path = new File(file);
            if(path.exists()){
                path.delete();
            }
        }catch (Exception e){}
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        //保存日志文件
        final String fileName = saveCrashInfo2File(mContext,ex);
        if(mContext!=null){
            //使用Toast来显示异常信息
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
//                    Intent intent = new Intent(mContext, SplashActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

//                    AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, restartIntent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    Looper.loop();
                }
            }.start();
        }
        Log.e("error",ex.getMessage());
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public HashMap<String,String> collectDeviceInfo(Context ctx) {
        HashMap<String,String> deviceInfo = new HashMap<>();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                deviceInfo.put("versionName", versionName);
                deviceInfo.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                deviceInfo.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
            }
        }
        return deviceInfo;
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    public String saveCrashInfo2File(Context context,Throwable ex) {

        StringBuffer stringBuffer = new StringBuffer();
        HashMap<String, String> deviceInfo = collectDeviceInfo(context);
        for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        stringBuffer.append(result);
        return outputTextFile(stringBuffer.toString());
    }
}

