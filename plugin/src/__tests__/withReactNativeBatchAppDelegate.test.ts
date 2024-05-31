import { appDelegateExpectedFixture, appDelegateFixture } from '../fixtures/appDelegate';
import { modifyAppDelegate } from '../ios/withReactNativeBatchAppDelegate';

describe(modifyAppDelegate, () => {
  it('should modify the AppDelegate', () => {
    const result = modifyAppDelegate(appDelegateFixture);

    expect(result).toEqual(appDelegateExpectedFixture);
  });
});
