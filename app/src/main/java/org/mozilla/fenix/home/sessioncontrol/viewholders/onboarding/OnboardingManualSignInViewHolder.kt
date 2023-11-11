/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.addUnderline
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.home.HomeFragmentDirections
import org.mozilla.fenix.onboarding.OnboardingController
import org.mozilla.fenix.onboarding.OnboardingInteractor
import org.mozilla.fenix.utils.LinkTextView

class OnboardingManualSignInViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val headerText: TextView
    private val fxa_sign_in_button: Button
    private val learn_more: LinkTextView

    init {

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.onboarding_manual_signin, view as ViewGroup, false)

        headerText =view.findViewById(R.id.header_text)
        fxa_sign_in_button =view.findViewById(R.id.fxa_sign_in_button)
        learn_more =view.findViewById(R.id.learn_more)


        val interactor = OnboardingInteractor(OnboardingController(itemView.context))

        fxa_sign_in_button.setOnClickListener {
            it.context.components.analytics.metrics.track(Event.OnboardingManualSignIn)

            val directions = HomeFragmentDirections.actionGlobalTurnOnSync()
            Navigation.findNavController(view).navigate(directions)
        }

        learn_more.addUnderline()
        learn_more.setOnClickListener {
            interactor.onLearnMoreClicked()
        }
    }

    fun bind() {
        val context = itemView.context
        headerText.text = context.getString(R.string.onboarding_account_sign_in_header)
    }

    companion object {
        const val LAYOUT_ID = R.layout.onboarding_manual_signin
    }
}
