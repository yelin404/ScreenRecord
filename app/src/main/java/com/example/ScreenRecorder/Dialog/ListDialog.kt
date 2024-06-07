package com.example.ScreenRecorder.Dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager

class ListDialog: Dialog {

    constructor(mContext: Context): super(mContext)
    constructor(mContext: Context,themeResId: Int): super(mContext,themeResId)
    constructor(mContext: Context,cancelable: Boolean,cancelListener: DialogInterface.OnCancelListener): super(mContext,cancelable,cancelListener)



    class Builder(private var context: Context) {

        private var resID: Int = 0
        private var themeResId: Int = 0
        private var cancel: Boolean = false
        private var cancelable: Boolean = false
        private var gravity: Int = Gravity.CENTER
        private var width: Double = 0.8
        private var height: Double = 1.0
        private var contentView: View? = null

        fun setContentView(resID: Int): Builder {
            this.resID = resID
            return this
        }

        fun getContentView(): View? {
            return contentView
        }

        fun setDialogTheme(themeResId: Int): Builder {
            this.themeResId = themeResId
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setCanceledOnTouchOutside(cancel: Boolean): Builder {
            this.cancel = cancel
            return this
        }

        fun setGravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }

        fun setWidth(width: Double): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Double): Builder {
            this.height = height
            return this
        }

        fun create(): ListDialog {
            var listDialog = ListDialog(context)
            listDialog.setCancelable(cancelable)
            listDialog.setCanceledOnTouchOutside(cancel)
            contentView = LayoutInflater.from(context).inflate(resID,null)
            listDialog.addContentView(contentView!!,
                ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))

            setWindowConfig(context,listDialog,gravity,width,height)
            return listDialog

        }

        fun setWindowConfig(context: Context,
                            listDialog: ListDialog,
                            gravity: Int,
                            width: Double,
                            height: Double) {
            var window: Window? = listDialog.window
            var params: WindowManager.LayoutParams? = window?.attributes
            val dm = context.resources.displayMetrics
            params?.gravity = gravity
            params?.width = (dm.widthPixels * width).toInt()
            params?.height = (dm.heightPixels * height).toInt()
            window?.attributes = params

        }



    }
}