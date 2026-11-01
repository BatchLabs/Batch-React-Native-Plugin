import { Platform } from 'react-native';

import { BatchEventEmitter } from '../src/BatchEventEmitter';
import { BatchPush, BatchPushEventPayload } from '../src/BatchPush';

jest.mock('react-native', () => ({ Platform: { OS: 'ios' }, Linking: {} }));
jest.mock('../src/NativeRNBatchModule', () => ({ default: {} }));
jest.mock('../src/BatchEventEmitter', () => ({ BatchEventEmitter: { addListener: jest.fn() } }));

const addListener = jest.mocked(BatchEventEmitter.addListener);

beforeEach(() => jest.clearAllMocks());

it.each(['ios', 'android'] as const)('delivers receipt payloads and removes the subscription on %s', platform => {
  Object.defineProperty(Platform, 'OS', { value: platform, configurable: true });
  const remove = jest.fn();
  addListener.mockReturnValue({ remove } as ReturnType<typeof BatchEventEmitter.addListener>);
  const callback = jest.fn();
  const payload: BatchPushEventPayload = { isPositiveAction: false, pushPayload: { example: 'receipt' } };

  const subscription = BatchPush.addListener('receive', callback);
  expect(addListener).toHaveBeenCalledWith('notification_receive', callback);
  addListener.mock.calls[0][1](payload);
  expect(callback).toHaveBeenCalledWith(payload);

  subscription.remove();
  expect(remove).toHaveBeenCalledTimes(1);
});

it('keeps open events separate from receipt events', () => {
  const onOpen = jest.fn();
  const onReceive = jest.fn();
  BatchPush.addListener('open', onOpen);
  BatchPush.addListener('receive', onReceive);
  expect(addListener.mock.calls.map(([event]) => event)).toEqual(['notification_open', 'notification_receive']);
});

it.each(['display', 'dismiss'] as const)('keeps %s unsupported on iOS', event => {
  Object.defineProperty(Platform, 'OS', { value: 'ios', configurable: true });
  const subscription = BatchPush.addListener(event, jest.fn());
  subscription.remove();
  expect(addListener).not.toHaveBeenCalled();
});
