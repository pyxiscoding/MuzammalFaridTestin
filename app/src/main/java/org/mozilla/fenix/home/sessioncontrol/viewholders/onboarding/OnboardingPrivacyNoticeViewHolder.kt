/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.home.sessioncontrol.OnboardingInteractor

class OnboardingPrivacyNoticeViewHolder(view: View, private val interactor: OnboardingInteractor) : RecyclerView.ViewHolder(view) {

   var header_text: TextView
   var description_text: TextView
   var read_button: Button

    init {

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.onboarding_privacy_notice, view as ViewGroup, false)

        header_text = view.findViewById(R.id.header_text)
        description_text = view.findViewById(R.id.description_text)
        read_button = view.findViewById(R.id.read_button)

        header_text.setOnboardingIcon(R.drawable.ic_onboarding_privacy_notice)

        val appName = view.context.getString(R.string.app_name)
        description_text.text = view.context.getString(R.string.onboarding_privacy_notice_description2, appName)

        read_button.setOnClickListener {
            it.context.components.analytics.metrics.track(Event.OnboardingPrivacyNotice)
            interactor.onReadPrivacyNoticeClicked()
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.onboarding_privacy_notice
    }
}
