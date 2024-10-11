import { NativeEventEmitter } from 'react-native';

const RNBatch = require('./NativeRNBatchModule').default;

export const BatchEventEmitter = new NativeEventEmitter(RNBatch);

export interface EmitterSubscription {
  remove: () => void;
}
