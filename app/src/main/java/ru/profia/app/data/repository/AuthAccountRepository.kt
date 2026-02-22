package ru.profia.app.data.repository

import ru.profia.app.data.local.datastore.PreferencesDataStore
import ru.profia.app.data.remote.AuthAccountApi
import ru.profia.app.data.remote.AuthSession
import ru.profia.app.data.remote.ResetPasswordRequestResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthAccountRepository @Inject constructor(
    private val authAccountApi: AuthAccountApi,
    private val preferencesDataStore: PreferencesDataStore
) {
    /** Вход по номеру телефона и паролю. */
    suspend fun login(login: String, password: String): Result<AuthSession> {
        val result = authAccountApi.login(login, password)
        result.getOrNull()?.let { session ->
            if (!session.isDemo) preferencesDataStore.setAuthSession(session)
        }
        return result
    }

    /** Регистрация по номеру телефона и паролю. */
    suspend fun register(login: String, password: String): Result<AuthSession> {
        val result = authAccountApi.register(login, password)
        result.getOrNull()?.let { session ->
            if (!session.isDemo) preferencesDataStore.setAuthSession(session)
        }
        return result
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> =
        authAccountApi.changePassword(oldPassword, newPassword)

    suspend fun setTwoFaEnabled(enabled: Boolean): Result<Unit> =
        authAccountApi.setTwoFaEnabled(enabled)

    suspend fun deleteAccount(): Result<Unit> =
        authAccountApi.deleteAccount()

    suspend fun requestResetPassword(loginOrEmail: String): Result<ResetPasswordRequestResult> =
        authAccountApi.requestResetPassword(loginOrEmail)

    suspend fun resetPassword(token: String, newPassword: String): Result<Unit> =
        authAccountApi.resetPassword(token, newPassword)

    suspend fun logout() {
        preferencesDataStore.clearAuthSession()
    }

    suspend fun hasSession(): Boolean = preferencesDataStore.hasAuthSession()
}
