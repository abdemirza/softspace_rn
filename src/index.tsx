import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'tap_pay_razer_rn' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

interface Config {
  attestationHost: string;
  attestationCertPinning: string;
  libGooglePlayProjNum: string;
  libAccessKey: string;
  libSecretKey: string;
  uniqueID: string;
  developerID: string;
  environment: string;
}

const TapPayRazerRn = NativeModules.TapPayRazerRn
  ? NativeModules.TapPayRazerRn
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return TapPayRazerRn.multiply(a, b);
}

export function startEMVProcessing(
  amount: string,
  referenceNumber: string
): Promise<boolean> {
  return TapPayRazerRn.startEMVProcessing(amount, referenceNumber);
}

export function checkAndRequestNFCPermission(): Promise<boolean> {
  return TapPayRazerRn.checkAndRequestNFCPermission();
}

export function performSettlement(): Promise<boolean> {
  return TapPayRazerRn.performSettlement();
}

export function refreshToken(): Promise<boolean> {
  return TapPayRazerRn.refreshToken();
}

export function getInstance(): Promise<boolean> {
  return TapPayRazerRn.getInstance();
}

export function initFasstapMPOSSDK(config: Config): Promise<boolean> {
  return TapPayRazerRn.initFasstapMPOSSDK(config);
}
