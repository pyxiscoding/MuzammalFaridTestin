/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.topsites

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.feature.top.sites.TopSite.Type.FRECENT
import mozilla.components.feature.top.sites.TopSite.Type.PINNED
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.loadIntoView
import org.mozilla.fenix.home.sessioncontrol.TopSiteInteractor
import org.mozilla.fenix.settings.SupportUtils
import org.mozilla.fenix.utils.view.ViewHolder

class TopSiteItemViewHolder(
        view: View,
        private val interactor: TopSiteInteractor
) : ViewHolder(view) {
    private lateinit var topSite: TopSite
    private var top_site_item: ConstraintLayout
    private var top_site_title: TextView
    private var pin_indicator: FrameLayout
    private var favicon_image: ImageView



    init {
        val view3 = LayoutInflater.from(view.context)
            .inflate(R.layout.top_site_item, view as ViewGroup, false)

        top_site_item = view3.findViewById(R.id.top_site_item)
        top_site_title = view3.findViewById(R.id.top_site_title)
        pin_indicator = view3.findViewById(R.id.pin_indicator)
        favicon_image = view3.findViewById(R.id.favicon_image)


        top_site_item.setOnClickListener {
            interactor.onSelectTopSite(topSite.url, topSite.type)
        }

        top_site_item.setOnLongClickListener {
            interactor.onTopSiteMenuOpened()
            it.context.components.analytics.metrics.track(Event.TopSiteLongPress(topSite.type))

            val topSiteMenu = TopSiteItemMenu(view.context, topSite.type != FRECENT) { item ->
                when (item) {
                    is TopSiteItemMenu.Item.OpenInPrivateTab -> interactor.onOpenInPrivateTabClicked(
                            topSite
                    )
                    is TopSiteItemMenu.Item.RenameTopSite -> interactor.onRenameTopSiteClicked(
                            topSite
                    )
                    is TopSiteItemMenu.Item.RemoveTopSite -> interactor.onRemoveTopSiteClicked(
                            topSite
                    )
                }
            }
            val menu = topSiteMenu.menuBuilder.build(view.context).show(anchor = it)
            it.setOnTouchListener @SuppressLint("ClickableViewAccessibility") { v, event ->
                onTouchEvent(v, event, menu)
            }
            true
        }
    }

    fun bind(topSite: TopSite) {
        top_site_title.text = topSite.title

        pin_indicator.visibility = if (topSite.type == PINNED) {
            View.VISIBLE
        } else {
            View.GONE
        }

        when (topSite.url) {
            SupportUtils.POCKET_TRENDING_URL -> {
                favicon_image.setImageDrawable(getDrawable(itemView.context, R.drawable.ic_pocket))
            }
            SupportUtils.BAIDU_URL -> {
                favicon_image.setImageDrawable(getDrawable(itemView.context, R.drawable.ic_baidu))
            }
            SupportUtils.JD_URL -> {
                favicon_image.setImageDrawable(getDrawable(itemView.context, R.drawable.ic_jd))
            }
            else -> {
                if (topSite.url.contains(SupportUtils.LUXXEL_URL)){
                    val iconSize =
                            itemView.context.resources.getDimensionPixelSize(R.dimen.preference_icon_drawable_size)
                    val d: Drawable = ContextCompat.getDrawable(itemView.context,R.drawable.ic_launcher_foreground)!!
                    when (itemView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            DrawableCompat.setTint(d, Color.WHITE)
                        }
                        Configuration.UI_MODE_NIGHT_NO -> {
                            DrawableCompat.setTint(d, Color.BLACK)
                        }
                    }
                    val bitmap = d.toBitmap()

                    val scaledIcon = Bitmap.createScaledBitmap(
                            bitmap,
                            iconSize,
                            iconSize,
                            true
                    )

                    val icon = BitmapDrawable(itemView.context.resources, scaledIcon)
                    favicon_image.setImageDrawable(icon)
                }else
                    itemView.context.components.core.icons.loadIntoView(favicon_image, topSite.url)
            }
        }

        this.topSite = topSite
    }

    private fun onTouchEvent(
            v: View,
            event: MotionEvent,
            menu: PopupWindow
    ): Boolean {
        if (event.action == MotionEvent.ACTION_CANCEL) {
            menu.dismiss()
        }
        return v.onTouchEvent(event)
    }

    companion object {
        const val LAYOUT_ID = R.layout.top_site_item
    }
}

class TopSiteItemMenu(
        private val context: Context,
        private val isPinnedSite: Boolean,
        private val onItemTapped: (Item) -> Unit = {}
) {
    sealed class Item {
        object OpenInPrivateTab : Item()
        object RenameTopSite : Item()
        object RemoveTopSite : Item()
    }

    val menuBuilder by lazy { BrowserMenuBuilder(menuItems) }

    private val menuItems by lazy {
        listOfNotNull(
                SimpleBrowserMenuItem(
                        context.getString(R.string.bookmark_menu_open_in_private_tab_button)
                ) {
                    onItemTapped.invoke(Item.OpenInPrivateTab)
                },
                if (isPinnedSite) SimpleBrowserMenuItem(
                        context.getString(R.string.rename_top_site)
                ) {
                    onItemTapped.invoke(Item.RenameTopSite)
                } else null,
                SimpleBrowserMenuItem(
                        if (isPinnedSite) {
                            context.getString(R.string.remove_top_site)
                        } else {
                            context.getString(R.string.delete_from_history)
                        }
                ) {
                    onItemTapped.invoke(Item.RemoveTopSite)
                }
        )
    }
}
