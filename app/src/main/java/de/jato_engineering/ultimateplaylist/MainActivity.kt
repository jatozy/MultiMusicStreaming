package de.jato_engineering.ultimateplaylist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.SpotifyHttpManager
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest
import java.net.URI
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture


lateinit var spotifyApi: SpotifyApi
lateinit var codeChallenge: String
lateinit var codeVerifier: String

fun getSpotifyPkceCodeChallenge(codeVerifier: String): String {
    val sha256 = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray())
    return Base64.encodeToString(sha256, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val action: String? = intent?.action

        if (action == "android.intent.action.MAIN") {
            val connect: Button = findViewById<Button>(R.id.execute_connect)
            connect.setOnClickListener {
                val debugOutput: TextView = findViewById(R.id.debug_output)

                try {
                    codeVerifier = getString(R.string.spotify_code_verifier)
                    codeChallenge = getSpotifyPkceCodeChallenge(codeVerifier) // helper method
                    spotifyApi = SpotifyApi.Builder()
                            .setClientId(getString(R.string.spotify_client_id))
                            .setRedirectUri(SpotifyHttpManager.makeUri(getString(R.string.spotify_redirect_uri)))
                            .build()
                    val authorizationCodeUriRequest: AuthorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(codeChallenge)
                            .build();

                    val uriFuture = authorizationCodeUriRequest.executeAsync()
                    val uri: URI = uriFuture.join()
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(uri.toString())
                    startActivity(openURL)

                } catch (e: Exception) {
                    val text: String = debugOutput.text.toString()
                    debugOutput.text = text + "\n\n" + "Error: " + e.toString()
                }
            }
        } else if (action == "android.intent.action.VIEW") {
            val debugOutput: TextView = findViewById(R.id.debug_output)
            try {
                val receivedRedirectResult: Uri? = intent?.data
                val securedRedirectResult: URI = URI(receivedRedirectResult.toString())
                val code: String? = securedRedirectResult.query.replace("code=", "")
                var text: String = debugOutput.text.toString()
                debugOutput.text = text + "\n\n" + "URI: >>" + securedRedirectResult.toString() + "<<\n\nCode >>" + code + "<<\n\nChallenge >>" + codeChallenge + "<<\n\nVerifier >>" + codeVerifier + "<<"

                var authorizationCodePKCERequest: AuthorizationCodePKCERequest = spotifyApi.authorizationCodePKCE(code, codeVerifier)
                        .build();

                val authorizationCodeCredentialsFuture: CompletableFuture<AuthorizationCodeCredentials> = authorizationCodePKCERequest.executeAsync()
                // Example Only. Never block in production code.
                val authorizationCodeCredentials: AuthorizationCodeCredentials = authorizationCodeCredentialsFuture.join()

                // Set access and refresh token for further "spotifyApi" object usage
                spotifyApi.accessToken = authorizationCodeCredentials.accessToken
                spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken

                text = debugOutput.text.toString()
                debugOutput.text = text + "\n\n" + "Expires in >>" + authorizationCodeCredentials.expiresIn.toString() + "<<"
            } catch (e: java.lang.Exception) {
                val text: String = debugOutput.text.toString()
                debugOutput.text = text + "\n\n" + "Java Exception >>" + e.toString() + "<<"
            } catch (e: Exception) {
                val text: String = debugOutput.text.toString()
                debugOutput.text = text + "\n\n" + "Kotlin Exception >>" + e.toString() + "<<"
            } catch (t: Throwable) {
                val text: String = debugOutput.text.toString()
                debugOutput.text = text + "\n\n" + "Throwable >>" + t.toString() + "<<"
            }
        }
    }
}
