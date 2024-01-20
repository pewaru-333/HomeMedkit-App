package ru.application.homemedkit.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ErrorCallback;

import ru.application.homemedkit.R;
import ru.application.homemedkit.connectionController.RequestAPI;
import ru.application.homemedkit.graphics.Snackbars;

public class ScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);

        requestCameraPermission();

        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setErrorCallback(new CodeEncodeError());
        mCodeScanner.setDecodeCallback(new RequestAPI(this));
        mCodeScanner.startPreview();
    }


    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private class CodeEncodeError implements ErrorCallback {
        @Override
        public void onError(@NonNull Throwable thrown) {
            new Snackbars(ScannerActivity.this).encodeError();
        }
    }
}
