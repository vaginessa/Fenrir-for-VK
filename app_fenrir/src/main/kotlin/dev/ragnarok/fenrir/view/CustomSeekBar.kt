package dev.ragnarok.fenrir.view

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.settings.CurrentTheme.getColorPrimary
import dev.ragnarok.fenrir.settings.CurrentTheme.getColorSecondary
import dev.ragnarok.fenrir.util.Utils
import kotlin.math.ceil

class CustomSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    interface CustomSeekBarListener {
        fun onSeekBarDrag(position: Long)
        fun onSeekBarMoving(position: Long)
    }

    private var thumbX = 0
    private var draggingThumbX = 0
    private var thumbDX = 0
    private var isDragging = false
    private var delegate: CustomSeekBarListener? = null
    private var lineColor: Int
    private var cacheColor: Int
    private var circleColor: Int
    private var progressColor: Int
    private var pressedCircleColor: Int
    private var noCircle: Boolean
    private val rect = RectF()
    private var lineHeight: Float
    private var bufferedProgress = 0f
    private var currentRadius: Float = Utils.dp(6f).toFloat()
    private var lastUpdateTime: Long = 0
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var thumbWidth: Int = Utils.dp(24f)
    private var duration: Long = -1

    private var layoutWidth: Int = 0
    private var layoutHeight: Int = 0
    private var pendingPosition: Long = -1

    fun setCustomSeekBarListener(seekBarListener: CustomSeekBarListener?) {
        delegate = seekBarListener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return onTouch(event.action, event.x, event.y)
    }

    private fun onTouch(action: Int, x: Float, y: Float): Boolean {
        if (duration <= 0) {
            return false
        }
        if (action == MotionEvent.ACTION_DOWN) {
            val additionWidth = (layoutHeight - thumbWidth) / 2
            if (x >= -additionWidth && x <= layoutWidth + additionWidth && y >= 0 && y <= layoutHeight) {
                if (!(thumbX - additionWidth <= x && x <= thumbX + thumbWidth + additionWidth)) {
                    thumbX = x.toInt() - thumbWidth / 2
                    if (thumbX < 0) {
                        thumbX = 0
                    } else if (thumbX > layoutWidth - thumbWidth) {
                        thumbX = layoutWidth - thumbWidth
                    }
                }
                isDragging = true
                draggingThumbX = thumbX
                thumbDX = (x - thumbX).toInt()
                invalidate()
                return true
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (isDragging) {
                parent?.requestDisallowInterceptTouchEvent(false)
                thumbX = draggingThumbX
                if (action == MotionEvent.ACTION_UP && delegate != null) {
                    delegate?.onSeekBarDrag((duration * (thumbX.toDouble() / (layoutWidth - thumbWidth).toDouble())).toLong())
                }
                isDragging = false
                invalidate()
                return true
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (isDragging) {
                parent?.requestDisallowInterceptTouchEvent(true)
                draggingThumbX = (x - thumbDX).toInt()
                if (draggingThumbX < 0) {
                    draggingThumbX = 0
                } else if (draggingThumbX > layoutWidth - thumbWidth) {
                    draggingThumbX = layoutWidth - thumbWidth
                }
                delegate?.onSeekBarMoving((duration * draggingThumbX.toDouble() / (layoutWidth - thumbWidth).toDouble()).toLong())
                invalidate()
                return true
            }
        }
        return false
    }

    fun setDuration(value: Long) {
        duration = value
        invalidate()
    }

    private fun setBufferedPosition(value: Long) {
        if (duration <= 0) {
            return
        }
        bufferedProgress = (value.toDouble() / duration).toFloat()
        invalidate()
    }

    fun updateFullState(duration: Long, pos: Long, buffered: Long) {
        var inv = false
        if (this.duration != duration) {
            this.duration = duration
            inv = true
        }
        val tmp = (buffered.toDouble() / duration).toFloat()
        if (bufferedProgress != tmp) {
            bufferedProgress = tmp
            inv = true
        }
        position = pos
        if (inv) {
            invalidate()
        }
    }

    var position: Long
        get() = if (duration == -1L) -1L else (duration * thumbX.toDouble() / (layoutWidth - thumbWidth).toDouble()).toLong()
        set(value) {
            if (duration <= 0) {
                thumbX = 0
                if (!isDragging) {
                    invalidate()
                }
                return
            }
            pendingPosition = value
            thumbX = ceil(((layoutWidth - thumbWidth) * (value.toDouble() / duration))).toInt()
            if (thumbX < 0) {
                thumbX = 0
            } else if (thumbX > layoutWidth - thumbWidth) {
                thumbX = layoutWidth - thumbWidth
            }
            if (!isDragging) {
                invalidate()
            }
        }

    private fun clamp(value: Float, min: Float, max: Float): Float {
        if (value > max) {
            return max
        } else if (value < min) {
            return min
        }
        return value
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val halfLayoutHeight = layoutHeight / 2
        val halfLineHeight = lineHeight / 2
        val halfThumbWidth = thumbWidth / 2
        if (pendingPosition >= 0 && thumbX <= 0 && layoutWidth > 0) {
            thumbX =
                ceil(((layoutWidth - thumbWidth) * (pendingPosition.toDouble() / duration))).toInt()
            if (thumbX < 0) {
                thumbX = 0
            } else if (thumbX > layoutWidth - thumbWidth) {
                thumbX = layoutWidth - thumbWidth
            }
            pendingPosition = -1
        }
        rect[halfThumbWidth.toFloat(), (halfLayoutHeight - halfLineHeight), (layoutWidth - halfThumbWidth).toFloat()] =
            (halfLayoutHeight + halfLineHeight)
        paint.color = lineColor
        canvas.drawRoundRect(rect, halfThumbWidth.toFloat(), halfThumbWidth.toFloat(), paint)
        if (bufferedProgress > 0 && duration > 0) {
            paint.color = cacheColor
            rect[halfThumbWidth.toFloat(), (halfLayoutHeight - halfLineHeight), halfThumbWidth + bufferedProgress * (layoutWidth - thumbWidth)] =
                (halfLayoutHeight + halfLineHeight)
            canvas.drawRoundRect(
                rect,
                halfThumbWidth.toFloat(),
                halfThumbWidth.toFloat(),
                paint
            )
        }
        rect[halfThumbWidth.toFloat(), (halfLayoutHeight - halfLineHeight), (halfThumbWidth + if (isDragging) draggingThumbX else thumbX).toFloat()] =
            (halfLayoutHeight + halfLineHeight)
        paint.color = progressColor
        canvas.drawRoundRect(rect, halfThumbWidth.toFloat(), halfThumbWidth.toFloat(), paint)

        if (duration <= 0) {
            return
        }
        val newRad = Utils.dp(if (isDragging) 8f else 6f)
        if (currentRadius != newRad.toFloat()) {
            val tmpColor = ArgbEvaluator().evaluate(
                clamp((currentRadius - Utils.dp(6f)) / 2, 0f, 1f),
                circleColor,
                pressedCircleColor
            ) as Int
            paint.color = tmpColor

            val newUpdateTime = SystemClock.elapsedRealtime()
            var dt = newUpdateTime - lastUpdateTime
            if (dt > 18) {
                dt = 16
            }
            lastUpdateTime = newUpdateTime
            if (currentRadius < newRad) {
                currentRadius += Utils.dp(1f) * (dt / 60.0f)
                if (currentRadius > newRad) {
                    currentRadius = newRad.toFloat()
                }
            } else {
                currentRadius -= Utils.dp(1f) * (dt / 60.0f)
                if (currentRadius < newRad) {
                    currentRadius = newRad.toFloat()
                }
            }
            paint.color = Color.argb(
                140,
                Color.red(tmpColor),
                Color.green(tmpColor),
                Color.blue(tmpColor)
            )
            canvas.drawCircle(
                ((if (isDragging) draggingThumbX else thumbX) + halfThumbWidth).toFloat(),
                halfLayoutHeight.toFloat(),
                currentRadius + Utils.dp(4f),
                paint
            )
            invalidate()
        } else {
            paint.color = if (isDragging) pressedCircleColor else circleColor
        }

        if (!noCircle) {
            canvas.drawCircle(
                ((if (isDragging) draggingThumbX else thumbX) + halfThumbWidth).toFloat(),
                halfLayoutHeight.toFloat(),
                currentRadius,
                paint
            )
        }
        if (isDragging) {
            if (noCircle) {
                canvas.drawCircle(
                    (draggingThumbX + halfThumbWidth).toFloat(),
                    halfLayoutHeight.toFloat(),
                    currentRadius,
                    paint
                )
            }
            paint.color = Color.argb(
                140,
                Color.red(pressedCircleColor),
                Color.green(pressedCircleColor),
                Color.blue(pressedCircleColor)
            )
            canvas.drawCircle(
                (draggingThumbX + halfThumbWidth).toFloat(),
                halfLayoutHeight.toFloat(),
                currentRadius + Utils.dp(4f),
                paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), Utils.dp(26f + lineHeight))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutWidth = right - left
        layoutHeight = bottom - top
    }

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CustomSeekBar, defStyleAttr, defStyleRes
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveAttributeDataForStyleable(
                context, R.styleable.CustomSeekBar,
                attrs, a, defStyleAttr, defStyleRes
            )
        }
        progressColor =
            a.getColor(R.styleable.CustomSeekBar_progressColor, getColorPrimary(context))
        cacheColor = a.getColor(R.styleable.CustomSeekBar_bufferedColor, Color.RED)
        lineColor = a.getColor(R.styleable.CustomSeekBar_lineColor, Color.parseColor("#aa666666"))
        circleColor = a.getColor(R.styleable.CustomSeekBar_progressColor, getColorPrimary(context))
        pressedCircleColor =
            a.getColor(R.styleable.CustomSeekBar_pressedCircleColor, getColorSecondary(context))
        lineHeight = a.getDimension(R.styleable.CustomSeekBar_lineHeight, Utils.dpf2(2f))
        val isAlpha = a.getBoolean(R.styleable.CustomSeekBar_applyAlpha, false)
        noCircle = a.getBoolean(R.styleable.CustomSeekBar_noCircle, false)
        a.recycle()

        if (isAlpha) {
            lineColor = Color.argb(
                51,
                Color.red(lineColor),
                Color.green(lineColor),
                Color.blue(lineColor)
            )

            cacheColor = Color.argb(
                120,
                Color.red(cacheColor),
                Color.green(cacheColor),
                Color.blue(cacheColor)
            )
        }
        if (isInEditMode) {
            setDuration(20)
            setBufferedPosition(18)
            position = 8
        }
    }
}
