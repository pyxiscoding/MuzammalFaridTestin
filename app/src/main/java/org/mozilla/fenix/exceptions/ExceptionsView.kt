/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.exceptions

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R
import org.mozilla.fenix.container.LayoutContainer

/**
 * View that contains and configures the Exceptions List
 */
abstract class ExceptionsView<T : Any>(
    container: ViewGroup,
    protected val interactor: ExceptionsInteractor<T>
) : LayoutContainer {

    var exceptions_learn_more: TextView
    var exceptions_list : RecyclerView
     var exceptions_empty_view : TextView
    final override val containerView: FrameLayout = LayoutInflater.from(container.context)
        .inflate(R.layout.component_exceptions, container, true)
        .findViewById(R.id.exceptions_wrapper)

    protected abstract val exceptionsAdapter: ExceptionsAdapter<T>

    init {
        containerView.findViewById<RecyclerView>(R.id.exceptions_list).apply {
            layoutManager = LinearLayoutManager(containerView.context)
        }

        exceptions_list = containerView.findViewById(R.id.exceptions_list)
        exceptions_empty_view = containerView.findViewById(R.id.exceptions_empty_view)
        exceptions_learn_more = containerView.findViewById(R.id.exceptions_learn_more)
    }

    fun update(items: List<T>) {
        exceptions_empty_view.isVisible = items.isEmpty()
        exceptions_list.isVisible = items.isNotEmpty()
        exceptionsAdapter.updateData(items)
    }
}
