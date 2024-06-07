package com.example.ScreenRecorder.Adapter

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.ScreenRecorder.AppClicked
import com.example.ScreenRecorder.Bean.AppInfo
import com.example.ScreenRecorder.MainActivity
import com.example.ScreenRecorder.MediaProjectionHelper
import com.example.ScreenRecorder.Utils.startApp
import com.example.ScreenRecorder.Utils.tip
import com.example.demo.R


class AppAdapter(
    var mContext:Context,
    var appList: MutableList<AppInfo>,
): RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onClick(v: View?)
    }



    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var appName: TextView
        var packageName: TextView

        init {
            imageView = itemView.findViewById(R.id.item_app_icon)
            appName = itemView.findViewById(R.id.item_app_name)
            packageName = itemView.findViewById(R.id.item_package_name)
        }

    }



    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_app,parent,false)

        var viewHolder = ViewHolder(view)

        viewHolder.itemView.setOnClickListener(View.OnClickListener {
            Toast.makeText(mContext,"你点击的是${viewHolder.appName.text}",Toast.LENGTH_SHORT).show()
            //启动录制
            if (mContext is MainActivity) {

                //把点击的Item信息传递进来,主要是要应用的包名和名字
                AppClicked.packageName =viewHolder.packageName.text.toString()
                AppClicked.name = viewHolder.appName.text.toString()

                //点击启动对应的APP
                startApp(mContext,viewHolder.packageName.text.toString())

                if (!isAppAlive(AppClicked.packageName!!)) {
                    Log.e("---------------->",viewHolder.packageName.text.toString())
                    if (MediaProjectionHelper.getService() != null ) {

                        //甩掉广告
                        val handler = Handler()
                        handler.postDelayed({
                            //延迟1秒执行
                            (mContext as MainActivity).startRecording()
                        }, 1000)
                    } else {
                        tip(mContext,"没有开启服务，故无法进行录屏！")
                    }

                }

            }
        })
        return viewHolder

    }


    private fun isAppAlive(packageName:String): Boolean {

        var usageStatsManager = mContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager.isAppInactive(packageName)

    }


    override fun getItemCount(): Int {
        return appList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.appName.text = appList[position].appName
        holder.packageName.text = appList[position].packageName
        holder.imageView.setImageDrawable(appList[position].icon)


    }
}