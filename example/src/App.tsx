import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ToastAndroid,
  DeviceEventEmitter,
  Modal,
  ActivityIndicator,
} from 'react-native';
import {
  checkAndRequestNFCPermission,
  initFasstapMPOSSDK,
  refreshToken,
  startEMVProcessing,
} from 'tap_pay_razer_rn';

type TransactionStatus =
  | null
  | 'Processing'
  | 'Transaction Successful'
  | 'Transaction Failed'
  | 'Present Card'
  | 'Authorising'
  | 'Card Detected'
  | 'Card Read Error'
  | 'Card Read Retry'
  | 'Card Read Completed';

const TRANSACTION_SUCCESSFUL = 1;
const TRANSACTION_FAILED = 2;
const PRESENT_CARD = 3;
const AUTHORISING = 4;
const CARD_DETECTED = 5;
const CARD_READ_ERROR = 6;
const CARD_READ_RETRY = 7;
const CARD_READ_COMPLETED = 8;

interface TransactionStatusModalProps {
  status: TransactionStatus;
  onClose: () => void;
}

const TransactionStatusModal: React.FC<TransactionStatusModalProps> = ({
  status,
  onClose,
}) => {
  return (
    <Modal
      animationType="slide"
      transparent={true}
      visible={status !== null}
      onRequestClose={() => {
        onClose();
      }}
    >
      <View style={styles.modalBackground}>
        <View style={styles.modalContainer}>
          {status === 'Processing' ? (
            <View style={styles.loaderContainer}>
              <ActivityIndicator size="large" color="#0000ff" />
              <Text style={styles.loadingText}>Processing...</Text>
            </View>
          ) : (
            <View style={styles.modalContent}>
              <Text style={styles.statusText}>{status}</Text>
              <TouchableOpacity onPress={() => onClose()}>
                <Text style={styles.closeButton}>Close</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      </View>
    </Modal>
  );
};

interface AppProps {}

const App: React.FC<AppProps> = () => {
  const [transactionStatus, setTransactionStatus] =
    React.useState<TransactionStatus>(null);
  const [loading, setLoading] = React.useState<boolean>(false);

  const handleRefreshToken = async () => {
    try {
      setLoading(true);
      await refreshToken(); // Assuming refreshTokenHandler returns a Promise
      setLoading(false);
      ToastAndroid.show('Token refreshed successfully', ToastAndroid.CENTER);
    } catch (error) {
      ToastAndroid.show(JSON.stringify(error), ToastAndroid.CENTER);
      setLoading(false);
    }
  };

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

    const handleTransactionEvent = (event: number) => {
      switch (event) {
        case TRANSACTION_SUCCESSFUL:
          setTransactionStatus('Transaction Successful');
          break;
        case TRANSACTION_FAILED:
          setTransactionStatus('Transaction Failed');
          break;
        case PRESENT_CARD:
          setTransactionStatus('Present Card');
          break;
        case AUTHORISING:
          setTransactionStatus('Authorising');
          break;
        case CARD_DETECTED:
          setTransactionStatus('Card Detected');
          break;
        case CARD_READ_ERROR:
          setTransactionStatus('Card Read Error');
          break;
        case CARD_READ_RETRY:
          setTransactionStatus('Card Read Retry');
          break;
        case CARD_READ_COMPLETED:
          setTransactionStatus('Card Read Completed');
          break;
        default:
          // Handle other cases if needed
          break;
      }
    };

    // Add event listener when the component mounts
    const eventListener = DeviceEventEmitter.addListener(
      'TransactionEvent',
      handleTransactionEvent
    );

    // Clean up the listener when the component unmounts
    return () => {
      if (eventListener) {
        eventListener.remove();
      }
    };
  }, []);

  const onPressHandler = () => {
    setTransactionStatus(null);
    startEMVProcessing('19', '1021234');
  };

  React.useEffect(() => {
    checkAndRequestNFCPermission().then((res) => {
      ToastAndroid.show(`${res}`, ToastAndroid.CENTER);
    });
  }, []);

  const clearTransactionStatus = () => {
    setTransactionStatus(null);
  };

  return (
    <View style={styles.container}>
      <View style={styles.container}>
        <TouchableOpacity onPress={onPressHandler}>
          <View style={styles.button}>
            <Text style={styles.buttonText}>Start Transaction</Text>
          </View>
        </TouchableOpacity>

        {/* Refresh Token Button */}
        <TouchableOpacity onPress={handleRefreshToken}>
          <View style={styles.button}>
            <Text style={styles.buttonText}>
              {loading ? 'Refreshing...' : 'Refresh Token'}
            </Text>
          </View>
        </TouchableOpacity>
      </View>
      <TransactionStatusModal
        status={transactionStatus}
        onClose={() => clearTransactionStatus()}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    width: 200,
    height: 50,
    backgroundColor: '#4CAF50',
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    marginVertical: 10,
    elevation: 3,
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },

  closeButton: {
    marginTop: 10,
    color: '#3498db',
    fontSize: 16,
    fontWeight: 'bold',
  },

  modalBackground: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContainer: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 10,
    width: '80%',
    alignItems: 'center',
  },
  modalContent: {
    alignItems: 'center',
  },
  statusText: {
    color: 'black',
    marginBottom: 10,
    fontSize: 18,
  },
  closeButton: {
    color: 'blue',
    fontSize: 16,
  },
  loaderContainer: {
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 10,
    color: 'black',
  },
});
export default App;
