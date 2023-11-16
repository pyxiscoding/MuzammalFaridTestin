/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.library.history.viewholders
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.HistoryListItemBinding
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.hideAndDisable
import org.mozilla.fenix.ext.showAndEnable
import org.mozilla.fenix.library.LibrarySiteItemView
import org.mozilla.fenix.library.SelectionHolder
import org.mozilla.fenix.library.history.HistoryFragmentState
import org.mozilla.fenix.library.history.HistoryInteractor
import org.mozilla.fenix.library.history.HistoryItem
import org.mozilla.fenix.library.history.HistoryItemMenu
import org.mozilla.fenix.library.history.HistoryItemTimeGroup
import org.mozilla.fenix.utils.Do


class HistoryListItemViewHolder(
    view: View,
    private val historyInteractor: HistoryInteractor,
    private val selectionHolder: SelectionHolder<HistoryItem>
) : RecyclerView.ViewHolder(view) {

    private var item: HistoryItem? = null
    lateinit var binding: HistoryListItemBinding
    private var viewLayout: View
    private var viewLayout2: View
    private var delete_button : MaterialButton
    private var history_layout : LibrarySiteItemView
    private var header_title : TextView
    private var recently_closed_tabs_description : TextView
    init {

        viewLayout = LayoutInflater.from(view.context)
            .inflate(R.layout.history_list_item,view as ViewGroup, false)

        delete_button = viewLayout.findViewById(R.id.delete_button)
        history_layout = viewLayout.findViewById(R.id.history_layout)
        header_title = viewLayout.findViewById(R.id.header_title)

        viewLayout2 = LayoutInflater.from(view.context)
            .inflate(R.layout.recently_closed_nav_item,view , false)

        recently_closed_tabs_description = viewLayout2.findViewById(R.id.recently_closed_tabs_description)

        setupMenu()

        delete_button.setOnClickListener {
            val selected = selectionHolder.selectedItems
            if (selected.isEmpty()) {
                historyInteractor.onDeleteAll()
            } else {
                historyInteractor.onDeleteSome(selected)
            }
        }

        itemView.findViewById<ConstraintLayout>(R.id.recently_closed_nav).setOnClickListener {
            historyInteractor.onRecentlyClosedClicked()
        }
    }

    fun bind(
        item: HistoryItem,
        timeGroup: HistoryItemTimeGroup?,
        showDeleteButton: Boolean,
        mode: HistoryFragmentState.Mode,
        isPendingDeletion: Boolean = false
    ) {
        if (isPendingDeletion) {
            history_layout.visibility = View.GONE
        } else {
            history_layout.visibility = View.VISIBLE
        }

        history_layout.titleView.text = item.title
        history_layout.urlView.text = item.url

        toggleTopContent(showDeleteButton, mode === HistoryFragmentState.Mode.Normal)

        val headerText = timeGroup?.humanReadable(itemView.context)
        toggleHeader(headerText)

        history_layout.setSelectionInteractor(item, selectionHolder, historyInteractor)
        history_layout.changeSelected(item in selectionHolder.selectedItems)

        if (this.item?.url != item.url) {
           history_layout.loadFavicon(item.url)
        }

        if (mode is HistoryFragmentState.Mode.Editing) {
            itemView.findViewById<ImageButton>(R.id.overflow_menu).hideAndDisable()
        } else {
            itemView.findViewById<ImageButton>(R.id.overflow_menu).showAndEnable()
        }

        this.item = item
    }

    private fun toggleHeader(headerText: String?) {
        if (headerText != null) {
            header_title.visibility = View.VISIBLE
            header_title.text = headerText
        } else {
            header_title.visibility = View.GONE
        }
    }

    private fun toggleTopContent(
        showTopContent: Boolean,
        isNormalMode: Boolean
    ) {
        delete_button.isVisible = showTopContent
        itemView.findViewById<ConstraintLayout>(R.id.recently_closed_nav).isVisible = showTopContent

        if (showTopContent) {
           delete_button.run {
                if (isNormalMode) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = DELETE_BUTTON_DISABLED_ALPHA
                }
            }
            val numRecentTabs = itemView.context.components.core.store.state.closedTabs.size
            recently_closed_tabs_description.text = String.format(
                itemView.context.getString(
                    if (numRecentTabs == 1)
                        R.string.recently_closed_tab else R.string.recently_closed_tabs
                ), numRecentTabs
            )
            itemView.findViewById<ConstraintLayout>(R.id.recently_closed_nav).run {
                if (isNormalMode) {
                    isEnabled = true
                    alpha = 1f
                } else {
                    isEnabled = false
                    alpha = DELETE_BUTTON_DISABLED_ALPHA
                }
            }
        }
    }

    private fun setupMenu() {
        val historyMenu = HistoryItemMenu(itemView.context) {
            val item = this.item ?: return@HistoryItemMenu
            Do exhaustive when (it) {
                HistoryItemMenu.Item.Copy -> historyInteractor.onCopyPressed(item)
                HistoryItemMenu.Item.Share -> historyInteractor.onSharePressed(item)
                HistoryItemMenu.Item.OpenInNewTab -> historyInteractor.onOpenInNormalTab(item)
                HistoryItemMenu.Item.OpenInPrivateTab -> historyInteractor.onOpenInPrivateTab(item)
                HistoryItemMenu.Item.Delete -> historyInteractor.onDeleteSome(setOf(item))
            }
        }

        history_layout.attachMenu(historyMenu.menuController)
    }

    companion object {
        const val DELETE_BUTTON_DISABLED_ALPHA = 0.4f
        const val LAYOUT_ID = R.layout.history_list_item
    }
}
