package com.monkeydemo.androidspirit

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.hjq.toast.Toaster
import com.monkeydemo.androidspirit.base.BaseActivity
import com.monkeydemo.androidspirit.base.ClickEvent
import com.monkeydemo.androidspirit.base.MessageEvent
import com.monkeydemo.androidspirit.databinding.ActivityMainBinding
import com.monkeydemo.androidspirit.databinding.ViewFloatBinding
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.MoveType
import com.yhao.floatwindow.PermissionListener
import com.yhao.floatwindow.Screen
import com.yhao.floatwindow.ViewStateListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Random


class MainActivity : BaseActivity<ActivityMainBinding>(), ViewStateListener, PermissionListener {
    companion object {
        const val TAG = "MainActivity"
    }

    val flow by lazy {
        flow<MessageEvent> {
            val random = Random()
            while (true){
                //阅读时间
                var duration = random.nextInt(3000)
                duration+=1000
                delay(duration.toLong())
                emit(getClickEvent())
            }
        }
    }

    var mIsStop: Boolean = true
    val mScreen: Rect = Rect()
    var mJob:Job? = null
    override fun initData(savedInstanceState: Bundle?) {
        val view = ViewFloatBinding.inflate(LayoutInflater.from(applicationContext), null, false)
        FloatWindow
            .with(getApplicationContext())
            .setTag("global")
            .setView(view.root)
            .setX(100) //设置控件初始位置
            .setY(Screen.height, 0.3f)
            .setDesktopShow(true)                        //桌面显示
            .setViewStateListener(this)    //监听悬浮控件状态改变
            .setPermissionListener(this)  //监听权限申请结果
            .setMoveType(MoveType.slide)
            .setMoveStyle(500, AccelerateInterpolator())  //贴边动画时长为500ms，加速插值器
            .build()
        FloatWindow.get("global").view.setOnClickListener {
            mIsStop = !mIsStop
            if (mIsStop) {
                //停止翻页
                mJob?.cancel()
                mJob = null
            } else {
                if (mJob!=null){
                    mJob?.start()
                }else{
                    mJob = lifecycleScope.launch {
                        flow.collect {
                            EventBus.getDefault().post(it)
                        }
                    }
                }
            }
        }
        initScreenInfo()
        val intent = Intent(this, MyAccessibilityService::class.java)
        startService(intent)
        if (isAccessibilitySettingsOn(this).not()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    fun getClickEvent(): ClickEvent {
        val random = Random()
        var rawX = random.nextInt(mScreen.width() / 2)
        var rawY = random.nextInt((mScreen.height() * 0.2).toInt())
        rawX += mScreen.width() / 2 + 40
        rawY += mScreen.height() / 2 + 10
        val dealX = (mScreen.width() - 40).coerceAtMost(rawX)
        val dealY = rawY
        return ClickEvent(dealX, dealY, mScreen)
    }

    fun initScreenInfo() {
        val dm: DisplayMetrics //屏幕分辨率容器
        dm = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels
        mScreen.set(0, 0, width, height)
    }

//    private ServiceConnection connection = new ServiceConnection() {
//        //这两个方法会在活动与服务成功绑定以及解除绑定前后调用
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            //向下转型获得mBinder
//            downloadBinder = (MyService.DownloadBinder) service;
//            downloadBinder.startDownload();
//            downloadBinder.getProgress();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };

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

    /**
     * 检测辅助功能是否开启<br></br>
     */
    fun isAccessibilitySettingsOn(mContext: Context): Boolean {
        var accessibilityEnabled = 0
        val service =
            mContext.packageName + "/" + MyAccessibilityService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (_: Settings.SettingNotFoundException) {
        }
        val mStringColonSplitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue: String = Settings.Secure.getString(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessibilityService = mStringColonSplitter.next()
                if (accessibilityService.equals(service, ignoreCase = true)) {
                    return true
                }
            }
        } else {
            Toaster.debugShow("***ACCESSIBILITY IS DISABLED***")
        }
        return false
    }

    override fun onFail() {

    }
}