package com.example.ScreenRecorder.Dialog

import android.content.Context
import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.databinding.ViewAppListBinding

class DialogManager {

    /**
     * 这个接口的目的是给设置recyclerview留一个入口，例如需要设置adapter，设置recyclerview的item的点击事件
     * 这些东西都需要在一个地方处理，但同时不同的recyclerview需要的配置也不近相同，因此设置一个接口，将选择权交给
     * 使用者，让他在需要的地方设置adapter即可，这里已经还有点 MVP架构的思维了
     */
    interface OnAppListListener {
        fun setRecyclerView(recyclerView: RecyclerView)
    }

    companion object {

        fun showAppListDialog(context: Context,listener: OnAppListListener) {
            var builder: ListDialog.Builder = ListDialog.Builder(context)
            var listDialog = builder.setContentView(R.layout.view_app_list)
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .setGravity(Gravity.CENTER_VERTICAL)
                .setWidth(0.8)
                .setHeight(0.8)
                .create()

            listDialog.show()

            var contentView = builder.getContentView()

            var binding: ViewAppListBinding = ViewAppListBinding.bind(contentView!!)

            listener!!.setRecyclerView(binding.appList)


        }
    }


}