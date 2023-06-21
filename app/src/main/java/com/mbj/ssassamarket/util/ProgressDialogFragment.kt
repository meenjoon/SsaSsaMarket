package com.mbj.ssassamarket.util

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.mbj.ssassamarket.databinding.DialogWorkingProgressBinding

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = DialogWorkingProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnKeyListener { dialog, keyCode, event ->
                return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
            }
        }
    }

    override fun onResume() {
        super.onResume()
        context?.setDialogSize(this, 0.9f, 0.2f)
    }

    private fun Context.setDialogSize(
        dialogFragment: DialogFragment,
        widthRatio: Float,
        heightRatio: Float
    ) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val window = dialogFragment.dialog?.window
        val display = windowManager.defaultDisplay
        val size = Point()

        //가로모드인 지 확인함
        val rotation = display.rotation
        val isRotated = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270

        val x: Int
        val y: Int

        fun calculateSize(value: Int, ratio: Float): Int {
            return (value * ratio).toInt()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            display.getSize(size)
        } else {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            size.x = bounds.width()
            size.y = bounds.height()
        }

        //가로모드일 때 width, height 바꿈
        if (isRotated) {
            x = calculateSize(size.y, widthRatio)
            y = calculateSize(size.x, heightRatio)
        } else {
            x = calculateSize(size.x, widthRatio)
            y = calculateSize(size.y, heightRatio)
        }

        window?.setLayout(x, y)
    }
}
