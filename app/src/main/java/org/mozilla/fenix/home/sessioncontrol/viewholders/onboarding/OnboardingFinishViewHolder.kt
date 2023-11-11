/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.home.sessioncontrol.OnboardingInteractor

class OnboardingFinishViewHolder(
    view: View,
    private val interactor: OnboardingInteractor
) : RecyclerView.ViewHolder(view) {

    var finish_button: Button

    init {
        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.onboarding_finish, view as ViewGroup, false)

        finish_button = view.findViewById(R.id.finish_button)


        finish_button.setOnClickListener {
            interactor.onStartBrowsingClicked()
            it.context.components.analytics.metrics.track(Event.OnboardingFinish)
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.onboarding_finish
    }
}
