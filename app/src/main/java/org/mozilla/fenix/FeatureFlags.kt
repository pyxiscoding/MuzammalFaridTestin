/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix

/**
 * A single source for setting feature flags that are mostly based on build type.
 */
/*
* log alifhasnain
* all that flags that were for only nightly or debug build will now also
* work for release build as conditions are added for release build
* */
object FeatureFlags {
    /**
     * Pull-to-refresh allows you to pull the web content down far enough to have the page to
     * reload.
     */
    val pullToRefreshEnabled = Config.channel.isNightlyOrDebug || Config.channel.isRelease

    /**
     * Enables the Nimbus experiments library.
     */
    const val nimbusExperiments = true

    /**
     * Enables the Addresses autofill feature.
     */
    val addressesFeature = Config.channel.isNightlyOrDebug || Config.channel.isRelease

    /**
     * Enables the Credit Cards autofill feature.
     */
    val creditCardsFeature = Config.channel.isNightlyOrDebug || Config.channel.isRelease

    /**
     * Enables WebAuthn support.
     */
    val webAuthFeature = Config.channel.isNightlyOrDebug || Config.channel.isRelease

    /**
     * Shows new three-dot toolbar menu design.
     */
    val toolbarMenuFeature = Config.channel.isDebug || Config.channel.isRelease

    /**
     * Enables the tabs tray re-write with Synced Tabs.
     */
    val tabsTrayRewrite = Config.channel.isDebug || Config.channel.isRelease

    /**
     * Enables the updated icon set look and feel.
     */
    val newIconSet = Config.channel.isNightlyOrDebug || Config.channel.isRelease
}
