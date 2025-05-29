package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.PerspectiveTransformation;
// Vertex class is not directly used by PerspectiveTransformation's project method.

import java.lang.reflect.Field; // For accessing private fields

public class TestPerspectiveTransformation {

    private static final float DELTA = 1e-5f; // Adjusted delta

    // Helper to access private fields for matrix component verification
    private float getPrivateField(PerspectiveTransformation obj, String fieldName) throws Exception {
        Field field = PerspectiveTransformation.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getFloat(obj);
    }

    @Test
    public void testConstructorAndMatrixSetup() throws Exception {
        double fovYRadians = Math.toRadians(90.0); // 90 degrees FoV
        int width = 800;
        int height = 600;
        int top = 0;
        int left = 0;
        float zMin = 1.0f;
        float zMax = 100.0f;

        PerspectiveTransformation pt = new PerspectiveTransformation(fovYRadians, width, height, top, left, zMin, zMax);

        // Expected values based on constructor logic:
        // h_2 = height / 2.0f = 300
        // w_2_screen = width / 2.0f = 400 (used for a13, a23)
        // tan(yangle / 2.0) = tan(PI/4) = 1.0
        // expected_a11 = h_2 / tan(yangle/2.0) = 300 / 1.0 = 300.0f
        // expected_a22 = -expected_a11 = -300.0f
        // expected_a13 = -(left + w_2_screen) = -(0 + 400) = -400.0f
        // expected_a23 = -(top + h_2) = -(0 + 300) = -300.0f
        // w_2_depth_calc = 1.0f / (zMin - zMax) = 1.0f / (1.0f - 100.0f) = 1.0f / -99.0f = -0.01010101...f
        // expected_a33 = -zMin * w_2_depth_calc = -1.0f * (1.0f / -99.0f) = 1.0f / 99.0f approx 0.01010101f
        // expected_a34 = zMax * zMin * w_2_depth_calc = 100.0f * 1.0f * (1.0f / -99.0f) = -100.0f / 99.0f approx -1.01010101f

        assertEquals("a11", 300.0f, getPrivateField(pt, "a11"), DELTA);
        assertEquals("a22", -300.0f, getPrivateField(pt, "a22"), DELTA);
        assertEquals("a13", -400.0f, getPrivateField(pt, "a13"), DELTA);
        assertEquals("a23", -300.0f, getPrivateField(pt, "a23"), DELTA);
        
        assertEquals("a33", 1.0f / 99.0f, getPrivateField(pt, "a33"), DELTA);
        assertEquals("a34", -100.0f / 99.0f, getPrivateField(pt, "a34"), DELTA);

        assertEquals("z_min", zMin, getPrivateField(pt, "z_min"), DELTA);
        assertEquals("z_max", zMax, getPrivateField(pt, "z_max"), DELTA);
        
        // Check inverse components as well
        // With a11 = 300, a13 = -400:
        // i11 = 1.0f / a11 = 1.0f / 300.0f
        // i13 = a13 / a11 = -400.0f / 300.0f = -4.0f / 3.0f
        // With a22 = -300, a23 = -300:
        // i22 = 1.0f / a22 = 1.0f / -300.0f
        // i23 = a23 / a22 = -300.0f / -300.0f = 1.0f
        assertEquals("i11", 1.0f / 300.0f, getPrivateField(pt, "i11"), DELTA);
        assertEquals("i13", -4.0f / 3.0f, getPrivateField(pt, "i13"), DELTA); // Corrected expected value
        assertEquals("i22", 1.0f / -300.0f, getPrivateField(pt, "i22"), DELTA);
        assertEquals("i23", 1.0f, getPrivateField(pt, "i23"), DELTA); // Corrected expected value
    }

    @Test
    public void testProjectPoint() throws Exception {
        double fovYRadians = Math.toRadians(90.0);
        int width = 800; int height = 600;
        int top = 0; int left = 0;
        float zMin = 1.0f; float zMax = 100.0f;
        PerspectiveTransformation pt = new PerspectiveTransformation(fovYRadians, width, height, top, left, zMin, zMax);

        float a11 = getPrivateField(pt, "a11"); // 300
        float a13 = getPrivateField(pt, "a13"); // -400
        float a22 = getPrivateField(pt, "a22"); // -300
        float a23 = getPrivateField(pt, "a23"); // -300
        float a33 = getPrivateField(pt, "a33"); // 1/99
        float a34 = getPrivateField(pt, "a34"); // -100/99

        // Point on near plane: eye_coord = (x, y, -zMin)
        // Example: x_eye = zMin, y_eye = zMin, z_eye = -zMin = -1.0
        // This point is at 45 degrees off center horizontally and vertically if fov=90.
        // x_eye/(-z_eye) = -1, y_eye/(-z_eye) = -1
        // Expected screen X: a11 * (x_eye/(-z_eye)) + a13 * (z_eye/(-z_eye)) = a11*(-1) + a13*(-1) = 300*(-1) + (-400)*(-1) = -300 + 400 = 100
        // Expected screen Y: a22 * (y_eye/(-z_eye)) + a23 * (z_eye/(-z_eye)) = a22*(-1) + a23*(-1) = (-300)*(-1) + (-300)*(-1) = 300 + 300 = 600
        // These are calculated using simplified formula: x_s = a11 * (x_e/-z_e) + a13 * (z_e/-z_e)
        // The actual formula in project(): wc = -1/z_e
        // x_s = (a11*x_e + a13*z_e) * wc
        
        float[] eyeCoordNear = {1.0f, 1.0f, -zMin}; // x=1, y=1, z=-1
        int[] screenCoordNear = new int[2];
        float depthNear = pt.project(eyeCoordNear, screenCoordNear);
        
        float wcNear = -1.0f / eyeCoordNear[2]; // wc = -1.0f / -1.0f = 1.0f
        int expectedXNear = (int)((a11 * eyeCoordNear[0] + a13 * eyeCoordNear[2]) * wcNear); // (300*1 - 400*(-1))*1 = 300+400 = 700
        int expectedYNear = (int)((a22 * eyeCoordNear[1] + a23 * eyeCoordNear[2]) * wcNear); // (-300*1 - 300*(-1))*1 = -300+300 = 0
        float expectedDepthNear = (a33 * eyeCoordNear[2] + a34) * wcNear; 
        // depth = ( (1/99)*(-1) + (-100/99) ) * 1 = (-1/99 - 100/99) = -101/99 = -1.0202...
        // This depth calculation seems to map z_min to a non-zero/non-negative value with this matrix.
        // Let's recheck a33/a34 logic: depth_out = -a33 - a34/z_eye
        // For z_eye = -z_min: depth_out = - (z_min/(z_max-z_min)) - (-(z_max*z_min)/(z_max-z_min)) / (-z_min)
        // = -z_min/(z_max-z_min) - (z_max*z_min)/((z_max-z_min)*z_min)
        // = -z_min/(z_max-z_min) - z_max/(z_max-z_min) = -(z_min+z_max)/(z_max-z_min)
        // With zMin=1, zMax=100: -(101)/(99) = -1.020202... This is what the code calculates.
        // This specific implementation's depth is not [0,1] or [-1,1] range directly for near/far.

        assertEquals("Projected X on near plane", expectedXNear, screenCoordNear[0]);
        assertEquals("Projected Y on near plane", expectedYNear, screenCoordNear[1]);
        assertEquals("Projected Depth on near plane", expectedDepthNear, depthNear, DELTA);

        // Point on far plane: eye_coord = (x, y, -zMax)
        float[] eyeCoordFar = {10.0f, 10.0f, -zMax}; // x=10, y=10, z=-100
        int[] screenCoordFar = new int[2];
        float depthFar = pt.project(eyeCoordFar, screenCoordFar);
        
        float wcFar = -1.0f / eyeCoordFar[2]; // wc = -1.0f / -100.0f = 0.01f
        int expectedXFar = (int)((a11 * eyeCoordFar[0] + a13 * eyeCoordFar[2]) * wcFar); // (300*10 - 400*(-100))*0.01 = (3000+40000)*0.01 = 43000*0.01 = 430
        int expectedYFar = (int)((a22 * eyeCoordFar[1] + a23 * eyeCoordFar[2]) * wcFar); // (-300*10 - 300*(-100))*0.01 = (-3000+30000)*0.01 = 27000*0.01 = 270
        float expectedDepthFar = (a33 * eyeCoordFar[2] + a34) * wcFar;
        // For z_eye = -z_max: depth_out = - (z_min/(z_max-z_min)) - (-(z_max*z_min)/(z_max-z_min)) / (-z_max)
        // = -z_min/(z_max-z_min) - (z_max*z_min)/((z_max-z_min)*z_max)
        // = -z_min/(z_max-z_min) - z_min/(z_max-z_min) = -2*z_min/(z_max-z_min)
        // With zMin=1, zMax=100: -2*1/(99) = -2/99 = -0.020202...
        
        assertEquals("Projected X on far plane", expectedXFar, screenCoordFar[0]);
        assertEquals("Projected Y on far plane", expectedYFar, screenCoordFar[1]);
        assertEquals("Projected Depth on far plane", expectedDepthFar, depthFar, DELTA);

        // Point at z_eye = 0
        float[] eyeCoordZZero = {1.0f, 1.0f, 0.0f};
        int[] screenCoordZZero = new int[2];
        float depthZZero = pt.project(eyeCoordZZero, screenCoordZZero);
        assertEquals("Projected X for z_eye=0", Integer.MAX_VALUE, screenCoordZZero[0]);
        assertEquals("Projected Y for z_eye=0", Integer.MAX_VALUE, screenCoordZZero[1]);
        assertEquals("Projected Depth for z_eye=0", Float.POSITIVE_INFINITY, depthZZero, DELTA);
    }

    @Test
    public void testIsVisible() {
        assertTrue("depth < zBuffer, depth >= 0", PerspectiveTransformation.isVisible(10.0f, 20.0f));
        assertFalse("depth == zBuffer", PerspectiveTransformation.isVisible(10.0f, 10.0f));
        assertFalse("depth > zBuffer", PerspectiveTransformation.isVisible(20.0f, 10.0f));
        assertFalse("depth < 0", PerspectiveTransformation.isVisible(-1.0f, 10.0f));
        assertTrue("depth == 0", PerspectiveTransformation.isVisible(0.0f, 1.0f));
    }

    @Test
    public void testUnproject() throws Exception {
        double fovYRadians = Math.toRadians(90.0);
        int width = 800; int height = 600;
        int top = 0; int left = 0;
        float zMin = 1.0f; float zMax = 100.0f;
        PerspectiveTransformation pt = new PerspectiveTransformation(fovYRadians, width, height, top, left, zMin, zMax);

        // Use the projected point from testProjectPoint (near plane)
        // screenCoordNear = {700, 0}, eyeZNear = -1.0f
        // Expected eyeCoordNear = {1.0f, 1.0f, -1.0f}
        int[] screenP = {700, 0};
        float eyeZ = -1.0f;
        float[] unprojectedP = new float[3];
        pt.unproject(screenP, eyeZ, unprojectedP);
        
        assertEquals("Unprojected X", 1.0f, unprojectedP[0], DELTA);
        assertEquals("Unprojected Y", 1.0f, unprojectedP[1], DELTA);
        assertEquals("Unprojected Z", -1.0f, unprojectedP[2], DELTA);

        // Test another point (far plane projection)
        // screenCoordFar = {430, 270}, eyeZFar = -100.0f
        // Expected eyeCoordFar = {10.0f, 10.0f, -100.0f}
        int[] screenPFar = {430, 270};
        float eyeZFar = -100.0f;
        float[] unprojectedPFar = new float[3];
        pt.unproject(screenPFar, eyeZFar, unprojectedPFar);

        assertEquals("Unprojected X (far)", 10.0f, unprojectedPFar[0], DELTA);
        assertEquals("Unprojected Y (far)", 10.0f, unprojectedPFar[1], DELTA);
        assertEquals("Unprojected Z (far)", -100.0f, unprojectedPFar[2], DELTA);
    }

    @Test
    public void testATrasform() throws Exception {
        // This method's exact geometric meaning is a bit unclear from the code alone.
        // It calculates: result[0] = (i11 * (x + 1) - i13); result[1] = (i22 * (y + 1) - i23);
        // The test should use the corrected i13 and i23 values.
        double fovYRadians = Math.toRadians(90.0);
        int width = 800; int height = 600;
        int top = 0; int left = 0;
        float zMin = 1.0f; float zMax = 100.0f;
        PerspectiveTransformation pt = new PerspectiveTransformation(fovYRadians, width, height, top, left, zMin, zMax);

        float i11 = 1.0f / 300.0f; 
        float i13 = -4.0f / 3.0f; // Corrected i13
        float i22 = -1.0f / 300.0f;
        float i23 = 1.0f;         // Corrected i23

        int x = 100;
        int y = 200;
        float[] result = new float[2]; // Assuming float[2] based on usage
        pt.aTrasform(x, y, result);

        float expectedR0 = (i11 * (x + 1) - i13);
        float expectedR1 = (i22 * (y + 1) - i23);
        
        assertEquals("aTrasform result[0]", expectedR0, result[0], DELTA);
        assertEquals("aTrasform result[1]", expectedR1, result[1], DELTA);
    }

    @Test
    public void testGetters() {
        PerspectiveTransformation pt = new PerspectiveTransformation(Math.toRadians(60), 800, 600, 0, 0, 0.1f, 1000f);
        assertEquals("getZMin", 0.1f, pt.getZMin(), DELTA);
        assertEquals("getZMax", 1000f, pt.getZMax(), DELTA);
    }
}
