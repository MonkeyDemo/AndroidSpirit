package com.monkeydemo.androidspirit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import com.monkeydemo.androidspirit.base.BaseActivity
import com.monkeydemo.androidspirit.databinding.ActivityMainBinding
import com.monkeydemo.androidspirit.databinding.ViewFloatBinding
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.MoveType
import com.yhao.floatwindow.PermissionListener
import com.yhao.floatwindow.Screen
import com.yhao.floatwindow.ViewStateListener

class MainActivity : BaseActivity<ActivityMainBinding>(),ViewStateListener, PermissionListener {
    override fun initData(savedInstanceState: Bundle?) {
        val view = ViewFloatBinding.inflate(LayoutInflater.from(applicationContext), null, false)
        FloatWindow
            .with(getApplicationContext())
            .setTag("global")
            .setView(view.root)
            .setX(100) //设置控件初始位置
            .setY(Screen.height,0.3f)
            .setDesktopShow(true)                        //桌面显示
            .setViewStateListener(this)    //监听悬浮控件状态改变
            .setPermissionListener(this)  //监听权限申请结果
            .setMoveType(MoveType.slide)
            .setMoveStyle(500, AccelerateInterpolator())  //贴边动画时长为500ms，加速插值器
            .build()
    }

    override fun initViewId() = R.layout.activity_main
    override fun onPositionUpdate(x: Int, y: Int) {
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun onDismiss() {
    }

    override fun onMoveAnimStart() {
    }

    override fun onMoveAnimEnd() {
    }

    override fun onBackToDesktop() {
    }

    override fun onSuccess() {

    }

    override fun onFail() {

    }
}