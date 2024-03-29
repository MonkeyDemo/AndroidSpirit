package com.monkeydemo.androidspirit.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {
    lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, initViewId())
        initData(savedInstanceState)
    }

    abstract fun initData(savedInstanceState: Bundle?)

    @LayoutRes
    abstract fun initViewId(): Int
}