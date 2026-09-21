package com.example.liyifan

import androidx.annotation.StringRes

/**
 * 应用支持的三种语言。
 *
 * [tag] 用来构造 [java.util.Locale]，同时也是 [android.content.res.Resources] 选择
 * `values-xx` / `drawable-xx-nodpi` 限定符目录的依据；
 * [labelRes] 指向语言自身的名称，该名称不随界面语言变化。
 */
enum class Language(val tag: String, @param:StringRes val labelRes: Int) {
    ZH("zh", R.string.language_zh),
    EN("en", R.string.language_en),
    JA("ja", R.string.language_ja);

    companion object {
        /** 系统语言不在支持范围内时使用的兜底语言（默认目录即英文资源）。 */
        val DEFAULT = EN

        /** 把语言标签（如系统 locale 的 "zh"、"ja"）归一化为受支持的语言。 */
        fun fromTag(tag: String?): Language =
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: DEFAULT
    }
}
