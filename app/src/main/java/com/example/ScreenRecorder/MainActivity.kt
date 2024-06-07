package com.example.ScreenRecorder

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ScreenRecorder.Adapter.AppAdapter
import com.example.ScreenRecorder.Bean.AppInfo
import com.example.ScreenRecorder.Dialog.DialogManager
import com.example.ScreenRecorder.Utils.*
import com.example.demo.R
import com.example.demo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnClickListener {





    var tag: String = "123"
    private lateinit var binding: ActivityMainBinding


    //这里定义需要动态请求的权限，注意：这些权限必须现在Manifest里面申请
    private final val  PERMISSIONS_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.QUERY_ALL_PACKAGES)

    //请求码，用于在onRequestPermissionsResult函数中区分是谁发送的请求
    private final val PERMISSION_REQUEST_CODE = 1

    //请求码，用于startActivityForResult中
    private final val REQUEST_CODE = 2024

    private var screenRecordService: ScreenRecordService? = null
    private  var  mediaProjection: MediaProjection? = null
    private  var mediaProjectionManager: MediaProjectionManager? = null

    private  var metrics: DisplayMetrics? = null

    private lateinit var recyclerView: RecyclerView

    private  var serviceConnection: ServiceConnection? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //检查权限
        com.example.ScreenRecorder.Utils.checkPermission(this,PERMISSIONS_STORAGE,PERMISSION_REQUEST_CODE)


        //设置按钮点击事件
        binding.btnStartService.setOnClickListener(this)
        binding.btnStopService.setOnClickListener(this)
        binding.btnStartRecorder.setOnClickListener(this)
        binding.btnStopRecorder.setOnClickListener(this)
        binding.btnDeviceList.setOnClickListener(this)

//        val registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result : ActivityResult ->
//            if (result.resultCode == RESULT_OK) {
//                tip(this,"hello world")
//            }
//
//        }
//
//        var intent = Intent(this,ScreenRecordService::class.java)
//
//        registerLauncher.launch(intent)
//


    }


    //所有权限申请后需要调用这个函数!!!
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //需要先行判断请求码是否相同，不然不知道是不是哪里发出来的请求码
        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (isAllGranted(grantResults)) {

                Log.e(tag,"12233556666666")
                Toast.makeText(this,"权限申请通过！！！",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"请同意申请的所有权限，否则应用无法正常使用！！！",Toast.LENGTH_SHORT).show()
            }
        }
    }


    //返回方法，获取service返回信息
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //先判断请求码是不是一致的
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            MediaProjectionHelper.setMediProjection(resultCode,data)
            NotificationHelper.getInstance().notify(this,"1","喜羊羊",NotificationManager.IMPORTANCE_HIGH)

        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording() {


        //正式开始录屏
        MediaProjectionHelper.start()


    }


    @RequiresApi(Build.VERSION_CODES.S)
    fun stopRecording() {
        MediaProjectionHelper.stop()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_start_recorder -> {

                if (MediaProjectionHelper.getService() == null ) {
                    tip(this,"请先开启服务！")
                    return
                }

                //开始录制
                if (MediaProjectionHelper.isRecording()) {
                    Toast.makeText(this@MainActivity,"当前正在录屏，请不要重复点击哦！",Toast.LENGTH_SHORT).show();
                } else {
                    tip(this,"开始录屏!")
                    startRecording()
                }

            }

            R.id.btn_stop_recorder -> {

                if (MediaProjectionHelper.getService() == null ) {
                    tip(this,"请先开启服务！")
                    return
                }

                if (!MediaProjectionHelper.isRecording()) {
                    //没有在录屏，无法停止，弹出提示
                    Toast.makeText(this@MainActivity,"您还没有录屏，无法停止，请先开始录屏吧！",Toast.LENGTH_SHORT).show();
                } else if (MediaProjectionHelper.isRecording()) {
                    //正在录屏，可以点击停止，停止录屏
                    tip(this,"结束录屏！")
                    stopRecording()
                }

            }
            R.id.btn_deviceList -> {
                //这里获取所有已安装的非系统APP
                var applist: MutableList<AppInfo>? = getAllAppInfo(this)
                DialogManager.showAppListDialog(this,object : DialogManager.OnAppListListener{
                    override fun setRecyclerView(recyclerView: RecyclerView) {
                        //绑定recyclerview
                        initAppList(recyclerView,applist)
                    }
                })

            }
            R.id.btn_start_service -> {
                //开始录制前先进性检查权限
                var array = arrayOf<String>(Manifest.permission.RECORD_AUDIO)
                com.example.ScreenRecorder.Utils.checkPermission(this,array,PERMISSION_REQUEST_CODE)

                if (!canUsageStats(this)) {
                    var intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    tip(this,"请授权！")
                    startActivity(intent)
                    return
                }

                tip(this,"服务已经启动！")


                MediaProjectionHelper.startService(this)



            }
            R.id.btn_stop_service -> {
                tip(this,"服务已经终止!")
                MediaProjectionHelper.stopService(this)

            }
        }
    }

    fun initAppList(recyclerView: RecyclerView,list: MutableList<AppInfo>?) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        var adapter = AppAdapter(this,list!!)
        recyclerView.adapter = adapter
    }


    override fun onDestroy() {
        MediaProjectionHelper.stopService(this)
        super.onDestroy()
    }

}