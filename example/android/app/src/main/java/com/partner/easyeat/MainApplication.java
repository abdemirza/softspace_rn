package com.partner.easyeat;

import static my.com.softspace.ssmpossdk.transaction.MPOSTransaction.TransactionEvents.TransactionResult.TransactionFailed;
import static my.com.softspace.ssmpossdk.transaction.MPOSTransaction.TransactionEvents.TransactionResult.TransactionSuccessful;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.facebook.react.PackageList;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactNativeHost;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.facebook.soloader.SoLoader;
import com.visa.CheckmarkMode;
import com.visa.CheckmarkTextOption;
import com.visa.SensoryBrandingView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import my.com.softspace.ssmpossdk.Environment;
import my.com.softspace.ssmpossdk.SSMPOSSDK;
import my.com.softspace.ssmpossdk.SSMPOSSDKConfiguration;
import my.com.softspace.ssmpossdk.transaction.MPOSTransaction;
import my.com.softspace.ssmpossdk.transaction.MPOSTransactionOutcome;
import my.com.softspace.ssmpossdk.transaction.MPOSTransactionParams;

public class MainApplication extends Application implements ReactApplication {

  private static Activity currentActivity;
  private final ReactNativeHost mReactNativeHost = new DefaultReactNativeHost(
    this
  ) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      @SuppressWarnings("UnnecessaryLocalVariable")
      List<ReactPackage> packages = new PackageList(this).getPackages();
      // Packages that cannot be autolinked yet can be added manually here, for example:
      return packages;
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }

    @Override
    protected boolean isNewArchEnabled() {
      return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
    }

    @Override
    protected Boolean isHermesEnabled() {
      return BuildConfig.IS_HERMES_ENABLED;
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */false);
    if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
      // If you opted-in for the New Architecture, we load the native entry point for this app.
      DefaultNewArchitectureEntryPoint.load();
    }
    ReactNativeFlipper.initializeFlipper(
      this,
      getReactNativeHost().getReactInstanceManager()
    );
    // Set up activity lifecycle callbacks to track the current activity
    registerActivityLifecycleCallbacks(
      new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(
          Activity activity,
          Bundle savedInstanceState
        ) {
          currentActivity = activity;
        }

        @Override
        public void onActivityStarted(Activity activity) {}

        @Override
        public void onActivityResumed(Activity activity) {
          currentActivity = activity;
        }

        @Override
        public void onActivityPaused(Activity activity) {}

        @Override
        public void onActivityStopped(Activity activity) {}

        @Override
        public void onActivitySaveInstanceState(
          Activity activity,
          Bundle outState
        ) {}

        @Override
        public void onActivityDestroyed(Activity activity) {
          if (activity == currentActivity) {
            currentActivity = null;
          }
        }
      }
    );
  }

  // Add your initialization method here
  private void initFasstapMPOSSDK() {
    // Replace this with your actual SDK initialization code
    try {
      // Configuration
      SSMPOSSDKConfiguration config = SSMPOSSDKConfiguration.Builder
        .create()
        .setAttestationHost(BuildConfig.ATTESTATION_HOST)
        .setAttestationHostCertPinning(BuildConfig.ATTESTATION_CERT_PINNING)
        .setAttestationHostReadTimeout(10000L)
        .setAttestationRefreshInterval(300000L)
        .setAttestationStrictHttp(true)
        .setAttestationConnectionTimeout(30000L)
        .setLibGooglePlayProjNum("262431422959") // use own google play project number
        .setLibAccessKey(BuildConfig.ACCESS_KEY)
        .setLibSecretKey(BuildConfig.SECRET_KEY)
        .setUniqueID("xdIu2XwPpPRrTSaJdZi1") // please set the userID shared by Soft Space
        .setDeveloperID("ZCh9mzZXqHzezf4")
        .setEnvironment(
          BuildConfig.FLAVOR_environment.equals("uat")
            ? Environment.UAT
            : Environment.PROD
        )
        .build();

      // SDK initialization
      SSMPOSSDK.init(this, config);

      // Request permission if required
      if (!SSMPOSSDK.hasRequiredPermission(this)) {
        if (currentActivity != null) {
          runOnUiThread(
            currentActivity,
            new Runnable() {
              @Override
              public void run() {
                SSMPOSSDK.requestPermissionIfRequired(currentActivity, 1000);
              }
            }
          );
        }
      }
    } catch (Exception e) {
      // Log.e("FasstapMPOSSDK", "Initialization error: " + e.getMessage());
    }
  }

  // Utility method to run code on the main thread
  private void runOnUiThread(Activity activity, Runnable runnable) {
    new Handler(Looper.getMainLooper()).post(runnable);
  }
}
