import { modifyMainJavaActivity, modifyMainKotlinActivity } from '../android/withReactNativeBatchMainActivity';
import {
  mainJavaActivityExpectedFixture,
  mainJavaActivityFixture,
  mainKotlinActivityExpectedFixture,
  mainKotlinActivityExpectedFixtureNullable,
  mainKotlinActivityFixture,
} from '../fixtures/mainActivity';

describe('withReactNativeBatchMainActivity', () => {
  describe('modifyMainJavaActivity', () => {
    it('should push on new intent in java main activity', () => {
      const result = modifyMainJavaActivity(mainJavaActivityFixture);
      expect(result).toEqual(mainJavaActivityExpectedFixture);
    });
  });

  describe('modifyMainKotlinActivity', () => {
    it('should push on new intent in kotlin main activity with non-nullable Intent (SDK 54+)', () => {
      const result = modifyMainKotlinActivity(mainKotlinActivityFixture, true);
      expect(result).toEqual(mainKotlinActivityExpectedFixture);
    });

    it('should push on new intent in kotlin main activity with nullable Intent (SDK 53-)', () => {
      const result = modifyMainKotlinActivity(mainKotlinActivityFixture, false);
      expect(result).toEqual(mainKotlinActivityExpectedFixtureNullable);
    });
  });
});
