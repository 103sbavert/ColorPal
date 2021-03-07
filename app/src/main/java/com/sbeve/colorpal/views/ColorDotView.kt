package com.sbeve.colorpal.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.sbeve.colorpal.R

class ColorDotView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var fillColor = Color.LTGRAY
        set(value) {
            field = value
            paintFill.color = value
            invalidate()
        }

    private val paintFill = Paint().apply {
        isAntiAlias = true
        color = fillColor
        style = Paint.Style.FILL
    }

    private var cx: Float = width / 2F
    private var cy: Float = height / 2F
    private val radius: Float
        //get the bigger one of the two
        get() = if (cx > cy) cy else cx

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorDotView, 0, 0)
        fillColor = a.getColor(R.styleable.ColorDotView_paletteColor, fillColor)
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cx = w / 2F
        cy = h / 2F
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(cx, cy, radius, paintFill)
    }
}
