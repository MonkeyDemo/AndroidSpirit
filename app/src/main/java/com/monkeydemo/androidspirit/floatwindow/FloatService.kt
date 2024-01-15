package com.monkeydemo.androidspirit.floatwindow

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.View
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.hjq.toast.Toaster
import com.monkeydemo.androidspirit.MyAccessibilityService
import com.monkeydemo.androidspirit.base.ClickEvent
import com.monkeydemo.androidspirit.base.MessageEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Random

/**
 * 管理后台服务类
 */
class FloatService : LifecycleService() {
    companion object {
        const val TAG = "FloatService"
    }

    var mFloatView: FloatWin? = null
    var mScreen: Rect? = null
    var mIsStop: Boolean = true
    var mJob: Job? = null
    val flow by lazy {
        flow<MessageEvent> {
            val random = Random()
            while (true) {
                //阅读时间
                var duration = random.nextInt(3000)
                duration += 1000
                delay(duration.toLong())
                getClickEvent()?.let {
                    emit(it)
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        val onBind = super.onBind(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mScreen = intent.getParcelableExtra("screenInfo", Rect::class.java)
        } else {
            mScreen = intent.getParcelableExtra("screenInfo")
        }

        TODO("Return the communication channel to the service.")
    }

    private fun startClick() {
        Toaster.show("dsdads")
//        mIsStop = !mIsStop
//        if (mIsStop) {
//            //停止翻页
//            mJob?.cancel()
//            mJob = null
//        } else {
//            if (mJob!=null){
//                mJob?.start()
//            }else{
//                mJob = lifecycleScope.launch {
//                    flow.collect {
//                        EventBus.getDefault().post(it)
//                    }
//                }
//            }
//        }
    }

    override fun onCreate() {
        super.onCreate()
        mFloatView = FloatWin(this, this)
        mFloatView?.mStartCall = this::startClick
        val intent = Intent(this, MyAccessibilityService::class.java)
        startService(intent)
    }

    fun getClickEvent(): ClickEvent? {
        return mScreen?.let {
            val random = Random()
            var rawX = random.nextInt(it.width() / 2)
            var rawY = random.nextInt((it.height() * 0.2).toInt())
            rawX += it.width() / 2 + 40
            rawY += it.height() / 2 + 10
            val dealX = (it.width() - 40).coerceAtMost(rawX)
            val dealY = rawY
            return ClickEvent(dealX, dealY, it)
        }
    }
}