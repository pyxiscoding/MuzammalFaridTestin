/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.downloads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.button.MaterialButton
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.feature.downloads.AbstractFetchDownloadService
import mozilla.components.feature.downloads.toMegabyteOrKilobyteString
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.container.LayoutContainer
import org.mozilla.fenix.ext.metrics
import org.mozilla.fenix.ext.settings

/**
 * [DynamicDownloadDialog] is used to show a view in the current tab to the user, triggered when
 *  downloadFeature.onDownloadStopped gets invoked. It uses [DynamicDownloadDialogBehavior] to
 *  hide when the users scrolls through a website as to not impede his activities.
 */
@Suppress("LongParameterList")
class DynamicDownloadDialog(
    private val container: ViewGroup,
    private val downloadState: DownloadState?,
    private val metrics: MetricController,
    private val didFail: Boolean,
    private val tryAgain: (String) -> Unit,
    private val onCannotOpenFile: (DownloadState) -> Unit,
    private val view: View,
    private val toolbarHeight: Int,
    private val onDismiss: () -> Unit
) : LayoutContainer {

    override val containerView: View?
        get() = container

    private val settings = container.context.settings()
    var download_dialog_title: TextView = TODO()
    var download_dialog_filename: TextView = TODO()
    var download_dialog_icon: ImageView = TODO()
    var download_dialog_action_button: MaterialButton = TODO()
    var download_dialog_close_button: ImageButton= TODO()

    init {
        val view = LayoutInflater.from(container.context)
            .inflate(R.layout.download_dialog_layout, container, false)

        download_dialog_title = view.findViewById(R.id.download_dialog_title)
        download_dialog_filename = view.findViewById(R.id.download_dialog_filename)
        download_dialog_icon = view.findViewById(R.id.download_dialog_icon)
        download_dialog_action_button = view.findViewById(R.id.download_dialog_action_button)
        download_dialog_close_button = view.findViewById(R.id.download_dialog_close_button)
        setupDownloadDialog(view)
    }

    private fun setupDownloadDialog(view: View) {
        if (downloadState == null) return
        view.apply {
            if (layoutParams is CoordinatorLayout.LayoutParams) {
                (layoutParams as CoordinatorLayout.LayoutParams).apply {

                    behavior =
                        DynamicDownloadDialogBehavior<View>(
                            context,
                            null,
                            toolbarHeight.toFloat()
                        )
                }
            }
        }

        if (settings.shouldUseBottomToolbar) {
            val params: ViewGroup.MarginLayoutParams =
                view.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = toolbarHeight
        }

        if (didFail) {
            download_dialog_title.text =
                container.context.getString(R.string.mozac_feature_downloads_failed_notification_text2)

            download_dialog_icon.setImageResource(
                mozilla.components.feature.downloads.R.drawable.mozac_feature_download_ic_download_failed
            )

            download_dialog_action_button.apply {
                text = context.getString(
                    mozilla.components.feature.downloads.R.string.mozac_feature_downloads_button_try_again
                )
                setOnClickListener {
                    tryAgain(downloadState.id)
                    context.metrics.track(Event.InAppNotificationDownloadTryAgain)
                    dismiss(view)
                }
            }
        } else {
            val titleText = container.context.getString(
                R.string.mozac_feature_downloads_completed_notification_text2
            ) + " (${downloadState.contentLength?.toMegabyteOrKilobyteString()})"

            download_dialog_title.text = titleText

            download_dialog_icon.setImageResource(
                mozilla.components.feature.downloads.R.drawable.mozac_feature_download_ic_download_complete
            )

            download_dialog_action_button.apply {
                text = context.getString(
                    mozilla.components.feature.downloads.R.string.mozac_feature_downloads_button_open
                )
                setOnClickListener {
                    metrics.track(Event.DownloadsItemOpened)

                    val fileWasOpened = AbstractFetchDownloadService.openFile(
                        context = context,
                        contentType = downloadState.contentType,
                        filePath = downloadState.filePath
                    )

                    if (!fileWasOpened) {
                        onCannotOpenFile(downloadState)
                    }

                    context.metrics.track(Event.InAppNotificationDownloadOpen)
                    dismiss(view)
                }
            }
        }

        download_dialog_close_button.setOnClickListener {
            dismiss(view)
        }

        download_dialog_filename.text = downloadState.fileName
    }

    fun show() {
        view.visibility = View.VISIBLE

        (view.layoutParams as CoordinatorLayout.LayoutParams).apply {
            (behavior as DynamicDownloadDialogBehavior).forceExpand(view)
        }
    }

    private fun dismiss(view: View) {
        view.visibility = View.GONE
        onDismiss()
    }

    companion object {
        fun getCannotOpenFileErrorMessage(context: Context, download: DownloadState): String {
            val fileExt = MimeTypeMap.getFileExtensionFromUrl(
                download.filePath
            )
            return context.getString(
                R.string.mozac_feature_downloads_open_not_supported1, fileExt
            )
        }
    }
}
