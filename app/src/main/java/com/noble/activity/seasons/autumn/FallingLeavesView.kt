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

        private val TAG = "FallingLeavesView"
        private val LOADING_COMPLETED = "100%"

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

    /**
     * 设置进度（自动刷新）
     * @param progress 0-100
     */
    //loading 100% 特效
    // 255 不透明
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

    /**
     * 设置叶子数目（自动刷新）
     * @param num 大于等于0的整数
     */
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

    /**
     * 设置每片叶子一个漂浮周期时间
     * @param time 默认值为 1500
     */
    var leafFloatTime: Long
        get() = mLeafFloatTime
        set(time) {
            if (time <= 0) {
                return
            }
            mLeafFloatTime = time
            postInvalidate()
        }

    /**
     * 设置每片叶子的旋转周期时间
     * @param time 默认值为 2000
     */
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

    /**
     * 根据测量模式获取相应的值
     * @param min 最小值(-1为不限)
     * @param max 最大值（-1为不限）
     * @param wrap wrap_content 时的取值
     * @param measureSpec 测量模式
     * @return 计算后得到的长度
     */
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
        canvas.drawRoundRect(
            0f,
            0f,
            mWidth.toFloat(),
            mHeight.toFloat(),
            mHeight / 2f,
            mHeight / 2f,
            mBgPaint!!
        )
        //叶子
        drawLeaves(canvas)
        //外圈（为了遮住超出背景的叶子）
        drawStrokeOval(canvas)
        // 进度条
        drawProgress(canvas, mProgress)
        //扇子外圈
        canvas.drawCircle(
            mFanCx.toFloat(),
            mFanCy.toFloat(),
            (mHeight / 2).toFloat(),
            mFanStrokePaint!!
        )
        //扇子内圈
        canvas.drawCircle(
            mFanCx.toFloat(),
            mFanCy.toFloat(),
            ((mHeight - mProgressMargin) / 2).toFloat(),
            mProgressPaint!!
        )
        if (isLoadingCompleted) {
            //绘制加载完成特效
            drawCompleted(canvas)
        } else {
            //绘制扇叶
            drawFan(canvas, mFanLen, mBitmapPaint)
        }
        //刷新
        postInvalidate()
    }

    /**
     * 画白色外圈
     */
    private fun drawStrokeOval(canvas: Canvas) {
        canvas.save()
        val path = Path()
        path.setFillType(Path.FillType.EVEN_ODD)
        path.addRoundRect(
            0f,
            0f,
            mWidth.toFloat(),
            mHeight.toFloat(),
            mHeight / 2f,
            mHeight / 2f,
            Path.Direction.CW
        )
        path.addRoundRect(
            mProgressMargin.toFloat(),
            mProgressMargin.toFloat(),
            (mWidth - mProgressMargin).toFloat(),
            (mHeight - mProgressMargin).toFloat(),
            (mYellowOvalHeight / 2).toFloat(),
            (mYellowOvalHeight / 2).toFloat(),
            Path.Direction.CW
        )
        canvas.clipPath(path)
        canvas.drawRoundRect(
            0f,
            0f,
            mWidth.toFloat(),
            mHeight.toFloat(),
            mHeight / 2f,
            mHeight / 2f,
            mBgPaint!!
        )
        canvas.restore()
    }

    /**
     * 画叶子
     */
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

    /**
     * 获取叶子的（x,y）位置
     * @param leaf 叶子
     * @param currentTime 当前时间
     */
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

    /**
     * 获取叶子的Y轴坐标
     * @param leaf 叶子
     * @return 经过计算的叶子Y轴坐标
     */
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

    /**
     * 绘制加载完成特效
     */
    private fun drawCompleted(canvas: Canvas) {
        // 每次绘制风扇透明度递减10
        var alpha = mCompletedFanPaint!!.getAlpha() - 10
        if (alpha <= 0) {
            alpha = 0
        }
        mCompletedFanPaint!!.setAlpha(alpha)
        // 文字透明度刚好与风扇相反
        mCompletedTextPaint!!.setAlpha(255 - alpha)
        // 计算透明因子
        val fraction = alpha / 255f
        // 叶片大小 和 文字大小 也是相反变化的
        val fanLen = fraction * mFanLen
        val textSize = (1 - fraction) * mCompletedTextSize
        mCompletedTextPaint!!.setTextSize(textSize)
        drawFan(canvas, fanLen.toInt(), mCompletedFanPaint)
        //测量文字占用空间
        val bounds = Rect()
        mCompletedTextPaint!!.getTextBounds(
            LOADING_COMPLETED,
            0,
            LOADING_COMPLETED.length,
            bounds
        )
        //画文字
        canvas.drawText(
            LOADING_COMPLETED,
            0,
            LOADING_COMPLETED.length,
            mFanCx - bounds.width() / 2f,
            mFanCy + bounds.height() / 2f,
            mCompletedTextPaint!!
        )
    }

    /**
     * 右边转动的扇子
     */
    private fun drawFan(canvas: Canvas, fanLen: Int, paint: Paint?) {
        canvas.save()
        val matrix = Matrix()
        //缩放到合适大小
        val scaleX = fanLen * 2f / mFanBitmapWidth
        val scaleY = fanLen * 2f / mFanBitmapHeight
        matrix.postScale(scaleX, scaleY)
        //平移
        matrix.postTranslate((mFanCx - fanLen).toFloat(), (mFanCy - fanLen).toFloat())
        //旋转
        mFanRotationAngle = (mFanRotationAngle + mFanRotateSpeed) % 360
        matrix.postRotate((-mFanRotationAngle).toFloat(), mFanCx.toFloat(), mFanCy.toFloat())
        canvas.drawBitmap(mFanBitmap!!, matrix, paint)
        canvas.restore()
    }

    /**
     * 绘制进度
     * @param progress 0-100
     */
    private fun drawProgress(canvas: Canvas, progress: Int) {
        //圆的半径
        val r = mYellowOvalHeight / 2
        //水平长度（已减去两个半圆）
        val len = mWidth - mHeight
        val circleProgress = 100f * r / (r + len)//左边半圆满时所对应的进度
        val rectProgress = 100f - circleProgress//单单中间矩形满所对应的进度

        if (progress < circleProgress) {
            //半圆内进度
            canvas.drawArc(
                mProgressMargin.toFloat(),
                (mHeight / 2 - r).toFloat(),
                (2 * r + mProgressMargin).toFloat(),
                (mHeight / 2 + r).toFloat(),
                (2 - progress / circleProgress) * 90,
                180 * progress / circleProgress,
                false,
                mProgressPaint!!
            )
        } else {
            canvas.drawArc(
                mProgressMargin.toFloat(),
                (mHeight / 2 - r).toFloat(),
                (2 * r + mProgressMargin).toFloat(),
                (mHeight / 2 + r).toFloat(),
                90f,
                180f,
                false,
                mProgressPaint!!
            )
            canvas.drawRect(
                (mProgressMargin + r).toFloat(),
                (mHeight / 2 - r).toFloat(),
                r + (progress - circleProgress) / rectProgress * len,
                (mHeight / 2 + r).toFloat(),
                mProgressPaint!!
            )
        }
    }

    /**
     * 叶子图片
     * @param resId 图片资源ID
     */
    fun setLeafSrc(@DrawableRes resId: Int) {
        mLeafBitmap = (getResources().getDrawable(resId) as BitmapDrawable).bitmap
        postInvalidate()
    }

    /**
     * 风扇图片
     * @param resId 图片资源ID
     */
    fun setFanSrc(@DrawableRes resId: Int) {
        mFanBitmap = (getResources().getDrawable(resId) as BitmapDrawable).bitmap
        postInvalidate()
    }

    /**
     * 设置进度条颜色
     */
    fun setProgressColor(@ColorInt color: Int) {
        mProgressColorId = color
        mProgressPaint!!.setColor(mProgressColorId)
        postInvalidate()
    }

    /**
     * 设置背景颜色
     */
    fun setBgColor(@ColorInt color: Int) {
        mBgColorId = color
        mBgPaint!!.setColor(mBgColorId)
        postInvalidate()
    }

    /**
     * 设置风扇描边颜色
     */
    fun setFanStroke(@ColorInt color: Int) {
        mFanStrokeColorId = color
        mFanStrokePaint!!.setColor(mFanStrokeColorId)
        postInvalidate()
    }

    /**
     * 设置风扇旋转速度
     * @param speed 默认值为 5
     */
    fun setFanRotateSpeed(speed: Int) {
        var speed = speed
        if (speed < 0) {
            speed = 0
        }
        mFanRotateSpeed = speed
        postInvalidate()
    }

    /**
     * 叶子飘动的振幅
     */
    private enum class AmplitudeType {
        LITTLE, MIDDLE, BIG
    }

    /**
     * 旋转方向
     */
    private enum class RotateDir {
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