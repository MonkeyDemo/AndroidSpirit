package com.monkeydemo.androidspirit.base

import android.app.Application
import com.hjq.toast.Toaster

class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 Toast 框架
        Toaster.init(this);
    }
}