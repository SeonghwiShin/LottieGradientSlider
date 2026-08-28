package io.github.seonghwishin.lottiegradientslider

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

sealed class SliderBackground {
    data class Gradient(
        @ColorInt val startColor: Int,
        @ColorInt val endColor: Int
    ) : SliderBackground()

    data class ImageUrl(val url: String) : SliderBackground()

    data class ImageRes(@DrawableRes val resId: Int) : SliderBackground()

    data class LottieUrl(val url: String) : SliderBackground()

    data class LottieRawRes(@RawRes val resId: Int) : SliderBackground()
}
