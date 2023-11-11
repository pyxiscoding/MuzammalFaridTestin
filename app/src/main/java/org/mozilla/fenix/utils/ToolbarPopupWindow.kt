/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.CustomTabSessionState
import mozilla.components.browser.state.store.BrowserStore
import org.mozilla.fenix.R
import org.mozilla.fenix.components.FenixSnackbar
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.components
import java.lang.ref.WeakReference

object ToolbarPopupWindow {
    fun show(
        view: WeakReference<View>,
        customTabSession: CustomTabSessionState? = null,
        handlePasteAndGo: (String) -> Unit,
        handlePaste: (String) -> Unit,
        copyVisible: Boolean = true
    ) {
        val context = view.get()?.context ?: return
        val clipboard = context.components.clipboardHandler
        if (!copyVisible && clipboard.text.isNullOrEmpty()) return

        val isCustomTabSession = customTabSession != null

        val customView = LayoutInflater.from(context)
            .inflate(R.layout.browser_toolbar_popup_window, null)
        val popupWindow = PopupWindow(
            customView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            context.resources.getDimensionPixelSize(R.dimen.context_menu_height),
            true
        )
        popupWindow.elevation =
            context.resources.getDimension(R.dimen.mozac_browser_menu_elevation)

        // This is a workaround for SDK<23 to allow popup dismissal on outside or back button press
        // See: https://github.com/mozilla-mobile/fenix/issues/10027
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        customView.findViewById<Button>(R.id.copy).isVisible = copyVisible

        customView.findViewById<Button>(R.id.paste).isVisible = !clipboard.text.isNullOrEmpty() && !isCustomTabSession
        customView.findViewById<Button>(R.id.paste_and_go).isVisible =
            !clipboard.text.isNullOrEmpty() && !isCustomTabSession

        customView.findViewById<Button>(R.id.copy).setOnClickListener {
            popupWindow.dismiss()
            clipboard.text = getUrlForClipboard(
                it.context.components.core.store,
                customTabSession
            )

            view.get()?.let {
                FenixSnackbar.make(
                    view = it,
                    duration = Snackbar.LENGTH_SHORT,
                    isDisplayedWithBrowserToolbar = true
                )
                    .setText(context.getString(R.string.browser_toolbar_url_copied_to_clipboard_snackbar))
                    .show()
            }
            context.components.analytics.metrics.track(Event.CopyUrlUsed)
        }

        customView.findViewById<Button>(R.id.paste).setOnClickListener {
            popupWindow.dismiss()
            handlePaste(clipboard.text!!)
        }

        customView.findViewById<Button>(R.id.paste_and_go).setOnClickListener {
            popupWindow.dismiss()
            handlePasteAndGo(clipboard.text!!)
        }

        view.get()?.let {
            popupWindow.showAsDropDown(
                it,
                context.resources.getDimensionPixelSize(R.dimen.context_menu_x_offset),
                0,
                Gravity.START
            )
        }
    }

    @VisibleForTesting
    internal fun getUrlForClipboard(
        store: BrowserStore,
        customTabSession: CustomTabSessionState? = null
    ): String? {
        return if (customTabSession != null) {
            customTabSession.content.url
        } else {
            val selectedTab = store.state.selectedTab
            selectedTab?.readerState?.activeUrl ?: selectedTab?.content?.url
        }
    }
}
