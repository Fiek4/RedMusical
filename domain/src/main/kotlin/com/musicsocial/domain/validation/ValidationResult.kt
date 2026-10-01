package com.musicsocial.domain.validation

/** Resultado de validar un formulario: un error (el primero) por campo. */
data class ValidationResult(val errors: Map<Field, ValidationError> = emptyMap()) {
    val isValid: Boolean get() = errors.isEmpty()

    fun errorFor(field: Field): ValidationError? = errors[field]

    companion object {
        val Valid = ValidationResult()
    }
}

/** Acumula errores; solo guarda el primero de cada campo. */
class ValidationBuilder {
    private val errors = linkedMapOf<Field, ValidationError>()

    fun check(field: Field, error: ValidationError?) {
        if (error != null && field !in errors) errors[field] = error
    }

    /** Ejecuta las reglas en orden y se queda con el primer error. */
    fun check(field: Field, vararg rules: () -> ValidationError?) {
        if (field in errors) return
        rules.firstNotNullOfOrNull { it() }?.let { errors[field] = it }
    }

    fun build() = ValidationResult(errors.toMap())
}

inline fun validate(block: ValidationBuilder.() -> Unit): ValidationResult =
    ValidationBuilder().apply(block).build()
