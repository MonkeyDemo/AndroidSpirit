package com.monkeydemo.androidspirit.floatwindow

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.monkeydemo.androidspirit.databinding.ViewFloatBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * 浮动窗口
 */
class FloatWin(lifecycleOwner: LifecycleOwner, context: Context) : DefaultLifecycleObserver {

    private val binding: ViewFloatBinding
    private var mWinManager: WindowManager
    private var mWinParams: WindowManager.LayoutParams

    private var mJob:Job? = null
    private var mLifecycleOwner:LifecycleOwner? = null

    /**
     * 点击开始回调
     */
    var mStartCall:(()->Unit)?=null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        mLifecycleOwner = lifecycleOwner
        mWinManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        binding = ViewFloatBinding.inflate(LayoutInflater.from(context), null, false)
        val params = WindowManager.LayoutParams()
        params.gravity = Gravity.BOTTOM or Gravity.END
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.format = PixelFormat.TRANSLUCENT
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.title = ""
        mWinParams = params
        initListener()
        mWinManager.addView(binding.root, params)
    }

    fun initListener() {
        binding.root.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0.toFloat()
            var downY = 0.toFloat()
            var downParamsX = 0
            var downParamsY = 0
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downParamsX = mWinParams.x
                        downParamsY = mWinParams.y
                        downX = event.rawX
                        downY = event.rawY
                        false
                    }

                    MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                        val distX = event.rawX - downX
                        val distY = event.rawY - downY
                        mWinParams.x = downParamsX + distX.toInt()
                        mWinParams.y = downParamsY + distY.toInt()
                        mWinManager.updateViewLayout(binding.root, mWinParams)
                        false
                    }

                    else -> false
                }
            }
        })
        val flow = callbackFlow<View> {
            val listener = View.OnClickListener {
                trySend(it)
            }
            binding.btnStart.setOnClickListener(listener)
            binding.btnMore.setOnClickListener(listener)
            binding.btnExit.setOnClickListener(listener)
            binding.btnHome.setOnClickListener(listener)
            awaitClose {
                binding.btnStart.setOnClickListener(null)
                binding.btnHome.setOnClickListener(null)
                binding.btnExit.setOnClickListener(null)
                binding.btnMore.setOnClickListener(null)
            }
        }
        mJob = mLifecycleOwner?.lifecycleScope?.launch {
            flow.debounce(800).collect {
                when(it.id){
                    binding.btnStart.id -> mStartCall?.invoke()
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mWinManager.removeView(binding.root)
        mJob?.cancel()
        mJob = null
        mLifecycleOwner = null
    }

}