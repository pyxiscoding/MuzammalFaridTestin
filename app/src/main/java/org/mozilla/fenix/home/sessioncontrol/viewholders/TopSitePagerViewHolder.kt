/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import mozilla.components.feature.top.sites.TopSite
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.home.sessioncontrol.AdapterItem
import org.mozilla.fenix.home.sessioncontrol.TopSiteInteractor
import org.mozilla.fenix.home.sessioncontrol.viewholders.topsites.PagerIndicator
import org.mozilla.fenix.home.sessioncontrol.viewholders.topsites.TopSitesPagerAdapter

class TopSitePagerViewHolder(
        view: View,
        interactor: TopSiteInteractor
) : RecyclerView.ViewHolder(view) {

    private val topSitesPagerAdapter = TopSitesPagerAdapter(interactor)
    private var pageIndicator: PagerIndicator? = null
    private var currentPage = 0
    private var top_sites_pager: ViewPager2

    private val topSitesPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (currentPage != position) {
                pageIndicator?.context?.components?.analytics?.metrics?.track(
                        Event.TopSiteSwipeCarousel(
                                position
                        )
                )
            }

            pageIndicator?.setSelection(position)
            currentPage = position
        }
    }

    init {
        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.component_top_sites_pager, view as ViewGroup, false)

        top_sites_pager = view.findViewById(R.id.top_sites_pager)
        pageIndicator = view.findViewById(R.id.page_indicator)


        top_sites_pager.apply {
            adapter = topSitesPagerAdapter
            registerOnPageChangeCallback(topSitesPageChangeCallback)
        }
    }

    fun update(payload: AdapterItem.TopSitePagerPayload) {
        for (item in payload.changed) {
            topSitesPagerAdapter.notifyItemChanged(currentPage, payload)
        }
    }

    fun bind(topSites: List<TopSite>) {
        val chunkedTopSites = topSites.chunked(TOP_SITES_PER_PAGE)
        topSitesPagerAdapter.submitList(chunkedTopSites)

        // Don't show any page indicator if there is only 1 page.
        val numPages = if (topSites.size > TOP_SITES_PER_PAGE) {
            TOP_SITES_MAX_PAGE_SIZE
        } else {
            0
        }

        pageIndicator?.isVisible = numPages > 1
        pageIndicator?.setSize(numPages)
    }

    companion object {
        const val LAYOUT_ID = R.layout.component_top_sites_pager
        const val TOP_SITES_MAX_PAGE_SIZE = 2
        const val TOP_SITES_PER_PAGE = 8
    }
}
