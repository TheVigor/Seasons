package com.noble.activity.seasons.autumn

import android.content.res.Resources
import android.graphics.*
import com.noble.activity.R
import com.noble.activity.seasons.winter.SnowFlake
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Leaf(resources: Resources, height: Int, width: Int) {

    private val mRandom = Random()
    private var mAddTime: Long = 0

    private var mLeafFloatTime = DEFAULT_LEAF_FLOAT_TIME
    private var mLeafRotateTime = DEFAULT_LEAF_ROTATE_TIME

    private var mProgressLen: Int = 200
    private var mLeafLen: Int = 50


    private var mHeight: Int = height

    var x: Float = 0.toFloat()
    var y: Float = 0.toFloat()
    var type: FallingLeavesView.AmplitudeType? = null
    var rotateAngle: Int = 0//旋转角度
    var rotateDir: FallingLeavesView.RotateDir? = null
    var startTime: Long = 0
    var n: Int = 0

    companion object {
        private const val VERTICAL_MULTIPLIER = 3 // Fall this much faster down than across
        private var mPaint: Paint? = null
        private var MAX_SIZE: Float = 0f

        private val DEFAULT_LEAF_FLOAT_TIME: Long = 1500       //叶子飘动一个周期花费的时间
        private val DEFAULT_LEAF_ROTATE_TIME: Long = 2000      //叶子旋转一个周期花费的时间

        // Initial falling angle [0, 2π)
        private fun newAngle(): Float {
            return (Math.random() * 2.0 * Math.PI).toFloat()
        }

        // Randomly position along x-axis
        private fun newX(width: Int): Float {
            return width * Math.random().toFloat()
        }

        // Place Y position. Deduct diameter to have it off screen.
        private fun newY(height: Int, radius: Float, atTop: Boolean): Float {
            return if (atTop) -radius * 2 else height * Math.random().toFloat() - radius * 2
        }
    }

    private val mAngleDelta = (Math.random() / 100).toFloat() // [0, 0.01)


    private var mRadius: Float = 0f
    private var mFlakeVelocity: Float = 0f
    private var mAngle: Float = 0f

    init {

        val randomType = mRandom.nextInt(3)
        when (randomType) {
            0 -> type = FallingLeavesView.AmplitudeType.LITTLE
            1 -> type = FallingLeavesView.AmplitudeType.MIDDLE
            else -> type = FallingLeavesView.AmplitudeType.BIG
        }
        //随机旋转方向
        val dir = mRandom.nextInt(2)
        when (dir) {
            0 -> rotateDir = FallingLeavesView.RotateDir.ANTICLOCKWISE
            else -> rotateDir = FallingLeavesView.RotateDir.CLOCKWISE
        }
        //随机起始角度
        rotateAngle = mRandom.nextInt(360)
        n = mRandom.nextInt(20)
        mAddTime += mRandom.nextInt(DEFAULT_LEAF_FLOAT_TIME.toInt())
        startTime = System.currentTimeMillis() + mAddTime





        // MAX_SIZE is the fraction of the shorter edge of the screen to use for a single flake
        if (MAX_SIZE == 0f) {
            MAX_SIZE = resources.getString(R.string.maxSize).toFloat()
        }
        if (mPaint == null) {
            mPaint = Paint()
            mPaint!!.isAntiAlias = true
            mPaint!!.color = Color.WHITE
            mPaint!!.style = Paint.Style.FILL_AND_STROKE
        }
        setScreenDimensions(height, width)
    }

    fun setScreenDimensions(height: Int, width: Int) {
        // Choose the smaller of height/width for radius calculations
        val shortEdge = min(width, height)
        mRadius = shortEdge.toFloat() * Math.random().toFloat() * MAX_SIZE

        // Individual flake velocity is related to, but not tied to actual flake radius.
        mFlakeVelocity = shortEdge.toFloat() * Math.random().toFloat() * MAX_SIZE / VERTICAL_MULTIPLIER

        mAngle = newAngle()
        x = newX(width) // Randomly position along x-axis
        y = 0f //newY(height, mRadius, false)
    }

    fun onDraw(
        canvas: Canvas,
        height: Int,
        width: Int,
        mLeafBitmap: Bitmap?
    ) {
        val currentTime = System.currentTimeMillis()

        // 获取 leaf 当前的坐标
        getLeafLocation(this, currentTime)
        canvas.save()
        val matrix = Matrix()
        // 缩放 自适应 View 的大小
        val scaleX = 0.2f //mLeafLen.toFloat() / mLeafBitmapWidth
        val scaleY = 0.2f //mLeafLen.toFloat() / mLeafBitmapHeight
        matrix.postScale(scaleX, scaleY)

        // 位移
        val transX = x
        val transY = y
        matrix.postTranslate(transX, transY)
        // 旋转
        // 计算旋转因子

        val rotateFraction = (currentTime - startTime) % mLeafRotateTime / mLeafRotateTime.toFloat()
        val rotate: Float
        when (rotateDir) {
            FallingLeavesView.RotateDir.CLOCKWISE ->
                //顺时针
                rotate = rotateFraction * 360 + rotateAngle
            else ->
                //逆时针
                rotate = -rotateFraction * 360 + rotateAngle
        }

        matrix.postRotate(rotate, transX, transY)
        canvas.drawBitmap(mLeafBitmap!!, matrix, mPaint)
        canvas.restore()

        // If the flake went off screen, bring it back at the top.
        if (x > height + mRadius * 2 || x < -mRadius * 2 || x > width + mRadius * 2) {
            mAngle = Leaf.newAngle()
            x = Leaf.newX(width)
            y = Leaf.newY(
                height,
                mRadius,
                true
            ) // Move to the top of the screen, just out of view
        }


    }


    private fun getLeafLocation(leaf: Leaf, currentTime: Long) {
        val intervalTime = currentTime - leaf.startTime
        if (intervalTime <= 0) {
            return
        } else if (intervalTime > mLeafFloatTime) {
            leaf.startTime = currentTime + Random().nextInt(mLeafFloatTime.toInt())
        }
        val fraction = intervalTime.toFloat() / mLeafFloatTime
        leaf.x += (mFlakeVelocity * cos(mAngle.toDouble())).toFloat() //25 //(1 - fraction) * mProgressLen
        leaf.y += (mFlakeVelocity.toDouble() * VERTICAL_MULTIPLIER.toDouble() * abs(sin(mAngle.toDouble()))).toFloat() //25 //getLeafLocationY(leaf)
        mAngle += mAngleDelta


//        if (leaf.x <= mYellowOvalHeight / 4) {
//            //叶子飘到最左边，有可能会超出边界，所以提前特殊处理
//            leaf.startTime = currentTime + Random().nextInt(mLeafFloatTime.toInt())
//            leaf.x = mProgressLen.toFloat()
//            leaf.y = getLeafLocationY(leaf)
//        }
    }

    private fun getLeafLocationY(leaf: Leaf): Float {
        val w = (Math.PI * 2 / mProgressLen).toFloat()//角频率
        val A: Float//计算振幅值
        when (leaf.type) {
            FallingLeavesView.AmplitudeType.LITTLE -> A = (mLeafLen / 3).toFloat()
            FallingLeavesView.AmplitudeType.MIDDLE -> A = (mLeafLen * 2 / 3).toFloat()
            else -> A = mLeafLen.toFloat()
        }
        // (mHeight-mLeafLen)/2 是为了让 Leaf 的Y轴起始位置居中
        return (A * Math.sin((w * leaf.x + leaf.n).toDouble()) + (mHeight - mLeafLen) / 2).toFloat()
    }





}