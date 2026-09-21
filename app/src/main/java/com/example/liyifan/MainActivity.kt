package com.example.liyifan

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.color.MaterialColors
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 三语言（中文 / English / 日本語）交互式 Hello World。
 *
 * 界面完全由代码构建，不使用任何 layout XML（`res/layout` 已删除）。
 * 文案取自 `values-xx/strings.xml`，国旗取自 `drawable-xx-nodpi/flag.png`，
 * 二者都依赖资源限定符。点击单选按钮时用 [createConfigurationContext] 取到目标语言的
 * 资源并直接刷新控件，因此既不重启 Activity，也不改动系统语言设置。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var flagView: ImageView
    private lateinit var greetingView: TextView
    private lateinit var subtitleView: TextView
    private lateinit var languageGroup: RadioGroup

    private var currentLanguage = Language.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(buildContentView())
        // 优先沿用上次选择的语言，否则跟随系统（系统语言不受支持时落到英文）
        applyLanguage(
            Language.fromTag(
                languagePrefs().getString(KEY_LANGUAGE, null)
                    ?: resources.configuration.locales[0].language
            )
        )
    }

    // ------------------------------------------------------------ 构建界面

    private fun buildContentView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(PADDING_DP.dp(), PADDING_DP.dp(), PADDING_DP.dp(), PADDING_DP.dp())
        }
        // 全屏（edge-to-edge）下把系统栏的高度补成内边距
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = PADDING_DP.dp() + bars.left,
                top = PADDING_DP.dp() + bars.top,
                right = PADDING_DP.dp() + bars.right,
                bottom = PADDING_DP.dp() + bars.bottom,
            )
            insets
        }

        flagView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(FLAG_WIDTH_DP.dp(), FLAG_HEIGHT_DP.dp())
            scaleType = ImageView.ScaleType.FIT_CENTER

            background = GradientDrawable().apply {
                cornerRadius = 6.dp().toFloat()
                setColor(Color.WHITE)
                setStroke(1.dp(), 0x33000000)
            }
            clipToOutline = true
        }

        greetingView = TextView(this).apply {
            layoutParams = matchWidth(topMarginDp = 24)
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, Color.BLACK))
        }

        subtitleView = TextView(this).apply {
            layoutParams = matchWidth(topMarginDp = 8)
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(
                themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            )
        }

        languageGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            layoutParams = matchWidth(topMarginDp = 40)
        }
        // 单选按钮的顺序与 Language.entries 的顺序一致，下标即 ordinal
        Language.entries.forEach { language ->
            languageGroup.addView(
                RadioButton(this@MainActivity).apply {
                    id = View.generateViewId()
                    setText(language.labelRes)
                    layoutParams =
                        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
            )
        }
        languageGroup.setOnCheckedChangeListener { group, checkedId ->
            val language = Language.entries.getOrNull(group.indexOfChild(group.findViewById(checkedId)))
            if (language != null && language != currentLanguage) applyLanguage(language)
        }

        root.addView(flagView)
        root.addView(greetingView)
        root.addView(subtitleView)
        root.addView(languageGroup)
        return root
    }

    // ------------------------------------------------------------ 语言切换

    /** 应用目标语言：刷新文案与国旗，并记住这次选择。整个过程不重启 Activity。 */
    private fun applyLanguage(language: Language) {
        currentLanguage = language
        val localized = localizedContext(language)

        greetingView.text = localized.getString(R.string.hello_greeting)
        subtitleView.text = localized.getString(R.string.hello_subtitle)
        flagView.contentDescription = localized.getString(R.string.a11y_flag)
        flagView.setImageDrawable(localized.getDrawable(R.drawable.flag))

        // 同步单选状态；若已勾选则不会再次触发回调
        languageGroup.check(languageGroup.getChildAt(language.ordinal).id)
        languagePrefs().edit().putString(KEY_LANGUAGE, language.tag).apply()
    }

    /** 取一个把 locale 固定为 [language] 的 [Context]，用它读资源就等于读该语言的资源版本。 */
    private fun localizedContext(language: Language): Context {
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(language.tag))
        return createConfigurationContext(configuration)
    }

    // ------------------------------------------------------------ 工具

    private fun languagePrefs() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun matchWidth(topMarginDp: Int = 0) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    ).apply { topMargin = topMarginDp.dp() }

    private fun themeColor(@AttrRes attribute: Int, fallback: Int): Int =
        MaterialColors.getColor(this, attribute, fallback)

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).roundToInt()

    private companion object {
        const val PADDING_DP = 24
        const val FLAG_WIDTH_DP = 180
        const val FLAG_HEIGHT_DP = 120
        const val PREFS_NAME = "multilang_hello"
        const val KEY_LANGUAGE = "language"
    }
}
