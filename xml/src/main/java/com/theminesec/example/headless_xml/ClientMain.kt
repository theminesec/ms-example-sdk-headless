package com.theminesec.example.headless_xml

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.theminesec.example.headless_xml.databinding.ActivityMainBinding
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.CvmSignatureMode
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.PaymentMethod
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.HeadlessSetup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ulid.ULID
import java.util.*

class ClientMain : AppCompatActivity() {

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    private val launcher = registerForActivityResult(
        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
    ) {
        Log.d(TAG, "MainActivity launcher result back for WrappedResult<Transaction>: $it}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.view = this
    }

    fun checkInitStatus() = lifecycleScope.launch {
        val sdkInitStatus = (application as ClientApp).sdkInitStatus.first()
        Log.d(TAG, "checkInitStatus: $sdkInitStatus")
    }

    fun initialSetup() = lifecycleScope.launch {
        val res = HeadlessSetup.initialSetup(this@ClientMain)
        Log.d(TAG, "setupInitial: $res")
    }

    fun launchNewSale() {
        launcher.launch(
            PoiRequest.ActionNew(
                tranType = TranType.SALE,
                amount = Amount(
                    "10.00".toBigDecimal(),
                    Currency.getInstance("USD"),
                ),
                profileId = "prof_01HYYPGVE7VB901M40SVPHTQ0V",
                preferredAcceptanceTag = "SME",
                forcePaymentMethod = null,
                description = "description 123",
                posReference = "pos_${ULID.randomULID()}",
                forceFetchProfile = true,
                cvmSignatureMode = CvmSignatureMode.ELECTRONIC_SIGNATURE
            )
        )
    }

    fun launchNewSaleWithSign() {
        launcher.launch(
            PoiRequest.ActionNew(
                tranType = TranType.SALE,
                amount = Amount(
                    "1001.00".toBigDecimal(),
                    Currency.getInstance("USD"),
                ),
                profileId = "prof_01HYYPGVE7VB901M40SVPHTQ0V",
                preferredAcceptanceTag = "SME",
                forcePaymentMethod = listOf(PaymentMethod.VISA, PaymentMethod.MASTERCARD),
                description = "description 123",
                posReference = "pos_${ULID.randomULID()}",
                forceFetchProfile = true,
                cvmSignatureMode = CvmSignatureMode.ELECTRONIC_SIGNATURE
            )
        )
    }

    fun launchNewSaleWrongProfile() {
        launcher.launch(
            PoiRequest.ActionNew(
                tranType = TranType.SALE,
                amount = Amount(
                    "20.00".toBigDecimal(),
                    Currency.getInstance("USD"),
                ),
                profileId = "wrong profile",
                forceFetchProfile = false
            )
        )
    }
}
