package com.traveldispatcher.app.data.supabase

import com.traveldispatcher.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.websocket.WebSocketDeflateExtension.Companion.install

object SupabaseConfig {

    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
                BuildConfig.SUPABASE_URL.startsWith("https://")

    val client: SupabaseClient? by lazy {
        if (!isConfigured) null
        else {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                httpEngine = OkHttp.create()
                install(Auth)
                install(Postgrest)
            }
        }
    }

    val auth: Auth?
        get() = client?.auth

    val postgrest: Postgrest?
        get() = client?.postgrest
}
