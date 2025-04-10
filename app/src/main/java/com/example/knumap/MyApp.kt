package com.example.knumap

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "b0f75bb70c0c18966a631d2cf9e71848") // 여기 네이티브 앱 키 입력!
    }
}