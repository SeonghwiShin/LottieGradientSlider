package io.github.seonghwishin.lottiegradientslider

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class LottieGradientSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    fun interface OnValueChangeListener {
        fun onValueChanged(value: Int, fromUser: Boolean)
    }

    private val backgroundLayer = FrameLayout(context)
    private val gradientView = View(context)
    private val imageView = ImageView(context)
    private val lottieView = LottieAnimationView(context)
    private val contentScrimView = View(context)
    private val progressOverlayView = View(context)
    private val overlaySeekBar = SeekBar(context)
    private val titleView = TextView(context)
    private val valueView = TextView(context)

    private var listener: OnValueChangeListener? = null
    private var sliderBackground: SliderBackground? = null

    var maxValue: Int = 100
        set(value) {
            field = value.coerceAtLeast(1)
            overlaySeekBar.max = field
            progress = progress.coerceIn(0, field)
        }

    var progress: Int = 50
        set(value) {
            field = value.coerceIn(0, maxValue)
            overlaySeekBar.progress = if (reverseProgress) maxValue - field else field
            valueView.text = field.toString()
            updateProgressOverlay()
        }

    var reverseProgress: Boolean = true
        set(value) {
            field = value
            overlaySeekBar.rotation = if (value) 180f else 0f
            progress = progress
        }

    var showValue: Boolean = true
        set(value) {
            field = value
            valueView.isVisible = value
        }

    var title: String = ""
        set(value) {
            field = value
            titleView.text = value
        }

    var cornerRadiusPx: Float = dp(12).toFloat()
        set(value) {
            field = value
            updateClip()
        }

    @ColorInt
    var contentScrimColor: Int = 0x33000000
        set(value) {
            field = value
            contentScrimView.setBackgroundColor(value)
        }

    @ColorInt
    var overlayColor: Int = Color.argb(128, 0, 0, 0)
        set(value) {
            field = value
            progressOverlayView.setBackgroundColor(value)
        }

    @ColorInt
    var textColor: Int = Color.WHITE
        set(value) {
            field = value
            titleView.setTextColor(value)
            valueView.setTextColor(value)
        }

    @ColorInt
    var textShadowColor: Int = 0x66000000
        set(value) {
            field = value
            applyTextShadow()
        }

    var textShadowRadiusPx: Float = dp(2).toFloat()
        set(value) {
            field = value.coerceAtLeast(0f)
            applyTextShadow()
        }

    var titlePaddingStartPx: Int = dp(12)
        set(value) {
            field = value.coerceAtLeast(0)
            updateTextMargins()
        }

    var valuePaddingEndPx: Int = dp(12)
        set(value) {
            field = value.coerceAtLeast(0)
            updateTextMargins()
        }

    init {
        isClickable = true
        clipChildren = false
        setupViews()
        readAttributes(attrs)
        updateClip()
        overlaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, rawProgress: Int, fromUser: Boolean) {
                val value = if (reverseProgress) maxValue - rawProgress else rawProgress
                progress = value
                listener?.onValueChanged(value, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
    }

    fun setOnValueChangeListener(listener: OnValueChangeListener?) {
        this.listener = listener
    }

    fun setGradient(@ColorInt startColor: Int, @ColorInt endColor: Int) {
        setBackground(SliderBackground.Gradient(startColor, endColor))
    }

    fun setImageUrl(url: String) {
        setBackground(SliderBackground.ImageUrl(url))
    }

    fun setImageResource(@DrawableRes resId: Int) {
        setBackground(SliderBackground.ImageRes(resId))
    }

    fun setLottieUrl(url: String) {
        setBackground(SliderBackground.LottieUrl(url))
    }

    fun setLottieRawResource(@RawRes resId: Int) {
        setBackground(SliderBackground.LottieRawRes(resId))
    }

    fun setCornerRadiusDp(radiusDp: Float) {
        cornerRadiusPx = radiusDp * resources.displayMetrics.density
    }

    fun setTextHorizontalPaddingDp(titleStartDp: Float, valueEndDp: Float) {
        titlePaddingStartPx = (titleStartDp * resources.displayMetrics.density).toInt()
        valuePaddingEndPx = (valueEndDp * resources.displayMetrics.density).toInt()
    }

    fun setBackground(background: SliderBackground) {
        if (sliderBackground == background) return

        sliderBackground = background
        resetBackgroundViews()
        when (background) {
            is SliderBackground.Gradient -> {
                gradientView.isVisible = true
                gradientView.background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(background.startColor, background.endColor)
                )
            }

            is SliderBackground.ImageUrl -> {
                imageView.isVisible = true
                Glide.with(this)
                    .load(background.url)
                    .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<android.graphics.drawable.Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            imageView.isGone = true
                            gradientView.isVisible = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable,
                            model: Any,
                            target: Target<android.graphics.drawable.Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean = false
                    })
                    .centerCrop()
                    .into(imageView)
            }

            is SliderBackground.ImageRes -> {
                imageView.isVisible = true
                imageView.setImageResource(background.resId)
            }

            is SliderBackground.LottieUrl -> {
                lottieView.isVisible = true
                lottieView.setAnimationFromUrl(background.url)
                lottieView.repeatCount = com.airbnb.lottie.LottieDrawable.INFINITE
                lottieView.playAnimation()
            }

            is SliderBackground.LottieRawRes -> {
                lottieView.isVisible = true
                lottieView.setAnimation(background.resId)
                lottieView.repeatCount = com.airbnb.lottie.LottieDrawable.INFINITE
                lottieView.playAnimation()
            }
        }
    }

    private fun setupViews() {
        val sliderHeight = dp(48)

        addView(backgroundLayer, LayoutParams(LayoutParams.MATCH_PARENT, sliderHeight).apply {
            gravity = Gravity.CENTER_VERTICAL
        })

        backgroundLayer.addView(gradientView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        backgroundLayer.addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        backgroundLayer.addView(lottieView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        backgroundLayer.addView(contentScrimView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        backgroundLayer.addView(progressOverlayView, LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.END
        })

        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        lottieView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.isGone = true
        lottieView.isGone = true
        contentScrimView.setBackgroundColor(contentScrimColor)
        progressOverlayView.setBackgroundColor(overlayColor)

        overlaySeekBar.max = maxValue
        overlaySeekBar.progressDrawable = ColorDrawable(Color.TRANSPARENT)
        overlaySeekBar.thumb = createThumbDrawable()
        overlaySeekBar.splitTrack = false
        addView(overlaySeekBar, LayoutParams(LayoutParams.MATCH_PARENT, sliderHeight).apply {
            gravity = Gravity.CENTER_VERTICAL
        })

        titleView.setTextColor(textColor)
        titleView.textSize = 12f
        titleView.includeFontPadding = false
        titleView.setShadowLayer(textShadowRadiusPx, 0f, 1f, textShadowColor)
        addView(titleView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            leftMargin = dp(12)
        })

        valueView.setTextColor(textColor)
        valueView.textSize = 12f
        valueView.includeFontPadding = false
        valueView.setShadowLayer(textShadowRadiusPx, 0f, 1f, textShadowColor)
        addView(valueView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            rightMargin = dp(12)
        })
    }

    private fun readAttributes(attrs: AttributeSet?) {
        val defaultStart = Color.parseColor("#38D790")
        val defaultEnd = Color.parseColor("#4A79F1")
        var start = defaultStart
        var end = defaultEnd
        context.withStyledAttributes(attrs, R.styleable.LottieGradientSliderView) {
            title = getString(R.styleable.LottieGradientSliderView_lgs_title).orEmpty()
            maxValue = getInt(R.styleable.LottieGradientSliderView_lgs_max, 100)
            reverseProgress = getBoolean(R.styleable.LottieGradientSliderView_lgs_reverseProgress, true)
            showValue = getBoolean(R.styleable.LottieGradientSliderView_lgs_showValue, true)
            cornerRadiusPx = getDimension(R.styleable.LottieGradientSliderView_lgs_cornerRadius, dp(12).toFloat())
            contentScrimColor = getColor(R.styleable.LottieGradientSliderView_lgs_contentScrimColor, 0x33000000)
            overlayColor = getColor(R.styleable.LottieGradientSliderView_lgs_overlayColor, Color.argb(128, 0, 0, 0))
            textColor = getColor(R.styleable.LottieGradientSliderView_lgs_textColor, Color.WHITE)
            textShadowColor = getColor(R.styleable.LottieGradientSliderView_lgs_textShadowColor, 0x66000000)
            textShadowRadiusPx = getDimension(R.styleable.LottieGradientSliderView_lgs_textShadowRadius, dp(2).toFloat())
            titlePaddingStartPx = getDimensionPixelSize(R.styleable.LottieGradientSliderView_lgs_titlePaddingStart, dp(12))
            valuePaddingEndPx = getDimensionPixelSize(R.styleable.LottieGradientSliderView_lgs_valuePaddingEnd, dp(12))
            start = getColor(R.styleable.LottieGradientSliderView_lgs_gradientStartColor, defaultStart)
            end = getColor(R.styleable.LottieGradientSliderView_lgs_gradientEndColor, defaultEnd)
            progress = getInt(R.styleable.LottieGradientSliderView_lgs_progress, 50)
        }
        setGradient(start, end)
    }

    private fun resetBackgroundViews() {
        lottieView.cancelAnimation()
        gradientView.isGone = true
        imageView.isGone = true
        lottieView.isGone = true
    }

    private fun updateClip() {
        backgroundLayer.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
            }
        }
        backgroundLayer.clipToOutline = true
        backgroundLayer.invalidateOutline()
        invalidateOutline()
    }

    private fun applyTextShadow() {
        titleView.setShadowLayer(textShadowRadiusPx, 0f, 1f, textShadowColor)
        valueView.setShadowLayer(textShadowRadiusPx, 0f, 1f, textShadowColor)
    }

    private fun updateTextMargins() {
        (titleView.layoutParams as? LayoutParams)?.let { params ->
            params.leftMargin = titlePaddingStartPx
            titleView.layoutParams = params
        }
        (valueView.layoutParams as? LayoutParams)?.let { params ->
            params.rightMargin = valuePaddingEndPx
            valueView.layoutParams = params
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateProgressOverlay()
    }

    private fun updateProgressOverlay() {
        if (backgroundLayer.width == 0) {
            backgroundLayer.post { updateProgressOverlay() }
            return
        }

        val overlayProgress = if (reverseProgress) maxValue - progress else progress
        val overlayWidth = (backgroundLayer.width * (overlayProgress / maxValue.toFloat())).toInt()
        progressOverlayView.layoutParams = (progressOverlayView.layoutParams as LayoutParams).apply {
            width = overlayWidth
            height = LayoutParams.MATCH_PARENT
            gravity = if (reverseProgress) Gravity.END else Gravity.START
        }
    }

    private fun createThumbDrawable(): ShapeDrawable {
        return ShapeDrawable(OvalShape()).apply {
            paint.color = Color.TRANSPARENT
            intrinsicWidth = dp(13)
            intrinsicHeight = dp(13)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
