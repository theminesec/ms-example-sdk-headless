package com.theminesec.example.headless

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.PaymentMethod
import com.theminesec.lib.dto.transaction.WalletType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.extension.toDisplayString
import com.theminesec.sdk.headless.ui.ScreenProvider
import com.theminesec.sdk.headless.ui.UiState
import com.theminesec.sdk.headless.ui.component.SignaturePad
import com.theminesec.sdk.headless.ui.component.SignatureState
import com.theminesec.sdk.headless.ui.component.resource.Icon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class ExampleBdoHeadlessImpl : HeadlessActivity() {
    override val experimentalScreenProvider = true
    override val screenProvider = ExampleScreenProvider

}

object ExampleScreenProvider : ScreenProvider() {
    @Composable
    override fun PreparationScreen(
        poiRequest: PoiRequest.ActionNew,
        preparingFlow: Flow<UiState.Preparing>,
        countdownFlow: StateFlow<Int>
    ) {
        val uiState by preparingFlow.collectAsStateWithLifecycle(initialValue = UiState.Preparing.Idle)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top, // 明确指定参数
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BdoTopEdgeSection {
                BdoToolbar()
                BdoPromoBanner()
                BdoAmountDisplay(poiRequest.amount)
            }
            // body part
            Spacer(Modifier.weight(1f, true))
            CircularProgressIndicator(
                modifier = Modifier.size(144.dp),
                strokeCap = StrokeCap.Round,
                strokeWidth = 8.dp,
                color = bdoColorBtnPrimary,
            )
            Spacer(Modifier.size(16.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                text = uiState.toBdoDisplay(),
                textAlign = TextAlign.Center,
                style = BdoTypeStyles.instruction,
            )
            Spacer(Modifier.weight(1f, true))

            BottomSpacer()
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun AwaitingCardScreen(
        poiRequest: PoiRequest.ActionNew,
        awaitingFlow: Flow<UiState.Awaiting>,
        supportedMethods: List<PaymentMethod>,
        countdownFlow: StateFlow<Int>,
        onAbort: () -> Unit
    ) {
        val uiState by awaitingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top, // 明确指定参数
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BdoTopEdgeSection {
                BdoToolbar(onAbort)
                BdoPromoBanner()
                BdoAmountDisplay(poiRequest.amount)
            }

            // content
            Spacer(Modifier.weight(1f, true))
            AwaitCardGif()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                text = uiState.toBdoDisplay(),
                textAlign = TextAlign.Center,
                style = BdoTypeStyles.instruction,
            )
            Spacer(Modifier.weight(1f, true))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.CenterHorizontally
                ),
            ) {
                supportedMethods.forEach { it.Icon() }
//                WalletType.entries.forEach { it.Icon()
                WalletType.values().forEach { it.Icon() }
            }

            BottomSpacer()
        }
    }

    @Composable
    override fun ProcessingScreen(
        poiRequest: PoiRequest,
        processingFlow: Flow<UiState.Processing>,
        countdownFlow: StateFlow<Int>
    ) {
        val uiState by processingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)

        BdoTopEdgeSection(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.weight(1f, true))
            LoadingGif()
            Spacer(Modifier.size(16.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                text = uiState.toBdoDisplay(),
                textAlign = TextAlign.Center,
                color = bdoColorPrimaryForeground,
                style = BdoTypeStyles.instruction,
            )
            Spacer(Modifier.weight(1f, true))

            BottomSpacer()
        }
    }

    @Composable
    override fun SignatureScreen(
        poiRequest: PoiRequest.ActionNew,
        signatureState: SignatureState,
        onSignatureConfirm: () -> Unit,
        displayPmAndLast4: Pair<PaymentMethod, String>,
        approvalCode: String?
    ) {
        val lineState by signatureState.signatureLines.collectAsState(initial = emptyList())
        val clearInteractionSource =
            remember { MutableInteractionSource() }
        val confirmInteractionSource =
            remember { MutableInteractionSource() }

        // for enforce landscape
        // val context = LocalContext.current as ComponentActivity
        // DisposableEffect(Unit) {
        //     context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //     onDispose {
        //         context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        //     }
        // }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top, // 明确指定参数
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BdoTopEdgeSection {
                BdoToolbar()
                BdoPromoBanner()
                BdoAmountDisplay(poiRequest.amount)
            }

            Spacer(Modifier.weight(1f, true))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Digital Signature",
                    style = BdoTypeStyles.instruction,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Place signature within the box",
                    style = BdoTypeStyles.body,
                    color = bdoColorDesc
                )
            }
            Spacer(Modifier.size(12.dp))
            SignaturePad(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .aspectRatio(2f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, bdoColorBorderPrimary, RoundedCornerShape(12.dp)),
                state = signatureState,
            )

            Text(
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                text = "I agree to pay the above total amount according to the card issuer agreement.",
                style = BdoTypeStyles.body,
                color = bdoColorDesc,
            )

            Spacer(Modifier.weight(1f, true))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End)
                    .padding(
                        bottom = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp, minWidth = 72.dp),
                    onClick = { signatureState.clearSignatureLines() },
                    enabled = lineState.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bdoColorBtnSecondary,
                        contentColor = bdoColorPrimary,
                    ),
                    shape = RoundedCornerShape(size = 8.dp),
                    interactionSource = clearInteractionSource,
                    elevation = null  // 禁用按钮阴影效果
                ) {
                    Text(text = "Clear")
                }
                Spacer(Modifier.size(16.dp))
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp, minWidth = 72.dp),
                    onClick = onSignatureConfirm,
                    enabled = lineState.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bdoColorBtnPrimary,
                        contentColor = bdoColorPrimaryForeground,
                    ),
                    shape = RoundedCornerShape(size = 8.dp),
                    interactionSource = confirmInteractionSource,
                    elevation = null  // 禁用按钮阴影效果
                ) {
                    Text(text = "OK")
                }
            }
        }
    }
}

// util for display text
@ReadOnlyComposable
@Composable
private fun UiState.toBdoDisplay() = when (this) {
    is UiState.Preparing.Idle -> "Preparing reader"
    is UiState.Preparing -> "Preparing reader"

    is UiState.Awaiting.Idle -> "Tap card on the\nback of the device"
    is UiState.Awaiting.Retryable -> when (this.res) {
        com.theminesec.sdk.headless.R.string.ui_state_desc_awaiting_retryable_unsupported_payment ->
            "Card not supported\nAsk for another card"
        else -> stringResource(res ?: com.theminesec.sdk.headless.R.string.ui_state_desc_awaiting_retryable_default_invalid_read)
    }

    is UiState.Processing -> "We're working on it!"

    else -> "Else"
}

// theme
private val bdoColorPrimary = Color(0xFF004EA8)
private val bdoColorPrimaryForeground = Color(0xFFFFFFFF)
private val bdoColorBtnPrimary = Color(0xFF0072D8)
private val bdoColorBtnSecondary = Color(0xFFE5F5FF)
private val bdoColorBorderPrimary = Color(0xFF99C1E7)
private val bdoColorDesc = Color(0xFF656565)

private object BdoTypeStyles {
    private val defaultTypography = Typography()

    private val nunito = FontFamily(
        Font(R.font.nunito_regular, FontWeight.Normal),
        Font(R.font.nunito_bold, FontWeight.Bold),
    )

    val body = defaultTypography.bodySmall.copy(
        fontFamily = nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val titleamount = defaultTypography.bodySmall.copy(
        fontFamily = nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 36.sp,
        textAlign = TextAlign.Left,
    )
    val title = defaultTypography.bodySmall.copy(
        fontFamily = nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        textAlign = TextAlign.Left,
    )
    val instruction = defaultTypography.titleMedium.copy(
        fontFamily = nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    )
    val promoBanner = defaultTypography.bodySmall.copy(
        fontFamily = nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        textAlign = TextAlign.Left,
    )
}

// shared component
@Composable
fun BdoTopEdgeSection(
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)
) {
    val context = LocalContext.current as ComponentActivity
    SideEffect {
        context.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(bdoColorPrimary.toArgb())
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bdoColorPrimary)
            .padding(
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

@Composable
fun BdoToolbar(
    onAbort: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        onAbort?.let {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onAbort
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_general_top_back_default),
                    contentDescription = "abort",
                    tint = bdoColorPrimaryForeground
                )
            }
        }
        Spacer(Modifier.weight(1f, true))
        Image(
            painter = painterResource(R.drawable.logo_bdo_checkout_horizontal),
            contentDescription = "BDO checkout "
        )
        Spacer(Modifier.size(16.dp))
    }
}

@Composable
fun BdoPromoBanner(
    text: String = "BDO BUY NOW, PAY LATER",
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        text = text,
        style = BdoTypeStyles.promoBanner,
        color = bdoColorPrimaryForeground,
    )
}

@Composable
fun BdoAmountDisplay(
    amount: Amount
) {
    Spacer(Modifier.height(36.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start, // 左对齐
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.et_code_money),
            style = BdoTypeStyles.titleamount,
            color = bdoColorPrimaryForeground,
        )
        Spacer(Modifier.height(8.dp))
        val amountNumberText = amount.toDisplayString(locale = Locale.US, showCurrency = false)
        Text(
            text = "${amount.currency.currencyCode.uppercase()} $amountNumberText",
            style = if (amountNumberText.length > 9) BdoTypeStyles.title.copy(fontSize = 22.sp) else BdoTypeStyles.title,
            color = bdoColorPrimaryForeground,
            softWrap = false,
        )
    }
    Spacer(Modifier.height(36.dp))
}


@Composable
fun AwaitCardGif() {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }
    AsyncImage(
        model = ImageRequest.Builder(context).data(R.drawable.awaitcard).build(),
        contentDescription = "Await card",
        imageLoader = imageLoader,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.size(240.dp),
    )
}

@Composable
fun LoadingGif() {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }
    AsyncImage(
        model = ImageRequest.Builder(context).data(R.drawable.loading).build(),
        contentDescription = "Loading",
        imageLoader = imageLoader,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.size(144.dp),
    )
}

@Composable
fun BottomSpacer() {
    Spacer(Modifier.size(48.dp))
}
