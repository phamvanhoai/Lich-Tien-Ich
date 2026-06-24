package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class ThemeOption(
    val id: Int,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

val ThemeOptions = listOf(
    ThemeOption(0, "Cổ điển (Classic Blue)", Color(0xFF1976D2), Color(0xFF42A5F5), Color(0xFF90CAF9)),
    ThemeOption(1, "Ngọc lục bảo (Emerald)", Color(0xFF2E7D32), Color(0xFF66BB6A), Color(0xFFA5D6A7)),
    ThemeOption(2, "Đỏ anh đào (Cherry)", Color(0xFFD32F2F), Color(0xFFEF5350), Color(0xFFEF9A9A)),
    ThemeOption(3, "Tím hoàng hôn (Sunset)", Color(0xFF6A1B9A), Color(0xFFAB47BC), Color(0xFFCE93D8)),
    ThemeOption(4, "Đại dương (Teal Ocean)", Color(0xFF00796B), Color(0xFF26A69A), Color(0xFF80CBC4)),
    ThemeOption(5, "Hổ phách (Amber Gold)", Color(0xFFFF8F00), Color(0xFFFFB300), Color(0xFFFFE082)),
    ThemeOption(6, "Sơn trà (Camelia Pink)", Color(0xFFC2185B), Color(0xFFEC407A), Color(0xFFF48FB1)),
    ThemeOption(7, "Bạc hà (Fresh Mint)", Color(0xFF009688), Color(0xFF4DB6AC), Color(0xFFB2DFDB)),
    ThemeOption(8, "Cam san hô (Coral)", Color(0xFFE64A19), Color(0xFFFF7043), Color(0xFFFFCCBC)),
    ThemeOption(9, "Oải hương (Lavender)", Color(0xFF7E57C2), Color(0xFF9575CD), Color(0xFFD1C4E9)),
    ThemeOption(10, "Rừng thông (Pine Wood)", Color(0xFF388E3C), Color(0xFF66BB6A), Color(0xFFC8E6C9)),
    ThemeOption(11, "Than đá (Slate Gray)", Color(0xFF455A64), Color(0xFF78909C), Color(0xFFCFD8DC)),
    ThemeOption(12, "Nâu cacao (Cocoa)", Color(0xFF5D4037), Color(0xFF8D6E63), Color(0xFFD7CCC8)),
    ThemeOption(13, "Bầu trời (Sky Breeze)", Color(0xFF0288D1), Color(0xFF29B6F6), Color(0xFFB3E5FC)),
    ThemeOption(14, "Mâm xôi (Raspberry)", Color(0xFFAD1457), Color(0xFFD81B60), Color(0xFFF8BBD0)),
    ThemeOption(15, "Ô-liu (Olive)", Color(0xFF689F38), Color(0xFF9CCC65), Color(0xFFDCEDC8)),
    ThemeOption(16, "Cát vàng (Sahara)", Color(0xFFFBC02D), Color(0xFFFDD835), Color(0xFFFFF9C4)),
    ThemeOption(17, "Mận chín (Plum Velvet)", Color(0xFF4A148C), Color(0xFF8E24AA), Color(0xFFE1BEE7)),
    ThemeOption(18, "Đêm chàm (Indigo Night)", Color(0xFF303F9F), Color(0xFF5C6BC0), Color(0xFFC5CAE9)),
    ThemeOption(19, "Tối giản đen (Carbon)", Color(0xFF212121), Color(0xFF757575), Color(0xFFE0E0E0))
)

data class FontSizeOption(
    val index: Int,
    val name: String,
    val scaleFactor: Float
)

val FontSizeOptions = listOf(
    FontSizeOption(0, "Siêu nhỏ (80%)", 0.80f),
    FontSizeOption(1, "Rất nhỏ (85%)", 0.85f),
    FontSizeOption(2, "Nhỏ (90%)", 0.90f),
    FontSizeOption(3, "Hơi nhỏ (95%)", 0.95f),
    FontSizeOption(4, "Chuẩn (100%)", 1.00f),
    FontSizeOption(5, "Vừa phải (105%)", 1.05f),
    FontSizeOption(6, "Hơi lớn (110%)", 1.10f),
    FontSizeOption(7, "Lớn (115%)", 1.15f),
    FontSizeOption(8, "Rất lớn (120%)", 1.20f),
    FontSizeOption(9, "Khổng lồ (130%)", 1.30f)
)
