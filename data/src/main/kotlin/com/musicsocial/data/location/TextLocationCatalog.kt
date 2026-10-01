package com.musicsocial.data.location

import com.musicsocial.domain.model.Country
import com.musicsocial.domain.repository.LocationCatalog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Catálogo leído de un texto con este formato (ver app/src/main/assets/locations.txt):
 *
 * ```
 * #CL|Chile
 * @Región Metropolitana de Santiago
 * Santiago
 * Puente Alto
 * ```
 *
 * `#` abre un país, `@` una región y cada línea siguiente es una ciudad.
 * El texto se lee una sola vez, la primera vez que se pide algo.
 */
class TextLocationCatalog(private val source: suspend () -> String) : LocationCatalog {

    private class Entry(val country: Country, val regions: Map<String, List<String>>)

    private val mutex = Mutex()
    private var cache: Map<String, Entry>? = null

    private suspend fun entries(): Map<String, Entry> = mutex.withLock {
        cache ?: parse(source()).also { cache = it }
    }

    override suspend fun countries() = entries().values.map { it.country }

    override suspend fun regions(countryCode: String) =
        entries()[countryCode]?.regions?.keys?.toList().orEmpty()

    override suspend fun cities(countryCode: String, region: String) =
        entries()[countryCode]?.regions?.get(region).orEmpty()

    override suspend fun regionOf(countryCode: String, city: String): String? =
        entries()[countryCode]?.regions?.entries?.firstOrNull { city in it.value }?.key

    private fun parse(text: String): Map<String, Entry> {
        val result = LinkedHashMap<String, Entry>()
        var regions: MutableMap<String, List<String>>? = null
        var cities: MutableList<String>? = null
        text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            when (line[0]) {
                '#' -> {
                    val (code, name) = line.substring(1).split('|', limit = 2)
                    val countryRegions = LinkedHashMap<String, List<String>>()
                    result[code] = Entry(Country(code, name), countryRegions)
                    regions = countryRegions
                    cities = null
                }
                '@' -> {
                    val regionCities = mutableListOf<String>()
                    regions?.put(line.substring(1), regionCities)
                    cities = regionCities
                }
                else -> cities?.add(line)
            }
        }
        return result
    }
}
