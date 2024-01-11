import { modifyMainActivity } from '../android/withReactNativeBatchMainActivity';
import {
  mainJavaActivityExpectedFixture,
  mainJavaActivityFixture,
  mainKotlinActivityExpectedFixture,
  mainKotlinActivityFixture,
} from '../fixtures/mainActivity';

describe(modifyMainActivity, () => {
  it('should push on new intent in java main activity', () => {
    const result = modifyMainActivity(mainJavaActivityFixture);
    expect(result).toEqual(mainJavaActivityExpectedFixture);
  });

  it('should push on new intent in kotlin main activity', () => {
    const result = modifyMainActivity(mainKotlinActivityFixture);

    expect(result).toEqual(mainKotlinActivityExpectedFixture);
  });
});
