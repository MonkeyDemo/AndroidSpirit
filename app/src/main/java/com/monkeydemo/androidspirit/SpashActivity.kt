package com.monkeydemo.androidspirit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.monkeydemo.androidspirit.base.BaseActivity
import com.monkeydemo.androidspirit.databinding.ActivitySpashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SpashActivity : BaseActivity<ActivitySpashBinding>() {
    val flow: Flow<Int> by lazy {
        flow {
            var time = 4
            while (time > 0) {
                emit(time)
                delay(1000)
                time--
            }

        }
    }
    var launcher: ActivityResultLauncher<Intent>? = null
    override fun initData(savedInstanceState: Bundle?) {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        val job = lifecycleScope.launch {
            flow.onCompletion {
                gotoMain()
            }.collect {
                binding.tvClock.text = "$it 跳过"
            }
        }
        binding.tvClock.setOnClickListener {
            job.cancel()
            gotoMain()
        }
    }

    override fun initViewId(): Int = R.layout.activity_spash


    fun gotoMain() {
        launcher?.launch(Intent(this, MainActivity::class.java))
        this.finish()
    }
}