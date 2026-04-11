package com.io.dronecontroller.ui.login

import com.io.dronecontroller.domain.model.RunStatus
import com.io.dronecontroller.domain.usecase.LoginUseCaseContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var fakeUseCase: FakeLoginUseCase
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fakeUseCase = FakeLoginUseCase()
        viewModel = LoginViewModel(fakeUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `空入力でログインするとバリデーションエラーになる`() = runTest(dispatcher) {
        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("ユーザー名とパスワードを入力してください", state.errorMessage)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun `ログイン成功時はisLoggedInがtrueになる`() = runTest(dispatcher) {
        fakeUseCase.result = RunStatus.Success(Unit)
        viewModel.updateUsername("demo")
        viewModel.updatePassword("demo123")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertEquals(null, state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `ログイン失敗時はエラーメッセージを表示する`() = runTest(dispatcher) {
        fakeUseCase.result = RunStatus.Error("認証失敗")
        viewModel.updateUsername("wrong")
        viewModel.updatePassword("wrong")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoggedIn)
        assertEquals("認証失敗", state.errorMessage)
        assertFalse(state.isLoading)
    }
}

private class FakeLoginUseCase : LoginUseCaseContract {
    var result: RunStatus<Unit> = RunStatus.Success(Unit)

    override suspend fun invoke(username: String, password: String): RunStatus<Unit> = result
}

