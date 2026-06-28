package com.example.data.repository

import com.example.data.remote.datasource.IAuthRemoteDataSource
import com.example.domain.auth.model.User
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {

    private val remoteDataSource = mockk<IAuthRemoteDataSource>()
    private val repository = AuthRepositoryImpl(remoteDataSource, Dispatchers.Unconfined)

    @Test
    fun `loginWithEmail returns mapped User on success`() = runTest {
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "123"
            every { email } returns "test@test.com"
            every { displayName } returns "Test User"
            every { photoUrl } returns null
        }
        
        coEvery { remoteDataSource.signInWithEmail("test@test.com", "password") } returns firebaseUser

        val result = repository.loginWithEmail("test@test.com", "password")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals("123", user?.uid)
        assertEquals("test@test.com", user?.email)
        assertEquals("Test User", user?.displayName)
    }

    @Test
    fun `loginWithEmail returns failure on Exception`() = runTest {
        val exception = Exception("Firebase Error")
        coEvery { remoteDataSource.signInWithEmail("test@test.com", "password") } throws exception

        val result = repository.loginWithEmail("test@test.com", "password")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
