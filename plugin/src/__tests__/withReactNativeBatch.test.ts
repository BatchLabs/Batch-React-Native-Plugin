import { Props } from '../withReactNativeBatch';

describe('withReactNativeBatch', () => {
  describe('Props default values', () => {
    it('should default shouldUseNonNullableIntent to false when props is undefined', () => {
      const props = undefined;
      const _props = props || { androidApiKey: '', iosApiKey: '', shouldUseNonNullableIntent: false };

      expect(_props.shouldUseNonNullableIntent).toBe(false);
    });

    it('should default shouldUseNonNullableIntent to false when props is provided without the property', () => {
      const props: Props = {
        androidApiKey: 'FAKE_ANDROID_API_KEY',
        iosApiKey: 'FAKE_IOS_API_KEY',
      };
      const _props = props || { androidApiKey: '', iosApiKey: '', shouldUseNonNullableIntent: false };

      expect(_props.shouldUseNonNullableIntent).toBeUndefined();
    });

    it('should use provided shouldUseNonNullableIntent value when explicitly set to true', () => {
      const props: Props = {
        androidApiKey: 'FAKE_ANDROID_API_KEY',
        iosApiKey: 'FAKE_IOS_API_KEY',
        shouldUseNonNullableIntent: true,
      };
      const _props = props || { androidApiKey: '', iosApiKey: '', shouldUseNonNullableIntent: false };

      expect(_props.shouldUseNonNullableIntent).toBe(true);
    });

    it('should use provided shouldUseNonNullableIntent value when explicitly set to false', () => {
      const props: Props = {
        androidApiKey: 'FAKE_ANDROID_API_KEY',
        iosApiKey: 'FAKE_IOS_API_KEY',
        shouldUseNonNullableIntent: false,
      };
      const _props = props || { androidApiKey: '', iosApiKey: '', shouldUseNonNullableIntent: false };

      expect(_props.shouldUseNonNullableIntent).toBe(false);
    });
  });
});
