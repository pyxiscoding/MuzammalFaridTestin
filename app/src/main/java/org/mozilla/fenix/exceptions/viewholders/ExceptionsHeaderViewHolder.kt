/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.exceptions.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R


class ExceptionsHeaderViewHolder(
    view: View,
    @StringRes description: Int
) : RecyclerView.ViewHolder(view) {

    var exceptions_description: TextView
    init {
        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.exceptions_description, view as ViewGroup)

        exceptions_description = view.findViewById(R.id.exceptions_description)

        exceptions_description.text = view.context.getString(description)
    }

    companion object {
        const val LAYOUT_ID = R.layout.exceptions_description
    }
}
