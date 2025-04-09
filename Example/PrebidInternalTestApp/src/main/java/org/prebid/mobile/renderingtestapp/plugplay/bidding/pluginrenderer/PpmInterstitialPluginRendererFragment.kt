/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.renderingtestapp.plugplay.bidding.pluginrenderer

import android.os.Bundle
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment
import org.prebid.mobile.renderingtestapp.utils.CommandLineArgumentParser
import org.prebid.mobile.renderingtestapp.utils.SampleCustomRenderer
import java.util.*

open class PpmInterstitialPluginRendererFragment : BaseBidInterstitialFragment() {
    private val TAG = PpmInterstitialPluginRendererFragment::class.java.simpleName
    private val samplePluginRenderer = SampleCustomRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PrebidMobile.registerPluginRenderer(samplePluginRenderer)
    }

    override fun initInterstitialAd(adUnitFormat: AdUnitFormat, adUnitId: String?,
                                    configId: String?, width: Int, height: Int) {
        interstitialAdUnit = if (adUnitFormat == AdUnitFormat.VIDEO) {
            InterstitialAdUnit(
                requireContext(),
                configId,
                EnumSet.of(adUnitFormat)
            )
        } else {
            InterstitialAdUnit(requireContext(), configId)
        }
        interstitialAdUnit?.setInterstitialAdUnitListener(this)
        interstitialAdUnit?.setMinSizePercentage(AdSize(30, 30))
    }

    override fun onDestroy() {
        PrebidMobile.unregisterPluginRenderer(samplePluginRenderer)
        super.onDestroy()
    }
}