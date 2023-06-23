package com.mbj.ssassamarket.ui.writing

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentWritingBinding
import com.mbj.ssassamarket.ui.BaseFragment

class WritingFragment : BaseFragment() {
    override val binding get() = _binding as FragmentWritingBinding
    override val layoutId: Int get() = R.layout.fragment_writing
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySpinner()
        setTouchInterceptHandling()
    }

    private fun setupCategorySpinner() {
        val categoriesWithHint = getCategoriesWithHint()
        val adapter = createSpinnerAdapter(categoriesWithHint)

        with(binding.writingCategorySpinner) {
            this.adapter = adapter
            onItemSelectedListener = createItemSelectedListener()
        }
    }

    private fun getCategoriesWithHint(): Array<String> {
        val categories = resources.getStringArray(R.array.category_list)
        val categoryHint = getString(R.string.category_hint)
        return arrayOf(categoryHint) + categories
    }

    private fun createSpinnerAdapter(categories: Array<String>): ArrayAdapter<String> {
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_item_category,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun createItemSelectedListener(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position) as String

                if (selectedItem == getString(R.string.category_hint)) {
                    if (view is TextView) {
                        view.setTextColor(Color.parseColor("#B9B6B6"))
                    }
                } else {
                    if (view is TextView) {
                        view.setTextColor(Color.BLACK)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.writingCategorySpinner.setSelection(0)
            }
        }
    }

    private fun setTouchInterceptHandling() {
        binding.writingContentTiet.setOnTouchListener { view, motionEvent ->
            if (view.id == R.id.writing_content_tiet) {
                view.parent.requestDisallowInterceptTouchEvent(true)
                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }
}
