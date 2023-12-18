package com.partner.easyeat;

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
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactMethod;
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

public class TapPayRazerModule extends ReactContextBaseJavaModule {

  public static final String NAME = "TapPayRazer";
  private Activity mActivity = null;
  private static final String CARD_TYPE_VISA = "0";
  private static final String CARD_TYPE_MASTERCARD = "1";
  private static final String CARD_TYPE_AMEX = "2";
  private static final String CARD_TYPE_JCB = "3";
  private static final String CARD_TYPE_DISCOVER = "23";
  private MPOSTransactionOutcome _transactionOutcome;
  private volatile boolean isTrxRunning = false;
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  Context context;

  public TapPayRazerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext.getApplicationContext();
  }

  @Override
  public void initialize() {
    super.initialize();
    // initFasstapMPOSSDK();
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

  private void initFasstapMPOSSDK() {
    try {
      ReactActivity reactActivity = (ReactActivity) getCurrentActivity();
      // final Activity reactActivity = getCurrentActivity();
      // ReactApplicationContext context = getReactApplicationContext();
      // Get the ReactApplicationContext
      // Obtain the Context
      if (reactActivity == null || context == null) {
        // Handle the case when the activity is not available
        new Handler(Looper.getMainLooper())
          .post(
            new Runnable() {
              @Override
              public void run() {
                Toast
                  .makeText(context, "IT IS NULL", Toast.LENGTH_SHORT)
                  .show();
              }
            }
          );
        return;
      }

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
      reactActivity.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            SSMPOSSDK.init(context, config);
          }
        }
      );
      // if (checkLocationPermission(reactActivity)) {
      //   // Location permission is already granted
      //   // You can now perform actions that require location access
      // } else {
      //   // Location permission is not granted, request it
      //   requestLocationPermission(reactActivity);
      // }
      // });
    } catch (Exception e) {
      new Handler(Looper.getMainLooper())
        .post(
          new Runnable() {
            @Override
            public void run() {
              Toast
                .makeText(
                  getReactApplicationContext(),
                  "Errorrrrr: " + e.getMessage(),
                  Toast.LENGTH_LONG
                )
                .show();
            }
          }
        );
    }
  }

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
    showToast("refreshToken()");
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
            public void onTransactionUIEvent(int event) {
              showToast("onTransactionUIEvent :: " + event);
            }
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
    showToast("handleRefreshTokenResult");
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

  // @ReactMethod
  // public void startEMVProcessing(
  //   String amount,
  //   String referenceNumber,
  //   Promise promise
  // ) {
  //   // ReactActivity reactActivity = (ReactActivity) getCurrentActivity();
  //   Activity reactActivity = getCurrentActivity();
  //   if (reactActivity == null) {
  //     // Handle the case when the activity is not available
  //     showToast("REACT ACTIVITY IS NULL");
  //     return;
  //   }
  //   String refNo = "SS" + Calendar.getInstance().getTimeInMillis();
  //   if (amount != null && Double.parseDouble(amount) <= 0) {
  //     writeLog("Amount cannot be zero!");
  //     toggleTransactionRunning(false);
  //     return;
  //   }

  //   try {
  //     _transactionOutcome = null;
  //     // initialize();
  //     MPOSTransactionParams transactionalParams = MPOSTransactionParams.Builder
  //       .create()
  //       .setReferenceNumber(refNo)
  //       .setAmount(amount)
  //       .build();
  //     SSMPOSSDK
  //       .getInstance()
  //       .getTransaction()
  //       .startTransaction(
  //         reactActivity, // 'this' should be the reference to your current activity
  //         transactionalParams,
  //         new MPOSTransaction.TransactionEvents() {
  //           @Override
  //           public void onTransactionResult(
  //             int result,
  //             MPOSTransactionOutcome transactionOutcome
  //           ) {
  //             _transactionOutcome = transactionOutcome;
  //             reactActivity.runOnUiThread(() -> {
  //               writeLog("onTransactionResult :: " + result);
  //               WritableMap resultMap = new WritableNativeMap();
  //               resultMap.putInt("result", result);
  //               if (result == TransactionSuccessful) {
  //                 // ... (rest of the existing code)

  //                 // Modify UI elements using callback if needed
  //                 resultMap.putString("message", "Transaction Successful");
  //               } else if (result == TransactionFailed) {
  //                 // Additional UI updates or callbacks for failed transaction
  //                 resultMap.putString("message", "Transaction Failed");
  //               }
  //               promise.resolve(resultMap);
  //             });
  //           }

  //           @Override
  //           public void onTransactionUIEvent(int event) {
  //             reactActivity.runOnUiThread(() -> {
  //               // ... (existing UI event handling code)
  //             });
  //           }
  //         }
  //       );
  //   } catch (Exception e) {
  //     // Log.e(TAG, e.getMessage(), e);
  //     promise.reject("TRANSACTION_ERROR", e.getMessage());
  //     showToast("TRANSACTION_ERROR" + e + amount + refNo);
  //   }
  // }

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
                      showToast("onTransactionResult :: " + result);

                      if (
                        result != TransactionSuccessful &&
                        transactionOutcome != null
                      ) {
                        showToast(
                          transactionOutcome.getStatusCode() +
                          " - " +
                          transactionOutcome.getStatusMessage()
                        );
                      }

                      // Resolve the promise with the result
                      if (promise != null) {
                        promise.resolve(result);
                        showToast("promise resolve ");
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
                    public void run() {
                      showToast("onTransactionUIEvent :: " + event);
                    }
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
      .makeText(getReactApplicationContext(), message, Toast.LENGTH_SHORT)
      .show();
  }

  // Check if the location permission is granted
  public static boolean checkLocationPermission(Context context) {
    int permissionResult = ActivityCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    );
    return permissionResult == PackageManager.PERMISSION_GRANTED;
  }

  // Request location permission
  public static void requestLocationPermission(Context context) {
    ActivityCompat.requestPermissions(
      (Activity) context,
      new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
      LOCATION_PERMISSION_REQUEST_CODE
    );
  }

  // Handle the result of the permission request
  public static void onRequestPermissionsResult(
    int requestCode,
    String[] permissions,
    int[] grantResults
  ) {
    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
      if (
        grantResults.length > 0 &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED
      ) {
        // Location permission granted
        // You can now perform actions that require location access
      } else {
        // Location permission denied
        // You may want to inform the user about the importance of the permission for your app's functionality
      }
    }
  }

  @ReactMethod
  public void startEMVProcessing(
    String amount,
    String referenceNumberr,
    final Promise promise
  ) {
    ReactActivity currentActivity = (ReactActivity) getCurrentActivity();
    String referenceNumber = "SS" + Calendar.getInstance().getTimeInMillis();
    initFasstapMPOSSDK();
    if (currentActivity == null) {
      promise.reject("ACTIVITY_NULL", "Activity is null");
      showToast("ACTIVITY_NULL");
      return;
    }

    if (amount != null && Double.parseDouble(amount) <= 0) {
      promise.reject("INVALID_AMOUNT", "Amount cannot be zero!");
      return;
    }

    currentActivity.runOnUiThread(
      new Runnable() {
        @Override
        public void run() {
          writeLog("Amount, Authorised: " + amount);
          showToast("Amount, Authorised: " + amount);
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

              // Handle transaction result
              if (result == TransactionSuccessful) {
                // Handle successful transaction
                showToast("Transaction completed successfully");
                promise.resolve("Transaction completed successfully");
              } else if (result == TransactionFailed) {
                // Handle failed transaction
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
                  promise.reject("TRANSACTION_FAILED", outcome);
                  showToast("TRANSACTION_FAILED");
                } else {
                  promise.reject("TRANSACTION_FAILED", "Error :: " + result);
                  showToast("TRANSACTION_FAILED" + "Error :: " + result);
                }
              }
              // toggleTransactionRunning(false);
            }

            @Override
            public void onTransactionUIEvent(int event) {
              currentActivity.runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    showToast("Line 686 running");
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
                      promise.resolve("Card read completed");
                      showToast("Card read completed");
                    } else if (event == TransactionUIEvent.RequestSignature) {
                      // Handle request signature event
                      promise.resolve("Signature is required");
                      showToast("Signature is required");
                    } else {
                      // Handle other transaction UI events
                      switch (event) {
                        case TransactionUIEvent.PresentCard:
                          promise.resolve("Present your card");
                          showToast("Present your card");
                          break;
                        case TransactionUIEvent.Authorising:
                          promise.resolve("Authorising...");
                          showToast("Authorising...");
                          break;
                        case TransactionUIEvent.CardPresented:
                          showToast("Card detected");
                          break;
                        case TransactionUIEvent.CardReadError:
                          promise.reject("CARD_READ_ERROR", "Card read failed");
                          showToast("CARD_READ_ERROR");
                        case TransactionUIEvent.CardReadRetry:
                          promise.reject("CARD_READ_RETRY", "Card read retry");
                          showToast("CARD_READ_RETRY");
                          break;
                        default:
                          promise.resolve("onTransactionUIEvent :: " + event);
                          break;
                      }
                    }
                  }
                }
              );
            }
          }
        );
    } catch (Exception e) {
      // Log.e(TAG, e.getMessage(), e);
      promise.reject("EXCEPTION", e);
      showToast("EXCEPTION FROM EMV " + e);
    }
  }
}
