package com.tohoku.cafeteria.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.domain.model.FoodItem
import com.tohoku.cafeteria.domain.model.FoodVariant
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCarouselComponent(
    modifier: Modifier = Modifier,
    title: String,
    items: List<FoodItem>,
    onItemClick: (FoodItem) -> Unit
) {
    Column {
        Text(
            modifier = modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { items.count() },
            preferredItemWidth = dimensionResource(R.dimen.width_carousel),
            itemSpacing = dimensionResource(R.dimen.padding_small)
        ) { i ->
            val item = items[i]
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable { onItemClick(item) }
            ) {
                AsyncImage(
                    model = item.url,
                    modifier = Modifier.height(dimensionResource(R.dimen.width_carousel)).maskClip(MaterialTheme.shapes.large),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = item.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(dimensionResource(R.dimen.padding_small))
                        .padding(dimensionResource(R.dimen.padding_xsmall))
                        .graphicsLayer {
                            // Translate the text to be pinned to the left side of the item's mask
                            translationX = carouselItemInfo.maskRect.left
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuCarouselComponentPreview() {
    CafeteriaAITheme {
        MenuCarouselComponent(
            title = "Burgers",
            items = List(5) {
                FoodItem(
                    foodId = 1,
                    name = "Sample Burger",
                    url = "https://media.istockphoto.com/id/520410807/photo/cheeseburger.jpg?s=612x612&w=0&k=20&c=fG_OrCzR5HkJGI8RXBk76NwxxTasMb1qpTVlEM0oyg4=",
                    foodVariantsList = listOf(
                        FoodVariant(
                            variantName = "S",
                            variantId = 101,
                            price = 500,
                            calories = 500,
                            protein = 25,
                            fat = 20,
                            carbohydrates = 50
                        ),
                        FoodVariant(
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
            },
            onItemClick = { }
        )
    }
}
