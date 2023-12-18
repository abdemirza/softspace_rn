import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ToastAndroid,
  NativeModules,
} from 'react-native';
import {
  checkAndRequestNFCPermission,
  refreshToken,
  startEMVProcessing,
} from 'tap_pay_razer_rn';
// import {
//   checkAndRequestNFCPermission,
//   multiply,
//   refreshToken,
//   startEMVProcessing,
// } from 'tap_pay_razer_rn';

export default function App() {
  React.useEffect(() => {
    console.log(NativeModules.TapPayRazer, 'HELLO');
  }, []);

  const refreshTokenHandler = () => {
    refreshToken();
  };

  const onPressHandler = () => {
    checkAndRequestNFCPermission()
      .then((result) => {
        ToastAndroid.show(`${result}`, ToastAndroid.CENTER);
        startEMVProcessing('19', '1021234')
          .then((res) => {
            ToastAndroid.show(`${res}`, ToastAndroid.CENTER);
          })
          .catch((error) => {
            console.log(error);
          });
        // performSettlement().then((res) => {
        //   console.log(res);
        // });
      })
      .catch((error) => {
        console.error(error);
        // Handle error
      });
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity onPress={onPressHandler}>
        <View style={styles.button}>
          <Text>Start Transaction</Text>
        </View>
      </TouchableOpacity>

      <TouchableOpacity onPress={refreshTokenHandler}>
        <View style={styles.button}>
          <Text>Refresh Token</Text>
        </View>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  button: {
    width: 200,
    height: 100,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
