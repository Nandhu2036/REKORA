package com.rekora.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val RekoraShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

object RekoraCorners {
    // Apple continuous curve squircles
    val Card = RoundedCornerShape(20.dp)
    val CardLarge = RoundedCornerShape(26.dp)
    val CardSmall = RoundedCornerShape(14.dp)
    val Squircle = RoundedCornerShape(22.dp)
    
    // Buttons & Chips
    val Button = RoundedCornerShape(14.dp)
    val ButtonSmall = RoundedCornerShape(10.dp)
    val Chip = RoundedCornerShape(50) // Pill
    val Pill = RoundedCornerShape(50)
    val Tag = RoundedCornerShape(8.dp)
    
    // Specialized Surfaces
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val SearchBar = RoundedCornerShape(18.dp)
    val Image = RoundedCornerShape(16.dp)
    val Avatar = RoundedCornerShape(50)
    val Dock = RoundedCornerShape(32.dp)
    val SegmentedControl = RoundedCornerShape(12.dp)
}
