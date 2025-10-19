package com.theminesec.example.headless.landing

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.theminesec.example.headless.landing.ui.HelperLayout
import com.theminesec.example.headless.landing.ui.component.ThemedButton
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.common.toCurrency
import com.theminesec.lib.dto.poi.CvmSignatureMode
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.poi.Referencable
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.HeadlessService
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

abstract class LandingMain : ComponentActivity() {
    abstract val headlessImplClass: Class<out HeadlessActivity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // After each pin entry this get called, which means host activity gets re-created
        Log.d("LifeCycleDebug", "ON CREATE")

        val viewModel: HelperViewModel by viewModels()
        var completedSaleTranId: String? by mutableStateOf(null)
        var completedSalePosReference: String? by mutableStateOf(null)
        var completedSaleRequestId: String? by mutableStateOf(null)
        var completedRefundTranId: String? by mutableStateOf(null)


        lifecycleScope.launch {
            val status = (application as ExampleApp).sdkInitStatus.first()
            viewModel.writeMessage("SDK Init: $status")
        }

        val launcher = registerForActivityResult(
            HeadlessActivity.contract(headlessImplClass)
        ) {
            viewModel.resetRandomPosReference()
            viewModel.writeMessage("ActivityResult: $it")
            when (it) {
                is WrappedResult.Success -> {
                    if (it.value.tranType == TranType.SALE) {
                        completedSaleTranId = it.value.tranId
                        completedSalePosReference = it.value.posReference
                        completedSaleRequestId = it.value.actions.firstOrNull()?.requestId
                    }
                    if (it.value.tranType == TranType.REFUND) {
                        completedRefundTranId = it.value.tranId
                    }
                }

                is WrappedResult.Failure -> {
                    viewModel.writeMessage("Failed")
                }
            }
        }

        setContent {
            HelperLayout {
                ThemedButton(onClick = {
                    lifecycleScope.launch {
                        val status = (application as ExampleApp).sdkInitStatus.first()
                        viewModel.writeMessage("SDK Init: $status")
                    }
                }) {
                    Text(text = "Get SDK status")
                }

                ThemedButton(onClick = {
                    lifecycleScope.launch {
                        val res = HeadlessSetup.initialSetup(this@LandingMain)
                        viewModel.writeMessage("initialSetup: $res")
                    }
                }) {
                    Text(text = "Initial setups (keys etc)")
                }

                HorizontalDivider()

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "ProfileId") },
                    value = viewModel.profileId,
                    onValueChange = {},
                    enabled = false,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "Currency") },
                        value = viewModel.currency,
                        onValueChange = {},
                        enabled = false
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "Amount") },
                        value = viewModel.amountStr,
                        onValueChange = viewModel::handleInputAmt,
                        isError = viewModel.amountStr.isEmpty(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "POS message ID") },
                    value = viewModel.posReference,
                    onValueChange = { viewModel.posReference = it },
                )
                TextButton(
                    onClick = { viewModel.resetRandomPosReference() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = "Set random POS message ID")
                }

                var ttod by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { ttod = !ttod },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Switch(ttod, { ttod = it })
                    Text(text = "TTOD (Tap to own device)")
                }

                var signOnPaper by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { signOnPaper = !signOnPaper },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Switch(signOnPaper, { signOnPaper = it })
                    Text(text = "Signature on paper")
                }

                var forceFetchProfile by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { forceFetchProfile = !forceFetchProfile },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Switch(forceFetchProfile, { forceFetchProfile = it })
                    Text(text = "Force fetch profile")
                }

                ThemedButton(onClick = {
                    launcher.launch(
                        PoiRequest.ActionNew(
                            tranType = TranType.SALE,
                            amount = Amount(
                                value = viewModel.amountStr.toBigDecimal(),
                                currency = viewModel.currency.toCurrency(),
                            ),
                            profileId = viewModel.profileId,
                            tapToOwnDevice = ttod,
                            posReference = viewModel.posReference,
                            forceFetchProfile = forceFetchProfile,
                            cvmSignatureMode = if (signOnPaper) CvmSignatureMode.SIGN_ON_PAPER else CvmSignatureMode.ELECTRONIC_SIGNATURE,
                            //preferredAcceptanceTag = "SME",
                            //forcePaymentMethod = listOf(PaymentMethod.VISA, PaymentMethod.MASTERCARD),
                            //description = "description 123",
                            extra = buildMap {
                                put("extra1", "value1")
                                put("extra2", "value2")
                            }
                        )
                    )
                }) {
                    Text(text = "PoiRequest.ActionNew")
                }

                HorizontalDivider()
                Text(text = "With UI\nActivity result launcher", style = MaterialTheme.typography.titleLarge)

                Text(text = "Last SALE: $completedSaleTranId")
                Text(text = "Last REFUND: $completedRefundTranId")

                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionVoid(it)
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionVoid")
                }
                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(it)
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (full amt)")
                }
                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(
                            it,
                            Amount(BigDecimal("0.5"), Currency.getInstance(viewModel.currency))
                        )
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (partial amt)")
                }
                ThemedButton(onClick = {
                    completedRefundTranId?.let {
                        val action = PoiRequest.ActionVoid(it)
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionVoid (using refund tran id)")
                }
                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val query = PoiRequest.Query(Referencable.TranId(it))
                        launcher.launch(query)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.Query (TranId)")
                }
                ThemedButton(onClick = {
                    completedSalePosReference?.let {
                        val query = PoiRequest.Query(Referencable.PosReference(it))
                        launcher.launch(query)
                    } ?: viewModel.writeMessage("No pos ref")
                }) {
                    Text(text = "PoiRequest.Query (PosReference)")
                }
                ThemedButton(onClick = {
                    completedSaleRequestId?.let {
                        val query = PoiRequest.Query(Referencable.RequestId(it))
                        launcher.launch(query)
                    } ?: viewModel.writeMessage("No poi req id")
                }) {
                    Text(text = "PoiRequest.Query (RequestId)")
                }

                HorizontalDivider()
                Text(text = "Without UI\nSuspended API", style = MaterialTheme.typography.titleLarge)

                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionVoid(it)
                        lifecycleScope.launch {
                            HeadlessService.createAction(action).also { viewModel.writeMessage("action result: $it") }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionVoid")
                }
                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(it)
                        lifecycleScope.launch {
                            HeadlessService.createAction(action).also { viewModel.writeMessage("action result: $it") }
                        }
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (full amt)")
                }
                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(it, Amount(BigDecimal("0.5"), Currency.getInstance(viewModel.currency)))
                        lifecycleScope.launch {
                            HeadlessService.createAction(action).also { viewModel.writeMessage("action result: $it") }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (partial amt)")
                }
                ThemedButton(onClick = {
                    completedSaleTranId?.let {
                        val query = Referencable.TranId(it)
                        lifecycleScope.launch {
                            HeadlessService.getTransaction(query).also { viewModel.writeMessage("query result: $it") }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.Query (TranId)")
                }
                ThemedButton(onClick = {
                    completedSalePosReference?.let {
                        val query = Referencable.PosReference(it)
                        lifecycleScope.launch {
                            HeadlessService.getTransaction(query).also { viewModel.writeMessage("query result: $it") }
                        }
                    } ?: viewModel.writeMessage("No pos ref")
                }) {
                    Text(text = "PoiRequest.Query (PosReference)")
                }
                ThemedButton(onClick = {
                    completedSaleRequestId?.let {
                        val query = Referencable.RequestId(it)
                        lifecycleScope.launch {
                            HeadlessService.getTransaction(query).also { viewModel.writeMessage("query result: $it") }
                        }
                    } ?: viewModel.writeMessage("No req id")
                }) {
                    Text(text = "PoiRequest.Query (RequestId)")
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        claimNfcForeground()
    }

    private fun claimNfcForeground() {
        NfcAdapter
            .getDefaultAdapter(this)
            .enableForegroundDispatch(
                /* activity = */ this,
                /* intent = */ PendingIntent.getActivity(
                    this, 0, Intent(this, this.javaClass).addFlags(
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    ), PendingIntent.FLAG_MUTABLE
                ),
                /* filters = */ arrayOf(
                    IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                    IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
                ),
                /* techLists = */ arrayOf(
                    arrayOf(
                        NfcA::class.java.name,
                        IsoDep::class.java.name
                    )
                )
            )
    }
}

