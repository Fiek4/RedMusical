package com.musicsocial.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/** Base para pruebas de ViewModel: reemplaza Dispatchers.Main (en Android también se hace así). */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTest {
    protected val app = TestApp()

    @BeforeTest
    fun setUpMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDownMain() = Dispatchers.resetMain()
}
