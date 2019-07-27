package com.noble.activity.utils

import android.content.Context

object UiUtils {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context 上下文
     * @param dpValue dp
     * @return px
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        return (dpValue * getDensity(context) + 0.5f).toInt()
    }

    /**
     * convert px to its equivalent sp
     *
     * 将px转换为sp
     */
    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.getResources().getDisplayMetrics().scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }


    /**
     * convert sp to its equivalent px
     *
     * 将sp转换为px
     */
    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.getResources().getDisplayMetrics().scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param context 上下文
     * @param pxValue px
     * @return dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        return (pxValue / getDensity(context) + 0.5f).toInt()
    }

    /**
     * 获取当前手机的屏幕像素密度
     *
     * @param context 上下文
     * @return 像素密度
     */
    fun getDensity(context: Context): Float {
        return context.getResources().getDisplayMetrics().density
    }
}