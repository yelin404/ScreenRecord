package com.example.ScreenRecorder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ScreenRecorder.Utils.getCurrentTime
import com.example.ScreenRecorder.Utils.getSaveDirectoryByMediaStore
import com.example.ScreenRecorder.Utils.getsaveDirectoryByFile
import com.example.demo.R


class ScreenRecordService : Service() {

    //管理类
    private var mediaProjectionManager: MediaProjectionManager? = null

    //录屏工具
    private  var mediaProjection: MediaProjection? = null

    //录像机--其实是记录功能
    private  var mediaRecorder: MediaRecorder? = null

    //用于录屏的虚拟屏幕
    private  var virtualDisplay: VirtualDisplay? = null

    //屏幕宽
    private var width: Int = 0

    //屏幕高
    private var height: Int = 0

    //设置像素
    private var dpi = 0

    //判断是否在录屏
    private var isRecording: Boolean = false

    //视频存储路径
    private var videoPath: String = ""


    inner class ScreenRecordBinder: Binder() {
        public fun getService(): ScreenRecordService {
            return this@ScreenRecordService
        }
    }


    override fun onBind(intent: Intent): IBinder {
        return ScreenRecordBinder()
    }


    companion object {
        /**
         * 绑定service
         */
        fun bindService(context: Context,serviceConnection: ServiceConnection) {
            var intent = Intent(context,ScreenRecordService::class.java)
            context.bindService(intent,serviceConnection,Service.BIND_AUTO_CREATE)
        }


        /**
         * 解绑服务
         */
        fun unbindService(context: Context,serviceConnection: ServiceConnection) {
            context.unbindService(serviceConnection)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager  = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            var channel = NotificationChannel("123","Screen record",NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        //创建builder
        var builder = NotificationCompat.Builder(this,"123")
            .setContentTitle("这个是标题")
            .setContentText("这个是内容")
            .setSmallIcon(R.drawable.nature)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.nature))

        //获取构建好的通知
        var notification = builder.build()



        startForeground(1,notification)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }


    fun setMediaManager(manager: MediaProjectionManager) {
        mediaProjectionManager = manager
    }


    fun setMediaProjection(projection: MediaProjection) {
        mediaProjection = projection
    }


    fun setConfig(width: Int,height: Int,dpi: Int) {
        this@ScreenRecordService.width = width
        this@ScreenRecordService.height = height
        this@ScreenRecordService.dpi = dpi
    }


    //判断是否运行
    fun isRunning(): Boolean = isRecording


    @RequiresApi(Build.VERSION_CODES.S)
    private fun initRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.apply {
            //设置音频来源
            setAudioSource(MediaRecorder.AudioSource.MIC)
            //设置视频来源，其实就是让你选择录屏
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            //设置视频格式
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            //如果版本大于等于安卓10，视频存储路径就用Mediastore，反之用file API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setOutputFile(getSaveDirectoryByMediaStore(this@ScreenRecordService))
            } else {
                //设置视频存储地址，命名规则为  保存文件夹 + 时间戳 + .mp4
                videoPath = getsaveDirectoryByFile(this@ScreenRecordService) +  getCurrentTime() + ".mp4"
                setOutputFile(videoPath)
            }

            //设置视频大下
            setVideoSize(width, height)
            //设置视频编码
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            //设置音频编码
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            //设置视频码率
            setVideoEncodingBitRate(3 * 1024 *1024)
            //设置帧数
            setVideoFrameRate(30)

            try {
                mediaRecorder!!.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ScreenRecordService,
                    "Recorder录像机prepare失败，无法使用，请重新初始化！",
                    Toast.LENGTH_SHORT).show();
            }

        }
    }


    private fun createVirtualDisplay() {



        //虚拟屏幕通过MediaProjection获取，传入一系列传过来的参数
        try {
            if (virtualDisplay == null) {
                virtualDisplay = mediaProjection!!.createVirtualDisplay("VirtualScreen",width,
                    height,dpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mediaRecorder!!.surface,
                    null,null)
            } else {
                virtualDisplay!!.surface = mediaRecorder!!.surface
            }


        }catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this,"virtualDisplay创建录屏异常，请退出重试！",Toast.LENGTH_SHORT).show();
        }
    }




    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(): Boolean {

        //先判断录屏工具是否存在且是否在录屏
        if (mediaProjection == null || isRecording) {
            return false
        }

        //配置mediaRecorder
        initRecorder()

        //获取屏幕参数用以创建虚拟屏幕
        createVirtualDisplay()

        try {
            mediaRecorder!!.start()
            isRecording = true
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this,"开启失败，没有开始录屏",Toast.LENGTH_SHORT).show();
            isRecording = false
            return false
        }

    }


    /**
     * 停止录屏
     */
    fun stopRecrding() {

        isRecording = false

        //停止mediaRecorder
        if (mediaRecorder != null) {
            mediaRecorder!!.stop()
            mediaRecorder!!.reset()
            mediaRecorder!!.release()
        }

        mediaRecorder = null

        //停止virtualDisplay
        if (virtualDisplay != null) {
            virtualDisplay!!.release()
        }

        virtualDisplay = null

    }

    fun destroy() {

        stopRecrding()

        if (mediaProjection != null) {
            mediaProjection!!.stop()
            mediaProjection = null
        }

        if (mediaProjectionManager != null) {
            mediaProjectionManager = null
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }


    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }




}