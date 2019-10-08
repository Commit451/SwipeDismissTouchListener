package com.commit451.swipedismisstouchlistener

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * A [View.OnTouchListener] that makes any [View] dismissable when the
 * user swipes (drags her finger) horizontally across the view.
 */
class SwipeDismissTouchListener
/**
 * Constructs a new swipe-to-dismiss touch listener for the given view.
 *
 * @param view The view to make dismissable.
 */
(private val view: View) : View.OnTouchListener {

    // Cached ViewConfiguration and system-wide constant values
    private val slop: Int
    private val minFlingVelocity: Int
    private val maxFlingVelocity: Int
    private val animationTime: Long
    private var onDismissListener: ((view: View) -> Unit)? = null
    private var viewWidth = 1 // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private var downX: Float = 0.toFloat()
    private var downY: Float = 0.toFloat()
    private var swiping: Boolean = false
    private var swipingSlop: Int = 0
    private var velocityTracker: VelocityTracker? = null
    private var translationX: Float = 0.toFloat()

    init {
        val vc = ViewConfiguration.get(view.context)
        slop = vc.scaledTouchSlop
        minFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        maxFlingVelocity = vc.scaledMaximumFlingVelocity
        animationTime = view.context.resources.getInteger(
                android.R.integer.config_shortAnimTime).toLong()
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(translationX, 0f)

        if (viewWidth < 2) {
            viewWidth = this.view.width
        }

        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // TODO: ensure this is a finger, and set a flag
                downX = motionEvent.rawX
                downY = motionEvent.rawY
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(motionEvent)
                return true
            }

            MotionEvent.ACTION_UP -> {
                val velocityTracker = velocityTracker ?: return false

                val deltaX = motionEvent.rawX - downX
                velocityTracker.addMovement(motionEvent)
                velocityTracker.computeCurrentVelocity(1000)
                val velocityX = velocityTracker.xVelocity
                val absVelocityX = abs(velocityX)
                val absVelocityY = abs(velocityTracker.yVelocity)
                var dismiss = false
                var dismissRight = false
                if (abs(deltaX) > viewWidth / 2 && swiping) {
                    dismiss = true
                    dismissRight = deltaX > 0
                } else if (minFlingVelocity <= absVelocityX && absVelocityX <= maxFlingVelocity
                        && absVelocityY < absVelocityX
                        && absVelocityY < absVelocityX && swiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = velocityX < 0 == deltaX < 0
                    dismissRight = velocityTracker.xVelocity > 0
                }
                if (dismiss) {
                    // dismiss
                    this.view.animate()
                            .translationX((if (dismissRight) viewWidth else -viewWidth).toFloat())
                            .alpha(0f)
                            .setDuration(animationTime)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    performDismiss()
                                }
                            })
                } else if (swiping) {
                    // cancel
                    this.view.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(animationTime)
                            .setListener(null)
                }
                velocityTracker.recycle()
                this.velocityTracker = null
                translationX = 0f
                downX = 0f
                downY = 0f
                swiping = false
            }

            MotionEvent.ACTION_CANCEL -> {
                if (velocityTracker == null) {
                    return false
                }

                this.view.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(animationTime)
                        .setListener(null)
                velocityTracker!!.recycle()
                velocityTracker = null
                translationX = 0f
                downX = 0f
                downY = 0f
                swiping = false
            }

            MotionEvent.ACTION_MOVE -> {
                val velocityTracker = velocityTracker ?: return false

                velocityTracker.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - downX
                val deltaY = motionEvent.rawY - downY
                if (abs(deltaX) > slop && abs(deltaY) < abs(deltaX) / 2) {
                    swiping = true
                    swipingSlop = if (deltaX > 0) slop else -slop
                    this.view.parent.requestDisallowInterceptTouchEvent(true)

                    // Cancel listview's touch
                    val cancelEvent = MotionEvent.obtain(motionEvent)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL or (motionEvent.actionIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                    this.view.onTouchEvent(cancelEvent)
                    cancelEvent.recycle()
                }

                if (swiping) {
                    translationX = deltaX
                    this.view.translationX = deltaX - swipingSlop
                    // TODO: use an ease-out interpolator or such
                    this.view.alpha = 0f.coerceAtLeast(1f.coerceAtMost(1f - 2f * abs(deltaX) / viewWidth))
                    return true
                }
            }
        }
        return false
    }

    fun setOnDismissListener(onDismissListener: ((view: View) -> Unit)?) {
        this.onDismissListener = onDismissListener
    }

    private fun performDismiss() {
        // Animate the dismissed view to zero-height and then fire the dismiss callback.
        // This triggers layout on each animation frame; in the future we may want to do something
        // smarter and more performant.

        val lp = view.layoutParams
        val originalHeight = view.height

        val animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onDismissListener?.invoke(view)
                // Reset view presentation
                view.alpha = 1f
                view.translationX = 0f
                lp.height = originalHeight
                view.layoutParams = lp
            }
        })

        animator.addUpdateListener { valueAnimator ->
            lp.height = valueAnimator.animatedValue as Int
            view.layoutParams = lp
        }

        animator.start()
    }
}