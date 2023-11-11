/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.browser.infobanner

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import com.google.android.material.button.MaterialButton
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.settings

/**
 * Displays an Info Banner in the specified container with a message and an optional action.
 * The container can be a placeholder layout inserted in the original screen, or an existing layout.
 *
 * @param context - A [Context] for accessing system resources.
 * @param container - The layout where the banner will be shown
 * @param message - The message displayed in the banner
 * @param dismissText - The text on the dismiss button
 * @param actionText - The text on the action to perform button
 * @param actionToPerform - The action to be performed on action button press
 */
@SuppressWarnings("LongParameterList")
open class InfoBanner(
    private val context: Context,
    private val container: ViewGroup,
    private val message: String,
    private val dismissText: String,
    private val actionText: String? = null,
    private val dismissByHiding: Boolean = false,
    private val dismissAction: (() -> Unit)? = null,
    private val actionToPerform: (() -> Unit)? = null
) {
    @SuppressLint("InflateParams")
    @VisibleForTesting
    internal val bannerLayout = LayoutInflater.from(context)
        .inflate(R.layout.info_banner, null)

    internal open fun showBanner() {
        bannerLayout.findViewById<TextView>(R.id.banner_info_message).text = message
        bannerLayout.findViewById<MaterialButton>(R.id.dismiss).text = dismissText

        if (actionText.isNullOrEmpty()) {
            bannerLayout.findViewById<MaterialButton>(R.id.action).visibility = GONE
        } else {
            bannerLayout.findViewById<MaterialButton>(R.id.action).text = actionText
        }

        container.addView(bannerLayout)

        bannerLayout.findViewById<MaterialButton>(R.id.dismiss).setOnClickListener {
            dismissAction?.invoke()
            if (dismissByHiding) { bannerLayout.visibility = GONE } else { dismiss() }
        }

        bannerLayout.findViewById<MaterialButton>(R.id.dismiss).setOnClickListener {
            actionToPerform?.invoke()
        }

        context.settings().lastCfrShownTimeInMillis = System.currentTimeMillis()
    }

    internal fun dismiss() {
        container.removeView(bannerLayout)
    }
}
