/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.logins.view

import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.extensions.LayoutContainer
import org.mozilla.fenix.R
import org.mozilla.fenix.settings.logins.LoginsListState

/**
 * View that contains and configures the Login Details
 */
class LoginDetailView(override val containerView: ViewGroup) : LayoutContainer {
    fun update(login: LoginsListState) {
        containerView.findViewById<TextView>(R.id.webAddressText).text = login.currentItem?.origin
        containerView.findViewById<TextView>(R.id.usernameText).text = login.currentItem?.username
                containerView.findViewById<TextView>(R.id.passwordText).text = login.currentItem?.password
    }
}
