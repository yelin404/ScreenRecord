package com.example.ScreenRecorder

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

object  MediaProjectionHelper {



    var tag: String = "123"

    //这里定义需要动态请求的权限，注意：这些权限必须现在Manifest里面申请
    private final val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )

    //请求码，用于在onRequestPermissionsResult函数中区分是谁发送的请求
    private final val PERMISSION_REQUEST_CODE = 1

    //请求码，用于startActivityForResult中
    private final val REQUEST_CODE = 2024

    private var screenRecordService: ScreenRecordService? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null

    private var metrics: DisplayMetrics? = null

    private var serviceConnection: ServiceConnection? = null

    fun getInstance(): MediaProjectionHelper {
        return MediaProjectionHelper
    }


    fun getService(): Service? {
        return screenRecordService
    }


    fun isRecording(): Boolean {
        return screenRecordService!!.isRunning()
    }


    fun setMediProjection(resultCode: Int,data: Intent?) {
        mediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode,data!!)
        screenRecordService!!.setMediaProjection(mediaProjection!!)
    }


    fun startService(activity: Activity) {

        mediaProjectionManager = activity.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        activity.startActivityForResult(mediaProjectionManager!!.createScreenCaptureIntent(),REQUEST_CODE)

        //设置屏幕宽高
        metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)



        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

                //获取seivice对象
                if (service is ScreenRecordService.ScreenRecordBinder) {
                    screenRecordService = service.getService()
                    /**
                     *  这段代码有点问题,无法保证screenRecodrService已经实例化
                     */
                    screenRecordService!!.setMediaManager(mediaProjectionManager!!)

                    //screenRecordService!!.setMediaProjection(mediaProjection!!)

                    screenRecordService!!.setConfig(metrics!!.widthPixels,metrics!!.heightPixels,metrics!!.densityDpi)

                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {

                //释放内存
                screenRecordService = null
            }

        }
        ScreenRecordService.bindService(activity,serviceConnection!!)

    }


    fun stopService(activity: Activity) {
        screenRecordService = null
        if (serviceConnection != null ) {
            ScreenRecordService.unbindService(activity,serviceConnection!!)
            serviceConnection = null
        }
        metrics = null
        mediaProjectionManager = null

    }


    /**
     * 开始屏幕录制
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun start() {
        screenRecordService!!.startRecording()
    }


    /**
     * tingzhi屏幕录制
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun stop() {
        screenRecordService!!.stopRecrding()
    }
}