/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.collections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.CollectionTabListRowBinding
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.loadIntoView
import org.mozilla.fenix.home.Tab
import org.mozilla.fenix.utils.view.ViewHolder

class CollectionCreationTabListAdapter(
    private val interactor: CollectionCreationInteractor
) : RecyclerView.Adapter<TabViewHolder>() {

    private var tabs: List<Tab> = listOf()
    private var selectedTabs: MutableSet<Tab> = mutableSetOf()
    private var hideCheckboxes = false
    private lateinit var binding: CollectionTabListRowBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        binding = CollectionTabListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return TabViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            when (payloads[0]) {
                is CheckChanged -> {
                    val checkChanged = payloads[0] as CheckChanged
                    if (checkChanged.shouldBeChecked) {
                        binding.tabSelectedCheckbox.isChecked = true
                    } else if (checkChanged.shouldBeUnchecked) {
                        binding.tabSelectedCheckbox.isChecked = false
                    }
                    binding.tabSelectedCheckbox.isGone = checkChanged.shouldHideCheckBox
                }
            }
        }
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = tabs[position]
        val isSelected = selectedTabs.contains(tab)
        binding.tabSelectedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedTabs.add(tab)
                interactor.addTabToSelection(tab)
            } else {
                selectedTabs.remove(tab)
                interactor.removeTabFromSelection(tab)
            }
        }
        holder.bind(tab, isSelected, hideCheckboxes)
    }

    override fun getItemCount(): Int = tabs.size

    fun updateData(tabs: List<Tab>, selectedTabs: Set<Tab>, hideCheckboxes: Boolean = false) {
        val diffUtil = DiffUtil.calculateDiff(
            TabDiffUtil(
                this.tabs,
                tabs,
                this.selectedTabs,
                selectedTabs,
                this.hideCheckboxes,
                hideCheckboxes
            )
        )

        this.tabs = tabs
        this.selectedTabs = selectedTabs.toMutableSet()
        this.hideCheckboxes = hideCheckboxes

        diffUtil.dispatchUpdatesTo(this)
    }
}

class TabViewHolder(view: View) : ViewHolder(view) {

    val hostname = view.findViewById<TextView>(R.id.hostname)
    val tab_title = view.findViewById<TextView>(R.id.tab_title)
    val tab_selected_checkbox = view.findViewById<CheckBox>(R.id.tab_selected_checkbox)
    val favicon_image = view.findViewById<ImageView>(R.id.favicon_image)

    init {
        view.findViewById<CardView>(R.id.collection_item_tab).setOnClickListener {
            tab_selected_checkbox.isChecked = !tab_selected_checkbox.isChecked
        }
    }

    fun bind(tab: Tab, isSelected: Boolean, shouldHideCheckBox: Boolean) {
        hostname.text = tab.hostname
        tab_title.text = tab.title
        tab_selected_checkbox.isInvisible = shouldHideCheckBox
        itemView.isClickable = !shouldHideCheckBox
        if (tab_selected_checkbox.isChecked != isSelected) {
            tab_selected_checkbox.isChecked = isSelected
        }

        itemView.context.components.core.icons.loadIntoView(favicon_image, tab.url)
    }
}
