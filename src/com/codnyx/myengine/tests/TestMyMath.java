package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import com.codnyx.myengine.MyMath;
import java.util.Arrays; // For array comparisons if needed, though component-wise is better for floats

/**
 * JUnit test class for the MyMath class.
 * This class tests the static utility methods for vector and quaternion mathematics.
 * Note: Methods sin, cos, sqrt specified in the task are NOT present in MyMath.java.
 * Tests are for methods actually present in MyMath.java.
 */
public class TestMyMath {

    private static final float DELTA = 1e-6f;

    // --- Vector Operations ---

    @Test
    public void testDotProduct() {
        float[] v1 = {1f, 2f, 3f};
        float[] v2 = {4f, -5f, 6f};
        float expected = 1f*4f + 2f*(-5f) + 3f*6f; // 4 - 10 + 18 = 12
        assertEquals("Dot product {1,2,3} . {4,-5,6}", expected, MyMath.dotProduct(v1, v2), DELTA);

        float[] v3 = {0f, 0f, 0f};
        assertEquals("Dot product with zero vector", 0f, MyMath.dotProduct(v1, v3), DELTA);

        float[] v4 = {-1f, -1f, -1f};
        assertEquals("Dot product with negative components", -6f, MyMath.dotProduct(v1, v4), DELTA);
    }

    @Test
    public void testCrossProduct() {
        float[] a = {1f, 0f, 0f}; // i
        float[] b = {0f, 1f, 0f}; // j
        float[] result = new float[3];
        MyMath.crossProduct(a, b, result);
        assertArrayEquals("Cross product i x j should be k ({0,0,1})", new float[]{0f, 0f, 1f}, result, DELTA);

        float[] c = {2f, 3f, 4f};
        float[] d = {5f, 6f, 7f};
        // cx = 3*7 - 4*6 = 21 - 24 = -3
        // cy = 4*5 - 2*7 = 20 - 14 = 6
        // cz = 2*6 - 3*5 = 12 - 15 = -3
        float[] expectedCD = {-3f, 6f, -3f};
        MyMath.crossProduct(c, d, result);
        assertArrayEquals("Cross product {2,3,4} x {5,6,7}", expectedCD, result, DELTA);

        // Test a x a = 0
        MyMath.crossProduct(c, c, result);
        assertArrayEquals("Cross product v x v should be {0,0,0}", new float[]{0f,0f,0f}, result, DELTA);
    }

    @Test
    public void testSubtract() {
        float[] a = {5f, 4f, 3f};
        float[] b = {1f, 2f, 1f};
        float[] result = new float[3];
        MyMath.subtract(a, b, result);
        assertArrayEquals("Subtract {5,4,3} - {1,2,1}", new float[]{4f, 2f, 2f}, result, DELTA);

        float[] c = {0f, 0f, 0f};
        MyMath.subtract(a, c, result);
        assertArrayEquals("Subtract vector - zero vector", a, result, DELTA);
        
        MyMath.subtract(c, a, result);
        assertArrayEquals("Subtract zero vector - vector", new float[]{-5f, -4f, -3f}, result, DELTA);
    }

    @Test
    public void testAdd() {
        float[] a = {1f, 2f, 3f};
        float[] b = {4f, 5f, 6f};
        float[] result = new float[3];
        MyMath.add(a, b, result);
        assertArrayEquals("Add {1,2,3} + {4,5,6}", new float[]{5f, 7f, 9f}, result, DELTA);
        
        float[] c = {0f, 0f, 0f};
        MyMath.add(a, c, result);
        assertArrayEquals("Add vector + zero vector", a, result, DELTA);

        float[] d = {-1f, -2f, -3f};
        MyMath.add(a,d,result);
        assertArrayEquals("Add vector + negative vector (cancel out)", new float[]{0f,0f,0f}, result, DELTA);
    }

    @Test
    public void testMultiplyVectorByScalar() {
        float[] vec = {1f, 2f, 3f};
        float scalar = 3f;
        float[] result = new float[3];
        MyMath.multiply(vec, scalar, result);
        assertArrayEquals("Multiply {1,2,3} by 3", new float[]{3f, 6f, 9f}, result, DELTA);

        MyMath.multiply(vec, 0f, result);
        assertArrayEquals("Multiply vector by 0", new float[]{0f, 0f, 0f}, result, DELTA);

        MyMath.multiply(vec, -1f, result);
        assertArrayEquals("Multiply vector by -1", new float[]{-1f, -2f, -3f}, result, DELTA);
    }
    
    @Test
    public void testScale() { // Alias for multiply
        float[] vec = {1f, 2f, 3f};
        float scalar = 2.5f;
        float[] result = new float[3];
        MyMath.scale(scalar, vec, result);
        assertArrayEquals("Scale {1,2,3} by 2.5", new float[]{2.5f, 5.0f, 7.5f}, result, DELTA);
    }

    @Test
    public void testLength() {
        float[] vec1 = {3f, 4f, 0f}; // Pythagorean triple 3-4-5
        assertEquals("Length of {3,4,0}", 5f, MyMath.length(vec1), DELTA);

        float[] vec2 = {0f, 0f, 0f};
        assertEquals("Length of {0,0,0}", 0f, MyMath.length(vec2), DELTA);

        float[] vec3 = {1f, 1f, 1f};
        assertEquals("Length of {1,1,1}", (float)Math.sqrt(3.0), MyMath.length(vec3), DELTA);
        
        float[] vec4 = {1f, 0f, 0f};
        assertEquals("Length of {1,0,0}", 1f, MyMath.length(vec4), DELTA);
    }

    @Test
    public void testNormalize() {
        float[] vec = {3f, 4f, 0f};
        MyMath.normalize(vec); // In-place
        assertArrayEquals("Normalize {3,4,0}", new float[]{3f/5f, 4f/5f, 0f}, vec, DELTA);
        assertEquals("Length after normalize should be 1", 1f, MyMath.length(vec), DELTA);

        float[] vecZero = {0f, 0f, 0f};
        MyMath.normalize(vecZero);
        // Implementation sets to {0,0,0} if length is too small
        assertArrayEquals("Normalize {0,0,0} should result in {0,0,0}", new float[]{0f,0f,0f}, vecZero, DELTA);
        
        float[] vecAlreadyNormal = {1f, 0f, 0f};
        MyMath.normalize(vecAlreadyNormal);
        assertArrayEquals("Normalize already normal vector", new float[]{1f,0f,0f}, vecAlreadyNormal, DELTA);
    }
    
    @Test
    public void testInit() {
        float[] vec = new float[3];
        MyMath.init(5.5f, vec);
        assertArrayEquals("Init vector with 5.5f", new float[]{5.5f, 5.5f, 5.5f}, vec, DELTA);
        MyMath.init(0f, vec);
        assertArrayEquals("Init vector with 0f", new float[]{0f, 0f, 0f}, vec, DELTA);
    }

    // --- Quaternion Operations ---
    private static final float[] QUAT_IDENTITY = {0f,0f,0f,1f};

    @Test
    public void testCreateRotationQuaternion() {
        float[] axis = {1f, 0f, 0f}; // X-axis
        double angle = Math.PI / 2.0; // 90 degrees
        float[] result = new float[4];
        MyMath.createRotationQuaterion(axis, angle, result);
        
        float sinAngleOver2 = (float)Math.sin(Math.PI / 4.0); // sqrt(2)/2
        float cosAngleOver2 = (float)Math.cos(Math.PI / 4.0); // sqrt(2)/2
        
        assertArrayEquals("Quaternion for 90 deg rot around X-axis", 
                          new float[]{sinAngleOver2, 0f, 0f, cosAngleOver2}, result, DELTA);

        float[] resultAlloc = MyMath.createRotationQuaterion(axis, angle);
        assertArrayEquals("Quaternion (alloc version) for 90 deg rot around X-axis", 
                          new float[]{sinAngleOver2, 0f, 0f, cosAngleOver2}, resultAlloc, DELTA);

        // Zero rotation
        MyMath.createRotationQuaterion(axis, 0.0, result);
        assertArrayEquals("Quaternion for 0 deg rotation", QUAT_IDENTITY, result, DELTA);
    }

    @Test
    public void testQuaternionProduct() {
        // Q1 * Identity = Q1
        float[] q1 = {0.1f, 0.2f, 0.3f, 0.9f}; // Needs to be normalized for rotation, but product works on any
        MyMath.quaternionNormalize(q1); // Normalize for meaningful test if it were a rotation
        float[] result = new float[4];
        MyMath.quaternionProduct(q1, QUAT_IDENTITY, result);
        assertArrayEquals("Q1 * Identity should be Q1", q1, result, DELTA);

        // Identity * Q1 = Q1
        MyMath.quaternionProduct(QUAT_IDENTITY, q1, result);
        assertArrayEquals("Identity * Q1 should be Q1", q1, result, DELTA);

        // Example from a known source, e.g. q_i * q_j = q_k
        // q_i = (1,0,0,0) (representing rotation by PI around x-axis if angle/2)
        // For this test, let's use q = (x,y,z,w)
        // Let q_x_90 = (sqrt(0.5), 0, 0, sqrt(0.5)) -> 90 deg rot around x
        // Let q_y_90 = (0, sqrt(0.5), 0, sqrt(0.5)) -> 90 deg rot around y
        float val = (float)Math.sqrt(0.5);
        float[] qx90 = {val, 0f, 0f, val};
        float[] qy90 = {0f, val, 0f, val};
        // Product qx90 * qy90 (from online calculator or manual derivation):
        // x = v1x*s2 + s1*v2x + (v1y*v2z - v1z*v2y) = val*val + val*0 + (0*0 - 0*val) = 0.5
        // y = v1y*s2 + s1*v2y + (v1z*v2x - v1x*v2z) = 0*val + val*val + (0*0 - val*0) = 0.5
        // z = v1z*s2 + s1*v2z + (v1x*v2y - v1y*v2x) = 0*val + val*0 + (val*val - 0*0) = 0.5
        // w = s1*s2 - (v1x*v2x + v1y*v2y + v1z*v2z) = val*val - (val*0 + 0*val + 0*0) = 0.5
        float[] expectedQxQy = {0.5f, 0.5f, 0.5f, 0.5f};
        MyMath.quaternionProduct(qx90, qy90, result);
        assertArrayEquals("Product of qx90 * qy90", expectedQxQy, result, DELTA);
        
        float[] resultAlloc = MyMath.quaternionProduct(qx90, qy90);
        assertArrayEquals("Product of qx90 * qy90 (alloc version)", expectedQxQy, resultAlloc, DELTA);
    }

    @Test
    public void testQuaternionLengthAndNormalize() {
        float[] q = {1f, 2f, 3f, 4f};
        float expectedLength = (float)Math.sqrt(1*1 + 2*2 + 3*3 + 4*4); // sqrt(1+4+9+16) = sqrt(30)
        assertEquals("Quaternion length", expectedLength, MyMath.quaternionLength(q), DELTA);

        MyMath.quaternionNormalize(q); // In-place
        assertEquals("Length after normalize should be 1", 1f, MyMath.quaternionLength(q), DELTA);
        assertArrayEquals("Normalized quaternion", 
                          new float[]{1f/expectedLength, 2f/expectedLength, 3f/expectedLength, 4f/expectedLength}, 
                          q, DELTA);
        
        float[] qZero = {0f,0f,0f,0f}; // Not a valid rotation quaternion
        MyMath.quaternionNormalize(qZero);
        // Implementation sets to (0,0,0,1) if length is too small
        assertArrayEquals("Normalize zero quaternion", QUAT_IDENTITY, qZero, DELTA);
    }

    @Test
    public void testQuaternionInvert() {
        float[] q = {1f, 1f, 1f, 1f}; // Non-unit quaternion
        MyMath.quaternionNormalize(q); // Now it's a unit quaternion
        
        float[] result = new float[4];
        MyMath.quaternionInvert(q, result); // q_inv for unit quaternion is its conjugate (-x,-y,-z,w)
        
        assertArrayEquals("Invert unit quaternion", new float[]{-q[0], -q[1], -q[2], q[3]}, result, DELTA);

        // Test product of q * q_inv = Identity
        float[] product = MyMath.quaternionProduct(q, result);
        assertArrayEquals("Q * Q_inv should be Identity", QUAT_IDENTITY, product, DELTA);

        // Test in-place inversion
        float[] qCopy = Arrays.copyOf(q, q.length);
        MyMath.quaternionInvert(qCopy); // In-place
        assertArrayEquals("In-place invert unit quaternion", result, qCopy, DELTA);
        
        // Test inversion of non-unit quaternion
        float[] qNonUnit = {1f,0f,0f,1f}; // Length sqrt(2)
        float magSq = 1*1 + 0*0 + 0*0 + 1*1; // 2
        float[] expectedInvNonUnit = {-1f/magSq, 0f/magSq, 0f/magSq, 1f/magSq};
        MyMath.quaternionInvert(qNonUnit, result);
        assertArrayEquals("Invert non-unit quaternion {1,0,0,1}", expectedInvNonUnit, result, DELTA);

        // Test inversion of zero quaternion (should result in identity or error state)
        float[] qZero = {0f,0f,0f,0f};
        MyMath.quaternionInvert(qZero, result);
        assertArrayEquals("Invert zero quaternion", QUAT_IDENTITY, result, DELTA);
    }

    @Test
    public void testRotateMethod_ActualBehavior() {
        // The 'rotate' method in MyMath is documented to rotate point p.
        // However, its implementation inverts the quaternion 'q' (first argument) and returns it.
        // It does not use q_inverse and does not return a rotated point.
        // This test verifies the ACTUAL behavior of the provided code.
        
        float[] q = { (float)Math.sqrt(0.5), 0f, 0f, (float)Math.sqrt(0.5) }; // Normalized: 90 deg rot around X
        float[] qOriginal = Arrays.copyOf(q, q.length);
        float[] qInvExpected = new float[4];
        MyMath.quaternionInvert(qOriginal, qInvExpected); // Calculate expected inverse

        float[] p = {0f, 1f, 0f}; // Point to "rotate"
        float[] qInverseParam = {0f,0f,0f,1f}; // This param is unused by the current implementation

        // The method modifies 'q' in place and returns 'q'.
        float[] returnedQ = MyMath.rotate(q, qInverseParam, p);

        assertSame("Returned quaternion should be the same instance as input q", q, returnedQ);
        assertArrayEquals("Input quaternion q should be modified to its inverse", qInvExpected, q, DELTA);
        
        // Verify p is untouched
        assertArrayEquals("Point p should remain unchanged", new float[]{0f,1f,0f}, p, DELTA);
    }
    
    @Test
    public void testAreEqual() {
        float[] v1 = {1.0f, 2.0f, 3.0f};
        float[] v2 = {1.0f, 2.0f, 3.0f};
        float[] v3 = {1.0f, 2.0f, 3.000001f}; // Slightly different
        float[] v4 = {1.1f, 2.0f, 3.0f};

        assertTrue("v1 should be equal to v2", MyMath.areEqual(v1, v2));
        // MyMath.areEqual uses exact float comparison, so v1 and v3 will not be equal.
        assertFalse("v1 should not be equal to v3 due to precision", MyMath.areEqual(v1, v3));
        assertFalse("v1 should not be equal to v4", MyMath.areEqual(v1, v4));
    }

    @Test
    public void testToString() {
        float[] v = {1.23456f, -0.78901f, 100.0f};
        String expected = String.format("(%.4f,%.4f,%.4f)", v[0], v[1], v[2]);
        // Produces: (1.2346,-0.7890,100.0000) based on rounding rules of String.format
        assertEquals("toString formatting", expected, MyMath.toString(v));

        float[] v2 = {0f, 0f, 0f};
        expected = "(0.0000,0.0000,0.0000)";
        assertEquals("toString for zero vector", expected, MyMath.toString(v2));
    }

    // --- Tests for sin, cos, sqrt (as per task, though not in MyMath.java) ---
    // These will fail as MyMath does not have these. Included to show awareness of task.
    /*
    @Test
    public void testSinFunction() {
        // This test would be for MyMath.sin if it existed.
        // assertEquals(Math.sin(0), MyMath.sin(0), DELTA);
        // assertEquals(Math.sin(Math.PI/2), MyMath.sin((float)(Math.PI/2)), DELTA);
        fail("MyMath.sin(float) does not exist in the provided MyMath.java");
    }

    @Test
    public void testCosFunction() {
        // This test would be for MyMath.cos if it existed.
        // assertEquals(Math.cos(0), MyMath.cos(0), DELTA);
        // assertEquals(Math.cos(Math.PI), MyMath.cos((float)Math.PI), DELTA);
        fail("MyMath.cos(float) does not exist in the provided MyMath.java");
    }

    @Test
    public void testSqrtFunction() {
        // This test would be for MyMath.sqrt if it existed.
        // assertEquals(Math.sqrt(4.0), MyMath.sqrt(4.0f), DELTA);
        // assertEquals(Math.sqrt(2.0), MyMath.sqrt(2.0f), DELTA);
        // For negative input:
        // assertTrue(Float.isNaN(MyMath.sqrt(-1.0f))); // Assuming it behaves like Math.sqrt
        fail("MyMath.sqrt(float) does not exist in the provided MyMath.java");
    }
    */
}
