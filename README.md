# tap_pay_razer_rn

Enable contactless payments on react native

## Installation

```sh
npm install softspace_rn
```

## Usage

```js
import {
  initFasstapMPOSSDK,
  refreshToken,
  startEMVProcessing,
} from 'softspace_rn';

// initialise the library with your creds
React.useEffect(() => {
  // add your configurations here
  const config = {
    attestationHost: '',
    libSecretKey: '',
    attestationCertPinning: '',
    libGooglePlayProjNum: '',
    libAccessKey: '',
    uniqueID: '',
    developerID: '',
    environment: '',
  };
  initFasstapMPOSSDK(config);
  refreshToken();
}, []);

// to start the payment call this method with the amount
startEmvProcessing('1000');
```

## How to run example app

To run the example app replace the my-release-key.keystore with your keystore file and add change the credentials in gradle.properties to run the app properly

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
