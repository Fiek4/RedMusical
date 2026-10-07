package com.musicsocial.presentation

import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.Limits
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.call.ApplyViewModel
import com.musicsocial.presentation.call.CreateCallViewModel
import com.musicsocial.presentation.call.ReviewApplicationsViewModel
import com.musicsocial.presentation.common.UiError
import com.musicsocial.presentation.feed.FeedViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Flujo principal del MVP: Ana publica, Luis se postula, Ana acepta y se abre el chat. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CollabFlowViewModelTest : ViewModelTest() {

    private suspend fun publishAsAna(): String {
        app.signIn("ana")
        val vm = CreateCallViewModel(app.createCall, app.clock)
        val form = validCallForm()
        vm.onTitleChange(form.title)
        vm.onLookingForChange(form.lookingFor!!)
        vm.onGenreChange(form.genre!!)
        vm.onDescriptionChange(form.description)
        vm.onBpmChange(form.bpm)
        vm.onAudioSelected(form.referenceAudio)
        vm.onDeadlineChange(form.deadline!!)
        vm.onDealTypeChange(form.dealType!!)
        vm.onPublish()
        return assertNotNull(vm.uiState.value.publishedCallId)
    }

    private suspend fun applyAsLuis(callId: String): ApplyViewModel {
        app.signIn("luis", roles = setOf(ArtistRole.RAPPER))
        val vm = ApplyViewModel(callId, app.auth, app.calls, app.applications, app.applyToCall)
        vm.onDemoSelected(audio(45))
        vm.onMessageChange("Me encanta el beat")
        vm.onSend()
        return vm
    }

    @Test
    fun `publicar sin datos muestra errores`() = runTest {
        app.signIn("ana")
        val vm = CreateCallViewModel(app.createCall, app.clock)
        vm.onPublish()
        assertEquals(ValidationError.Required, vm.uiState.value.errors[Field.TITLE])
        assertEquals(ValidationError.Required, vm.uiState.value.errors[Field.AUDIO])
    }

    @Test
    fun `plan gratis permite solo 2 convocatorias activas`() = runTest {
        repeat(Limits.FREE_MAX_ACTIVE_CALLS) { publishAsAna() }
        val vm = CreateCallViewModel(app.createCall, app.clock)
        vm.onTitleChange("Tercera")
        vm.onPublish()
        assertEquals(ValidationError.PlanLimitReached(Limits.FREE_MAX_ACTIVE_CALLS), vm.uiState.value.errors[Field.GENERAL])
    }

    @Test
    fun `el feed muestra convocatorias segun el filtro`() = runTest {
        publishAsAna()
        app.signIn("luis", roles = setOf(ArtistRole.RAPPER), genres = setOf(Genre.DRILL))
        val feed = FeedViewModel(app.auth, app.users, app.calls, app.clock)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { feed.uiState.collect {} }

        assertEquals(1, feed.uiState.value.calls.size)

        feed.onGenreToggle(Genre.DRILL) // quita drill
        feed.onGenreToggle(Genre.RNB) // agrega R&B
        assertTrue(feed.uiState.value.calls.isEmpty())
    }

    @Test
    fun `postularse y no poder repetir`() = runTest {
        val callId = publishAsAna()
        val first = applyAsLuis(callId)
        assertTrue(first.uiState.value.applied)

        val second = applyAsLuis(callId)
        assertEquals(ValidationError.AlreadyApplied, second.uiState.value.errors[Field.GENERAL])
    }

    @Test
    fun `no puedo postularme a mi propia convocatoria`() = runTest {
        val callId = publishAsAna()
        val vm = ApplyViewModel(callId, app.auth, app.calls, app.applications, app.applyToCall)
        vm.onDemoSelected(audio(45))
        vm.onSend()
        assertEquals(ValidationError.CannotApplyToOwnCall, vm.uiState.value.errors[Field.GENERAL])
    }

    @Test
    fun `el detalle oculta el formulario en mi propia convocatoria`() = runTest {
        val callId = publishAsAna()
        val vm = ApplyViewModel(callId, app.auth, app.calls, app.applications, app.applyToCall)
        assertTrue(vm.uiState.value.isOwnCall)
        assertFalse(vm.uiState.value.canApply)
    }

    @Test
    fun `el detalle recuerda que ya me postule`() = runTest {
        val callId = publishAsAna()
        applyAsLuis(callId)
        val vm = ApplyViewModel(callId, app.auth, app.calls, app.applications, app.applyToCall)
        assertTrue(vm.uiState.value.alreadyApplied)
        assertFalse(vm.uiState.value.canApply)
    }

    @Test
    fun `aceptar una postulacion abre el chat`() = runTest {
        val callId = publishAsAna()
        applyAsLuis(callId)
        app.signIn("ana")

        val vm = ReviewApplicationsViewModel(callId, app.applications, app.users, app.review)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        val application = vm.uiState.value.pending.single()
        assertEquals("Luis", vm.uiState.value.applicants["luis"]?.artistName)

        vm.onAccept(application.id)

        val state = vm.uiState.value
        assertTrue(state.pending.isEmpty())
        assertEquals(1, state.accepted.size)
        val conversation = assertNotNull(app.chats.findByApplication(application.id))
        assertEquals(conversation.id, state.openConversationId)
        assertEquals(setOf("ana", "luis"), conversation.participantIds)
    }

    @Test
    fun `solo el autor puede revisar postulaciones`() = runTest {
        val callId = publishAsAna()
        applyAsLuis(callId) // sesión queda como Luis

        val vm = ReviewApplicationsViewModel(callId, app.applications, app.users, app.review)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        vm.onReject(vm.uiState.value.pending.single().id)

        assertEquals(UiError.NOT_ALLOWED, vm.uiState.value.error)
        assertEquals(1, vm.uiState.value.pending.size)
    }
}
