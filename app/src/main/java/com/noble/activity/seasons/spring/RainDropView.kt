package com.noble.activity.seasons.spring

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.noble.activity.R
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class RainDropView @JvmOverloads constructor(context: Context, @Nullable attrs: AttributeSet? = null) :
    View(context, attrs) {

    private var mPaint: Paint? = null
    private var mPath: Path? = null

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    private var RAIN_COUNT = 70
    private val random = Random()

    private var rainDots = mutableListOf<RainDrop>()

    private var MAX_SPEED = 36
    private var MIN_SPEED = 20
    private var MAX_LENGTH = 40f
    private var MIN_LENGTH = 20f
    private var WATER_R = 3f
    private var MAX_ALPHA = 30 // Max 255

    private val DIFF_TIME = 16

    private var hasAlphaGrad = true
    private var runnable: Runnable? = null

    init {
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {

        if (attrs != null) {

            val ta = context.obtainStyledAttributes(attrs, R.styleable.RainDropView)
            RAIN_COUNT = ta.getInt(R.styleable.RainDropView_rv_dot_count, RAIN_COUNT)
            if (RAIN_COUNT < 0) {
                RAIN_COUNT = 1
            }

            MAX_SPEED = ta.getInt(R.styleable.RainDropView_rv_max_speed, MAX_SPEED)
            MIN_SPEED = ta.getInt(R.styleable.RainDropView_rv_min_speed, MIN_SPEED)
            MAX_LENGTH = ta.getDimensionPixelSize(R.styleable.RainDropView_rv_max_length, MAX_LENGTH.toInt()).toFloat()
            MIN_LENGTH = ta.getDimensionPixelSize(R.styleable.RainDropView_rv_min_length, MIN_LENGTH.toInt()).toFloat()
            WATER_R = ta.getDimensionPixelSize(R.styleable.RainDropView_rv_water_radius, WATER_R.toInt()).toFloat()

            var maxAlpha = ta.getFloat(R.styleable.RainDropView_rv_max_alpha, 0.12f)
            if (maxAlpha <= 0) {
                maxAlpha = 0.01f
            } else if (maxAlpha > 1) {
                maxAlpha = 1f
            }

            MAX_ALPHA = (maxAlpha * 255).toInt()
            hasAlphaGrad = ta.getBoolean(R.styleable.RainDropView_rv_alpha_gradient, true)
            ta.recycle()
        }

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.color = Color.WHITE
        mPaint!!.style = Paint.Style.FILL

        mPath = Path()

        runnable = Runnable {
            invalidate()
            postDelayed(runnable, DIFF_TIME.toLong())
        }
        postDelayed(runnable, DIFF_TIME.toLong())
    }

    override fun onVisibilityChanged(@NonNull changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        removeCallbacks(runnable)
        if (visibility == View.VISIBLE) {
            post(runnable)
        }

    }

    fun enableAlphaGradient(enable: Boolean) {
        hasAlphaGrad = enable
    }

    /**
     * @param alpha float: 0 ~ 1
     */
    fun setMaxAlpha(alpha: Float) {
        var alpha = alpha
        if (alpha <= 0) {
            alpha = 0.01f
        } else if (alpha > 1) {
            alpha = 1f
        }
        MAX_ALPHA = (255 * alpha).toInt()
    }

    /**
     * @param MAX_SPEED
     */
    fun setMaxSpeed(MAX_SPEED: Int) {
        this.MAX_SPEED = MAX_SPEED
    }

    /**
     * @param MIN_SPEED
     */
    fun setMinSpeed(MIN_SPEED: Int) {
        this.MIN_SPEED = MIN_SPEED
    }

    /**
     * @param MAX_LENGTH water max length
     */
    fun setMaxLength(MAX_LENGTH: Float) {
        this.MAX_LENGTH = MAX_LENGTH
    }

    /**
     * @param MIN_LENGTH water min length
     */
    fun setMinLength(MIN_LENGTH: Float) {
        this.MIN_LENGTH = MIN_LENGTH
    }

    fun setWaterRadius(WATER_R: Float) {
        this.WATER_R = WATER_R
    }

    fun setRainCount(RAIN_COUNT: Int) {
        this.RAIN_COUNT = if (RAIN_COUNT < 1) 1 else RAIN_COUNT
        initRainDots()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewWidth = width
        viewHeight = height
        initRainDots()
    }

    private fun initRainDots() {
        rainDots = mutableListOf()

        for (i in 0 until RAIN_COUNT) {
            rainDots.add(RainDrop(random.nextInt(viewWidth), random.nextInt(viewHeight),
                random.nextInt(MAX_SPEED), random.nextFloat()))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (viewWidth <= 0 || viewHeight <= 0) {
            return
        }
        for (i in 0 until RAIN_COUNT) {

            if (rainDots[i].x > viewWidth || rainDots[i].y > viewHeight) {
                rainDots[i].x = random.nextInt(viewWidth)
                rainDots[i].y = 0
            }

            rainDots[i].y += rainDots[i].speed + MIN_SPEED

            //draw dot
            mPath!!.reset()
            val scaleYDiff = MAX_LENGTH * rainDots[i].speed / MAX_SPEED
            val R = WATER_R
            val L = MIN_LENGTH + scaleYDiff
            mPath!!.addCircle(R, L, R, Path.Direction.CW)
            mPath!!.moveTo(R, 0f)
            val x =
                R + sqrt(R.toDouble().pow(2.0) - R.toDouble().pow(4.0) / L.toDouble().pow(2.0))
            val y = L - R.toDouble().pow(2.0) / L.toDouble()
            mPath!!.lineTo(x.toFloat(), y.toFloat())
            mPath!!.lineTo((2 * R - x).toFloat(), y.toFloat())
            mPath!!.close()
            mPath!!.offset(rainDots[i].x.toFloat(), rainDots[i].y.toFloat())

            if (hasAlphaGrad) {
                mPaint!!.alpha = (rainDots[i].alpha * MAX_ALPHA).toInt()
            } else {
                mPaint!!.alpha = MAX_ALPHA
            }

            canvas.drawPath(mPath!!, mPaint!!)
        }
    }
}