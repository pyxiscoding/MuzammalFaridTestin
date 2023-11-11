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

class OnboardingHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var header_text: TextView

    init {

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.onboarding_header, view as ViewGroup, false)

        header_text = view.findViewById(R.id.header_text)


        //  val appName = view.context.getString(R.string.app_name)
        header_text.text = view.context.getString(R.string.onboarding_header)
    }

    companion object {
        const val LAYOUT_ID = R.layout.onboarding_header
    }
}
