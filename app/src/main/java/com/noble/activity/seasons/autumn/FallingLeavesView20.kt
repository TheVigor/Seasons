package com.noble.activity.seasons.autumn

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.noble.activity.R

class FallingLeavesView20 : View {

    private var mLeaves = mutableListOf<Leaf>()

    private var mLeafBitmap: Bitmap? = null

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
        mLeafBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_leaf_3)

        val numLeaves = 10 //resources.getInteger(R.integer.flakeCount)
        mLeaves = mutableListOf()
        for (i in 0 until numLeaves) {
            mLeaves.add(Leaf(resources, height, width))
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mLeaves.forEach { flake ->
            flake.setScreenDimensions(height, width)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mLeaves.forEach { leaf ->
            leaf.onDraw(canvas, height, width, mLeafBitmap)
        }

        invalidate()
    }

}