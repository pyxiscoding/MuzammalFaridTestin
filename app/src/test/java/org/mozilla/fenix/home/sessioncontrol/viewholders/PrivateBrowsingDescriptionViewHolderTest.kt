/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders

import android.view.LayoutInflater
import android.view.View
import io.mockk.mockk
import io.mockk.verify
import kotlinx.android.synthetic.main.private_browsing_description.view.*
import mozilla.components.support.test.robolectric.testContext
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.helpers.FenixRobolectricTestRunner
import org.mozilla.fenix.home.sessioncontrol.TabSessionInteractor

@RunWith(FenixRobolectricTestRunner::class)
class PrivateBrowsingDescriptionViewHolderTest {

    private lateinit var view: View
    private lateinit var interactor: TabSessionInteractor

    @Before
    fun setup() {
        view = LayoutInflater.from(testContext)
            .inflate(PrivateBrowsingDescriptionViewHolder.LAYOUT_ID, null)
        interactor = mockk(relaxed = true)
    }

    @Test
    fun `call interactor on click`() {
        PrivateBrowsingDescriptionViewHolder(view, interactor)

        view.private_session_description.performClick()
        verify { interactor.onPrivateBrowsingLearnMoreClicked() }
    }
}
