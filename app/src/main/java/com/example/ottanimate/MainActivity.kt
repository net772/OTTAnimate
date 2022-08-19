package com.example.ottanimate

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.*
import com.example.ottanimate.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    //매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    private var isGateringMotionAnimating: Boolean = false
    private var isCurationMotionAnimating: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동 생성된 뷰 바인딩 클래스에서의 inflate라는 메서드를 활용해서
        // 액티비티에서 사용할 바인딩 클래스의 인스턴스 생성
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActivity()
    }

    private fun initActivity() {
        setStatusBarTransparent()
        initInsetMargin()
        initAppBar()
        initScrollViewListeners()
        initMotionLayoutListeners()
    }

    /** 툴바를 상태바 밑에서 시작되도록 bottomMargin  */
    private fun initInsetMargin() = with(binding) {
        toolbarContainer.layoutParams = (toolbarContainer.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(0, this@MainActivity.statusBarHeight(), 0, 0)
            }
    }

    /** appBar 투명도 조절 */
    private fun initAppBar() = with(binding) {
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange / 2 // 800
            val abstractOffset = abs(verticalOffset) // 0 ~ 800

            toolbarBackgroundView.alpha = when {
                abstractOffset >= abstractOffset / 2 -> {
                    (abstractOffset - totalScrollRange).toFloat() / totalScrollRange
                }
                else -> 0f
            }
        })
    }

    private fun initScrollViewListeners() = with (binding) {
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrolledValue = binding.scrollView.scrollY.toFloat().dpToPx(this@MainActivity)
            val height = (bottomContainer.height + 120f.dpToPx(this@MainActivity)) / 2 // marginBottom 120dp

            if (scrolledValue > height) {
                if (isGateringMotionAnimating.not()) {
                    binding.gatheringDigitalThingsBackgroundMotionLayout.transitionToEnd()
                    binding.gatheringDigitalThingsMotionLayout.transitionToEnd()
                    binding.buttonShownMotionLayout.transitionToEnd()
                }
            } else {
                if (isGateringMotionAnimating.not()) {
                    binding.gatheringDigitalThingsBackgroundMotionLayout.transitionToStart()
                    binding.gatheringDigitalThingsMotionLayout.transitionToStart()
                    binding.buttonShownMotionLayout.transitionToStart()
                }
            }

            if (scrolledValue > binding.scrollView.height) {
                if (isCurationMotionAnimating.not()) {
                    binding.curationAnimationMotionLayout.setTransition(R.id.curation_animation_start1, R.id.curation_animation_end1)
                    binding.curationAnimationMotionLayout.transitionToEnd()
                    isCurationMotionAnimating = true
                }
            }
        }
    }

    private fun initMotionLayoutListeners() = with(binding) {
        gatheringDigitalThingsMotionLayout.setTransitionListener(
            started = { isGateringMotionAnimating = true },
            completed = { isGateringMotionAnimating = false }
        )
        curationAnimationMotionLayout.setTransitionListener(
            completed = { currentId ->
                when (currentId) {
                    R.id.curation_animation_end1 -> {
                        binding.curationAnimationMotionLayout.setTransition(R.id.curation_animation_start2, R.id.curation_animation_end2)
                        binding.curationAnimationMotionLayout.transitionToEnd()
                    }
                }
            }
        )
    }
}

fun Float.dpToPx(context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)

fun Context.statusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else 0
}

fun Context.navigationHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else 0
}

/** 상태바 투명하게 */
fun Activity.setStatusBarTransparent() {
    window.apply {
        setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = Color.TRANSPARENT
    }
    if(Build.VERSION.SDK_INT >= 30) {	// API 30 에 적용
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

fun MotionLayout.setTransitionListener(started: () -> Unit = {}, completed: (Int) -> Unit = {}) = setTransitionListener(object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) = started()
        override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) = Unit
        override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) = completed(currentId)
        override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) = Unit
    })
