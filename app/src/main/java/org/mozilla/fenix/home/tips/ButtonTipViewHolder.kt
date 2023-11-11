/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.tips

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.components.tips.Tip
import org.mozilla.fenix.components.tips.TipType
import org.mozilla.fenix.ext.addUnderline
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.home.sessioncontrol.SessionControlInteractor
import org.mozilla.fenix.utils.Settings
import org.mozilla.fenix.utils.view.ViewHolder

class ButtonTipViewHolder(
    view: View,
    private val interactor: SessionControlInteractor,
    private val metrics: MetricController = view.context.components.analytics.metrics,
    private val settings: Settings = view.context.components.settings
) : ViewHolder(view) {

    var tip: Tip? = null
    var tip_header_text: TextView
    var tip_description_text: TextView
    var tip_learn_more: TextView
    var tip_button: Button
    var tip_close: ImageView

    init {
        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.button_tip_item, view as ViewGroup, false)

        tip_header_text = view.findViewById(R.id.tip_header_text)
        tip_description_text = view.findViewById(R.id.tip_description_text)
        tip_button = view.findViewById(R.id.tip_button)
        tip_learn_more = view.findViewById(R.id.tip_learn_more)
        tip_close = view.findViewById(R.id.tip_close)

    }

    fun bind(tip: Tip) {
        require(tip.type is TipType.Button)

        this.tip = tip

        metrics.track(Event.TipDisplayed(tip.identifier))

        tip_header_text.text = tip.title
        tip.titleDrawable?.let {
            tip_header_text.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
        }
        tip_description_text.text = tip.description
        tip_button.text = tip.type.text

        tip_learn_more.isVisible = tip.learnMoreURL != null
        if (tip.learnMoreURL != null) {
            tip_learn_more.addUnderline()

            tip_learn_more.setOnClickListener {
                (itemView.context as HomeActivity).openToBrowserAndLoad(
                    searchTermOrURL = tip.learnMoreURL,
                    newTab = true,
                    from = BrowserDirection.FromHome
                )
            }
        }

        tip_button.setOnClickListener {
            tip.type.action.invoke()
            metrics.track(Event.TipPressed(tip.identifier))
        }

        tip_close.setOnClickListener {
            metrics.track(Event.TipClosed(tip.identifier))

            settings.preferences
                .edit()
                .putBoolean(tip.identifier, false)
                .apply()

            interactor.onCloseTip(tip)
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.button_tip_item
    }
}
