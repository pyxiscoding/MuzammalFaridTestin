/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.search

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.state.availableSearchEngines
import mozilla.components.feature.search.ext.createSearchEngine
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.components.FenixSnackbar
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.databinding.FragmentAddSearchEngineBinding
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.requireComponents
import org.mozilla.fenix.ext.showToolbar
import org.mozilla.fenix.settings.SupportUtils

@SuppressWarnings("LargeClass", "TooManyFunctions")
class AddSearchEngineFragment : Fragment(),
        CompoundButton.OnCheckedChangeListener {
    private var availableEngines: List<SearchEngine> = listOf()
    private var selectedIndex: Int = -1
    private val engineViews = mutableListOf<View>()
    lateinit var binding: FragmentAddSearchEngineBinding
    lateinit var includedView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddSearchEngineBinding.inflate(inflater, container, false)
        includedView = binding.customSearchEngin.customSearchEngineForm
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        availableEngines = requireContext()
                .components
                .core
                .store
                .state
                .search
                .availableSearchEngines

        selectedIndex = if (availableEngines.isEmpty()) CUSTOM_INDEX else FIRST_INDEX
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutInflater = LayoutInflater.from(context)
        val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        instance = this
        val setupSearchEngineItem: (Int, SearchEngine) -> Unit = { index, engine ->
            val engineId = engine.id
            val engineItem = makeButtonFromSearchEngine(
                    engine = engine,
                    layoutInflater = layoutInflater,
                    res = requireContext().resources
            )
            engineItem.id = index
            engineItem.tag = engineId
            engineItem.findViewById<RadioButton>(R.id.radio_button).isChecked = selectedIndex == index
            engineViews.add(engineItem)
            binding.searchEngineGroup.addView(engineItem, layoutParams)
        }

        availableEngines.forEachIndexed(setupSearchEngineItem)

        val engineItem = makeCustomButton(layoutInflater)
        engineItem.id = CUSTOM_INDEX
        engineItem.findViewById<RadioButton>(R.id.radio_button).isChecked = selectedIndex == CUSTOM_INDEX
        engineViews.add(engineItem)
        binding.searchEngineGroup.addView(engineItem, layoutParams)

        toggleCustomForm(selectedIndex == CUSTOM_INDEX)

        binding.customSearchEngin.customSearchEnginesLearnMore.setOnClickListener {
            (activity as HomeActivity).openToBrowserAndLoad(
                    searchTermOrURL = SupportUtils.getSumoURLForTopic(
                            requireContext(),
                            SupportUtils.SumoTopic.CUSTOM_SEARCH_ENGINES
                    ),
                    newTab = true,
                    from = BrowserDirection.FromAddSearchEngineFragment
            )
        }
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.search_engine_add_custom_search_engine_title))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_custom_searchengine_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_search_engine -> {
                when (selectedIndex) {
                    CUSTOM_INDEX -> createCustomEngine()
                    else -> {
                        val engine = availableEngines[selectedIndex]
                        requireComponents.useCases.searchUseCases.addSearchEngine(engine)
                        findNavController().popBackStack()
                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Suppress("ComplexMethod")
    private fun createCustomEngine() {
        binding.customSearchEngin.customSearchEngineNameField .error = ""
        binding.customSearchEngin.customSearchEngineSearchStringField .error = ""

        val name = binding.customSearchEngin.editEngineName.text?.toString()?.trim() ?: ""
        val searchString = binding.customSearchEngin.editSearchString.text?.toString() ?: ""

        if (checkForErrors(name, searchString)) {
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Main) {
            val result = withContext(IO) {
                SearchStringValidator.isSearchStringValid(
                        requireComponents.core.client,
                        searchString
                )
            }

            when (result) {
                SearchStringValidator.Result.CannotReach -> {
                    binding.customSearchEngin.customSearchEngineSearchStringField .error = resources
                            .getString(R.string.search_add_custom_engine_error_cannot_reach, name)
                }
                SearchStringValidator.Result.Success -> {
                    val searchEngine = createSearchEngine(
                            name,
                            searchString.toSearchUrl(),
                            requireComponents.core.icons.loadIcon(IconRequest(searchString)).await().bitmap
                    )

                    requireComponents.useCases.searchUseCases.addSearchEngine(searchEngine)

                    val successMessage = resources
                            .getString(R.string.search_add_custom_engine_success_message, name)

                    view?.also {
                        FenixSnackbar.make(
                                view = it,
                                duration = FenixSnackbar.LENGTH_SHORT,
                                isDisplayedWithBrowserToolbar = false
                        )
                                .setText(successMessage)
                                .show()
                    }

                    context?.components?.analytics?.metrics?.track(Event.CustomEngineAdded)
                    findNavController().popBackStack()
                }
            }
        }
    }

    fun checkForErrors(name: String, searchString: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.customSearchEngin.customSearchEngineNameField .error = resources
                        .getString(R.string.search_add_custom_engine_error_empty_name)
                true
            }
            searchString.isEmpty() -> {
                binding.customSearchEngin.customSearchEngineSearchStringField.error =
                        resources.getString(R.string.search_add_custom_engine_error_empty_search_string)
                true
            }
            !searchString.contains("%s") -> {
                binding.customSearchEngin.customSearchEngineSearchStringField.error =
                        resources.getString(R.string.search_add_custom_engine_error_missing_template)
                true
            }
            else -> false
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        engineViews.forEach {
            when (it.findViewById<RadioButton>(R.id.radio_button) == buttonView) {
                true -> {
                    selectedIndex = it.id
                }
                false -> {
                    it.findViewById<RadioButton>(R.id.radio_button).setOnCheckedChangeListener(null)
                    it.findViewById<RadioButton>(R.id.radio_button).isChecked = false
                    it.findViewById<RadioButton>(R.id.radio_button).setOnCheckedChangeListener(this)
                }
            }
        }

        toggleCustomForm(selectedIndex == -1)
    }

    private fun makeCustomButton(layoutInflater: LayoutInflater): View {
        val wrapper = layoutInflater
                .inflate(R.layout.custom_search_engine_radio_button, null) as ConstraintLayout
        wrapper.setOnClickListener { wrapper.findViewById<RadioButton>(R.id.radio_button).isChecked = true }
        wrapper.findViewById<RadioButton>(R.id.radio_button).setOnCheckedChangeListener(this)
        return wrapper
    }

    private fun toggleCustomForm(isEnabled: Boolean) {
        binding.customSearchEngin.customSearchEngineForm.alpha = if (isEnabled) ENABLED_ALPHA else DISABLED_ALPHA
        binding.customSearchEngin.editSearchString.isEnabled = isEnabled
        binding.customSearchEngin.editEngineName .isEnabled = isEnabled
        binding.customSearchEngin.customSearchEnginesLearnMore.isEnabled = isEnabled
    }

    private fun makeButtonFromSearchEngine(
            engine: SearchEngine,
            layoutInflater: LayoutInflater,
            res: Resources
    ): View {
        val wrapper = layoutInflater
                .inflate(R.layout.search_engine_radio_button, null) as LinearLayout
        wrapper.setOnClickListener { wrapper.findViewById<RadioButton>(R.id.radio_button).isChecked = true }
        wrapper.findViewById<RadioButton>(R.id.radio_button).setOnCheckedChangeListener(this)
        wrapper.findViewById<TextView>(R.id.engine_text).text = engine.name
        val iconSize = res.getDimension(R.dimen.preference_icon_drawable_size).toInt()
        val engineIcon = BitmapDrawable(res, engine.icon)
        engineIcon.setBounds(0, 0, iconSize, iconSize)
        wrapper.findViewById<ImageView>(R.id.engine_icon).setImageDrawable(engineIcon)
        wrapper.findViewById<ImageButton>(R.id.overflow_menu).visibility = View.GONE
        return wrapper
    }

    companion object {
        private const val ENABLED_ALPHA = 1.0f
        private const val DISABLED_ALPHA = 0.2f
        private const val CUSTOM_INDEX = -1
        private const val FIRST_INDEX = 5
        @SuppressLint("StaticFieldLeak")
        var instance: AddSearchEngineFragment? = null
    }
}

private fun String.toSearchUrl(): String {
    return replace("%s", "{searchTerms}")
}
