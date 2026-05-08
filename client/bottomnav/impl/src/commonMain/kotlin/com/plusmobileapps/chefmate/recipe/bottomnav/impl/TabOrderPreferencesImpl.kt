package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.bottomnav.BOTTOM_NAV_TAB_ORDER_KEY
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.DEFAULT_TAB_ORDER
import com.plusmobileapps.chefmate.recipe.bottomnav.TabOrderPreferences
import com.plusmobileapps.chefmate.recipe.bottomnav.stableId
import com.plusmobileapps.chefmate.recipe.bottomnav.tabFromStableId
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TabOrderPreferencesImpl(private val settings: Settings) : TabOrderPreferences {

    private val _tabOrder = MutableStateFlow(readPersisted())

    override val tabOrder: StateFlow<List<BottomNavBloc.Tab>> = _tabOrder.asStateFlow()

    override fun setTabOrder(tabs: List<BottomNavBloc.Tab>) {
        val normalized = normalize(tabs)
        settings.putString(
            BOTTOM_NAV_TAB_ORDER_KEY,
            normalized.joinToString(SEPARATOR) { it.stableId },
        )
        _tabOrder.value = normalized
    }

    private fun readPersisted(): List<BottomNavBloc.Tab> {
        val raw = settings.getStringOrNull(BOTTOM_NAV_TAB_ORDER_KEY) ?: return DEFAULT_TAB_ORDER
        if (raw.isBlank()) return DEFAULT_TAB_ORDER
        val parsed = raw.split(SEPARATOR).mapNotNull(::tabFromStableId)
        return normalize(parsed)
    }

    /**
     * Drops duplicates and unknown ids, then appends any tabs missing from the persisted list so
     * newly added tabs surface at the end without corrupting user order.
     */
    private fun normalize(tabs: List<BottomNavBloc.Tab>): List<BottomNavBloc.Tab> {
        val seen = LinkedHashSet<BottomNavBloc.Tab>()
        tabs.forEach { seen.add(it) }
        BottomNavBloc.Tab.entries.forEach { seen.add(it) }
        return seen.toList()
    }

    private companion object {
        const val SEPARATOR = ","
    }
}
