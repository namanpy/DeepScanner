package com.minip.scanner.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.FillType
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class SelectView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var x1 = 20F
    var y1 = 20F

    var x2 = 120F
    var y2 = 20F

    var x3 = 20F
    var y3 = 120F

    var x4 = 120F
    var y4 = 120F

    var viewWidth = 0F
    var viewHeight = 0F

    var pointRadius = 35F
    val paint = Paint()

    val borderpadding = 30F

    var imageindex : Int = 0

    var isCircle1Pressed = false;
    var isCircle2Pressed = false;
    var isCircle3Pressed = false;
    var isCircle4Pressed = false;

    lateinit var onSelectCoordinatesListener : OnSelectCoordinatesListener

    fun setCurrentImageIndex(imageindex: Int) {
        this.imageindex = imageindex
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        if (canvas != null) {


            paint.setStyle(Paint.Style.FILL)
            paint.setColor(Color.CYAN)
            canvas.drawCircle(x1, y1, pointRadius, paint)
            canvas.drawCircle(x2, y2, pointRadius, paint)
            canvas.drawCircle(x3, y3, pointRadius, paint)
            canvas.drawCircle(x4, y4, pointRadius, paint)
            paint.alpha = 60;

            var path = Path()
            path.reset()
            path.setFillType(FillType.EVEN_ODD)
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x4, y4)
            path.lineTo(x3, y3)
            path.close()
            canvas.drawPath(path, paint);

            paint.alpha = 100;
            paint.strokeWidth = pointRadius/2;

            canvas.drawLine(x1,y1,x2,y2, paint)
            canvas.drawLine(x2,y2,x4,y4, paint)
            canvas.drawLine(x4,y4,x3,y3, paint)
            canvas.drawLine(x3,y3,x1,y1, paint)

        }


    }
    fun isTouching( x : Float, y : Float, x1 : Float, y1 : Float): Boolean {

        return ( Math.pow((x - x1).toDouble(), 2.0)   + Math.pow((y - y1).toDouble(), 2.0)  ) < (pointRadius * pointRadius)

    }
    fun setCoordinates( x1 : Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.x4 = x4;
        this.y4 = y4;
        invalidate()
    }
    override fun onTouchEvent(event: MotionEvent?) : Boolean {
        super.onTouchEvent(event)

        if(event != null ) {
            var x = event.x
            var y = event.y


            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(isTouching(x,y,x1,y1)) {
                        isCircle1Pressed = true;
                    }

                    if(isTouching(x,y,x2,y2)) {
                        isCircle2Pressed = true;
                    }

                    if(isTouching(x,y,x3,y3)) {
                        isCircle3Pressed = true;
                    }

                    if(isTouching(x,y,x4,y4)) {
                        isCircle4Pressed = true;
                    }

                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    if(isCircle1Pressed) {
                        x1 = Math.max(0f, Math.min(x, viewWidth - borderpadding))
                        y1 = Math.max(0f, Math.min(y, viewHeight - borderpadding))
                    }
                    if(isCircle2Pressed) {
                        x2 = Math.max(0f, Math.min(x, viewWidth - borderpadding))
                        y2 = Math.max(0f, Math.min(y, viewHeight - borderpadding))
                    }
                    if(isCircle3Pressed) {
                        x3 = Math.max(0f, Math.min(x, viewWidth - borderpadding))
                        y3 = Math.max(0f, Math.min(y, viewHeight - borderpadding))
                    }
                    if(isCircle4Pressed) {
                        x4 = Math.max(0f, Math.min(x, viewWidth - borderpadding))
                        y4 = Math.max(0f, Math.min(y, viewHeight - borderpadding))
                    }

                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    isCircle1Pressed = false;
                    isCircle2Pressed = false;
                    isCircle3Pressed = false;
                    isCircle4Pressed = false;

                    if(onSelectCoordinatesListener != null) {

                        onSelectCoordinatesListener.onSelectCoordinates(imageindex, x1, y1, x2, y2, x3, y3, x4, y4)

                    }

                    invalidate()
                }
            }

        }
    return true
    }

    fun _setOnSelectCoordinatesListener(onSelectCoordinatesListener : OnSelectCoordinatesListener) {
        this.onSelectCoordinatesListener = onSelectCoordinatesListener
    }

    override fun onSizeChanged(xNew: Int, yNew: Int, xOld: Int, yOld: Int) {
        super.onSizeChanged(xNew, yNew, xOld, yOld)
        viewWidth = xNew.toFloat()
        viewHeight = yNew.toFloat()
    }
    interface OnSelectCoordinatesListener {
        fun onSelectCoordinates( imageindex : Int, x1 : Float, y1 : Float, x2 : Float, y2: Float, x3 : Float, y3 : Float, x4 : Float, y4 : Float    )
    }
}