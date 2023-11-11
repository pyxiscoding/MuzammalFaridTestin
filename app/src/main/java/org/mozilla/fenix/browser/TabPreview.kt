/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.browser

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import mozilla.components.browser.tabstray.thumbnail.TabThumbnailView
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
import mozilla.components.concept.base.images.ImageLoadRequest
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.TabPreviewBinding
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.theme.ThemeManager
import kotlin.math.max

class TabPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val thumbnailLoader = ThumbnailLoader(context.components.core.thumbnailStorage)
    val fakeToolbar: LinearLayout = TODO()
    val previewThumbnail: TabThumbnailView = TODO()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.tab_preview, this, true)
        val view2 = LayoutInflater.from(context).inflate(R.layout.tabs_tray_tab_counter, this, true)

         fakeToolbar= view.findViewById(R.id.fakeToolbar)
        previewThumbnail= view.findViewById(R.id.previewThumbnail)
        val counter_box = view2.findViewById<ImageView>(R.id.counter_box)
        val counter_text = view2.findViewById<TextView>(R.id.counter_text)

        if (!context.settings().shouldUseBottomToolbar) {
            fakeToolbar.updateLayoutParams<LayoutParams> {
                gravity = Gravity.TOP
            }

            fakeToolbar.background = AppCompatResources.getDrawable(
                context,
                ThemeManager.resolveAttribute(R.attr.bottomBarBackgroundTop, context)
            )
        }

        // Change view properties to avoid confusing the UI tests
        counter_box.id = View.NO_ID
        counter_text.id = View.NO_ID
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        previewThumbnail.translationY = if (!context.settings().shouldUseBottomToolbar) {
            fakeToolbar.height.toFloat()
        } else {
            0f
        }
    }

    fun loadPreviewThumbnail(thumbnailId: String) {
        doOnNextLayout {
            val thumbnailSize = max(previewThumbnail.height, previewThumbnail.width)
            thumbnailLoader.loadIntoView(
                previewThumbnail,
                ImageLoadRequest(thumbnailId, thumbnailSize)
            )
        }
    }
}
