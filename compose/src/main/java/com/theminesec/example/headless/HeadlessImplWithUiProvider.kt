package com.theminesec.example.headless

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.transaction.PaymentMethod
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.ui.ButtonsColor
import com.theminesec.sdk.headless.ui.PinEntryDetails
import com.theminesec.sdk.headless.ui.PinEntryMode
import com.theminesec.sdk.headless.ui.PinPadConfig
import com.theminesec.sdk.headless.ui.PinPadStyle
import com.theminesec.sdk.headless.ui.TextStyleConfig
import com.theminesec.sdk.headless.ui.ThemeProvider
import com.theminesec.sdk.headless.ui.UiProvider
import com.theminesec.sdk.headless.ui.UiState
import com.theminesec.sdk.headless.util.PinPadStylesUtil


class HeadlessImplWithUiProvider : HeadlessActivity() {
    override fun provideTheme(): ThemeProvider = ClientHeadlessThemeProvider()
    override fun provideUi(): UiProvider = ClientUiProvider()

    // Option #1 change pinEntryMode
    override val pinPadConfig: PinPadConfig = PinPadConfig(pinEntryMode = PinEntryMode.MOVING.name)

    // Option #2 change pinEntryMode, pinEntryDetails and pinPadStyle
    /*override val pinPadConfig: PinPadConfig by lazy {
        getPinPadConfig(this, PinEntryMode.FIXED)
    }*/
}

class ClientHeadlessThemeProvider : ThemeProvider() {
    override fun provideColors(darkTheme: Boolean): HeadlessColors {
        return if (darkTheme) {
            HeadlessColorsDark().copy(
                primary = Color(0xFFD5A23B).toArgb()
            )
        } else {
            HeadlessColorsLight().copy(
                primary = Color(0xFFFFC145).toArgb()
            )
        }
    }
}

class ClientUiProvider : UiProvider() {
    @Composable
    override fun AmountDisplay(amount: Amount, title: String?, description: String?) {
        Box(
            modifier = Modifier.border(1.dp, Color.Red),
            contentAlignment = Alignment.Center
        ) {
            super.AmountDisplay(amount, title, description)
        }
    }

    @Composable
    override fun AcceptanceMarkDisplay(supportedPayments: List<PaymentMethod>, showWallet: Boolean) {
        Box(
            modifier = Modifier.border(1.dp, Color.Red),
            contentAlignment = Alignment.Center
        ) {
            super.AcceptanceMarkDisplay(supportedPayments, true)
        }
    }

    @Composable
    override fun AwaitCardIndicator() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Red),
        ) {
            super.AwaitCardIndicator()
        }
    }

    @Composable
    override fun ProgressIndicator() {
        Box(
            modifier = Modifier.border(1.dp, Color.Red),
            contentAlignment = Alignment.Center
        ) {
            super.ProgressIndicator()
        }
    }

    @Composable
    override fun UiStateDisplay(modifier: Modifier, uiState: UiState, countdownSec: Int) {
        Box(
            modifier = Modifier.border(1.dp, Color.Red),
            contentAlignment = Alignment.Center
        ) {
            super.UiStateDisplay(modifier, uiState, countdownSec)
        }
    }
}

fun getPinPadConfig(context: Context, mode: PinEntryMode) =
    PinPadConfig(
        pinEntryMode = mode.name,
        pinEntryDetails = PinEntryDetails(
            pinDescription = "Your PIN Please!",
            amount = "100.75 USD",
            description = "Please pay MineSec monthly invoice",
            supportPINBypass = false
        ),
        pinPadStyle = PinPadStyle(
            backgroundColor = Color.LightGray.toArgb(),
            amountStyle = getOswaldBold30(context),
            digitStyle = getOswaldBold30(context),
            descriptionStyle = getJbmMedium25(context),
            buttonsColor = ButtonsColor(
                abortBtn = Color.DarkGray.toArgb(),
                confirmBtn = Color.Blue.toArgb(),
                clearBtn = Color.Red.toArgb(),
            )
        )
    )

fun getJbmMedium25(context: Context) = TextStyleConfig(
    fontSize = 25f,
    fontFileUri = PinPadStylesUtil.getFontUri(
        context = context,
        fontResId = R.font.jbm_medium,
        fileName = "jbm_medium.ttf"
    )?.toString()
)

fun getOswaldBold30(context: Context) = TextStyleConfig(
    fontSize = 30f,
    fontFileUri = PinPadStylesUtil.getFontUri(
        context = context,
        fontResId = R.font.oswald_bold,
        fileName = "oswald_bold.ttf"
    )?.toString()
)