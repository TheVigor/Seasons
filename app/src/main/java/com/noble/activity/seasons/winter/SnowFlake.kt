package com.noble.activity.seasons.winter

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.noble.activity.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SnowFlake(resources: Resources, height: Int, width: Int) {

    companion object {
        private const val VERTICAL_MULTIPLIER = 3 // Fall this much faster down than across
        private var mPaint: Paint? = null
        private var MAX_SIZE: Float = 0f

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

    private var mX: Float = 0f
    private var mY: Float = 0f

    private var mRadius: Float = 0f
    private var mFlakeVelocity: Float = 0f
    private var mAngle: Float = 0f

    init {
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
        mX = newX(width) // Randomly position along x-axis
        mY = newY(height, mRadius, false)
    }

    fun onDraw(canvas: Canvas, height: Int, width: Int) {
        mX += (mFlakeVelocity * cos(mAngle.toDouble())).toFloat()

        // Make the snow vertically fall at VERTICAL_MULTIPLIER times the increment
        mY += (mFlakeVelocity.toDouble() * VERTICAL_MULTIPLIER.toDouble() * abs(sin(mAngle.toDouble()))).toFloat()
        mAngle += mAngleDelta

        // If the flake went off screen, bring it back at the top.
        if (mY > height + mRadius * 2 || mX < -mRadius * 2 || mX > width + mRadius * 2) {
            mAngle = newAngle()
            mX = newX(width)
            mY = newY(
                height,
                mRadius,
                true
            ) // Move to the top of the screen, just out of view
        }

        canvas.drawCircle(mX, mY, mRadius, mPaint!!)
    }

}