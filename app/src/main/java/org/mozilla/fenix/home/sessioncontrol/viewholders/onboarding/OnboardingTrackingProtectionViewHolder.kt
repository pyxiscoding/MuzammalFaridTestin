/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.onboarding

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.Event.OnboardingTrackingProtection.Setting
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.onboarding.OnboardingRadioButton
import org.mozilla.fenix.utils.view.addToRadioGroup


class OnboardingTrackingProtectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var standard: OnboardingRadioButton
    private var strict: OnboardingRadioButton
    private var trackingProtectionToggle: SwitchCompat
    private var description_text: TextView

    init {
        //this icon is set on XML now
//        view.header_text.setOnboardingIcon(R.drawable.ic_onboarding_tracking_protection)

        val view = LayoutInflater.from(view.context)
            .inflate(R.layout.onboarding_tracking_protection, view as ViewGroup, false)


        trackingProtectionToggle = view.findViewById(R.id.tracking_protection_toggle)
        standard = view.findViewById(R.id.tracking_protection_standard_option)
        strict = view.findViewById(R.id.tracking_protection_strict_default)
        description_text = view.findViewById(R.id.description_text)

        description_text.text = view.context.getString(
                R.string.onboarding_tracking_protection_description_2
        )

        trackingProtectionToggle.apply {
            isChecked = view.context.settings().shouldUseTrackingProtection
            setOnCheckedChangeListener { _, isChecked ->
                updateTrackingProtectionSetting(isChecked)
                updateRadioGroupState(isChecked)
            }
        }

        setupRadioGroup(trackingProtectionToggle.isChecked)
        updateRadioGroupState(trackingProtectionToggle.isChecked)
    }

    private fun setupRadioGroup(isChecked: Boolean) {

        updateRadioGroupState(isChecked)

        addToRadioGroup(standard, strict)

        strict.isChecked =
                itemView.context.settings().useStrictTrackingProtection
        standard.isChecked =
                !itemView.context.settings().useStrictTrackingProtection

        standard.onClickListener {
            updateTrackingProtectionPolicy()
            itemView.context.components.analytics.metrics
                    .track(Event.OnboardingTrackingProtection(Setting.STANDARD))
        }

        strict.onClickListener {
            updateTrackingProtectionPolicy()
            itemView.context.components.analytics.metrics
                    .track(Event.OnboardingTrackingProtection(Setting.STRICT))
        }
    }

    private fun updateRadioGroupState(isChecked: Boolean) {
        standard.isEnabled = isChecked
        strict.isEnabled = isChecked
    }

    private fun updateTrackingProtectionSetting(enabled: Boolean) {
        itemView.context.settings().shouldUseTrackingProtection = enabled
        with(itemView.context.components) {
            val policy = core.trackingProtectionPolicyFactory.createTrackingProtectionPolicy()
            useCases.settingsUseCases.updateTrackingProtection.invoke(policy)
            useCases.sessionUseCases.reload.invoke()
        }
    }

    private fun updateTrackingProtectionPolicy() {
        itemView.context?.components?.let {
            val policy = it.core.trackingProtectionPolicyFactory
                    .createTrackingProtectionPolicy()
            it.useCases.settingsUseCases.updateTrackingProtection.invoke(policy)
            it.useCases.sessionUseCases.reload.invoke()
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.onboarding_tracking_protection
    }
}
