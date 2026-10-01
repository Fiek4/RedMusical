package com.musicsocial.domain.usecase

class NotLoggedInException : IllegalStateException("No hay sesión iniciada")

class NotFoundException(what: String) : NoSuchElementException("$what no encontrado")

class NotAllowedException(reason: String) : IllegalStateException(reason)

class EmailAlreadyRegisteredException : IllegalStateException("El email ya está registrado")

class InvalidCredentialsException : IllegalStateException("Email o contraseña incorrectos")
