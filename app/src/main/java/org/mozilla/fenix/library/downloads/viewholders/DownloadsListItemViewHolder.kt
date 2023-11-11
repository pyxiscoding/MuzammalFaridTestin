/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.library.downloads.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import mozilla.components.feature.downloads.toMegabyteOrKilobyteString
import org.mozilla.fenix.R
import org.mozilla.fenix.library.SelectionHolder
import org.mozilla.fenix.library.downloads.DownloadInteractor
import org.mozilla.fenix.library.downloads.DownloadItem
import org.mozilla.fenix.ext.getIcon
import org.mozilla.fenix.ext.showAndEnable
import org.mozilla.fenix.library.LibrarySiteItemView
import org.mozilla.fenix.library.downloads.DownloadFragmentState
import org.mozilla.fenix.library.downloads.DownloadItemMenu

class DownloadsListItemViewHolder(
    view: View,
    private val downloadInteractor: DownloadInteractor,
    private val selectionHolder: SelectionHolder<DownloadItem>
) : RecyclerView.ViewHolder(view) {

    private var item: DownloadItem? = null
    private var delete_downloads_button : MaterialButton
    private var download_layout : LibrarySiteItemView
    private var favicon : ImageView
    private var overflow_menu : ImageButton

    init {
        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.download_list_item, view as ViewGroup, false)

        val view2 = LayoutInflater.from(view.context)
            .inflate(R.layout.library_site_item, view as ViewGroup, false)
        favicon = view2.findViewById(R.id.favicon)
        overflow_menu = view2.findViewById(R.id.overflow_menu)

        view.addView(view2);

        delete_downloads_button = view.findViewById(R.id.delete_downloads_button)
        download_layout = view.findViewById(R.id.download_layout)


        setupMenu()

        delete_downloads_button.setOnClickListener {
            val selected = selectionHolder.selectedItems
            if (selected.isEmpty()) {
                downloadInteractor.onDeleteAll()
            } else {
                downloadInteractor.onDeleteSome(selected)
            }
        }
    }

    fun bind(
        item: DownloadItem,
        mode: DownloadFragmentState.Mode,
        isPendingDeletion: Boolean = false
    ) {
        download_layout.visibility = if (isPendingDeletion) {
            View.GONE
        } else {
            View.VISIBLE
        }
        download_layout.titleView.text = item.fileName
        download_layout.urlView.text = item.size.toLong().toMegabyteOrKilobyteString()

        toggleTopContent(false, mode == DownloadFragmentState.Mode.Normal)

        download_layout.setSelectionInteractor(item, selectionHolder, downloadInteractor)
        download_layout.changeSelected(item in selectionHolder.selectedItems)

        favicon.setImageResource(item.getIcon())

        overflow_menu.setImageResource(R.drawable.ic_delete)

        overflow_menu.showAndEnable()

        overflow_menu.setOnClickListener {
            downloadInteractor.onDeleteSome(setOf(item))
        }

        this.item = item
    }

    private fun toggleTopContent(
        showTopContent: Boolean,
        isNormalMode: Boolean
    ) {
        delete_downloads_button.isVisible = showTopContent

        if (showTopContent) {
            delete_downloads_button.run {
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
        val downloadMenu = DownloadItemMenu(itemView.context) {
            val item = this.item ?: return@DownloadItemMenu

            if (it == DownloadItemMenu.Item.Delete) {
                downloadInteractor.onDeleteSome(setOf(item))
            }
        }
        download_layout.attachMenu(downloadMenu.menuController)
    }

    companion object {
        const val DELETE_BUTTON_DISABLED_ALPHA = 0.4f
        const val LAYOUT_ID = R.layout.download_list_item
    }
}
