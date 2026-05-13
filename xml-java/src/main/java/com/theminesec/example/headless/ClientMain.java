package com.theminesec.example.headless;

import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.theminesec.example.headless.databinding.ActivityMainBinding;
import com.theminesec.example.headless.landing.BaseConfig;
import com.theminesec.lib.dto.common.Amount;
import com.theminesec.lib.dto.poi.CvmSignatureMode;
import com.theminesec.lib.dto.poi.PoiRequest;
import com.theminesec.lib.dto.transaction.TranType;
import com.theminesec.sdk.headless.HeadlessActivity;
import com.theminesec.sdk.headless.HeadlessSetup;
import com.theminesec.sdk.headless.model.WrappedResult;
import com.theminesec.sdk.headless.model.setup.SdkInitResp;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class ClientMain extends AppCompatActivity {
    private final ActivityResultLauncher<PoiRequest> launcher = registerForActivityResult(
            HeadlessActivity.Companion.contract(ClientHeadlessImpl.class),
            result -> Log.d(Constants.TAG, "MainActivity launcher result back for WrappedResult<Transaction>: " + result.toString())
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.setView(this);
    }

    public void checkInitStatus() {
        WrappedResult<SdkInitResp> sdkInitStatus = ((ClientApp) getApplication()).sdkInitStatus.getValue();
        if (sdkInitStatus != null) {
            Log.d(Constants.TAG, "Init status: " + sdkInitStatus);
        }
    }

    public void initialSetup() {
        HeadlessSetup.INSTANCE.initialSetupCallback(this, null, res -> {
            Log.d(Constants.TAG, "Setup res: " + res.toString());
            return null;
        });
    }

    public void launchNewSale() {
        launcher.launch(new PoiRequest.ActionNew(
                TranType.SALE,
                new Amount(
                        new BigDecimal("10.00"),
                        Currency.getInstance("HKD")
                ),
                BaseConfig.profileId,
                null,
                null,
                "description",
                UUID.randomUUID().toString(),
                null,
                CvmSignatureMode.SIGN_ON_PAPER,
                null,
                true,
                false,
                null,
                null,
                null,
                null
        ));
    }

    public void launchNewSaleWithSign() {
        launcher.launch(new PoiRequest.ActionNew(
                TranType.SALE,
                new Amount(
                        new BigDecimal("1001.00"),
                        Currency.getInstance("HKD")
                ),
                BaseConfig.profileId,
                null,
                null,
                "description",
                UUID.randomUUID().toString(),
                null,
                CvmSignatureMode.SIGN_ON_PAPER,
                null,
                false,
                false,
                null,
                null,
                null,
                null
        ));
    }

    public void launchNewSaleWrongProfile() {
        launcher.launch(new PoiRequest.ActionNew(
                TranType.SALE,
                new Amount(
                        BigDecimal.ONE,
                        Currency.getInstance("HKD")
                ),
                "wrong profile",
                null,
                null,
                "description",
                UUID.randomUUID().toString(),
                null,
                CvmSignatureMode.SIGN_ON_PAPER,
                null,
                false,
                false,
                null,
                null,
                null,
                null
        ));
    }
}
