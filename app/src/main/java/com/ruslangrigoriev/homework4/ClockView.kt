package com.ruslangrigoriev.homework4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
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
        const val SCAlE_DEFAULT_COLOR = Color.BLACK
        const val HOUR_DEFAULT_WIDTH = 20F
        const val MINUTE_DEFAULT_WIDTH = 15F
        const val SECOND_DEFAULT_WIDTH = 7F
        const val DEFAULT_SIZE = 100F
    }

    private var hour by Delegates.notNull<Float>()
    private var minute by Delegates.notNull<Float>()
    private var second by Delegates.notNull<Float>()

    private var hourColor by Delegates.notNull<Int>()
    private var minuteColor by Delegates.notNull<Int>()
    private var secondColor by Delegates.notNull<Int>()
    private var scaleColor by Delegates.notNull<Int>()

    private var scaleStrokeWidth by Delegates.notNull<Float>()
    private var hourStrokeWidth by Delegates.notNull<Float>()
    private var minuteStrokeWidth by Delegates.notNull<Float>()
    private var secondStrokeWidth by Delegates.notNull<Float>()

    private lateinit var scalePaint: Paint
    private lateinit var handPaint: Paint
    private var halfSize by Delegates.notNull<Float>()

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
        scaleColor = typedArray.getColor(R.styleable.ClockView_scaleColor, SCAlE_DEFAULT_COLOR)
        hourStrokeWidth = typedArray.getFloat(R.styleable.ClockView_hourWidth, HOUR_DEFAULT_WIDTH)
        minuteStrokeWidth = typedArray.getFloat(R.styleable.ClockView_minuteWidth, MINUTE_DEFAULT_WIDTH)
        secondStrokeWidth = typedArray.getFloat(R.styleable.ClockView_secondWidth, SECOND_DEFAULT_WIDTH)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        //установка размера по умолчания для wrap_content
        val size =
            if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_SIZE,
                    resources.displayMetrics
                ).toInt()
            } else {
                //установка одинаковой высоты и ширины по меньшему значению
                Math.min(widthSpecSize, heightSpecSize)
            }
        setMeasuredDimension(size, size)

        //установка зависимости толщины круга и делений от общего размера
        scaleStrokeWidth = size / resources.displayMetrics.density / 15F
        initPaint()
        halfSize = size / 2.toFloat()
    }

    private fun initPaint() {
        scalePaint = Paint()
        with(scalePaint) {
            color = scaleColor
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        handPaint = Paint()
        handPaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        getCurrentTime()
        drawCircle(canvas)
        drawScale(canvas)
        drawHour(canvas)
        drawMinute(canvas)
        drawSecond(canvas)
        postInvalidateDelayed(1000)
    }

    private fun getCurrentTime() {
        val calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR).toFloat()
        minute = calendar.get(Calendar.MINUTE).toFloat()
        second = calendar.get(Calendar.SECOND).toFloat()
    }

    private fun drawCircle(canvas: Canvas?) {
        scalePaint.strokeWidth = scaleStrokeWidth
        canvas?.drawCircle(
            halfSize,
            halfSize,
            halfSize - scaleStrokeWidth / 2,
            scalePaint
        )
    }

    private fun drawScale(canvas: Canvas?) {
        canvas?.save()
        scalePaint.strokeWidth = scaleStrokeWidth
        for (i in 0..12) {
            canvas?.drawLine(halfSize, 0f, halfSize, scaleStrokeWidth * 3, scalePaint)
            canvas?.rotate(360 / 12.toFloat(), halfSize, halfSize)
        }
        canvas?.restore()
    }

    private fun drawHour(canvas: Canvas?) {
        val mHour = hour + minute / 60 /* уточнение для плавного хода стрелки */
        val longRadius = halfSize * 0.5F
        val shortRadius = halfSize * 0.15F
        val angle = (Math.PI / 6).toFloat() /* шаг часовой стрелки 30° */
        handPaint.strokeWidth = hourStrokeWidth
        handPaint.color = hourColor
        drawHand(longRadius, shortRadius, angle, mHour, canvas)
    }

    private fun drawMinute(canvas: Canvas?) {
        val mMinute = minute + second / 60 /* уточнение для плавного хода стрелки */
        val longRadius = halfSize * 0.7F
        val shortRadius = halfSize * 0.2F
        val angle = (Math.PI / 30).toFloat() /* шаг минутной стрелки 6° */
        handPaint.strokeWidth = minuteStrokeWidth
        handPaint.color = minuteColor
        drawHand(longRadius, shortRadius, angle, mMinute, canvas)
    }

    private fun drawSecond(canvas: Canvas?) {
        val longRadius = halfSize * 0.8F
        val shortRadius = halfSize * 0.25F
        val angle = (Math.PI / 30).toFloat() /* шаг минутной стрелки 6° */
        handPaint.strokeWidth = secondStrokeWidth
        handPaint.color = secondColor
        drawHand(longRadius, shortRadius, angle, second, canvas)
    }

    private fun drawHand(
        longRadius: Float,
        shortRadius: Float,
        angle: Float,
        time: Float,
        canvas: Canvas?
    ) {
        val startX = (halfSize - shortRadius * sin(time * angle))
        val startY = (halfSize + shortRadius * cos(time * angle))
        val endX = (halfSize + longRadius * sin(time * angle))
        val endY = (halfSize - longRadius * cos(time * angle))
        canvas?.drawLine(startX, startY, endX, endY, handPaint)
    }
}