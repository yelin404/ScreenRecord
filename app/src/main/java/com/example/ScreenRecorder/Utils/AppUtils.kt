package com.example.ScreenRecorder.Utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ScreenRecorder.Bean.AppInfo
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileDescriptor

fun getAllAppInfo(context: Context): MutableList<AppInfo>? {
    var appList: MutableList<AppInfo> = ArrayList()
    val packageManager = context.packageManager
    var packageList: List<ApplicationInfo> = packageManager.getInstalledApplications(0)
    var bean: AppInfo
    for (item in packageList) {
        if ((item.flags and ApplicationInfo.FLAG_SYSTEM) <= 0) {
            var name = packageManager.getApplicationLabel(item).toString()
            bean = AppInfo(item.packageName,item.loadIcon(packageManager),name)
            appList!!.add(bean)
        }
    }

    return appList
}


fun tip(context: Context,message: String) {
    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
}


//成功启动APP返回 true，失败则返回 false
fun startApp(context: Context,packageName: String ){
    var intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent == null) {
        tip(context,"未找到已选择的已安装的APP！")
        return
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(intent!!)
}

fun createFile(context: Context) {
    var uri = MediaStore.Files.getContentUri("external")

    var contentValue = ContentValues()

    contentValue.put(MediaStore.Downloads.RELATIVE_PATH,"${Environment.DIRECTORY_DOWNLOADS}/hello")
    contentValue.put(MediaStore.Downloads.DISPLAY_NAME,"hello.txt")
    contentValue.put(MediaStore.Downloads.TITLE,"hello")

    var contentResolver =context.contentResolver

    var insert = contentResolver.insert(uri,contentValue)

    var os = contentResolver.openOutputStream(insert!!)
    var bos = BufferedOutputStream(os)
    bos.write("hello world".toByteArray())
    bos.close()
}

//获取存储文件夹的位置，注意，是文件夹，不是文件！！！
@RequiresApi(Build.VERSION_CODES.O)
fun getsaveDirectoryByFile(context: Context): String? {
    /**
     * 这里的条件判断可能有问题
     */
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        var rootDir: String =Environment.getExternalStorageDirectory().absolutePath + "/录屏文件/"
        //Environment.getExternalStorageDirectory().absolutePath + "/测试目录/"
        Log.e("-------------------------------------->>  ",rootDir)
        //"sdcard/录屏文件/"

        Log.i("TAG", "getsaveDirectory: "+rootDir)

        var file: File = File(rootDir)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(context,"录制视频存储文件夹创建失败！！！",Toast.LENGTH_SHORT).show()
                return null
            }

        }
        return rootDir
    } else {
        return null
    }



}

@RequiresApi(Build.VERSION_CODES.O)
fun getSaveDirectoryByMediaStore(context: Context): FileDescriptor {
    var contentResolver = context.contentResolver
    var contentValues = ContentValues()
    var name = getCurrentTime()
    contentValues.apply {
        put(MediaStore.Video.Media.DISPLAY_NAME,name )
        put(MediaStore.Video.Media.RELATIVE_PATH,Environment.DIRECTORY_MOVIES)
        put(MediaStore.Video.Media.MIME_TYPE,"video/mp4")
    }

    var uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,contentValues)
    var file = contentResolver.openFileDescriptor(uri!!,"w")

    return file!!.fileDescriptor

}
