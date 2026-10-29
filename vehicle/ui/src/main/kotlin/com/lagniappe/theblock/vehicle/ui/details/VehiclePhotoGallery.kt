package com.lagniappe.theblock.vehicle.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.lagniappe.theblock.vehicle.ui.R
import kotlinx.coroutines.launch

@Composable
internal fun VehiclePhotoGallery(images: List<String>) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.vehicle_details_no_photos))
        }
        return
    }

    val pager = rememberPagerState(pageCount = { images.size })
    val scope = rememberCoroutineScope()
    val placeholder = painterResource(R.drawable.ic_vehicle_placeholder)
    Column {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large),
            key = { it },
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = stringResource(
                    R.string.vehicle_details_photo_number, page + 1, images.size,
                ),
                contentScale = ContentScale.Fit,
                placeholder = placeholder,
                error = placeholder,
                fallback = placeholder,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                enabled = pager.currentPage > 0 && !pager.isScrollInProgress,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.vehicle_details_previous_photo),
                )
            }
            Text(
                stringResource(R.string.vehicle_details_photo_number, pager.currentPage + 1, images.size),
                style = MaterialTheme.typography.labelLarge,
            )
            IconButton(
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                enabled = pager.currentPage < images.lastIndex && !pager.isScrollInProgress,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.vehicle_details_next_photo),
                )
            }
        }
    }
}
