/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.addons

import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.translateDescription
import mozilla.components.feature.addons.ui.updatedAtDate
import org.mozilla.fenix.R
import org.mozilla.fenix.container.LayoutContainer
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

interface AddonDetailsInteractor {

    /**
     * Open the given addon siteUrl in the browser.
     */
    fun openWebsite(addonSiteUrl: Uri)

    /**
     * Display the updater dialog.
     */
    fun showUpdaterDialog(addon: Addon)
}

/**
 * Shows the details of an add-on.
 */
class AddonDetailsView(
    override val containerView: View,
    private val interactor: AddonDetailsInteractor
) : LayoutContainer {

    private val dateFormatter = DateFormat.getDateInstance()
    private val numberFormatter = NumberFormat.getNumberInstance(Locale.getDefault())

    fun bind(addon: Addon) {
        bindDetails(addon)
        bindAuthors(addon)
        bindVersion(addon)
        bindLastUpdated(addon)
        bindWebsite(addon)
        bindRating(addon)
    }

    private fun bindRating(addon: Addon) {
        addon.rating?.let { rating ->
            val resources = containerView.resources
            val ratingContentDescription =
                resources.getString(R.string.mozac_feature_addons_rating_content_description)
            containerView.findViewById<RatingBar>(R.id.rating_view).contentDescription = String.format(ratingContentDescription, rating.average)
            containerView.findViewById<RatingBar>(R.id.rating_view).rating = rating.average

            containerView.findViewById<TextView>(R.id.users_count).text = numberFormatter.format(rating.reviews)
        }
    }

    private fun bindWebsite(addon: Addon) {
        containerView.findViewById<TextView>(R.id.home_page_label).setOnClickListener {
            interactor.openWebsite(addon.siteUrl.toUri())
        }
    }

    private fun bindLastUpdated(addon: Addon) {
        containerView.findViewById<TextView>(R.id.last_updated_text).text = dateFormatter.format(addon.updatedAtDate)
    }

    private fun bindVersion(addon: Addon) {
        var version = addon.installedState?.version
        if (version.isNullOrEmpty()) {
            version = addon.version
        }
        containerView.findViewById<TextView>(R.id.version_text).text = version

        if (addon.isInstalled()) {
            containerView.findViewById<TextView>(R.id.version_text).setOnLongClickListener {
                interactor.showUpdaterDialog(addon)
                true
            }
        } else {
            containerView.findViewById<TextView>(R.id.version_text).setOnLongClickListener(null)
        }
    }

    private fun bindAuthors(addon: Addon) {
        containerView.findViewById<TextView>(R.id.author_text).text = addon.authors.joinToString { author -> author.name }.trim()
    }

    private fun bindDetails(addon: Addon) {
        val detailsText = addon.translateDescription(containerView.context)

        val parsedText = detailsText.replace("\n", "<br/>")
        val text = HtmlCompat.fromHtml(parsedText, HtmlCompat.FROM_HTML_MODE_COMPACT)

        val spannableStringBuilder = SpannableStringBuilder(text)
        val links = spannableStringBuilder.getSpans<URLSpan>()
        for (link in links) {
            addActionToLinks(spannableStringBuilder, link)
        }
        containerView.findViewById<TextView>(R.id.details).text = spannableStringBuilder
        containerView.findViewById<TextView>(R.id.details).movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addActionToLinks(
        spannableStringBuilder: SpannableStringBuilder,
        link: URLSpan
    ) {
        val start = spannableStringBuilder.getSpanStart(link)
        val end = spannableStringBuilder.getSpanEnd(link)
        val flags = spannableStringBuilder.getSpanFlags(link)
        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                view.setOnClickListener {
                    interactor.openWebsite(link.url.toUri())
                }
            }
        }
        spannableStringBuilder.setSpan(clickable, start, end, flags)
        spannableStringBuilder.removeSpan(link)
    }
}
