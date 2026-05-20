package com.lavozapp

import android.app.Application
import com.lavozapp.data.repository.AuthRepository

class LavozApp : Application() {
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        authRepository = AuthRepository(this)
    }

    companion object {
        lateinit var instance: LavozApp
            private set
    }
}
