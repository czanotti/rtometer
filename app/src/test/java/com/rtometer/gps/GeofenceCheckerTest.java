package com.rtometer.gps;

import com.rtometer.data.db.Office;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class GeofenceCheckerTest {

    // ── isWithin ─────────────────────────────────────────────────────────────

    @Test
    public void sameCoordinates_returnsTrue() {
        Office office = office(51.5074, -0.1278, 200);
        assertTrue(GeofenceChecker.isWithin(51.5074, -0.1278, office));
    }

    @Test
    public void withinRadius_returnsTrue() {
        Office office = office(51.5074, -0.1278, 200);
        // ~100 m north
        assertTrue(GeofenceChecker.isWithin(51.5083, -0.1278, office));
    }

    @Test
    public void outsideRadius_returnsFalse() {
        Office office = office(51.5074, -0.1278, 200);
        // ~1 km north
        assertFalse(GeofenceChecker.isWithin(51.5164, -0.1278, office));
    }

    @Test
    public void justInsideBoundary_returnsTrue() {
        // 0.001° latitude ≈ 111 m; well within 200 m radius
        Office office = office(0.0, 0.0, 200);
        assertTrue(GeofenceChecker.isWithin(0.001, 0.0, office));
    }

    // ── distanceMeters ────────────────────────────────────────────────────────

    @Test
    public void distanceMeters_samePoint_isZero() {
        double d = GeofenceChecker.distanceMeters(40.7128, -74.006, 40.7128, -74.006);
        assertEquals(0.0, d, 0.001);
    }

    @Test
    public void distanceMeters_knownDistance_isCorrect() {
        // 1° latitude ≈ 111 km; test 0.1° ≈ 11.1 km
        double d = GeofenceChecker.distanceMeters(0.0, 0.0, 0.1, 0.0);
        assertEquals(11132.0, d, 200.0); // within 200 m of known value
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Office office(double lat, double lng, int radius) {
        Office o = new Office();
        o.latitude = lat;
        o.longitude = lng;
        o.radiusMeters = radius;
        return o;
    }
}
