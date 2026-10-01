package com.musicsocial.data.location

import com.musicsocial.domain.model.Country
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextLocationCatalogTest {

    private val text = """
        #CL|Chile
        @Región Metropolitana de Santiago
        Puente Alto
        Santiago
        @Valparaíso
        Valparaíso
        Viña del Mar

        #PE|Perú
        @Lima
        Lima
        Miraflores
    """.trimIndent()

    private var reads = 0
    private val catalog = TextLocationCatalog { reads++; text }

    @Test
    fun `lee paises, regiones y ciudades en orden`() = runTest {
        assertEquals(listOf(Country("CL", "Chile"), Country("PE", "Perú")), catalog.countries())
        assertEquals(listOf("Región Metropolitana de Santiago", "Valparaíso"), catalog.regions("CL"))
        assertEquals(listOf("Valparaíso", "Viña del Mar"), catalog.cities("CL", "Valparaíso"))
        assertEquals(listOf("Lima", "Miraflores"), catalog.cities("PE", "Lima"))
    }

    @Test
    fun `encuentra la region de una ciudad guardada`() = runTest {
        assertEquals("Valparaíso", catalog.regionOf("CL", "Viña del Mar"))
        assertNull(catalog.regionOf("CL", "Ciudad inventada"))
        assertNull(catalog.regionOf("XX", "Lima"))
    }

    @Test
    fun `codigos desconocidos devuelven listas vacias`() = runTest {
        assertEquals(emptyList(), catalog.regions("XX"))
        assertEquals(emptyList(), catalog.cities("CL", "No existe"))
    }

    @Test
    fun `el texto se lee una sola vez`() = runTest {
        catalog.countries()
        catalog.regions("CL")
        catalog.cities("PE", "Lima")
        assertEquals(1, reads)
    }
}
