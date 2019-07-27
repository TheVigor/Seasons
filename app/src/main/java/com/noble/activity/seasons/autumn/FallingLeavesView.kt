package com.noble.activity.seasons.autumn

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes

import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import com.noble.activity.R
import com.noble.activity.utils.UiUtils
import java.util.*
import kotlin.collections.ArrayList


class FallingLeavesView : View {

    companion object {

        private val DEFAULT_LEAF_FLOAT_TIME: Long = 1500       //叶子飘动一个周期花费的时间
        private val DEFAULT_LEAF_ROTATE_TIME: Long = 2000      //叶子旋转一个周期花费的时间
    }

    private var mHeight: Int = 0
    private var mWidth: Int = 0

    private var mDefaultHeight: Int = 0
    private var mMinWidth: Int = 0
    private var mMinHeight: Int = 0
    private var mMaxHeight: Int = 0

    private var mYellowOvalHeight: Int = 0//进度条高
    private var mProgressMargin: Int = 0//进度与边界的Margin
    private var mFanRotationAngle = 0//叶子每次旋转的角度
    private var mProgressLen: Int = 0//px，进度长度，用于计算叶子飘动轨迹

    private var mFanLen: Int = 0//单个扇叶长度
    private var mFanCx: Int = 0//风扇坐标
    private var mFanCy: Int = 0
    private var mFanBitmapWidth: Int = 0//FanBitmap宽和长
    private var mFanBitmapHeight: Int = 0

    private var mLeafBitmapWidth: Int = 0//LeafBitmap宽和长
    private var mLeafBitmapHeight: Int = 0
    private var mLeafLen: Int = 0//叶片边长（叶子须是正方形）

    private var mProgressPaint: Paint? = null
    private var mBgPaint: Paint? = null

    private var mFanStrokePaint: Paint? = null
    private var mBitmapPaint: Paint? = null
    private var mLeafBitmap: Bitmap? = null
    private var mFanBitmap: Bitmap? = null
    private var mCompletedFanPaint: Paint? = null//绘制100%时的扇叶
    private var mCompletedTextPaint: Paint? = null//绘制100%时的文字
    private var mContext: Context? = null

    private var isLoadingCompleted = false//是否加载完成
    private var mProgress = 0//进度值
    private var mFanRotateSpeed = 5//每次扇子旋转的偏移量
    private var mLeafNum = 8//叶子数（默认为8）
    private var mCompletedTextSize: Float = 0.toFloat()
    @ColorInt
    private var mBgColorId = Color.WHITE
    @ColorInt
    private var mProgressColorId = getResources().getColor(R.color.orange)
    @ColorInt
    private var mFanStrokeColorId = Color.WHITE//扇子描边颜色
    private var mLeafFloatTime = DEFAULT_LEAF_FLOAT_TIME
    private var mLeafRotateTime = DEFAULT_LEAF_ROTATE_TIME
    private val mLeafList = mutableListOf<Leaf>()
    private val mLeafFactory = LeafFactory()

    var progress: Int
        get() = mProgress
        set(progress) {
            if (progress < 0) {
                mProgress = 0
            } else if (progress > 100) {
                mProgress = 100
            } else {
                mProgress = progress
            }
            if (progress == 100) {
                isLoadingCompleted = true
            } else {
                isLoadingCompleted = false
            }
            mCompletedFanPaint!!.setAlpha(255)
            postInvalidate()
        }


    var leafNum: Int
        get() = mLeafNum
        set(num) {
            var num = num
            if (num < 0) {
                num = 0
            }
            mLeafList.clear()
            mLeafList.addAll(mLeafFactory.generateLeafs(num))
            postInvalidate()
        }

    var leafFloatTime: Long
        get() = mLeafFloatTime
        set(time) {
            if (time <= 0) {
                return
            }
            mLeafFloatTime = time
            postInvalidate()
        }


    var leafRotateTime: Long
        get() = mLeafRotateTime
        set(time) {
            if (time <= 0) {
                return
            }
            mLeafRotateTime = time
            postInvalidate()
        }

    constructor(context: Context) : super(context) {
        initData(context)
        initPaint()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.LeavesLoading)
            mProgress = ta.getInteger(R.styleable.LeavesLoading_progress, 0)
            if (mProgress > 100) {
                mProgress = 100
            } else if (mProgress < 0) {
                mProgress = 0
            }
            if (mProgress == 100) {
                isLoadingCompleted = true
            }
            mLeafFloatTime =
                ta.getInteger(R.styleable.LeavesLoading_leafFloatSpeed, DEFAULT_LEAF_FLOAT_TIME.toInt()).toLong()
            if (mLeafFloatTime <= 0) {
                mLeafFloatTime = DEFAULT_LEAF_FLOAT_TIME
            }
            mLeafRotateTime =
                ta.getInteger(R.styleable.LeavesLoading_leafRotateSpeed, DEFAULT_LEAF_ROTATE_TIME.toInt()).toLong()
            if (mLeafRotateTime < 0) {
                mLeafRotateTime = DEFAULT_LEAF_ROTATE_TIME
            }
            mFanRotateSpeed = ta.getInteger(R.styleable.LeavesLoading_fanRotateSpeed, 5)
            if (mFanRotateSpeed < 0) {
                mFanRotateSpeed = 5
            }
            val leaf = ta.getDrawable(R.styleable.LeavesLoading_leafSrc)
            if (leaf != null) {
                mLeafBitmap = (leaf as BitmapDrawable).bitmap
            }
            val fan = ta.getDrawable(R.styleable.LeavesLoading_fanSrc)
            if (fan != null) {
                mFanBitmap = (fan as BitmapDrawable).bitmap
            }
            mLeafNum = ta.getInteger(R.styleable.LeavesLoading_leafNum, 8)
            if (mLeafNum < 0) {
                mLeafNum = 0
            }
            mBgColorId = ta.getColor(
                R.styleable.LeavesLoading_bgColor,
                getResources().getColor(R.color.white)
            )
            mProgressColorId = ta.getColor(
                R.styleable.LeavesLoading_progressColor,
                getResources().getColor(R.color.orange)
            )
            mFanStrokeColorId = ta.getColor(
                R.styleable.LeavesLoading_fanStrokeColor,
                Color.WHITE
            )
            ta.recycle()
        }
        initData(context)
        initPaint()
    }

    private fun initData(context: Context) {
        mMinHeight = UiUtils.dip2px(context, 40f)
        mMinWidth = UiUtils.dip2px(context, 200f)
        mMaxHeight = UiUtils.dip2px(context, 100f)
        mDefaultHeight = UiUtils.dip2px(context, 60f)
        mContext = context
        mLeafList.addAll(mLeafFactory.generateLeafs(mLeafNum))
    }

    private fun initPaint() {
        mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mProgressPaint!!.setColor(mProgressColorId)

        mBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBgPaint!!.setColor(mBgColorId)

        mFanStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mFanStrokePaint!!.setColor(mFanStrokeColorId)

        if (mLeafBitmap == null) {
            mLeafBitmap = BitmapFactory.decodeResource(mContext!!.getResources(), R.drawable.iv_leaf_3)
        }
        mLeafBitmapHeight = mLeafBitmap!!.height
        mLeafBitmapWidth = mLeafBitmap!!.width

        if (mFanBitmap == null) {
            mFanBitmap = BitmapFactory.decodeResource(mContext!!.getResources(), R.drawable.iv_fan_3)
        }
        mFanBitmapWidth = mFanBitmap!!.width
        mFanBitmapHeight = mFanBitmap!!.height

        mBitmapPaint = Paint()
        mBitmapPaint!!.setAntiAlias(true)
        mBitmapPaint!!.setDither(true)
        mBitmapPaint!!.setFilterBitmap(true)

        mCompletedFanPaint = Paint()
        mCompletedFanPaint!!.setAntiAlias(true)
        mCompletedFanPaint!!.setDither(true)
        mCompletedFanPaint!!.setFilterBitmap(true)
        mCompletedFanPaint!!.setAlpha(255)

        mCompletedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCompletedTextPaint!!.setStyle(Paint.Style.STROKE)
        mCompletedTextPaint!!.setColor(Color.WHITE)
        mCompletedTextPaint!!.setAlpha(0)
        mCompletedTextPaint!!.setFakeBoldText(true)
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getSize(mMinWidth, -1, mMinWidth, widthMeasureSpec),
            getSize(mMinHeight, mMaxHeight, mDefaultHeight, heightMeasureSpec)
        )
    }

    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = h
        mWidth = w
        mProgressMargin = mHeight / 8
        mYellowOvalHeight = mHeight - mProgressMargin * 2
        mFanCx = mWidth - mHeight / 2
        mFanCy = mHeight / 2
        mFanLen = (mHeight - mProgressMargin * 2) / 2
        mLeafLen = (mHeight * 0.3f).toInt()
        mProgressLen = mWidth - mHeight + mYellowOvalHeight / 2
        if (mHeight <= UiUtils.dip2px(mContext!!, 50f)) {
            mCompletedTextSize = UiUtils.sp2px(mContext!!, 13f).toFloat()
        } else if (mHeight < UiUtils.dip2px(mContext!!, 75f)) {
            mCompletedTextSize = UiUtils.sp2px(mContext!!, 16f).toFloat()
        } else {
            mCompletedTextSize = UiUtils.sp2px(mContext!!, 18f).toFloat()
        }
        mCompletedTextPaint!!.setTextSize(mCompletedTextSize)
    }

    private fun getSize(min: Int, max: Int, wrap: Int, measureSpec: Int): Int {
        var result = min
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        when (specMode) {
            MeasureSpec.UNSPECIFIED ->
                //某个值
                result = specSize
            MeasureSpec.AT_MOST ->
                // wrap_content
                result = wrap
            MeasureSpec.EXACTLY ->
                // match_parent
                result = specSize
        }
        if (max != -1) {
            result = Math.min(max, result)
        }
        if (min != -1) {
            result = Math.max(min, result)
        }
        return result
    }


    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //背景
        mBgPaint!!.setStyle(Paint.Style.FILL)

        drawLeaves(canvas)
        postInvalidate()
    }


    private fun drawLeaves(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        for (leaf in mLeafList) {
            if (currentTime > leaf.startTime && leaf.startTime != 0L) {
                // 获取 leaf 当前的坐标
                getLeafLocation(leaf, currentTime)
                canvas.save()
                val matrix = Matrix()
                // 缩放 自适应 View 的大小
                val scaleX = mLeafLen.toFloat() / mLeafBitmapWidth
                val scaleY = mLeafLen.toFloat() / mLeafBitmapHeight
                matrix.postScale(scaleX, scaleY)
                // 位移
                val transX = leaf.x
                val transY = leaf.y
                matrix.postTranslate(transX, transY)
                // 旋转
                // 计算旋转因子
                val rotateFraction = (currentTime - leaf.startTime) % mLeafRotateTime / mLeafRotateTime.toFloat()
                val rotate: Float
                when (leaf.rotateDir) {
                    FallingLeavesView.RotateDir.CLOCKWISE ->
                        //顺时针
                        rotate = rotateFraction * 360 + leaf.rotateAngle
                    else ->
                        //逆时针
                        rotate = -rotateFraction * 360 + leaf.rotateAngle
                }
                // 旋转中心选择 Leaf 的中心坐标
                matrix.postRotate(rotate, transX + mLeafLen / 2, transY + mLeafLen / 2)
                canvas.drawBitmap(mLeafBitmap!!, matrix, mBitmapPaint)
                canvas.restore()
            }
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
        leaf.x = (1 - fraction) * mProgressLen
        leaf.y = getLeafLocationY(leaf)

        if (leaf.x <= mYellowOvalHeight / 4) {
            //叶子飘到最左边，有可能会超出边界，所以提前特殊处理
            leaf.startTime = currentTime + Random().nextInt(mLeafFloatTime.toInt())
            leaf.x = mProgressLen.toFloat()
            leaf.y = getLeafLocationY(leaf)
        }
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


    fun setLeafSrc(@DrawableRes resId: Int) {
        mLeafBitmap = (getResources().getDrawable(resId) as BitmapDrawable).bitmap
        postInvalidate()
    }


    fun setFanSrc(@DrawableRes resId: Int) {
        mFanBitmap = (getResources().getDrawable(resId) as BitmapDrawable).bitmap
        postInvalidate()
    }


    fun setProgressColor(@ColorInt color: Int) {
        mProgressColorId = color
        mProgressPaint!!.setColor(mProgressColorId)
        postInvalidate()
    }


    fun setBgColor(@ColorInt color: Int) {
        mBgColorId = color
        mBgPaint!!.setColor(mBgColorId)
        postInvalidate()
    }


    fun setFanStroke(@ColorInt color: Int) {
        mFanStrokeColorId = color
        mFanStrokePaint!!.setColor(mFanStrokeColorId)
        postInvalidate()
    }

    fun setFanRotateSpeed(speed: Int) {
        var speed = speed
        if (speed < 0) {
            speed = 0
        }
        mFanRotateSpeed = speed
        postInvalidate()
    }


    enum class AmplitudeType {
        LITTLE, MIDDLE, BIG
    }


    enum class RotateDir {
        CLOCKWISE, //顺时针
        ANTICLOCKWISE//逆时针
    }

    private inner class Leaf {
        internal var x: Float = 0.toFloat()
        internal var y: Float = 0.toFloat()//坐标
        internal var type: AmplitudeType? = null//叶子飘动振幅
        internal var rotateAngle: Int = 0//旋转角度
        internal var rotateDir: RotateDir? = null//旋转方向
        internal var startTime: Long = 0//起始时间
        internal var n: Int = 0//初始相位
    }

    private inner class LeafFactory {
        private val mRandom = Random()
        private var mAddTime: Long = 0

        internal fun generateLeaf(): Leaf {
            val leaf = Leaf()
            //随机振幅
            val randomType = mRandom.nextInt(3)
            when (randomType) {
                0 -> leaf.type = AmplitudeType.LITTLE
                1 -> leaf.type = AmplitudeType.MIDDLE
                else -> leaf.type = AmplitudeType.BIG
            }
            //随机旋转方向
            val dir = mRandom.nextInt(2)
            when (dir) {
                0 -> leaf.rotateDir = RotateDir.ANTICLOCKWISE
                else -> leaf.rotateDir = RotateDir.CLOCKWISE
            }
            //随机起始角度
            leaf.rotateAngle = mRandom.nextInt(360)
            leaf.n = mRandom.nextInt(20)
            mAddTime += mRandom.nextInt(mLeafFloatTime.toInt())
            leaf.startTime = System.currentTimeMillis() + mAddTime
            return leaf
        }

        internal fun generateLeafs(num: Int): List<Leaf> {
            val leaves = mutableListOf<Leaf>()
            for (i in 0 until num) {
                leaves.add(generateLeaf())
            }
            return leaves
        }
    }

}