import { BatchEventAttributes } from './BatchEventAttributes';
import { BatchProfileAttributeEditor } from './BatchProfileAttributeEditor';
import Log from './helpers/Logger';
const RNBatch = require('./NativeRNBatchModule').default;

/**
 * Represents a locations, using lat/lng coordinates
 */
export interface Location {
  /**
   * Latitude
   */
  latitude: number;

  /**
   * Longitude
   */
  longitude: number;

  /**
   * Date of the tracked location
   */
  date?: Date;

  /**
   * Precision radius in meters
   */
  precision?: number;
}

/**
 * Batch's user module
 */
export const BatchProfile = {
  /**
   * Creates a new instance of BatchProfileAttributeEditor
   * @function
   * @returns {BatchProfileAttributeEditor} A new instance of BatchProfileAttributeEditor
   */
  editor: (): BatchProfileAttributeEditor => new BatchProfileAttributeEditor(),

  /**
   * Identifies this device with a profile using a Custom User ID.
   * @param {string | null} identifier - Custom user ID of the profile you want to identify against.
   * If a profile already exists, this device will be attached to it. Must not be longer than 1024 characters.
   */
  identify: (identifier: string | null): void => {
    if (typeof identifier === 'undefined') {
      return;
    }
    RNBatch.profile_identify(identifier);
  },

  /**
   * Track an event. Batch must be started at some point, or events won't be sent to the server.
   * @param name The event name. Must be a string.
   * @param data The event attributes (optional). Must be an object.
   */
  trackEvent: (name: string, data?: BatchEventAttributes): Promise<void> => {
    // Since _toInternalRepresentation is private, we have to resort to this little hack to access the method.
    // That syntax keeps the argument type checking, while casting as any would not.
    return RNBatch.profile_trackEvent(name, data instanceof BatchEventAttributes ? data['_toInternalRepresentation']() : null);
  },

  /**
   * Track a geolocation update
   * You can call this method from any thread. Batch must be started at some point, or location updates won't be sent to the server.
   * @param location User location object
   */
  trackLocation: (location: Location): void => {
    if (typeof location !== 'object') {
      Log(false, 'BatchUser - Invalid trackLocation argument. Skipping.');
      return;
    }

    if (typeof location.latitude !== 'number' || isNaN(location.latitude)) {
      Log(false, 'BatchUser - Invalid latitude. Skipping.');
      return;
    }

    if (typeof location.longitude !== 'number' || isNaN(location.longitude)) {
      Log(false, 'BatchUser - Invalid longitude. Skipping.');
      return;
    }

    if (location.precision && (typeof location.precision !== 'number' || isNaN(location.precision))) {
      Log(false, 'BatchUser - Invalid precision. Skipping.');
      return;
    }

    if (location.date && !(location.date instanceof Date)) {
      Log(false, 'BatchUser - Invalid date. Skipping.');
      return;
    }

    RNBatch.profile_trackLocation({
      date: location.date ? location.date.getTime() : undefined,
      latitude: location.latitude,
      longitude: location.longitude,
      precision: location.precision,
    });
  },
};
