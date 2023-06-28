package com.mbj.ssassamarket.util

import com.mbj.ssassamarket.data.model.Category

object CategoryFormat {
    fun stringToCategory(string: String): Category {
        return when(string) {
            Category.CLOTHING.label -> Category.CLOTHING
            Category.APPLIANCE.label -> Category.APPLIANCE
            Category.FOOD.label -> Category.FOOD
            Category.FURNITURE.label -> Category.FURNITURE
            else -> Category.OTHERS
        }
    }

    fun getCategoryLabelFromInput(string: String): String {
        return when (string) {
            Category.CLOTHING.label -> "의류"
            Category.APPLIANCE.label -> "가전제품"
            Category.FOOD.label -> "식품"
            Category.FURNITURE.label -> "가구"
            Category.OTHERS.label -> "기타"
            else -> "카테고리를 선택해주세요"
        }
    }

    fun categoryToString(category: Category): String {
        return when(category) {
            Category.CLOTHING -> Category.CLOTHING.label
            Category.APPLIANCE -> Category.APPLIANCE.label
            Category.FOOD -> Category.FOOD.label
            Category.FURNITURE -> Category.FURNITURE.label
            else -> Category.OTHERS.label
        }
    }
}
