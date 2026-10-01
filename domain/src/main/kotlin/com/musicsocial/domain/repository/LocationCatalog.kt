package com.musicsocial.domain.repository

import com.musicsocial.domain.model.Country

/** Lista cerrada de países → regiones → ciudades para los selectores del perfil. */
interface LocationCatalog {
    suspend fun countries(): List<Country>
    suspend fun regions(countryCode: String): List<String>
    suspend fun cities(countryCode: String, region: String): List<String>

    /** Región a la que pertenece una ciudad guardada, o null si no está en el catálogo. */
    suspend fun regionOf(countryCode: String, city: String): String?
}
