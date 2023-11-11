/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.logins.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.loadIntoView
import org.mozilla.fenix.settings.logins.SavedLogin
import org.mozilla.fenix.settings.logins.interactor.SavedLoginsInteractor
import org.mozilla.fenix.utils.view.ViewHolder

class LoginsListViewHolder(
    var view: View,
    private val interactor: SavedLoginsInteractor
) : ViewHolder(view) {

    private var loginItem: SavedLogin? = null

    init {
        view = LayoutInflater.from(view.context)
            .inflate(R.layout.logins_item, view as ViewGroup, false)

    }

    fun bind(item: SavedLogin) {
        this.loginItem = SavedLogin(
            guid = item.guid,
            origin = item.origin,
            password = item.password,
            username = item.username,
            timeLastUsed = item.timeLastUsed
        )
        view.findViewById<TextView>(R.id.webAddressView).text = item.origin
        view.findViewById<TextView>(R.id.usernameView).isVisible = item.username.isNotEmpty()
        view.findViewById<TextView>(R.id.usernameView).text = item.username

        updateFavIcon(item.origin)

        itemView.setOnClickListener {
            interactor.onItemClicked(item)
        }
    }

    private fun updateFavIcon(url: String) {
        itemView.context.components.core.icons.loadIntoView(view.findViewById(R.id.favicon_image), url)
    }
}
