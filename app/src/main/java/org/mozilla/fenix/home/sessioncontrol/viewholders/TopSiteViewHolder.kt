/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.feature.top.sites.TopSite
import org.mozilla.fenix.R
import org.mozilla.fenix.home.sessioncontrol.TopSiteInteractor
import org.mozilla.fenix.home.sessioncontrol.viewholders.topsites.TopSitesAdapter
import org.mozilla.fenix.utils.AccessibilityGridLayoutManager

class TopSiteViewHolder(
        view: View,
        interactor: TopSiteInteractor
) : RecyclerView.ViewHolder(view) {

    private val topSitesAdapter = TopSitesAdapter(interactor)
    private var top_sites_list: RecyclerView

    init {

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.component_top_sites, view as ViewGroup, false)

        top_sites_list = view.findViewById(R.id.top_sites_list)
        val gridLayoutManager =
                AccessibilityGridLayoutManager(view.context, SPAN_COUNT)

        top_sites_list.apply {
            adapter = topSitesAdapter
            layoutManager = gridLayoutManager
        }
    }

    fun bind(topSites: List<TopSite>) {
        topSitesAdapter.submitList(topSites)
    }

    companion object {
        const val LAYOUT_ID = R.layout.component_top_sites
        const val SPAN_COUNT = 4
    }
}
