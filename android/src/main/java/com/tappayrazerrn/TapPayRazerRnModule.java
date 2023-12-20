package com.tappayrazerrn;

import static my.com.softspace.ssmpossdk.transaction.MPOSTransaction.TransactionEvents.TransactionResult.TransactionFailed;
import static my.com.softspace.ssmpossdk.transaction.MPOSTransaction.TransactionEvents.TransactionResult.TransactionSuccessful;

import android.Manifest;
import android.app.Activity;
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
import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.visa.CheckmarkMode;
import com.visa.CheckmarkTextOption;
import com.visa.SensoryBrandingView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import my.com.softspace.ssmpossdk.Environment;
import my.com.softspace.ssmpossdk.SSMPOSSDK;
import my.com.softspace.ssmpossdk.SSMPOSSDKConfiguration;
import my.com.softspace.ssmpossdk.transaction.MPOSTransaction;
import my.com.softspace.ssmpossdk.transaction.MPOSTransactionOutcome;
import my.com.softspace.ssmpossdk.transaction.MPOSTransactionParams;

@ReactModule(name = TapPayRazerRnModule.NAME)
public class TapPayRazerRnModule extends ReactContextBaseJavaModule {

  public static final String NAME = "TapPayRazerRn";
  private static TapPayRazerRnModule instance;
  private Activity mActivity = null;
  private static final String CARD_TYPE_VISA = "0";
  private static final String CARD_TYPE_MASTERCARD = "1";
  private static final String CARD_TYPE_AMEX = "2";
  private static final String CARD_TYPE_JCB = "3";
  private static final String CARD_TYPE_DISCOVER = "23";
  private MPOSTransactionOutcome _transactionOutcome;
  private volatile boolean isTrxRunning = false;
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private static final int NFC_PERMISSION_REQUEST_CODE = 2;
  private String TAG = NAME;

  public TapPayRazerRnModule(ReactApplicationContext reactContext) {
    super(reactContext);
    instance = this;
  }

  @Override
  public void initialize() {
    super.initialize();
  }

  @ReactMethod
  public static TapPayRazerRnModule getInstance() {
    return instance;
  }

  // Custom callback interface that includes both transaction results and UI events
  public interface EMVCallback {
    void onTransactionResult(String result);
    void onTransactionUIEvent(int event);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void initFasstapMPOSSDK(ReadableMap inputConfig) {
    final ReactApplicationContext reactContext = getReactApplicationContext();
    Log.i("ABUZAR", BuildConfig.ATTESTATION_HOST);

    String attestationHost = inputConfig.getString("attestationHost");
    String attestationCertPinning = inputConfig.getString(
      "attestationCertPinning"
    );
    String libGooglePlayProjNumStr = inputConfig.getString(
      "libGooglePlayProjNum"
    );
    String libAccessKey = inputConfig.getString("libAccessKey");
    String libSecretKey = inputConfig.getString("libSecretKey");
    String uniqueID = inputConfig.getString("uniqueID");
    String developerID = inputConfig.getString("developerID");
    String environmentValue = inputConfig.getString("environment");

    runOnUiThread(
      reactContext,
      new Runnable() {
        @Override
        public void run() {
          try {
            Environment environment = environmentValue.equals("uat")
              ? Environment.UAT
              : Environment.PROD;

            SSMPOSSDKConfiguration config = SSMPOSSDKConfiguration.Builder
              .create()
              .setAttestationHost(attestationHost)
              .setAttestationHostCertPinning(attestationCertPinning)
              .setAttestationHostReadTimeout(10000L)
              .setAttestationRefreshInterval(300000L)
              .setAttestationStrictHttp(true)
              .setAttestationConnectionTimeout(30000L)
              .setLibGooglePlayProjNum(libGooglePlayProjNumStr) // use own google play project number
              .setLibAccessKey(libAccessKey)
              .setLibSecretKey(libSecretKey)
              .setUniqueID(uniqueID) // please set the userID shared by Soft Space
              .setDeveloperID(developerID)
              .setEnvironment(environment)
              .build();

            SSMPOSSDK.init(reactContext, config);
          } catch (Exception e) {
            Log.e(TAG, e.getMessage());
          }
        }
      }
    );

    requestLocationPermission();
    requestNfcPermission();
  }

  // Utility method to run code on the main thread
  private void runOnUiThread(
    ReactApplicationContext reactContext,
    Runnable runnable
  ) {
    reactContext.runOnUiQueueThread(runnable);
  }

  // @ReactMethod
  // public void initFasstapMPOSSDK() {
  //   Context context = getReactApplicationContext();

  //   SSMPOSSDKConfiguration config = SSMPOSSDKConfiguration.Builder
  //     .create()
  //     .setAttestationHost(BuildConfig.ATTESTATION_HOST)
  //     .setAttestationHostCertPinning(BuildConfig.ATTESTATION_CERT_PINNING)
  //     .setAttestationHostReadTimeout(10000L)
  //     .setAttestationRefreshInterval(300000L)
  //     .setAttestationStrictHttp(true)
  //     .setAttestationConnectionTimeout(30000L)
  //     .setLibGooglePlayProjNum("262431422959") // use own google play project number
  //     .setLibAccessKey(BuildConfig.ACCESS_KEY)
  //     .setLibSecretKey(BuildConfig.SECRET_KEY)
  //     .setUniqueID("xdIu2XwPpPRrTSaJdZi1") // please set the userID shared by Soft Space
  //     .setDeveloperID("ZCh9mzZXqHzezf4")
  //     .setEnvironment(
  //       BuildConfig.FLAVOR_environment.equals("uat")
  //         ? Environment.UAT
  //         : Environment.PROD
  //     )
  //     .build();

  //   SSMPOSSDK.init(context, config);

  //   if (!SSMPOSSDK.hasRequiredPermission(context)) {
  //     // Assuming the method takes only the context as a parameter
  //     SSMPOSSDK.requestPermissionIfRequired(context);
  //   }
  // }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  private void writeLog(String a) {}

  @ReactMethod
  // public void performSettlement() {
  //   try {
  //     MPOSTransactionParams transactionalParams = MPOSTransactionParams.Builder
  //       .create()
  //       .build();
  //     ReactActivity reactActivity = (ReactActivity) getCurrentActivity();
  //     SSMPOSSDK
  //       .getInstance()
  //       .getTransaction()
  //       .performSettlement(
  //         reactActivity,
  //         transactionalParams,
  //         new MPOSTransaction.TransactionEvents() {
  //           @Override
  //           public void onTransactionResult(
  //             int result,
  //             MPOSTransactionOutcome transactionOutcome
  //           ) {
  //             // runOnUiThread(
  //             //   new Runnable() {
  //             //     @Override
  //             //     public void run() {
  //             //       // writeLog("onTransactionResult :: " + result);
  //             //       if (
  //             //         result != TransactionSuccessful &&
  //             //         transactionOutcome != null
  //             //       ) {
  //             //         // writeLog(
  //             //         //   transactionOutcome.getStatusCode() +
  //             //         //   " - " +
  //             //         //   transactionOutcome.getStatusMessage()
  //             //         // );
  //             //       }
  //             //     }
  //             //   }
  //             // );
  //           }

  //           @Override
  //           public void onTransactionUIEvent(int event) {
  //             // runOnUiThread(
  //             //   new Runnable() {
  //             //     @Override
  //             //     public void run() {
  //             //       // writeLog("onTransactionUIEvent :: " + event);
  //             //     }
  //             //   }
  //             // );
  //           }
  //         }
  //       );
  //   } catch (Exception e) {}
  // }

  private void toggleTransactionRunning(boolean isRunning) {
    if (isRunning) {
      isTrxRunning = true;
      // btnStartTrx.setText("Cancel\nTransaction");
      // if (btnRefundTrx.isEnabled()) btnRefundTrx.setText(
      //   "Cancel\nRefund Transaction"
      // );
    } else {
      isTrxRunning = false;
      // btnStartTrx.setText("Start\nTransaction");
      // btnRefundTrx.setText("Refund\nTransaction");
      // btnUploadSignature.setEnabled(false);
    }
  }

  @ReactMethod
  private void refreshToken() {
    try {
      final Activity currentActivity = getCurrentActivity();
      // String userId = edtUserID.getText().toString();
      // String developerId = edtDeveloperID.getText().toString();

      SSMPOSSDKConfiguration sdkConfig = SSMPOSSDK
        .getInstance()
        .getSSMPOSSDKConfiguration();
      sdkConfig.uniqueID = "xdIu2XwPpPRrTSaJdZi1";
      sdkConfig.developerID = "ZCh9mzZXqHzezf4";

      SSMPOSSDK
        .getInstance()
        .getTransaction()
        .refreshToken(
          currentActivity,
          new MPOSTransaction.TransactionEvents() {
            @Override
            public void onTransactionResult(
              int result,
              MPOSTransactionOutcome transactionOutcome
            ) {
              showToast("result :: " + result);
              // handleRefreshTokenResult(result, transactionOutcome);
            }

            @Override
            public void onTransactionUIEvent(int event) {}
          }
        );
    } catch (Exception e) {
      showToast("Exception " + e.getMessage());
    }
  }

  private void handleRefreshTokenResult(
    int result,
    MPOSTransactionOutcome transactionOutcome
  ) {
    writeLog("onTransactionResult :: " + result);
    if (result == TransactionSuccessful) {
      // enableTransactionButtons(true);
    } else {
      handleTransactionFailure(transactionOutcome);
    }
  }

  private void handleTransactionFailure(
    MPOSTransactionOutcome transactionOutcome
  ) {
    // btnStartTrx.setEnabled(false);
    // btnVoidTrx.setEnabled(false);
    // btnGetTransactionStatus.setEnabled(false);
    // btnSettlement.setEnabled(false);
    showToast("handleTransactionFailure running");
    if (transactionOutcome != null) {
      writeLog(
        transactionOutcome.getStatusCode() +
        " - " +
        transactionOutcome.getStatusMessage()
      );
    }
  }

  private void enableTransactionButtons(boolean enable) {
    // btnStartTrx.setEnabled(enable);
    // btnVoidTrx.setEnabled(!enable);
    // btnGetTransactionStatus.setEnabled(!enable);
    // btnSettlement.setEnabled(enable);
  }

  @ReactMethod
  public void checkAndRequestNFCPermission(final Promise promise) {
    ReactActivity reactActivity = (ReactActivity) getCurrentActivity();

    if (reactActivity == null) {
      // Handle the case when the activity is not available
      promise.reject("ACTIVITY_NOT_AVAILABLE", "Activity is not available");
      return;
    }

    // Check if NFC permission is granted
    if (
      ContextCompat.checkSelfPermission(
        reactActivity,
        Manifest.permission.NFC
      ) ==
      PackageManager.PERMISSION_GRANTED
    ) {
      promise.resolve("NFC permission is granted");
    } else {
      // Request NFC permission
      final int PERMISSION_REQUEST_CODE = 1;

      PermissionListener permissionListener = new PermissionListener() {
        @Override
        public boolean onRequestPermissionsResult(
          int requestCode,
          String[] permissions,
          int[] grantResults
        ) {
          if (requestCode == PERMISSION_REQUEST_CODE) {
            if (
              grantResults.length > 0 &&
              grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
              promise.resolve("NFC permission granted after request");
            } else {
              promise.reject("NFC_PERMISSION_DENIED", "NFC permission denied");
            }
            return true;
          }
          return false;
        }
      };

      ((PermissionAwareActivity) reactActivity).requestPermissions(
          new String[] { Manifest.permission.NFC },
          PERMISSION_REQUEST_CODE,
          permissionListener
        );
    }
  }

  @ReactMethod
  public void performSettlement(final Promise promise) {
    try {
      MPOSTransactionParams transactionalParams = MPOSTransactionParams.Builder
        .create()
        .build();

      SSMPOSSDK
        .getInstance()
        .getTransaction()
        .performSettlement(
          getCurrentActivity(),
          transactionalParams,
          new MPOSTransaction.TransactionEvents() {
            @Override
            public void onTransactionResult(
              final int result,
              final MPOSTransactionOutcome transactionOutcome
            ) {
              new Handler(Looper.getMainLooper())
                .post(
                  new Runnable() {
                    @Override
                    public void run() {
                      if (
                        result != TransactionSuccessful &&
                        transactionOutcome != null
                      ) {
                        // showToast(
                        //   transactionOutcome.getStatusCode() +
                        //   " - " +
                        //   transactionOutcome.getStatusMessage()
                        // );
                      }

                      // Resolve the promise with the result
                      if (promise != null) {
                        promise.resolve(result);
                      }
                    }
                  }
                );
            }

            @Override
            public void onTransactionUIEvent(final int event) {
              new Handler(Looper.getMainLooper())
                .post(
                  new Runnable() {
                    @Override
                    public void run() {}
                  }
                );
            }
          }
        );
    } catch (Exception e) {
      // Reject the promise with an error message
      if (promise != null) {
        promise.reject("PERFORM_SETTLEMENT_ERROR", e.getMessage());
        showToast("PERFORM_SETTLEMENT_ERROR" + e.getMessage());
      }
    }
  }

  private void showToast(String message) {
    Toast
      .makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG)
      .show();
  }

  @ReactMethod
  public void requestLocationPermission() {
    if (!checkLocationPermission()) {
      ActivityCompat.requestPermissions(
        getCurrentActivity(),
        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
        LOCATION_PERMISSION_REQUEST_CODE
      );
    }
  }

  public void requestNfcPermission() {
    if (!checkNfcPermission()) {
      ActivityCompat.requestPermissions(
        getCurrentActivity(),
        new String[] { Manifest.permission.NFC },
        NFC_PERMISSION_REQUEST_CODE
      );
    }
  }

  private boolean checkNfcPermission() {
    int permissionResult = ContextCompat.checkSelfPermission(
      getReactApplicationContext(),
      Manifest.permission.NFC
    );
    return permissionResult == PackageManager.PERMISSION_GRANTED;
  }

  private boolean checkLocationPermission() {
    int permissionResult = ContextCompat.checkSelfPermission(
      getReactApplicationContext(),
      Manifest.permission.ACCESS_FINE_LOCATION
    );
    return permissionResult == PackageManager.PERMISSION_GRANTED;
  }

  @ReactMethod
  public void startEMVProcessing(
    String amount,
    String referenceNumberr,
    final Promise promise
  ) {
    final String ACTIVITY_NULL_ERROR = "ACTIVITY_NULL";
    final String INVALID_AMOUNT_ERROR = "INVALID_AMOUNT";
    final String TRANSACTION_FAILED_ERROR = "TRANSACTION_FAILED";
    final String CARD_READ_ERROR_MSG = "CARD_READ_ERROR";
    final String CARD_READ_RETRY_ERROR = "CARD_READ_RETRY";
    final String EXCEPTION_ERROR = "EXCEPTION";
    final String TRANSACTION_EVENT = "TransactionEvent";
    final int TRANSACTION_SUCCESSFUL = 1;
    final int TRANSACTION_FAILED = 2;
    final int PRESENT_CARD = 3;
    final int AUTHORISING = 4;
    final int CARD_DETECTED = 5;
    final int CARD_READ_ERROR = 6;
    final int CARD_READ_RETRY = 7;
    final int CARD_READ_COMPLETED = 8;

    ReactActivity currentActivity = (ReactActivity) getCurrentActivity();
    String referenceNumber = "SS" + Calendar.getInstance().getTimeInMillis();
    // Add your event data to the params if needed

    if (currentActivity == null) {
      showToast("ACTIVITY_NULL");
      return;
    }

    if (amount != null && Double.parseDouble(amount) <= 0) {
      getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("TransactionEvent", INVALID_AMOUNT_ERROR);
      return;
    }

    currentActivity.runOnUiThread(
      new Runnable() {
        @Override
        public void run() {
          writeLog("Amount, Authorised: " + amount);
        }
      }
    );
    try {
      _transactionOutcome = null;
      MPOSTransactionParams transactionalParams = MPOSTransactionParams.Builder
        .create()
        .setReferenceNumber(referenceNumber)
        .setAmount(amount)
        .build();
      SSMPOSSDK
        .getInstance()
        .getTransaction()
        .startTransaction(
          currentActivity,
          transactionalParams,
          new MPOSTransaction.TransactionEvents() {
            @Override
            public void onTransactionResult(
              int result,
              MPOSTransactionOutcome transactionOutcome
            ) {
              _transactionOutcome = transactionOutcome;
              // currentActivity.runOnUiThread(
              //   new Runnable() {
              //     @Override
              //     public void run() {
              // Handle transaction result
              if (result == TransactionSuccessful) {
                // Handle successful transaction
                getReactApplicationContext()
                  .getJSModule(
                    DeviceEventManagerModule.RCTDeviceEventEmitter.class
                  )
                  .emit("TransactionEvent", TRANSACTION_SUCCESSFUL);
              } else if (result == TransactionFailed) {
                // Handle failed transaction
                getReactApplicationContext()
                  .getJSModule(
                    DeviceEventManagerModule.RCTDeviceEventEmitter.class
                  )
                  .emit("TransactionEvent", TRANSACTION_FAILED);
                if (transactionOutcome != null) {
                  String outcome =
                    transactionOutcome.getStatusCode() +
                    " - " +
                    transactionOutcome.getStatusMessage();
                  if (
                    transactionOutcome.getTransactionID() != null &&
                    transactionOutcome.getTransactionID().length() > 0
                  ) {
                    outcome +=
                      "\nTransaction ID :: " +
                      transactionOutcome.getTransactionID() +
                      "\n";
                    outcome +=
                      "Reference No :: " +
                      transactionOutcome.getReferenceNo() +
                      "\n";
                    outcome +=
                      "Approval code :: " +
                      transactionOutcome.getApprovalCode() +
                      "\n";
                    outcome +=
                      "Card number :: " + transactionOutcome.getCardNo() + "\n";
                    outcome +=
                      "Cardholder name :: " +
                      transactionOutcome.getCardHolderName() +
                      "\n";
                    outcome +=
                      "Acquirer ID :: " +
                      transactionOutcome.getAcquirerID() +
                      "\n";
                    outcome +=
                      "RRN :: " + transactionOutcome.getRrefNo() + "\n";
                    outcome +=
                      "Trace No :: " + transactionOutcome.getTraceNo() + "\n";
                    outcome +=
                      "Transaction Date Time UTC :: " +
                      transactionOutcome.getTransactionDateTime();
                  }
                  showToast("TRANSACTION_FAILED");
                } else {
                  showToast("TRANSACTION_FAILED" + "Error :: " + result);
                }
              }
              // }
              // }
              // );
              // toggleTransactionRunning(false);
            }

            @Override
            public void onTransactionUIEvent(int event) {
              // currentActivity.runOnUiThread(
              // new Runnable() {
              // @Override
              // public void run() {
              // Handle transaction UI events
              if (event == TransactionUIEvent.CardReadOk) {
                // Handle card read OK event
                ToneGenerator toneGenerator = new ToneGenerator(
                  AudioManager.STREAM_MUSIC,
                  ToneGenerator.MAX_VOLUME
                );
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_P, 500);

                Vibrator v = (Vibrator) currentActivity.getSystemService(
                  Context.VIBRATOR_SERVICE
                );
                if (v.hasVibrator()) {
                  v.vibrate(
                    VibrationEffect.createOneShot(
                      200,
                      VibrationEffect.DEFAULT_AMPLITUDE
                    )
                  );
                }
                showToast("Card read completed");
                getReactApplicationContext()
                  .getJSModule(
                    DeviceEventManagerModule.RCTDeviceEventEmitter.class
                  )
                  .emit("TransactionEvent", CARD_READ_COMPLETED);
              } else if (event == TransactionUIEvent.RequestSignature) {
                // Handle request signature event
                showToast("Signature is required");
              } else {
                // Handle other transaction UI events
                switch (event) {
                  case TransactionUIEvent.PresentCard:
                    getReactApplicationContext()
                      .getJSModule(
                        DeviceEventManagerModule.RCTDeviceEventEmitter.class
                      )
                      .emit("TransactionEvent", PRESENT_CARD);
                    break;
                  case TransactionUIEvent.Authorising:
                    getReactApplicationContext()
                      .getJSModule(
                        DeviceEventManagerModule.RCTDeviceEventEmitter.class
                      )
                      .emit("TransactionEvent", AUTHORISING);
                    break;
                  case TransactionUIEvent.CardPresented:
                    getReactApplicationContext()
                      .getJSModule(
                        DeviceEventManagerModule.RCTDeviceEventEmitter.class
                      )
                      .emit("TransactionEvent", CARD_DETECTED);
                    break;
                  case TransactionUIEvent.CardReadError:
                    getReactApplicationContext()
                      .getJSModule(
                        DeviceEventManagerModule.RCTDeviceEventEmitter.class
                      )
                      .emit("TransactionEvent", CARD_READ_ERROR_MSG);
                    showToast("CARD_READ_ERROR");
                    break;
                  case TransactionUIEvent.CardReadRetry:
                    getReactApplicationContext()
                      .getJSModule(
                        DeviceEventManagerModule.RCTDeviceEventEmitter.class
                      )
                      .emit("TransactionEvent", CARD_READ_RETRY);
                    break;
                  default:
                    getReactApplicationContext()
                      .getJSModule(
                        DeviceEventManagerModule.RCTDeviceEventEmitter.class
                      )
                      .emit("TransactionEvent", event);
                    break;
                }
              }
              // }
              // }
              // );
            }
          }
        );
    } catch (Exception e) {
      // Log.e(TAG, e.getMessage(), e);
      showToast("EXCEPTION FROM EMV " + e);
    }
  }

  private void emitEvent(String eventName, Object eventData) {
    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, eventData);
  }
}
