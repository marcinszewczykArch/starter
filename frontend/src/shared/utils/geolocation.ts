import type { LocationDto } from '../api/types';

/**
 * Request GPS location from browser.
 * Returns null if user denies permission or on timeout.
 *
 * @param timeoutMs Maximum time to wait for location (default: 3000ms)
 * @returns LocationDto with lat/lng or null
 */
export function requestGpsLocation(timeoutMs = 3000): Promise<LocationDto | null> {
  return new Promise((resolve) => {
    // Check if geolocation is supported
    if (!navigator.geolocation) {
      console.log('Geolocation not supported');
      resolve(null);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
      },
      (error) => {
        // User denied or error occurred - that's fine, we'll use IP geolocation
        console.log('Geolocation error:', error.message);
        resolve(null);
      },
      {
        timeout: timeoutMs,
        enableHighAccuracy: false, // Don't need high accuracy, faster response
        maximumAge: 60000, // Accept cached position up to 1 minute old
      }
    );
  });
}
