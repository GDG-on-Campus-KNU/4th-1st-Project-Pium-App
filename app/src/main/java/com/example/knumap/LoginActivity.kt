package com.example.knumap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.knumap.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import java.security.MessageDigest
import android.util.Base64
import com.example.knumap.model.KakaoLoginRequest
import com.example.knumap.model.KakaoLoginResponse
import com.example.knumap.network.AuthService
import com.example.knumap.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getKeyHash(this)
        // 로그인 버튼 클릭 시
        binding.kakaoLoginButton.setOnClickListener {
            loginWithKakao()
        }
    }

    private fun loginWithKakao() {
        val context = this

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡 앱 로그인
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                handleLoginResult(token, error)
            }
        } else {
            // 카카오 계정 (웹뷰) 로그인
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                handleLoginResult(token, error)
            }
        }
    }

    private fun handleLoginResult(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Log.e("KakaoLogin", "로그인 실패: ${error.localizedMessage}")
            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            val kakaoAccessToken = token.accessToken
            Log.d("KakaoLogin", "로그인 성공! accessToken: $kakaoAccessToken")
            Toast.makeText(this, "로그인 성공! 토큰 콘솔 확인", Toast.LENGTH_SHORT).show()

            // ✅ 서버에 카카오 access token 전송
            lifecycleScope.launch {
                try {
                    val request = KakaoLoginRequest(kakaoAccessToken)
                    val response = RetrofitClient.authService.kakaoLogin(request)
                    if (response.isSuccessful) {
                        Log.d("LOGIN_DEBUG", "✅ HTTP 응답 성공 (2xx)")

                        // 2. body().success 여부 로그
                        val successField = response.body()?.success
                        Log.d("LOGIN_DEBUG", "response.body()?.success == $successField")

                        if (successField == true) {
                            Log.d("LOGIN_DEBUG", "🎉 최종 조건 만족 → 로그인 성공 처리됨")
                            // 여기서 정상 처리 로직 실행
                        } else {
                            Log.e("LOGIN_DEBUG", "❗ 응답 바디 내부 success == false 또는 null")
                        }

                    } else {
                        Log.e("LOGIN_DEBUG", "❌ HTTP 응답 실패 → response.isSuccessful == false")
                        Log.e("LOGIN_DEBUG", "실패 이유: ${response.errorBody()?.string()}")
                    }
                    if (response.isSuccessful && response.body()?.success == true) {
                        val loginData = response.body()?.data
                        val jwtAccess = loginData?.jwtAccessToken.orEmpty()
                        val jwtRefresh = loginData?.refreshToken.orEmpty()
                        val accountId = loginData?.accountId ?: -1L
                        Log.d("JWT", "access_token: $jwtAccess")
                        Log.d("JWT", "refresh_token: $jwtRefresh")
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        saveJwtToken(jwtAccess, jwtRefresh,accountId) // ✅ 저장
                        // ✅ MapsActivity로 이동
                        val intent = Intent(this@LoginActivity, MapsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                    } else {
                        Log.e("JWT", "응답 실패: ${response.code()}")
                        Toast.makeText(this@LoginActivity, "서버 로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("JWT", "네트워크 오류: ${e.message}")
                    Toast.makeText(this@LoginActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun getKeyHash(context: Context): String? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners ?: return null
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            for (signature in signatures ?: emptyArray()) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("KeyHash", "keyHash: $keyHash")
                return keyHash
            }
            null
        } catch (e: Exception) {
            Log.e("KeyHash", "error: ${e.message}")
            null
        }
    }
    private fun saveJwtToken(accessToken: String, refreshToken: String, accountId: Long)  {
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putLong("account_id", accountId)
            apply()
        }
    }

}