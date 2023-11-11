/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import mozilla.components.concept.engine.prompt.ShareData
import org.mozilla.fenix.R
import org.mozilla.fenix.share.listadapters.ShareTabsAdapter

/**
 * Callbacks for possible user interactions on the [ShareCloseView]
 */
interface ShareCloseInteractor {
    fun onShareClosed()
}

class ShareCloseView(
    override val containerView: ViewGroup,
    private val interactor: ShareCloseInteractor
) : LayoutContainer {

    val adapter = ShareTabsAdapter()

    init {
        LayoutInflater.from(containerView.context)
            .inflate(R.layout.share_close, containerView, true)

        containerView.findViewById<AppCompatImageView>(R.id.closeButton).setOnClickListener { interactor.onShareClosed() }

        containerView.findViewById<RecyclerView>(R.id.shared_site_list).layoutManager = LinearLayoutManager(containerView.context)
        containerView.findViewById<RecyclerView>(R.id.shared_site_list).adapter = adapter
    }

    fun setTabs(tabs: List<ShareData>) {
        adapter.submitList(tabs)
    }
}
