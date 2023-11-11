/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.library.recentlyclosed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.state.state.recover.RecoverableTab
import org.mozilla.fenix.R
import org.mozilla.fenix.library.LibrarySiteItemView
import org.mozilla.fenix.library.history.HistoryItemMenu
import org.mozilla.fenix.utils.Do

class RecentlyClosedItemViewHolder(
    view: View,
    private val recentlyClosedFragmentInteractor: RecentlyClosedFragmentInteractor
) : RecyclerView.ViewHolder(view) {

    private var item: RecoverableTab? = null
    private var viewLayout: View
    private var history_layout: LibrarySiteItemView

    init {

        viewLayout = LayoutInflater.from(view.context)
            .inflate(R.layout.history_list_item,view as ViewGroup, false)

        history_layout = view.findViewById(R.id.history_layout)

        setupMenu()
    }

    fun bind(
        item: RecoverableTab
    ) {
        history_layout.titleView.text =
            if (item.title.isNotEmpty()) item.title else item.url
        history_layout.urlView.text = item.url

        if (this.item?.url != item.url) {
           history_layout.loadFavicon(item.url)
        }

        itemView.setOnClickListener {
            recentlyClosedFragmentInteractor.restore(item)
        }

        this.item = item
    }

    private fun setupMenu() {
        val historyMenu = HistoryItemMenu(itemView.context) {
            val item = this.item ?: return@HistoryItemMenu
            Do exhaustive when (it) {
                HistoryItemMenu.Item.Copy -> recentlyClosedFragmentInteractor.onCopyPressed(item)
                HistoryItemMenu.Item.Share -> recentlyClosedFragmentInteractor.onSharePressed(item)
                HistoryItemMenu.Item.OpenInNewTab -> recentlyClosedFragmentInteractor.onOpenInNormalTab(
                    item
                )
                HistoryItemMenu.Item.OpenInPrivateTab -> recentlyClosedFragmentInteractor.onOpenInPrivateTab(
                    item
                )
                HistoryItemMenu.Item.Delete -> recentlyClosedFragmentInteractor.onDeleteOne(
                    item
                )
            }
        }

       history_layout.attachMenu(historyMenu.menuController)
    }

    companion object {
        const val LAYOUT_ID = R.layout.history_list_item
    }
}
