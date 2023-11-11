/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.increaseTapArea
import org.mozilla.fenix.home.sessioncontrol.CollectionInteractor
import org.mozilla.fenix.utils.view.ViewHolder

@OptIn(ExperimentalCoroutinesApi::class)
open class NoCollectionsMessageViewHolder(
    view: View,
    viewLifecycleOwner: LifecycleOwner,
    store: BrowserStore,
    interactor: CollectionInteractor
) : ViewHolder(view) {

    var add_tabs_to_collections_button: MaterialButton
    var remove_collection_placeholder: AppCompatImageButton


    init {

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.no_collections_message, view as ViewGroup, false)

        add_tabs_to_collections_button = view.findViewById(R.id.add_tabs_to_collections_button)
        remove_collection_placeholder = view.findViewById(R.id.remove_collection_placeholder)


        add_tabs_to_collections_button.setOnClickListener {
            interactor.onAddTabsToCollectionTapped()
        }

        remove_collection_placeholder.increaseTapArea(
            view.resources.getDimensionPixelSize(R.dimen.tap_increase_16)
        )

        remove_collection_placeholder.setOnClickListener {
            interactor.onRemoveCollectionsPlaceholder()
        }

        add_tabs_to_collections_button.isVisible = store.state.normalTabs.isNotEmpty()

        store.flowScoped(viewLifecycleOwner) { flow ->
            flow.map { state -> state.normalTabs.size }
                .ifChanged()
                .collect { tabs ->
                    add_tabs_to_collections_button.isVisible = tabs > 0
                }
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.no_collections_message
    }
}
