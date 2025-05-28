package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import com.codnyx.myengine.MyMath; // Targetting static methods in MyMath for quaternion operations

import java.util.Arrays;

/**
 * Test class for Quaternion functionality.
 * NOTE: The class com.codnyx.myengine.Quaternion is currently a placeholder
 * with no implemented fields or methods. This test class therefore targets the
 * static quaternion operations available in com.codnyx.myengine.MyMath,
 * which use float[4] arrays to represent quaternions (x, y, z, w).
 */
public class TestQuaternion {

    private static final float DELTA = 1e-6f;
    private static final float[] IDENTITY_QUATERNION = {0f, 0f, 0f, 1f};

    private void assertQuaternionEquals(String message, float[] expected, float[] actual) {
        assertArrayEquals(message, expected, actual, DELTA);
    }

    // --- Test MyMath.createRotationQuaterion ---
    @Test
    public void testCreateRotationQuaternion() {
        float[] axisX = {1f, 0f, 0f};
        float[] axisY = {0f, 1f, 0f};
        float[] axisZ = {0f, 0f, 1f};
        float[] result = new float[4];

        // 0-degree rotation (should be identity)
        MyMath.createRotationQuaterion(axisX, 0.0, result);
        assertQuaternionEquals("0-deg rotation around X", IDENTITY_QUATERNION, result);

        // 90-degree rotation around X-axis (angle/2 = PI/4, sin(PI/4)=cos(PI/4)=sqrt(0.5))
        float valSqrtHalf = (float)Math.sqrt(0.5);
        MyMath.createRotationQuaterion(axisX, Math.PI / 2.0, result);
        assertQuaternionEquals("90-deg rotation around X", new float[]{valSqrtHalf, 0f, 0f, valSqrtHalf}, result);

        // 90-degree rotation around Y-axis
        MyMath.createRotationQuaterion(axisY, Math.PI / 2.0, result);
        assertQuaternionEquals("90-deg rotation around Y", new float[]{0f, valSqrtHalf, 0f, valSqrtHalf}, result);

        // 90-degree rotation around Z-axis
        MyMath.createRotationQuaterion(axisZ, Math.PI / 2.0, result);
        assertQuaternionEquals("90-deg rotation around Z", new float[]{0f, 0f, valSqrtHalf, valSqrtHalf}, result);
        
        // Test allocating version
        float[] allocatedResult = MyMath.createRotationQuaterion(axisX, Math.PI); // 180 deg around X
        // angle/2 = PI/2. sin(PI/2)=1, cos(PI/2)=0
        assertQuaternionEquals("180-deg rotation around X (alloc version)", new float[]{1f,0f,0f,0f}, allocatedResult);
    }

    // --- Test MyMath.quaternionLength ---
    @Test
    public void testQuaternionLength() {
        assertEquals("Length of identity quaternion", 1f, MyMath.quaternionLength(IDENTITY_QUATERNION), DELTA);
        float[] q = {1f, 2f, 3f, 4f}; // Length = sqrt(1+4+9+16) = sqrt(30)
        assertEquals("Length of (1,2,3,4)", (float)Math.sqrt(30.0), MyMath.quaternionLength(q), DELTA);
        float[] qZero = {0f,0f,0f,0f};
        assertEquals("Length of zero quaternion", 0f, MyMath.quaternionLength(qZero), DELTA);
    }

    // --- Test MyMath.quaternionNormalize ---
    @Test
    public void testQuaternionNormalize() {
        float[] q = {1f, 1f, 1f, 1f}; // Length = sqrt(4) = 2
        MyMath.quaternionNormalize(q); // In-place
        assertEquals("Length after normalize should be 1", 1f, MyMath.quaternionLength(q), DELTA);
        assertQuaternionEquals("Normalized (1,1,1,1)", new float[]{0.5f, 0.5f, 0.5f, 0.5f}, q);

        float[] qIdentity = Arrays.copyOf(IDENTITY_QUATERNION, 4);
        MyMath.quaternionNormalize(qIdentity);
        assertQuaternionEquals("Normalize identity quaternion", IDENTITY_QUATERNION, qIdentity);

        float[] qZero = {0f, 0f, 0f, 0f};
        MyMath.quaternionNormalize(qZero); // MyMath sets this to identity
        assertQuaternionEquals("Normalize zero quaternion results in identity", IDENTITY_QUATERNION, qZero);
    }

    // --- Test MyMath.quaternionProduct ---
    @Test
    public void testQuaternionProduct() {
        float[] q = {0.5f, 0.3f, 0.1f, 0.8f}; // Example quaternion
        MyMath.quaternionNormalize(q); // Ensure it's unit for meaningful identity tests if desired
        float[] result = new float[4];

        // Q * I = Q
        MyMath.quaternionProduct(q, IDENTITY_QUATERNION, result);
        assertQuaternionEquals("Q * I = Q", q, result);

        // I * Q = Q
        MyMath.quaternionProduct(IDENTITY_QUATERNION, q, result);
        assertQuaternionEquals("I * Q = Q", q, result);

        // Known product: 90-deg X rot * 90-deg Y rot
        // qX = (sqrt(0.5), 0, 0, sqrt(0.5))
        // qY = (0, sqrt(0.5), 0, sqrt(0.5))
        // Expected product (from TestMyMath): (0.5, 0.5, 0.5, 0.5)
        float valSqrtHalf = (float)Math.sqrt(0.5);
        float[] qX90 = {valSqrtHalf, 0f, 0f, valSqrtHalf};
        float[] qY90 = {0f, valSqrtHalf, 0f, valSqrtHalf};
        float[] expectedProduct = {0.5f, 0.5f, 0.5f, 0.5f};
        
        MyMath.quaternionProduct(qX90, qY90, result);
        assertQuaternionEquals("qX90 * qY90", expectedProduct, result);

        // Test allocating version
        float[] allocatedResult = MyMath.quaternionProduct(qX90, qY90);
        assertQuaternionEquals("qX90 * qY90 (alloc version)", expectedProduct, allocatedResult);
    }

    // --- Test MyMath.quaternionInvert ---
    @Test
    public void testQuaternionInvert() {
        float valSqrtHalf = (float)Math.sqrt(0.5);
        float[] qUnit = {valSqrtHalf, 0f, 0f, valSqrtHalf}; // Unit quaternion (90-deg X rot)
        float[] result = new float[4];

        // Invert unit quaternion (should be conjugate: -x, -y, -z, w)
        MyMath.quaternionInvert(qUnit, result);
        assertQuaternionEquals("Inverse of unit quaternion", new float[]{-valSqrtHalf, 0f, 0f, valSqrtHalf}, result);

        // Q * Q_inv = Identity
        float[] product = MyMath.quaternionProduct(qUnit, result, new float[4]);
        assertQuaternionEquals("Q * Q_inv = Identity", IDENTITY_QUATERNION, product);

        // Invert non-unit quaternion
        float[] qNonUnit = {1f, 0f, 0f, 1f}; // Length sqrt(2), magSq = 2
        // Expected: (-1/2, 0/2, 0/2, 1/2) = (-0.5, 0, 0, 0.5)
        MyMath.quaternionInvert(qNonUnit, result);
        assertQuaternionEquals("Inverse of non-unit (1,0,0,1)", new float[]{-0.5f, 0f, 0f, 0.5f}, result);
        
        // Test in-place version
        float[] qNonUnitCopy = Arrays.copyOf(qNonUnit, 4);
        MyMath.quaternionInvert(qNonUnitCopy);
        assertQuaternionEquals("In-place inverse of non-unit (1,0,0,1)", new float[]{-0.5f, 0f, 0f, 0.5f}, qNonUnitCopy);

        // Invert zero quaternion (MyMath sets to identity)
        float[] qZero = {0f, 0f, 0f, 0f};
        MyMath.quaternionInvert(qZero, result);
        assertQuaternionEquals("Inverse of zero quaternion results in identity", IDENTITY_QUATERNION, result);
    }

    // --- Test Rotation of a Vector ---
    // This requires q * p_as_quat * q_inv using MyMath.quaternionProduct
    // p_as_quat = (vector[0], vector[1], vector[2], 0)
    @Test
    public void testRotateVector() {
        float[] vector = {0f, 1f, 0f}; // Vector along Y-axis
        float[] resultVector = new float[3];
        
        // 1. Rotate by Identity Quaternion
        float[] pQuat = {vector[0], vector[1], vector[2], 0f};
        float[] qInvIdentity = Arrays.copyOf(IDENTITY_QUATERNION, 4); // Inverse of identity is identity
        MyMath.quaternionInvert(qInvIdentity); 

        float[] temp = MyMath.quaternionProduct(IDENTITY_QUATERNION, pQuat, new float[4]);
        float[] rotatedPQuat = MyMath.quaternionProduct(temp, qInvIdentity, new float[4]);
        resultVector[0] = rotatedPQuat[0];
        resultVector[1] = rotatedPQuat[1];
        resultVector[2] = rotatedPQuat[2];
        assertFloatArrayEquals("Rotate by Identity", vector, resultVector, DELTA);

        // 2. Rotate 90 degrees around X-axis: Y-axis should become Z-axis
        // qX90 = (sqrt(0.5), 0, 0, sqrt(0.5))
        float valSqrtHalf = (float)Math.sqrt(0.5);
        float[] qX90 = {valSqrtHalf, 0f, 0f, valSqrtHalf};
        float[] qX90Inv = new float[4];
        MyMath.quaternionInvert(qX90, qX90Inv); // {-valSqrtHalf, 0f, 0f, valSqrtHalf}

        temp = MyMath.quaternionProduct(qX90, pQuat, new float[4]);
        rotatedPQuat = MyMath.quaternionProduct(temp, qX90Inv, new float[4]);
        resultVector[0] = rotatedPQuat[0];
        resultVector[1] = rotatedPQuat[1];
        resultVector[2] = rotatedPQuat[2];
        assertFloatArrayEquals("Rotate (0,1,0) by 90deg around X", new float[]{0f, 0f, 1f}, resultVector, DELTA);

        // 3. Rotate (1,0,0) 90 degrees around Z-axis: (1,0,0) should become (0,1,0)
        float[] qZ90 = {0f, 0f, valSqrtHalf, valSqrtHalf};
        float[] qZ90Inv = new float[4];
        MyMath.quaternionInvert(qZ90, qZ90Inv);
        float[] vectorX = {1f, 0f, 0f};
        pQuat[0]=vectorX[0]; pQuat[1]=vectorX[1]; pQuat[2]=vectorX[2]; pQuat[3]=0f;

        temp = MyMath.quaternionProduct(qZ90, pQuat, new float[4]);
        rotatedPQuat = MyMath.quaternionProduct(temp, qZ90Inv, new float[4]);
        resultVector[0] = rotatedPQuat[0];
        resultVector[1] = rotatedPQuat[1];
        resultVector[2] = rotatedPQuat[2];
        assertFloatArrayEquals("Rotate (1,0,0) by 90deg around Z", new float[]{0f, 1f, 0f}, resultVector, DELTA);
    }
    
    // --- Conversion to Rotation Matrix (Not in MyMath.java) ---
    // @Test public void testToRotationMatrix() { fail("Functionality not present in MyMath.java"); }
    
    // --- Setters/Getters for a Quaternion class (Not applicable for MyMath static methods) ---
    // No direct setters/getters for float[] representations beyond array manipulation.

    // Helper to compare float arrays with a delta (already in MyMath tests, but good here too)
    private void assertFloatArrayEquals(String message, float[] expected, float[] actual, float delta) {
        assertEquals(message + " (array length)", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(message + " (element " + i + ")", expected[i], actual[i], delta);
        }
    }
}
