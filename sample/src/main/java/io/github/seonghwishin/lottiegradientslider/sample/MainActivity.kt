package io.github.seonghwishin.lottiegradientslider.sample

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.github.seonghwishin.lottiegradientslider.LottieGradientSliderView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.parseColor("#F5F7FA"))
        }

        val title = TextView(this).apply {
            text = "LottieGradientSlider"
            textSize = 24f
            setTextColor(Color.parseColor("#20242A"))
        }
        root.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(24)
        })

        root.addView(slider("Gradient #1", progress = 72).apply {
            setGradient(Color.parseColor("#FFE36E"), Color.parseColor("#8DE9FF"))
            contentScrimColor = 0x33000000
        })

        root.addView(slider("Image #2", progress = 48).apply {
            setImageUrl("https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=900")
        })

        root.addView(slider("Lottie #3", progress = 140, max = 200).apply {
            setLottieUrl("https://raw.githubusercontent.com/SeonghwiShin/LottieGradientSlider/main/sample-assets/background-animation-by-bajutech.json")
            contentScrimColor = 0x22000000
        })

        setContentView(root)
    }

    private fun slider(title: String, progress: Int, max: Int = 100): LottieGradientSliderView {
        return LottieGradientSliderView(this).apply {
            this.title = title
            this.maxValue = max
            this.progress = progress
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                bottomMargin = dp(16)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
