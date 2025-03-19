package com.tohoku.cafeteria.data.datasource

import com.tohoku.cafeteria.data.response.DetectResponse
import com.tohoku.cafeteria.data.response.FoodCategoryResponse
import com.tohoku.cafeteria.data.response.FoodItemResponse
import com.tohoku.cafeteria.data.response.FoodVariantResponse
import com.tohoku.cafeteria.data.response.RecommendationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class FoodMockDataSource : FoodDataSource {
    override suspend fun getMenu(): List<FoodCategoryResponse> {
        val sampleFoods = FoodItemResponse(
            foodId = 1,
            name = "Sample Burger",
            url = "android.resource://com.tohoku.cafeteria/drawable/sample_food",
            variants = listOf(
                FoodVariantResponse(
                    variantName = "S",
                    variantId = 101,
                    price = 500,
                    calories = 500f,
                    protein = 25f,
                    fat = 20f,
                    carbohydrates = 50f
                ),
                FoodVariantResponse(
                    variantName = "M",
                    variantId = 102,
                    price = 600,
                    calories = 600f,
                    protein = 30f,
                    fat = 25f,
                    carbohydrates = 60f
                ),
                FoodVariantResponse(
                    variantName = "L",
                    variantId = 103,
                    price = 700,
                    calories = 700f,
                    protein = 35f,
                    fat = 30f,
                    carbohydrates = 70f
                )
            )
        )
        return listOf(
            FoodCategoryResponse(
                category = "Burgers",
                items = List(3) {
                    sampleFoods
                }
            ),
            FoodCategoryResponse(
                category = "Drinks",
                items = List(3) {
                    sampleFoods
                }
            )
        )
    }

    override suspend fun requestRecommendation(query: String): Response<RecommendationResponse> {
        val sampleResponse = RecommendationResponse(
            additionalNotes = "24 years old, 50 kg, 165 cm, lactose intolerant.",
            detailNutritions = listOf(
                "Based on the user's age, weight, and height, the minimum energy intake is estimated using the Harris-Benedict equation and considering a sedentary lifestyle (Roza & Shizgal, 1984).",
                "According to current guidelines, the minimum protein intake for adults is 0.8 grams per kilogram of body weight (WHO, 2007).",
                "Based on a 24-year-old female, with a weight of 50 kg, the recommended minimum fat intake is approximately 0.8-1.0 grams per kilogram of body weight according to current nutritional guidelines [WARNING] (https://www.calculator.net/fat-calculator.html).",
                "According to current dietary guidelines and considering the user's age, weight, and height, a minimum carbohydrate intake of 130 grams per day is recommended to support basic metabolic functions (National Academies of Sciences, Engineering, and Medicine, 2005).",
                "Based on general dietary guidelines and considering the user's age, the recommended minimum daily fiber intake is approximately 25 grams (Anderson et al., 1998).",
                "According to the National Institutes of Health, adults aged 19-50 years need 1000 mg of calcium daily to maintain bone health, which is especially important for lactose-intolerant individuals who may have limited dairy intake (NIH, n.d.).",
                "Given the user's age, weight, height, and lactose intolerance, coupled with the need to determine a minimum vegetable intake, I will use the tavily_search_tool to find guidelines since I don't have specific journal data [WARNING]."
            ),
            minNutritions = listOf(1320, 40, 40, 130, 25, 1000, 400),
            recommendedMealDetail = "Enjoy a balanced meal featuring \"Hamburger steak with grated Japanese radish sauce\" and \"Rice (small)\". The hamburger steak offers a delightful umami flavor, perfectly complemented by the refreshing radish. This combination provides approximately 460 kcal of energy, 17.6g of protein, and 15.2g of fat, catering to your dietary requirements. Please note that this meal is somewhat low in fiber, calcium, and veggies compared to your target nutritional goals.",
            listMeals = listOf("hamburger steak with grated japanese radish sauce", "rice (small)"),
            verboseInFunction = true,
            recommendedMeals = listOf(101),
            id = "f64299e3-2985-44f6-a6ce-eedaec54c502"
        )
        return Response.success(sampleResponse)
    }

    override suspend fun requestNewRecommendation(query: String, rating: String): Response<RecommendationResponse> {
        val sampleResponse = RecommendationResponse(
            additionalNotes = "24 years old, 50 kg, 165 cm, lactose intolerant.",
            detailNutritions = listOf(
                "Based on the user's age, weight, and height, the minimum energy intake is estimated using the Harris-Benedict equation and considering a sedentary lifestyle (Roza & Shizgal, 1984).",
                "According to current guidelines, the minimum protein intake for adults is 0.8 grams per kilogram of body weight (WHO, 2007).",
                "Based on a 24-year-old female, with a weight of 50 kg, the recommended minimum fat intake is approximately 0.8-1.0 grams per kilogram of body weight according to current nutritional guidelines [WARNING] (https://www.calculator.net/fat-calculator.html).",
                "According to current dietary guidelines and considering the user's age, weight, and height, a minimum carbohydrate intake of 130 grams per day is recommended to support basic metabolic functions (National Academies of Sciences, Engineering, and Medicine, 2005).",
                "Based on general dietary guidelines and considering the user's age, the recommended minimum daily fiber intake is approximately 25 grams (Anderson et al., 1998).",
                "According to the National Institutes of Health, adults aged 19-50 years need 1000 mg of calcium daily to maintain bone health, which is especially important for lactose-intolerant individuals who may have limited dairy intake (NIH, n.d.).",
                "Given the user's age, weight, height, and lactose intolerance, coupled with the need to determine a minimum vegetable intake, I will use the tavily_search_tool to find guidelines since I don't have specific journal data [WARNING]."
            ),
            minNutritions = listOf(1320, 40, 40, 130, 25, 1000, 400),
            recommendedMealDetail = "Enjoy a balanced meal featuring \"Hamburger steak with grated Japanese radish sauce\" and \"Rice (small)\". The hamburger steak offers a delightful umami flavor, perfectly complemented by the refreshing radish. This combination provides approximately 460 kcal of energy, 17.6g of protein, and 15.2g of fat, catering to your dietary requirements. Please note that this meal is somewhat low in fiber, calcium, and veggies compared to your target nutritional goals.",
            listMeals = listOf("hamburger steak with grated japanese radish sauce", "rice (small)"),
            verboseInFunction = true,
            recommendedMeals = listOf(102, 103),
            id = "f64299e3-2985-44f6-a6ce-eedaec54c502"
        )
        return Response.success(sampleResponse)
    }

    override suspend fun resetMenu() { }

    override suspend fun detectAndSetCurrentMenu(
        imageUpload: MultipartBody.Part,
        method: RequestBody
    ): Response<DetectResponse> {
        val sampleResponse = DetectResponse(
            response = mapOf(
                "a" to 0.1f,
                "b" to 0.2f
            )
        )
        return Response.success(sampleResponse)
    }
}