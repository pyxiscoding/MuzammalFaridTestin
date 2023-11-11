/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.addUnderline
import org.mozilla.fenix.home.sessioncontrol.OnboardingInteractor
import org.mozilla.fenix.utils.LinkTextView

class OnboardingWhatsNewViewHolder(
    view: View,
    private val interactor: OnboardingInteractor
) : RecyclerView.ViewHolder(view) {

    private var  header_text: TextView
    private var  description_text: TextView
    private var  get_answers: LinkTextView

    init {

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.onboarding_whats_new, view as ViewGroup, false)

        header_text = view.findViewById(R.id.header_text)
        description_text = view.findViewById(R.id.description_text)
        get_answers = view.findViewById(R.id.get_answers)

        header_text.setOnboardingIcon(R.drawable.ic_whats_new)

        val appName = view.context.getString(R.string.app_name)
        description_text.text = view.context.getString(R.string.onboarding_whats_new_description, appName)

        get_answers.addUnderline()
        get_answers.setOnClickListener {
            interactor.onWhatsNewGetAnswersClicked()
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.onboarding_whats_new
    }
}
