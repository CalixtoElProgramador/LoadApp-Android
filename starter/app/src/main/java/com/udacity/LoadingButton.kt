package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var backgroundDefault = 0
    private var backgroundLoading = 0
    private var textDefault: CharSequence = ""
    private var textLoading: CharSequence = ""
    private var textColor = 0
    private var progressBarColor = 0

    private val paintButton = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var textButton = ""
    private val paintTextButton = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 40f
        typeface = Typeface.DEFAULT
    }

    private val boundsTextButton = Rect()
    private val progressCircleRect = RectF()
    private var progressCircleSize = 0f

    private val animatorSet: AnimatorSet = AnimatorSet().apply {
        duration = ANIMATION_DURATION_MILLIS
        disableViewDuringAnimation(this@LoadingButton)
    }

    private var loadingBackgroundAnimationValue = 0f
    private var loadingBackgroundAnimator = ValueAnimator()
    private var currentProgressCircleAnimationValue = 0f
    private val progressCircleAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateInterpolator()
        addUpdateListener {
            currentProgressCircleAnimationValue = it.animatedValue as Float
            invalidate()
        }
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            is ButtonState.Clicked -> {
                // Do something
            }
            is ButtonState.Loading -> {
                textButton = textLoading.toString()
                paintTextButton.getTextBounds(textButton, 0, textButton.length, boundsTextButton)
                val horizontalCenter = (widthSize / 2f + 120f)
                val verticalCenter = (heightSize / 2f)
                progressCircleRect.set(
                    horizontalCenter - progressCircleSize,
                    verticalCenter - progressCircleSize,
                    horizontalCenter + progressCircleSize,
                    verticalCenter + progressCircleSize
                )
                animatorSet.start()
            }
            is ButtonState.Completed -> {
                textButton = textDefault.toString()
                animatorSet.cancel()

            }
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundDefault = getColor(R.styleable.LoadingButton_backgroundDefault, 0)
            backgroundLoading = getColor(R.styleable.LoadingButton_backgroundLoading, 0)
            textColor = getColor(R.styleable.LoadingButton_android_textColor, 0)
            textDefault = getText(R.styleable.LoadingButton_textDefault)
            textLoading = getText(R.styleable.LoadingButton_textLoading)
        }
        textButton = textDefault.toString()
        progressBarColor = context.getColor(R.color.colorAccent)

    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawBackgroundColor(it)
            drawText(it)
            drawProgressCircle(it)
        }

    }

    private fun drawProgressCircle(canvas: Canvas) {
        buttonState.takeIf { it == ButtonState.Loading }?.let {
            with(paintButton) {
                color = progressBarColor
                canvas.drawArc(progressCircleRect, 0f, currentProgressCircleAnimationValue, true, this)
            }
        }
    }

    private fun drawText(canvas: Canvas) {
        with(paintTextButton) {
            color = textColor
            canvas.drawText(textButton, widthSize / 2f, (heightSize/2f) + computeTextOffset(), this)
        }

    }

    private fun TextPaint.computeTextOffset() = ((descent() - ascent()) / 2) - descent()

    private fun drawBackgroundColor(canvas: Canvas) {
        when (buttonState) {
            is ButtonState.Loading -> {
                with(paintButton) {
                    color = backgroundLoading
                    canvas.drawRect(
                        0f,
                        0f,
                        loadingBackgroundAnimationValue,
                        heightSize.toFloat(),
                        this
                    )
                }
                with(paintButton) {
                    color = backgroundDefault
                    canvas.drawRect(
                        loadingBackgroundAnimationValue,
                        0f,
                        widthSize.toFloat(),
                        heightSize.toFloat(),
                        this
                    )
                }
            }
            is ButtonState.Completed -> {
                canvas.drawColor(backgroundDefault)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize = (min(w, h) / 2) * SIZE_MULTIPLIER_PROGRESS_CIRCLE
        createLoadingBackgroundAnimation()

    }

    @SuppressLint("Recycle")
    private fun createLoadingBackgroundAnimation() {
        ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                loadingBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
            loadingBackgroundAnimator = this
            animatorSet.playTogether(progressCircleAnimator, loadingBackgroundAnimator)
        }
    }

    fun changeButtonState(state: ButtonState) {
        if (state != buttonState) {
            buttonState = state
            invalidate()
        }
    }

    companion object Constants {
        private const val ANIMATION_DURATION_MILLIS = 3000L
        private const val SIZE_MULTIPLIER_PROGRESS_CIRCLE = 0.5f
    }

}