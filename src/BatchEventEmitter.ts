import { NativeEventEmitter, NativeModules } from 'react-native';

export const BatchEventEmitter = new NativeEventEmitter(NativeModules.RNBatch);

export interface EmitterSubscription {
  remove: () => void;
}
