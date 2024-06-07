package com.example.ScreenRecorder.Utils

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process


fun isAllGranted(grantResults: IntArray): Boolean {
    /**
     * 这里需要判断onRequestPermissionsResult函数里这个grantResults数组所代表的permission
     * 是否全被授权，原理就是被授权后会返还
     * PackageManager.PERMISSION_GRANTED，类型为Int，值 = 0，如果被拒绝，则返还
     * PackageManager.PERMISSION_DENIED，类型为Int，值 = -1
     * 因此，如果请求许可被全部授权，则返还的grantResults值之和 = 0
     */
    if (grantResults.size > 0) {
        var sum: Int = 0
        for (grantResutl in grantResults) {
            sum += grantResutl
        }

        return  if(sum == 0) true else false
    } else return false
}

fun checkPermission(context: Context,array: Array<String>,requestCode: Int) {

    //检测需要申请权限的数组，只要有一个权限没有申请，就整个权限数组重新申请一次
    /**
     * 这里存在优化空间
     */
    for (permission in array) {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (context is Activity) {
                context.requestPermissions(array,requestCode)
                (context as Activity)
                break
            }
        }
    }

}

//这个函数是专门用来判断<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"/>这个权限的
//里面牵涉了很多安卓版本，所以能不动就不动
fun canUsageStats(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    var mode = 0
    mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return if (mode == AppOpsManager.MODE_DEFAULT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
    } else {
        mode == AppOpsManager.MODE_ALLOWED
    }
}
