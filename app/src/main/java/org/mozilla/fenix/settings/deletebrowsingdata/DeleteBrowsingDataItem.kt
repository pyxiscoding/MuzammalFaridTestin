/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.deletebrowsingdata

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import org.mozilla.fenix.R

class DeleteBrowsingDataItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private companion object {
        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.6f
    }

    val titleView: TextView
        get() = findViewById(R.id.title)

    val subtitleView: TextView
        get() = findViewById(R.id.subtitle)

    val checkbox: CheckBox
        get() = findViewById(R.id.checkbox)

    var isChecked: Boolean
        get() = checkbox.isChecked
        set(value) { checkbox.isChecked = value }

    var onCheckListener: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.delete_browsing_data_item, this, true)

        setOnClickListener {
            checkbox.isChecked = !checkbox.isChecked
        }

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckListener?.invoke(isChecked)
        }

        context.withStyledAttributes(attrs, R.styleable.DeleteBrowsingDataItem, defStyleAttr, 0) {
            val titleId = getResourceId(
                R.styleable.DeleteBrowsingDataItem_deleteBrowsingDataItemTitle,
                R.string.browser_menu_library
            )
            val subtitleId = getResourceId(
                R.styleable.DeleteBrowsingDataItem_deleteBrowsingDataItemSubtitle,
                R.string.empty_string
            )

            findViewById<TextView>(R.id.title).text = resources.getString(titleId)
            val subtitleText = resources.getString(subtitleId)
            findViewById<TextView>(R.id.subtitle).text = subtitleText
            if (subtitleText.isBlank()) findViewById<TextView>(R.id.subtitle).visibility = View.GONE
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
    }
}
