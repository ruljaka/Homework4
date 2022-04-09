package com.ruslangrigoriev.homework4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.properties.Delegates

class ClockView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    companion object {
        const val HOUR_DEFAULT_COLOR = Color.BLACK
        const val MINUTE_DEFAULT_COLOR = Color.BLUE
        const val SECOND_DEFAULT_COLOR = Color.RED
        const val HOUR_DEFAULT_WIDTH = 20F
        const val MINUTE_DEFAULT_WIDTH = 15F
        const val SECOND_DEFAULT_WIDTH = 7F
    }

    private var size by Delegates.notNull<Int>()
    private var halfSize by Delegates.notNull<Float>()
    private var hour by Delegates.notNull<Float>()
    private var minute by Delegates.notNull<Float>()
    private var second by Delegates.notNull<Float>()

    private var hourColor by Delegates.notNull<Int>()
    private var minuteColor by Delegates.notNull<Int>()
    private var secondColor by Delegates.notNull<Int>()

    private var circleStrokeWidth by Delegates.notNull<Float>()
    private var scaleStrokeWidth by Delegates.notNull<Float>()
    private var hourStrokeWidth by Delegates.notNull<Float>()
    private var minuteStrokeWidth by Delegates.notNull<Float>()
    private var secondStrokeWidth by Delegates.notNull<Float>()

    private lateinit var circlePaint: Paint
    private lateinit var handPaint: Paint

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        R.style.DefaultClockViewStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.ClockViewStyle
    )

    constructor(context: Context) : this(context, null)

    init {
        if (attributeSet != null) {
            initAttributes(attributeSet, defStyleAttr, defStyleRes)
        }
    }

    private fun initAttributes(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.ClockView,
            defStyleAttr,
            defStyleRes
        )
        hourColor = typedArray.getColor(R.styleable.ClockView_hourColor, HOUR_DEFAULT_COLOR)
        minuteColor = typedArray.getColor(R.styleable.ClockView_minuteColor, MINUTE_DEFAULT_COLOR)
        secondColor = typedArray.getColor(R.styleable.ClockView_secondColor, SECOND_DEFAULT_COLOR)
        hourStrokeWidth = typedArray.getFloat(R.styleable.ClockView_hourWidth, HOUR_DEFAULT_WIDTH)
        minuteStrokeWidth =
            typedArray.getFloat(R.styleable.ClockView_minuteWidth, MINUTE_DEFAULT_WIDTH)
        secondStrokeWidth =
            typedArray.getFloat(R.styleable.ClockView_secondWidth, SECOND_DEFAULT_WIDTH)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        size = Math.min(widthSpecSize, heightSpecSize)
        halfSize = size / 2.toFloat()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas?) {
        initCircleStrokeWidth()
        initPaint()
        getCurrentTime()
        drawCircle(canvas)
        drawScale(canvas)
        drawHour(canvas)
        drawMinute(canvas)
        drawSecond(canvas)
        postInvalidateDelayed(1000)
    }

    private fun initCircleStrokeWidth() {
        val sizeInDp = size.div(resources.displayMetrics.density)
        circleStrokeWidth = sizeInDp / 20F
        scaleStrokeWidth = sizeInDp / 10F
//        hourStrokeWidth = sizeInDp.div(10F)
//        minuteStrokeWidth = sizeInDp.div(13F)
//        secondStrokeWidth = sizeInDp.div(20F)
    }

    private fun initPaint() {
        circlePaint = Paint()
        with(circlePaint) {
            color = Color.BLACK
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        handPaint = Paint()
        handPaint.isAntiAlias = true
    }

    private fun getCurrentTime() {
        val calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR).toFloat()
        minute = calendar.get(Calendar.MINUTE).toFloat()
        second = calendar.get(Calendar.SECOND).toFloat()
    }

    private fun drawCircle(canvas: Canvas?) {
        circlePaint.strokeWidth = circleStrokeWidth
        canvas?.drawCircle(
            halfSize,
            halfSize,
            halfSize - circleStrokeWidth / 2,
            circlePaint
        )
    }

    private fun drawScale(canvas: Canvas?) {
        canvas?.save()
        circlePaint.strokeWidth = scaleStrokeWidth
        for (i in 0..12) {
            canvas?.drawLine(
                halfSize,
                0f,
                halfSize,
                scaleStrokeWidth * 2,
                circlePaint
            )
            canvas?.rotate(
                360 / 12.toFloat(),
                halfSize,
                halfSize
            )
        }
        canvas?.restore()
    }

    private fun drawHour(canvas: Canvas?) {
        val mHour = hour + minute / 60
        val long = halfSize * 0.5
        val short = halfSize * 0.15
        val startX = (halfSize - short * sin(mHour * (Math.PI / 6))).toFloat()
        val startY = (halfSize + short * cos(mHour * (Math.PI / 6))).toFloat()
        val endX = (halfSize + long * sin(mHour * (Math.PI / 6))).toFloat()
        val endY = (halfSize - long * cos(mHour * (Math.PI / 6))).toFloat()
        handPaint.strokeWidth = hourStrokeWidth
        handPaint.color = hourColor
        canvas?.drawLine(startX, startY, endX, endY, handPaint)
    }

    private fun drawMinute(canvas: Canvas?) {
        val mMinute = minute + second / 60
        val long = halfSize * 0.7
        val short = halfSize * 0.2
        val startX = (halfSize - short * sin(mMinute * (Math.PI / 30))).toFloat()
        val startY = (halfSize + short * cos(mMinute * (Math.PI / 30))).toFloat()
        val endX = (halfSize + long * sin(mMinute * (Math.PI / 30))).toFloat()
        val endY = (halfSize - long * cos(mMinute * (Math.PI / 30))).toFloat()
        handPaint.strokeWidth = minuteStrokeWidth
        handPaint.color = minuteColor
        canvas?.drawLine(startX, startY, endX, endY, handPaint)
    }

    private fun drawSecond(canvas: Canvas?) {
        val long = halfSize * 0.8
        val short = halfSize * 0.25
        val startX = (halfSize - short * sin(second * (Math.PI / 30))).toFloat()
        val startY = (halfSize + short * cos(second * (Math.PI / 30))).toFloat()
        val endX = (halfSize + long * sin(second * (Math.PI / 30))).toFloat()
        val endY = (halfSize - long * cos(second * (Math.PI / 30))).toFloat()
        handPaint.strokeWidth = secondStrokeWidth
        handPaint.color = secondColor
        canvas?.drawLine(startX, startY, endX, endY, handPaint)
    }

}