package com.theminesec.example.headless_xml

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.theminesec.example.headless_xml.databinding.CompAmountDisplayBinding
import com.theminesec.example.headless_xml.databinding.CompOrSignatureScreenBinding
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.transaction.Amount
import com.theminesec.sdk.headless.model.transaction.PaymentMethod
import com.theminesec.sdk.headless.ui.*
import com.theminesec.sdk.headless.ui.component.SignaturePad
import com.theminesec.sdk.headless.ui.component.SignatureState
import com.theminesec.sdk.headless.ui.component.resource.getImageRes
import kotlinx.coroutines.launch

class ClientHeadlessImpl : HeadlessActivity() {
    override fun provideTheme(): ThemeProvider {
        return ClientThemeProvider()
    }

    override fun provideUi(): UiProvider {
        return UiProvider(
            amountView = ClientViewProvider,
            progressIndicatorView = ClientViewProvider,
            awaitCardIndicatorView = ClientViewProvider,
            acceptanceMarksView = ClientViewProvider,
            signatureScreenView = ClientViewProvider
        )
    }
}

@SuppressLint("SetTextI18n")
object ClientViewProvider :
    AmountView,
    ProgressIndicatorView,
    AwaitCardIndicatorView,
    AcceptanceMarksView,
    SignatureScreenView {

    private fun Int.intToDp(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    override fun createAmountView(context: Context, amount: Amount, description: String?): View {
        val inflater = LayoutInflater.from(context)
        return DataBindingUtil.inflate<CompAmountDisplayBinding>(inflater, R.layout.comp_amount_display, null, false)
            .apply {
                root.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
                this.amount = amount
                this.description = "dummy description"
            }
            .root

        // return LinearLayout(context).apply {
        //     orientation = LinearLayout.VERTICAL
        //     layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        //     addView(TextView(context)
        //         .apply {
        //             textSize = 30F
        //             textAlignment = TEXT_ALIGNMENT_CENTER
        //             text = "Total amount here"
        //         })
        //     addView(TextView(context)
        //         .apply {
        //             textSize = 40F
        //             textAlignment = TEXT_ALIGNMENT_CENTER
        //             text = "${amount.currency.currencyCode}${amount.value}"
        //         })
        // }
    }

    override fun createAwaitCardIndicatorView(context: Context): View {
        val mediaPlayer = MediaPlayer()
        return TextureView(context).apply {
            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    val videoUri = Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(context.packageName)
                        .appendPath("${R.raw.demo_await}")
                        .build()

                    mediaPlayer.apply {
                        setDataSource(context, videoUri)
                        setSurface(Surface(surface))
                        isLooping = true
                        prepareAsync()
                        setOnPreparedListener { start() }
                    }
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        }
    }

    override fun createAcceptanceMarksView(context: Context, supportedPayments: List<PaymentMethod>, showWallet: Boolean): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL

            val iconLp = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            ).apply {
                marginEnd = 16.intToDp(context)
            }

            supportedPayments.forEach { pm ->
                addView(ImageView(context).apply {
                    setImageResource(pm.getImageRes()).apply {
                        layoutParams = iconLp
                    }
                })
            }
            if (showWallet) {
                addView(ImageView(context).apply {
                    setImageResource(PaymentMethod.APPLE_PAY.getImageRes())
                })
            }
        }
    }

    override fun createProgressIndicatorView(context: Context): View {
        val mediaPlayer = MediaPlayer()
        return TextureView(context).apply {
            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    val videoUri = Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(context.packageName)
                        .appendPath("${R.raw.demo_processing}")
                        .build()

                    mediaPlayer.apply {
                        setDataSource(context, videoUri)
                        setSurface(Surface(surface))
                        isLooping = true
                        prepareAsync()
                        setOnPreparedListener { start() }
                    }
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        }
        // return CircularProgressIndicator(context).apply {
        //     layoutParams = LayoutParams(
        //         LayoutParams.MATCH_PARENT,
        //         LayoutParams.MATCH_PARENT
        //     )
        //     indicatorSize = 200.intToDp(context)
        //     trackThickness = 8.intToDp(context)
        //     isIndeterminate = true
        //     trackColor = Color.TRANSPARENT
        //     setIndicatorColor(Color.parseColor("#FFD503"))
        //     isVisible = true
        // }
    }

    override fun createSignatureView(
        context: Context,
        amount: Amount?,
        maskedAccount: String?,
        paymentMethod: PaymentMethod?,
        approvalCode: String?,
        signatureState: SignatureState,
        onConfirm: () -> Unit
    ): View {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<CompOrSignatureScreenBinding>(inflater, R.layout.comp_or_signature_screen, null, false)
            .apply {
                root.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
                this.amount = amount
                this.maskedAccount = maskedAccount
                this.paymentMethod = paymentMethod
                this.approvalCode = approvalCode
            }

        binding.composeViewSignaturePad.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.Default)
            setContent { SignaturePad(state = signatureState) }
        }

        binding.btnClear.apply {
            (context as LifecycleOwner).lifecycleScope.launch {
                signatureState.signatureLines.collect {
                    isEnabled = it.isNotEmpty()
                }
            }
            setOnClickListener { signatureState.clearSignatureLines() }
        }
        binding.btnConfirm.apply {
            (context as LifecycleOwner).lifecycleScope.launch {
                signatureState.signatureLines.collect {
                    isEnabled = it.isNotEmpty()
                }
            }
            setOnClickListener { onConfirm() }
        }

        return binding.root
    }

}

class ClientThemeProvider : ThemeProvider() {
    override fun provideColors(darkTheme: Boolean): HeadlessColors {
        return HeadlessColorsLight().copy(
            primary = Color.parseColor("#FFD503"),
            foreground = Color.parseColor("#333333"),
            mutedForeground = Color.parseColor("#4E3C2E")
        )
    }
}
