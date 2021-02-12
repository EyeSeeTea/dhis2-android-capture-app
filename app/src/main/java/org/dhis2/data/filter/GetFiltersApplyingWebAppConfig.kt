package org.dhis2.data.filter

import org.dhis2.utils.filters.FilterItem
import org.hisp.dhis.android.core.settings.FilterConfig

class GetFiltersApplyingWebAppConfig {

    fun <T> execute(
        defaultFilters: LinkedHashMap<T, FilterItem>,
        webAppFilters: LinkedHashMap<T, FilterConfig>
    ): List<FilterItem> {
        val filtersToShowFromWebAppKeys = webAppFilters.filterValues { it.filter() }.keys.toList()
        val filterToShow = defaultFilters.filter { filtersToShowFromWebAppKeys.contains(it.key) }

        return filterToShow.values.toList()
    }
}