/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.onboarding

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.content.getDrawableWithTint
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.setBounds
import org.mozilla.fenix.home.sessioncontrol.OnboardingInteractor

class OnboardingPrivateBrowsingViewHolder(
    view: View,
    private val interactor: OnboardingInteractor
) : RecyclerView.ViewHolder(view) {

    private var header_text: TextView
    private var description_text_once: TextView
    private var open_settings_button: Button

    init {

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.onboarding_private_browsing, view as ViewGroup, false)

        header_text = view.findViewById(R.id.header_text)
        description_text_once = view.findViewById(R.id.description_text_once)
        open_settings_button = view.findViewById(R.id.open_settings_button)

        header_text.setOnboardingIcon(R.drawable.ic_outline_visibility_off)

        // Display a private browsing icon as a character inside the description text.
        val inlineIcon = PrivateBrowsingImageSpan(
            view.context,
            R.drawable.ic_outline_visibility_off,
            tint = view.context.getColorFromAttr(R.attr.primaryText),
            size = description_text_once.lineHeight
        )

        val text = SpannableString(view.context.getString(R.string.onboarding_private_browsing_description1)).apply {

            val spanStartIndex = indexOf(IMAGE_PLACEHOLDER)
            setSpan(
                    inlineIcon,
                spanStartIndex,
                spanStartIndex + IMAGE_PLACEHOLDER.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        description_text_once.text = text
        Log.d("TAG", "text :" +text)
        description_text_once.contentDescription = String.format(text.toString(), header_text.text)
        open_settings_button.setOnClickListener {
            it.context.components.analytics.metrics.track(Event.OnboardingPrivateBrowsing)
            interactor.onOpenSettingsClicked()
        }
    }

    class PrivateBrowsingImageSpan(
        context: Context,
        @DrawableRes drawableId: Int,
        @ColorInt tint: Int,
        size: Int
    ) : ImageSpan(
        context.getDrawableWithTint(drawableId, tint)!!.apply { setBounds(size) }
    ) {
        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            canvas.save()
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.descent - fmPaint.ascent
            val centerY = y + fmPaint.descent - fontHeight / 2
            val transY = (centerY - (drawable.bounds.bottom - drawable.bounds.top) / 2).toFloat()
            canvas.translate(x, transY)
            drawable.draw(canvas)
            canvas.restore()
        }
    }

    companion object {
        const val IMAGE_PLACEHOLDER = "%s"
        const val LAYOUT_ID = R.layout.onboarding_private_browsing
    }
}
