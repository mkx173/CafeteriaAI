package com.tohoku.cafeteria.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.domain.model.MenuItem
import com.tohoku.cafeteria.domain.model.NutritionData
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuFoodCategoryListComponent(
    modifier: Modifier = Modifier,
    categoryData: List<FoodCategory>
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(bottom = 16.dp).padding(horizontal = 8.dp),
    ) {
        if (categoryData.isNotEmpty()) {
            item {
                MenuCarouselComponent(
                    title = "Today's Menu",
                    items = categoryData[0].items
                )
            }
        }
        items(categoryData) { foodCategory ->
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = foodCategory.category,
                    style = MaterialTheme.typography.titleLarge,
                )
                foodCategory.items.forEach { foodItem ->
                    Surface (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        ListItem(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            headlineContent = {
                                Text(
                                    text = foodItem.name
                                )
                            },
                            leadingContent = {
                                Image(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape),
                                    painter = painterResource(R.drawable.sample_food),
                                    contentDescription = foodItem.name,
                                    contentScale = ContentScale.Crop
                                )
                            },
                            supportingContent = {
                                Text(text = "support")
                            },
                            trailingContent = {
                                Text(text = "trail")
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuFoodCategoryListComponentPreview() {
    CafeteriaAITheme {
        MenuFoodCategoryListComponent(
            categoryData = listOf(
                FoodCategory(
                    category = "Burgers",
                    items = List(5) {
                        MenuItem(
                            foodId = 1,
                            name = "Sample Burger",
                            url = "https://media.istockphoto.com/id/520410807/photo/cheeseburger.jpg?s=612x612&w=0&k=20&c=fG_OrCzR5HkJGI8RXBk76NwxxTasMb1qpTVlEM0oyg4=",
                            nutritionDataList = listOf(
                                NutritionData(
                                    variantName = "S",
                                    variantId = 101,
                                    price = 500,
                                    calories = 500,
                                    protein = 25,
                                    fat = 20,
                                    carbohydrates = 50
                                ),
                                NutritionData(
                                    variantName = "M",
                                    variantId = 102,
                                    price = 600,
                                    calories = 600,
                                    protein = 30,
                                    fat = 25,
                                    carbohydrates = 60
                                )
                            )
                        )
                    }
                )
            )
        )
    }
}
