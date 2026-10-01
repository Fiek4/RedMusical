package androidx.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/*
 * Imitación mínima de androidx.lifecycle.ViewModel para poder compilar y
 * probar los ViewModels fuera de Android. Tiene la misma API que usamos
 * (ViewModel, viewModelScope, onCleared), así el código de los ViewModels
 * no cambia al pasar a Android Studio con la librería real.
 */
abstract class ViewModel {
    internal val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    protected open fun onCleared() {}

    /** En Android lo llama el framework; aquí lo llaman las pruebas. */
    fun clear() {
        scope.cancel()
        onCleared()
    }
}

val ViewModel.viewModelScope: CoroutineScope
    get() = scope
