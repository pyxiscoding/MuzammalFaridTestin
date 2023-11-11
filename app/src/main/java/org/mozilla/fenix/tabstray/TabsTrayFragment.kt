/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import org.mozilla.fenix.R

class TabsTrayFragment : AppCompatDialogFragment(), TabsTrayInteractor {

    lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TabTrayDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val containerView = inflater.inflate(R.layout.fragment_tab_tray_dialog, container, false)
        view = LayoutInflater.from(containerView.context)
            .inflate(R.layout.component_tabstray2, containerView as ViewGroup, true)

        behavior = BottomSheetBehavior.from(view.findViewById<ConstraintLayout>(R.id.tab_wrapper))

        return containerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPager(view.context, this)
    }

    override fun setCurrentTrayPosition(position: Int) {
        view.findViewById<ViewPager2>(R.id.tabsTray).currentItem = position
    }

    override fun navigateToBrowser() {
        dismissAllowingStateLoss()

        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.browserFragment) {
            return
        }

        if (!navController.popBackStack(R.id.browserFragment, false)) {
            navController.navigate(R.id.browserFragment)
        }
    }

    override fun tabRemoved(sessionId: String) {
        // TODO re-implement these methods
        // showUndoSnackbarForTab(sessionId)
        // removeIfNotLastTab(sessionId)
    }

    private fun setupPager(context: Context, interactor: TabsTrayInteractor) {
        view.findViewById<ViewPager2>(R.id.tabsTray).apply {
            adapter = TrayPagerAdapter(context, interactor)
            isUserInputEnabled = false
        }

        view.findViewById<TabLayout>(R.id.tab_layout).addOnTabSelectedListener(TabLayoutObserver(interactor))
    }
}

/**
 * An observer for the [TabLayout] used for the Tabs Tray.
 */
internal class TabLayoutObserver(
    private val interactor: TabsTrayInteractor
) : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab) {
        interactor.setCurrentTrayPosition(tab.position)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) = Unit
    override fun onTabReselected(tab: TabLayout.Tab) = Unit
}
