public class MyReactActivity
  extends Activity
  implements DefaultHardwareBackBtnHandler {

  private ReactRootView mReactRootView;
  private ReactInstanceManager mReactInstanceManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SoLoader.init(this, false);

    mReactRootView = new ReactRootView(this);
    List<ReactPackage> packages = new PackageList(getApplication())
      .getPackages();
    // Packages that cannot be autolinked yet can be added manually here, for example:
    // packages.add(new MyReactNativePackage());
    // Remember to include them in `settings.gradle` and `app/build.gradle` too.

    mReactInstanceManager =
      ReactInstanceManager
        .builder()
        .setApplication(getApplication())
        .setCurrentActivity(this)
        .setBundleAssetName("index.android.bundle")
        .setJSMainModulePath("index")
        .addPackages(packages)
        .setUseDeveloperSupport(BuildConfig.DEBUG)
        .setInitialLifecycleState(LifecycleState.RESUMED)
        .build();
    // The string here (e.g. "MyReactNativeApp") has to match
    // the string in AppRegistry.registerComponent() in index.js
    mReactRootView.startReactApplication(
      mReactInstanceManager,
      "MyReactNativeApp",
      null
    );

    setContentView(mReactRootView);
  }

  @Override
  public void invokeDefaultOnBackPressed() {
    super.onBackPressed();
  }
}
