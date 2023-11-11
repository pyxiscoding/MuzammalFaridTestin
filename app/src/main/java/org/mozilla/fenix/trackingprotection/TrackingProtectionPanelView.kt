/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.trackingprotection

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import kotlinx.android.extensions.LayoutContainer
import mozilla.components.browser.state.state.CustomTabSessionState
import mozilla.components.support.ktx.android.net.hostWithoutCommonPrefixes
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.metrics
import org.mozilla.fenix.trackingprotection.TrackingProtectionCategory.CROSS_SITE_TRACKING_COOKIES
import org.mozilla.fenix.trackingprotection.TrackingProtectionCategory.CRYPTOMINERS
import org.mozilla.fenix.trackingprotection.TrackingProtectionCategory.FINGERPRINTERS
import org.mozilla.fenix.trackingprotection.TrackingProtectionCategory.SOCIAL_MEDIA_TRACKERS
import org.mozilla.fenix.trackingprotection.TrackingProtectionCategory.TRACKING_CONTENT
import org.mozilla.fenix.trackingprotection.TrackingProtectionCategory.REDIRECT_TRACKERS
import java.lang.Exception

/**
 * Interface for the TrackingProtectionPanelViewInteractor. This interface is implemented by objects that want
 * to respond to user interaction on the TrackingProtectionPanelView
 */
interface TrackingProtectionPanelViewInteractor {
    /**
     * Called whenever the settings option is tapped
     */
    fun selectTrackingProtectionSettings()

    /**
     * Called whenever the tracking protection toggle for this site is toggled
     * @param isEnabled new status of session tracking protection
     */
    fun trackingProtectionToggled(isEnabled: Boolean)

    /**
     * Called whenever back is pressed
     */
    fun onBackPressed()

    /**
     * Called whenever an active tracking protection category is tapped
     * @param category The Tracking Protection Category to view details about
     * @param categoryBlocked The trackers from this category were blocked
     */
    fun openDetails(category: TrackingProtectionCategory, categoryBlocked: Boolean)
}

/**
 * View that contains and configures the Tracking Protection Panel
 */
@SuppressWarnings("TooManyFunctions")
class TrackingProtectionPanelView(
    override val containerView: ViewGroup,
    val interactor: TrackingProtectionPanelInteractor
) : LayoutContainer, View.OnClickListener {

    val view: ConstraintLayout = LayoutInflater.from(containerView.context)
        .inflate(R.layout.component_tracking_protection_panel, containerView, true)
        .findViewById(R.id.panel_wrapper)

    private var mode: TrackingProtectionState.Mode = TrackingProtectionState.Mode.Normal

    private var bucketedTrackers = TrackerBuckets()

    private var shouldFocusAccessibilityView: Boolean = true

    init {
        view.findViewById<TextView>(R.id.protection_settings).setOnClickListener {
            interactor.selectTrackingProtectionSettings()
        }
        view.findViewById<ImageView>(R.id.details_back).setOnClickListener {
            interactor.onBackPressed()
        }
        setCategoryClickListeners()

//        var d: Drawable? = null
//        when (containerView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
//            Configuration.UI_MODE_NIGHT_YES -> {
//                d = ContextCompat.getDrawable(containerView.context,R.drawable.ic_logo_white)!!
//            }
//            Configuration.UI_MODE_NIGHT_NO -> {
//                d = ContextCompat.getDrawable(containerView.context,R.drawable.ic_logo_dark)!!
//            }
//        }
//        val bitmap = d?.toBitmap()
//
//        val iconSize =
//            containerView.context.resources.getDimensionPixelSize(R.dimen.preference_icon_drawable_size)
//
//        val scaledIcon = Bitmap.createScaledBitmap(
//            bitmap!!,
//            iconSize,
//            iconSize,
//            true
//        )
//        val icon = BitmapDrawable(containerView.context.resources, scaledIcon)
    }

    fun update(state: TrackingProtectionState) {
        mode = state.mode
        bucketedTrackers.updateIfNeeded(state.listTrackers)

        when (val mode = state.mode) {
            is TrackingProtectionState.Mode.Normal -> setUIForNormalMode(state)
            is TrackingProtectionState.Mode.Details -> setUIForDetailsMode(
                mode.selectedCategory,
                mode.categoryBlocked
            )
        }

        setAccessibilityViewHierarchy(view.findViewById(R.id.details_back), view.findViewById(R.id.category_title))
    }

    private fun setUIForNormalMode(state: TrackingProtectionState) {
        view.findViewById<ConstraintLayout>(R.id.details_mode).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.normal_mode).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.protection_settings).isGone = state.tab is CustomTabSessionState

        view.findViewById<TextView>(R.id.not_blocking_header).isGone = bucketedTrackers.loadedIsEmpty()
        bindUrl(state.url)
        bindTrackingProtectionInfo(state.isTrackingProtectionEnabled)

        // log alifhasnain
        // the "Blocked" text will always be hidden
        //blocking_header.isGone = bucketedTrackers.blockedIsEmpty()
        updateCategoryVisibility()
        focusAccessibilityLastUsedCategory(state.lastAccessedCategory)
    }

    private fun setUIForDetailsMode(
        category: TrackingProtectionCategory,
        categoryBlocked: Boolean
    ) {
        view.findViewById<ConstraintLayout>(R.id.normal_mode).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.details_mode).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.category_title).setText(category.title)
        view.findViewById<TextView>(R.id.blocking_text_list).text = bucketedTrackers.get(category, categoryBlocked).joinToString("\n")
        view.findViewById<TextView>(R.id.category_description).setText(category.description)
        view.findViewById<TextView>(R.id.details_blocking_header).setText(if (categoryBlocked) {
            R.string.enhanced_tracking_protection_blocked
        } else {
            R.string.enhanced_tracking_protection_allowed
        })

        view.findViewById<ImageView>(R.id.details_back).requestFocus()
        view.findViewById<ImageView>(R.id.details_back).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    /**
     * Will force accessibility focus to last entered details category.
     * Called when user returns from details_mode.
     * */
    private fun focusAccessibilityLastUsedCategory(categoryTitle: String) {
        if (categoryTitle.isNotEmpty()) {
            val viewToFocus = getLastUsedCategoryView(categoryTitle)
            if (viewToFocus != null && viewToFocus.isVisible && shouldFocusAccessibilityView) {
                viewToFocus.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                shouldFocusAccessibilityView = false
            }
        }
    }

    /**
     * Checks whether the permission was allowed or blocked when they were last used based on
     * visibility, where "..._loaded" titles correspond to "Allowed" permissions and the other
     * corresponds to "Blocked" permissions for each category.
     */
    private fun getLastUsedCategoryView(categoryTitle: String) = when (categoryTitle) {
        CROSS_SITE_TRACKING_COOKIES.name -> {
            if (view.findViewById<TextView>(R.id.cross_site_tracking).isGone) view.findViewById<TextView>(R.id.cross_site_tracking_loaded) else view.findViewById<TextView>(R.id.cross_site_tracking)
        }
        SOCIAL_MEDIA_TRACKERS.name -> {
            if (view.findViewById<TextView>(R.id.social_media_trackers).isGone) view.findViewById<TextView>(R.id.social_media_trackers_loaded) else view.findViewById<TextView>(R.id.social_media_trackers)
        }
        FINGERPRINTERS.name -> {
            if (view.findViewById<TextView>(R.id.fingerprinters).isGone) view.findViewById<TextView>(R.id.fingerprinters_loaded) else view.findViewById<TextView>(R.id.fingerprinters)
        }
        TRACKING_CONTENT.name -> {
            if (view.findViewById<TextView>(R.id.tracking_content).isGone) view.findViewById<TextView>(R.id.tracking_content_loaded) else view.findViewById<TextView>(R.id.tracking_content)
        }
        CRYPTOMINERS.name -> {
            if (view.findViewById<TextView>(R.id.cryptominers).isGone)
                view.findViewById<TextView>(R.id.cryptominers_loaded) else
            view.findViewById<TextView>(R.id.cryptominers)
        }
        REDIRECT_TRACKERS.name -> {
            if (view.findViewById<TextView>(R.id.redirect_trackers).isGone)
                view.findViewById<TextView>(R.id.redirect_trackers_loaded) else
            view.findViewById<TextView>(R.id.redirect_trackers)
        }
        else -> null
    }

    private fun updateCategoryVisibility() {
        // log alifhasnain
        // Made all tracking block list hidden
        /*cross_site_tracking.isGone =
            bucketedTrackers.get(CROSS_SITE_TRACKING_COOKIES, true).isEmpty()
        social_media_trackers.isGone =
            bucketedTrackers.get(SOCIAL_MEDIA_TRACKERS, true).isEmpty()
        fingerprinters.isGone = bucketedTrackers.get(FINGERPRINTERS, true).isEmpty()
        tracking_content.isGone = bucketedTrackers.get(TRACKING_CONTENT, true).isEmpty()
        cryptominers.isGone = bucketedTrackers.get(CRYPTOMINERS, true).isEmpty()
        redirect_trackers.isGone = bucketedTrackers.get(REDIRECT_TRACKERS, true).isEmpty()

        cross_site_tracking_loaded.isGone =
            bucketedTrackers.get(CROSS_SITE_TRACKING_COOKIES, false).isEmpty()
        social_media_trackers_loaded.isGone =
            bucketedTrackers.get(SOCIAL_MEDIA_TRACKERS, false).isEmpty()
        fingerprinters_loaded.isGone = bucketedTrackers.get(FINGERPRINTERS, false).isEmpty()
        tracking_content_loaded.isGone = bucketedTrackers.get(TRACKING_CONTENT, false).isEmpty()
        cryptominers_loaded.isGone = bucketedTrackers.get(CRYPTOMINERS, false).isEmpty()
        redirect_trackers_loaded.isGone = bucketedTrackers.get(REDIRECT_TRACKERS, false).isEmpty()*/
    }

    private fun setCategoryClickListeners() {
        view.findViewById<TextView>(R.id.social_media_trackers).setOnClickListener(this)
        view.findViewById<TextView>(R.id.fingerprinters).setOnClickListener(this)
        view.findViewById<TextView>(R.id.cross_site_tracking).setOnClickListener(this)
        view.findViewById<TextView>(R.id.tracking_content).setOnClickListener(this)
        view.findViewById<TextView>(R.id.cryptominers).setOnClickListener(this)
        view.findViewById<TextView>(R.id.cross_site_tracking_loaded).setOnClickListener(this)
        view.findViewById<TextView>(R.id.social_media_trackers_loaded).setOnClickListener(this)
        view.findViewById<TextView>(R.id.fingerprinters_loaded).setOnClickListener(this)
        view.findViewById<TextView>(R.id.tracking_content_loaded).setOnClickListener(this)
        view.findViewById<TextView>(R.id.cryptominers_loaded).setOnClickListener(this)
        view.findViewById<TextView>(R.id.redirect_trackers_loaded).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val category = getCategory(v) ?: return
        v.context.metrics.track(Event.TrackingProtectionTrackerList)
        shouldFocusAccessibilityView = true
        interactor.openDetails(category, categoryBlocked = !isLoaded(v))
    }

    private fun bindUrl(url: String) {
        this.view.findViewById<TextView>(R.id.url).text = url.toUri().hostWithoutCommonPrefixes
    }

    private fun bindTrackingProtectionInfo(isTrackingProtectionOn: Boolean) {
        view.findViewById<SwitchCompat>(R.id.trackingProtectionSwitch).findViewById<TextView>(R.id.trackingProtectionCategoryItemDescription).text =
            view.context.getString(if (isTrackingProtectionOn) R.string.etp_panel_on else R.string.etp_panel_off)
        view.findViewById<SwitchCompat>(R.id.trackingProtectionSwitch).findViewById<Switch>(R.id.switch_widget).isChecked = isTrackingProtectionOn
        view.findViewById<SwitchCompat>(R.id.trackingProtectionSwitch).findViewById<Switch>(R.id.switch_widget).jumpDrawablesToCurrentState()

        view.findViewById<SwitchCompat>(R.id.trackingProtectionSwitch).findViewById<Switch>(R.id.switch_widget).setOnCheckedChangeListener { _, isChecked ->
            try {
                interactor.trackingProtectionToggled(isChecked)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun onBackPressed(): Boolean {
        return when (mode) {
            is TrackingProtectionState.Mode.Details -> {
                mode = TrackingProtectionState.Mode.Normal
                interactor.onBackPressed()
                true
            }
            else -> false
        }
    }

    /**
     * Makes sure [view1] is followed by [view2] when navigating in accessibility mode.
     * */
    private fun setAccessibilityViewHierarchy(view1: View, view2: View) {
        ViewCompat.setAccessibilityDelegate(view2, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat
            ) {
                info.setTraversalAfter(view1)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })
    }

    companion object {

        /**
         * Returns the [TrackingProtectionCategory] corresponding to the view ID.
         */
        private fun getCategory(v: View) = when (v.id) {
            R.id.social_media_trackers, R.id.social_media_trackers_loaded -> SOCIAL_MEDIA_TRACKERS
            R.id.fingerprinters, R.id.fingerprinters_loaded -> FINGERPRINTERS
            R.id.cross_site_tracking, R.id.cross_site_tracking_loaded -> CROSS_SITE_TRACKING_COOKIES
            R.id.tracking_content, R.id.tracking_content_loaded -> TRACKING_CONTENT
            R.id.cryptominers, R.id.cryptominers_loaded -> CRYPTOMINERS
            R.id.redirect_trackers, R.id.redirect_trackers_loaded -> REDIRECT_TRACKERS
            else -> null
        }

        /**
         * Returns true if the view corresponds to a "loaded" category
         */
        private fun isLoaded(v: View) = when (v.id) {
            R.id.social_media_trackers_loaded,
            R.id.cross_site_tracking_loaded,
            R.id.fingerprinters_loaded,
            R.id.tracking_content_loaded,
            R.id.cryptominers_loaded,
            R.id.redirect_trackers_loaded -> true

            R.id.social_media_trackers,
            R.id.fingerprinters,
            R.id.cross_site_tracking,
            R.id.tracking_content,
            R.id.cryptominers,
            R.id.redirect_trackers -> false
            else -> false
        }
    }
}
