import { appDelegateExpectedFixture, appDelegateFixture } from '../fixtures/appDelegate';
import { swift_appDelegateExpectedFixture, swift_appDelegateFixture } from '../fixtures/swift_appDelegate';
import { modifyAppDelegate } from '../ios/withReactNativeBatchAppDelegate';

describe(modifyAppDelegate, () => {
  it('should modify the AppDelegate', () => {
    const result = modifyAppDelegate(appDelegateFixture);

    expect(result).toEqual(appDelegateExpectedFixture);
  });
});

describe(modifyAppDelegate, () => {
  it('should modify the swift_AppDelegate', () => {
    const result = modifyAppDelegate(swift_appDelegateFixture);

    expect(result).toEqual(swift_appDelegateExpectedFixture);
  });
});
