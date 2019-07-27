package com.noble.activity.seasons.winter

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.VisibleForTesting
import com.noble.activity.R

class SnowFlakeView : View {

    private var mAnimationDisabled = false
    private var mSnowFlakes = mutableListOf<SnowFlake>()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        val numFlakes = resources.getInteger(R.integer.flakeCount)
        mSnowFlakes = mutableListOf()
        for (i in 0 until numFlakes) {
            mSnowFlakes.add(SnowFlake(resources, height, width))
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mSnowFlakes.forEach { flake ->
            flake.setScreenDimensions(height, width)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mAnimationDisabled) {
            return
        }

        mSnowFlakes.forEach { flake ->
            flake.onDraw(canvas, height, width)
        }

        invalidate()
    }

    @VisibleForTesting
    fun disableAnimation() {
        mAnimationDisabled = true
    }
}