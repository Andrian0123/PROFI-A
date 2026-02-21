package ru.profia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.profia.app.R
import ru.profia.app.ui.theme.LogoGradientBrush
import ru.profia.app.ui.theme.MetallicSilver
import ru.profia.app.ui.theme.Primary
import ru.profia.app.ui.theme.PrimaryVariant
import ru.profia.app.ui.theme.OnPrimary
import ru.profia.app.ui.theme.TextSecondary
import ru.profia.app.ui.theme.White

/**
 * Круглая иконка приложения в стиле логотипа: градиент оливковый → серый, буква «A» металлик.
 */
@Composable
fun LogoCircle(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(LogoGradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "A",
            color = MetallicSilver,
            fontSize = (size.value * 0.55).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Логотип для стартового экрана: изображение логотипа PROFI-A (квадрат с «A», карандашом, инструментом и надписью).
 */
@Composable
fun SplashLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 120.dp
) {
    Image(
        painter = painterResource(id = R.drawable.profi_a_logo_square),
        contentDescription = stringResource(R.string.content_desc_logo),
        modifier = modifier
            .size(iconSize)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Fit
    )
}

/**
 * Логотип на тёмно-оливковом фоне (квадрат, для шапок).
 * @param showLetter — показывать ли букву «A» (false по умолчанию для шапки).
 */
@Composable
fun LogoSquare(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showLetter: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Primary),
        contentAlignment = Alignment.Center
    ) {
        if (showLetter) {
            Text(
                text = "A",
                color = White,
                fontSize = (size.value * 0.6).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Логотип PROFI-A: изображение логотипа (квадрат с «A» и надписью) + опционально текст.
 */
@Composable
fun ProfiALogo(
    modifier: Modifier = Modifier,
    logoSize: androidx.compose.ui.unit.Dp = 40.dp,
    showText: Boolean = true,
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profi_a_logo_square),
            contentDescription = stringResource(R.string.content_desc_logo),
            modifier = Modifier
                .size(logoSize)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
        if (showText) {
            Text(
                text = "ПРОФЙ-А",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Логотип PROFI-A: квадрат с буквой «А» (хаки) и опционально надпись.
 */
@Composable
fun ProfiLogo(
    modifier: Modifier = Modifier,
    showTitle: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Primary, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "А",
                color = OnPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (showTitle) {
            Text(
                text = "ПРОФЙ-А",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryVariant
            )
        }
    }
}

@Composable
fun AppLogoImage(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Image(
        painter = painterResource(id = R.drawable.profi_a_logo_square),
        contentDescription = stringResource(R.string.content_desc_logo),
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}
