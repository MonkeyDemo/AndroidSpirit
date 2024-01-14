package com.monkeydemo.androidspirit

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.hjq.toast.Toaster
import com.monkeydemo.androidspirit.base.ClickEvent
import com.monkeydemo.androidspirit.base.MessageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


class MyAccessibilityService : AccessibilityService() {

    var canDo = true

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        if (event is ClickEvent){
            doGestureDes(event.getGestureDescription())
        }
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType //事件类型
        val packageName = event.packageName ?: return //触发事件的包名
        val packageNameString = packageName.toString()
        // 要过滤的包名
        if (!packageNameString.startsWith("com.dragon.read")) return
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // 页面发生变化
                val rootNode = rootInActiveWindow ?: return
                val curWin = event.className.toString()
                Timber.tag(TAG).d("Activity切换,当前窗体：$curWin")
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // 组件发生变化
                val className = event.className.toString()
                Timber.tag(TAG).d("Activity内容改变事件:$className")
                val rootNode = rootInActiveWindow ?: return
                // 应用通知
                val texts = event.text
                if (!texts.isEmpty()) {
                    for (text in texts) {
                        val content = text.toString()
                        Timber.tag(TAG).d("收到通知:$content")
                    }
                }
            }

            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                val texts = event.text
                if (!texts.isEmpty()) {
                    for (text in texts) {
                        val content = text.toString()
                        Timber.tag(TAG).d("收到通知:$content")
                    }
                }
            }
        }
    }

    class AccessibilityBinder : Binder() {
        fun clickscreen(){

        }
    }

    val mBinder:AccessibilityBinder = AccessibilityBinder()


    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Toaster.debugShow("xxx辅助功能已开启")
    }

    internal inner class NodeID {
        var id: String? = null
    }

    // 封装一个点击节点的方法 （NodeID为自定义枚举类，getId得到的是字符串，可以直接用字符串）
    private fun clickView(nodeId: NodeID, index: Int): Boolean {
        var rootNode = rootInActiveWindow
        if (rootNode == null) {
            val windows = windows
            if (windows.size != 0) {
                Timber.tag(TAG).d("windows: $windows")
                rootNode = windows[0].root
            }
            if (rootNode == null) {
                Timber.tag(TAG).d("获取不到rootNode")
                return false
            }
        }
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(
            nodeId.id!!
        )
        val size = nodes.size
        if (size != 0) {
            val node = nodes[index]
            Timber.tag(TAG).d(
                nodeId.toString() + " 共找到" + size + "个控件， 点击第" + (index + 1) + "个，" + node
            )
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }
        Timber.tag(TAG).d("$nodeId 点击失败")
        return false
    }

    // 当一些控件没有id，可以通过getChild或者getParent的方式拿到 返回为一个AccessibilityNodeInfo实例，此后可以调用方法进行点击、滑动或者获取它在屏幕中的位置
    private fun getViewChild(nodeId: NodeID, index: Int, childIndex: Int): AccessibilityNodeInfo? {
        var rootNode = rootInActiveWindow
        if (rootNode == null) {
            val windows = windows
            if (windows.size != 0) {
                Timber.tag(TAG).d("windows: $windows")
                rootNode = windows[0].root
            }
            if (rootNode == null) {
                Timber.tag(TAG).d("获取不到rootNode")
                return null
            }
        }
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(
            nodeId.id!!
        )
        val size = nodes.size
        if (size != 0) {
            val node = nodes[index]
            Timber.tag(TAG).d(
                nodeId.toString() + " 共找到" + size + "个控件"
            )
            return node.getChild(childIndex)
        }
        Timber.tag(TAG).d("$nodeId childIndex: $childIndex 点击失败")
        return null
    }

    // 一个通过手势模拟点击的例子 rect为控件的位置 可以通过AccessibilityNodeInfo实例的getBoundsInScreen方法获取
    fun clickGesture(rect: Rect) {
        val path = Path()
        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        val strokeDescription: StrokeDescription
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            strokeDescription = StrokeDescription(path, 10, 10)
            val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription)
            dispatchGesture(gestureDescription.build(), null, null)
        }
    }

    fun doGestureDes(des:GestureDescription){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dispatchGesture(des, null, null)
        }
    }

    // 通过手势在x轴上滑动的例子 注意的距离可能需要通过dpi计算
    fun horizontalSlideGesture(rect: Rect, distanceX: Int) {
        // 有需要请认真阅读Path类说明
        val path = Path()
        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.centerX() + distanceX) as Float, rect.centerY().toFloat())
        val strokeDescription: StrokeDescription
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            strokeDescription = StrokeDescription(path, 0, 1500)
            val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription)
            dispatchGesture(gestureDescription.build(), null, null)
        }
    }

    companion object {
        const val TAG = "MyAccessibilityService"
    }
}