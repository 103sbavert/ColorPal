package com.sbeve.colorpal.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.sbeve.colorpal.R
import kotlin.properties.Delegates

class ColorDotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var fillColor = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }
    private var outLineColor = Color.BLACK

    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)

    private var cx by Delegates.notNull<Float>()
    private var cy by Delegates.notNull<Float>()
    private val radius: Float
        //get the bigger one of the two
        get() = if (cx > cy) cy else cx

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorDotView, defStyleAttr, 0)
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
        outLineColor = typedValue.data
        fillColor = a.getColor(R.styleable.ColorDotView_paletteColor, fillColor)
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cx = w / 2F
        cy = h / 2F
    }

    override fun onDraw(canvas: Canvas) {
        paintFill.style = Paint.Style.FILL
        paintFill.color = fillColor
        canvas.drawCircle(cx, cy, radius, paintFill)
        paintFill.style = Paint.Style.STROKE
        paintFill.color = outLineColor
        paintFill.strokeWidth = 2F
        canvas.drawCircle(cx, cy, radius - 2, paintFill)
    }
}
