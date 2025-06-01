package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import com.codnyx.myengine.FixedPoint16;

/**
 * JUnit test class for the FixedPoint16 class.
 * This class tests constructors, conversions, arithmetic operations,
 * and other public methods of FixedPoint16.
 */
public class TestFixedPoint16 {

    private static final float DELTA = 0.0001f; // Delta for float comparisons

    // --- Test Constructors ---

    @Test
    public void testConstructorFromInt() {
        FixedPoint16 fp = new FixedPoint16(5);
        assertEquals("Integer constructor: 5", 5.0f, fp.toFloat(), DELTA);
        assertEquals("Integer constructor: 5 (raw value)", 5 << FixedPoint16.SCALE_BITS, fp.value);

        FixedPoint16 fpNegative = new FixedPoint16(-3);
        assertEquals("Integer constructor: -3", -3.0f, fpNegative.toFloat(), DELTA);
        assertEquals("Integer constructor: -3 (raw value)", -3 << FixedPoint16.SCALE_BITS, fpNegative.value);
        
        FixedPoint16 fpZero = new FixedPoint16(0);
        assertEquals("Integer constructor: 0", 0.0f, fpZero.toFloat(), DELTA);
        assertEquals("Integer constructor: 0 (raw value)", 0, fpZero.value);
    }

    @Test
    public void testConstructorFromFloat() {
        FixedPoint16 fp = new FixedPoint16(3.75f);
        assertEquals("Float constructor: 3.75", 3.75f, fp.toFloat(), DELTA);

        FixedPoint16 fpNegative = new FixedPoint16(-2.5f);
        assertEquals("Float constructor: -2.5", -2.5f, fpNegative.toFloat(), DELTA);

        FixedPoint16 fpZero = new FixedPoint16(0.0f);
        assertEquals("Float constructor: 0.0", 0.0f, fpZero.toFloat(), DELTA);
        
        FixedPoint16 fpFractional = new FixedPoint16(0.125f);
        assertEquals("Float constructor: 0.125", 0.125f, fpFractional.toFloat(), DELTA);

        FixedPoint16 fpLarge = new FixedPoint16(10000.123f);
        assertEquals("Float constructor: 10000.123", 10000.123f, fpLarge.toFloat(), DELTA);
        
        FixedPoint16 fpSmallNegative = new FixedPoint16(-0.001f);
        // Expected value will be close to 0 due to precision.
        // The actual stored value is (int)(-0.001f * SCALE) which might be 0 or -1 depending on rounding/truncation.
        // (int)(-0.001 * 65536) = (int)(-65.536) = -65
        // -65 / 65536.0f = -0.0009918212890625f
        assertEquals("Float constructor: -0.001", -0.0009918f, fpSmallNegative.toFloat(), DELTA);
    }
    
    @Test
    public void testDefaultConstructor() {
        FixedPoint16 fp = new FixedPoint16();
        assertEquals("Default constructor should initialize to 0", 0.0f, fp.toFloat(), DELTA);
        assertEquals("Default constructor raw value should be 0", 0, fp.value);
    }

    // --- Test Conversions ---

    @Test
    public void testToFloat() {
        FixedPoint16 fp = new FixedPoint16();
        fp.value = FixedPoint16.FromFloat(123.456f);
        assertEquals("toFloat conversion", 123.456f, fp.toFloat(), DELTA);

        fp.value = FixedPoint16.FromFloat(-0.5f);
        assertEquals("toFloat conversion for negative", -0.5f, fp.toFloat(), DELTA);
    }

    @Test
    public void testToInt() {
        FixedPoint16 fp = new FixedPoint16(7.8f);
        assertEquals("toInt conversion (truncation): 7.8 -> 7", 7, fp.toInt());

        FixedPoint16 fpNegative = new FixedPoint16(-3.2f);
        assertEquals("toInt conversion (truncation): -3.2 -> -3", -3, fpNegative.toInt());
        
        FixedPoint16 fpAlmostNext = new FixedPoint16(4.999f);
        assertEquals("toInt conversion (truncation): 4.999 -> 4", 4, fpAlmostNext.toInt());

        FixedPoint16 fpAlmostPrev = new FixedPoint16(-2.001f);
        assertEquals("toInt conversion (truncation): -2.001 -> -2", -2, fpAlmostPrev.toInt());
        
        FixedPoint16 fpZero = new FixedPoint16(0.123f);
        assertEquals("toInt conversion (truncation): 0.123 -> 0", 0, fpZero.toInt());
    }

    // --- Test Arithmetic Operations ---

    @Test
    public void testAdd() {
        FixedPoint16 fp1 = new FixedPoint16(2.5f);
        FixedPoint16 fp2 = new FixedPoint16(3.25f);
        fp1.add(fp2);
        assertEquals("Add: 2.5 + 3.25 = 5.75", 5.75f, fp1.toFloat(), DELTA);

        FixedPoint16 fp3 = new FixedPoint16(-1.5f);
        fp1.add(fp3); // 5.75 + (-1.5) = 4.25
        assertEquals("Add: 5.75 + (-1.5) = 4.25", 4.25f, fp1.toFloat(), DELTA);

        FixedPoint16 fp4 = new FixedPoint16(0.0f);
        fp1.add(fp4); // 4.25 + 0.0 = 4.25
        assertEquals("Add: 4.25 + 0.0 = 4.25", 4.25f, fp1.toFloat(), DELTA);
        
        FixedPoint16 fp5 = new FixedPoint16(10000f);
        FixedPoint16 fp6 = new FixedPoint16(20000f);
        fp5.add(fp6);
        assertEquals("Add: 10000 + 20000 = 30000", 30000f, fp5.toFloat(), DELTA);

        FixedPoint16 fp7 = new FixedPoint16(-10000f);
        FixedPoint16 fp8 = new FixedPoint16(-20000f);
        fp7.add(fp8);
        assertEquals("Add: -10000 + (-20000) = -30000", -30000f, fp7.toFloat(), DELTA);
    }

    @Test
    public void testSubtract() {
        FixedPoint16 fp1 = new FixedPoint16(5.75f);
        FixedPoint16 fp2 = new FixedPoint16(3.25f);
        fp1.subtract(fp2);
        assertEquals("Subtract: 5.75 - 3.25 = 2.5", 2.5f, fp1.toFloat(), DELTA);

        FixedPoint16 fp3 = new FixedPoint16(-1.5f);
        fp1.subtract(fp3); // 2.5 - (-1.5) = 4.0
        assertEquals("Subtract: 2.5 - (-1.5) = 4.0", 4.0f, fp1.toFloat(), DELTA);
        
        FixedPoint16 fp4 = new FixedPoint16(0.0f);
        fp1.subtract(fp4); // 4.0 - 0.0 = 4.0
        assertEquals("Subtract: 4.0 - 0.0 = 4.0", 4.0f, fp1.toFloat(), DELTA);

        FixedPoint16 fp5 = new FixedPoint16(10000f);
        FixedPoint16 fp6 = new FixedPoint16(20000f);
        fp5.subtract(fp6);
        assertEquals("Subtract: 10000 - 20000 = -10000", -10000f, fp5.toFloat(), DELTA);
    }

    @Test
    public void testMultiply() {
        FixedPoint16 fp1 = new FixedPoint16(2.5f);
        FixedPoint16 fp2 = new FixedPoint16(3.0f);
        fp1.multiply(fp2);
        assertEquals("Multiply: 2.5 * 3.0 = 7.5", 7.5f, fp1.toFloat(), DELTA);

        FixedPoint16 fp3 = new FixedPoint16(-2.0f);
        fp1.multiply(fp3); // 7.5 * (-2.0) = -15.0
        assertEquals("Multiply: 7.5 * (-2.0) = -15.0", -15.0f, fp1.toFloat(), DELTA);

        FixedPoint16 fp4 = new FixedPoint16(0.5f);
        fp1.multiply(fp4); // -15.0 * 0.5 = -7.5
        assertEquals("Multiply: -15.0 * 0.5 = -7.5", -7.5f, fp1.toFloat(), DELTA);
        
        FixedPoint16 fp5 = new FixedPoint16(0.0f);
        fp1.multiply(fp5); // -7.5 * 0.0 = 0.0
        assertEquals("Multiply: -7.5 * 0.0 = 0.0", 0.0f, fp1.toFloat(), DELTA);

        // Test potential overflow - Max int is 2^31-1. SCALE is 2^16.
        // Max FixedPoint16 value is approx (2^31-1) / 2^16 = 2^15 - 2^-16 approx 32767
        // 200 * 200 = 40000. This will overflow if intermediate (long)value * fp.value is not handled carefully.
        // The implementation uses: value = (int)(((long)value)*fp.value >> SCALE_BITS); which is correct.
        FixedPoint16 fpLarge1 = new FixedPoint16(200.0f); // Raw: 200 * 65536
        FixedPoint16 fpLarge2 = new FixedPoint16(200.0f); // Raw: 200 * 65536
        // (200*SCALE) * (200*SCALE) >> SCALE = 40000 * SCALE * SCALE >> SCALE = 40000 * SCALE
        fpLarge1.multiply(fpLarge2);
        // The actual result will be wrapped due to int overflow.
        // (int)(2621440000L) is -1673527296.
        // -1673527296 / 65536.0f = -25536.0f.
        assertEquals("Multiply large numbers (check for wrap): 200.0 * 200.0", -25536.0f, fpLarge1.toFloat(), DELTA);

        FixedPoint16 fpMax = new FixedPoint16(32767.0f);
        FixedPoint16 fpTwo = new FixedPoint16(2.0f);
        // 32767.0 * 2.0 = 65534.0. This is close to max positive value for int after scaling.
        // (32767 * SCALE) * (2 * SCALE) >> SCALE = (32767 * 2) * SCALE
        // If this exceeds 2^31-1 it will overflow. (2^15-1)*2 * 2^16 = (2^16-2)*2^16 which is too large for the 'value' int.
        // Let's check the raw values:
        // fpMax.value = 32767 * 65536 = 2147418112
        // fpTwo.value = 2 * 65536 = 131072
        // ((long)fpMax.value * fpTwo.value) >> 16
        // (2147418112L * 131072L) >> 16 = 281462007631872L >> 16
        // This result 281462007631872L is (2147418112 * 2), which is 4294836224.
        // This value 4294836224 when cast to int becomes 0 because it's 2^32.
        // This indicates an overflow in the intermediate long multiplication before the shift if numbers are large enough.
        // The maximum product of two FixedPoint16 numbers that fits in a long is (2^63-1).
        // (val1 * val2) / SCALE. If val1 and val2 are around 2^31-1 (max int), their product is ~2^62. This fits.
        // The actual limit is on 'value' which is int. So result must be < (2^31-1).
        // (X * SCALE) * (Y * SCALE) / SCALE = X * Y * SCALE.
        // So X*Y*SCALE must be < 2^31-1. If X*Y is > 2^15-1 (approx 32767), it overflows.
        // So 200*200 = 40000 overflows. The test above for 40000 should fail.
        // It passes because 40000.0f * SCALE = 40000 * 65536 = 2621440000, which is > 2^31-1.
        // This means fpLarge1.toFloat() is actually showing a wrapped around value.
        // (int) (40000.0f * 65536.0f) = (int) 2621440000.0f = -1673527296 (due to overflow)
        // So the test: assertEquals("Multiply large numbers: 200.0 * 200.0 = 40000.0", 40000.0f, fpLarge1.toFloat(), DELTA);
        // becomes: assertEquals(40000.0f, (float)-1673527296 / 65536.0f, DELTA)
        // assertEquals(40000.0f, -25536.0f, DELTA) which should fail.
        // Let's re-verify the calculation for fpLarge1.multiply(fpLarge2);
        // fpLarge1.value = 200 * 65536 = 13107200
        // fpLarge2.value = 200 * 65536 = 13107200
        // value = (int)(((long)13107200 * 13107200) >> 16)
        // value = (int)((17179869184000000L) >> 16)
        // value = (int)(262144000000L) -> this overflows long if not careful, but 1.7e16 is fine for long.
        // value = (int)(262144000000L) >> 16 means 262144000000 / 65536 = 4000000000
        // (int)4000000000L is -294967296.
        // So fpLarge1.toFloat() will be -294967296 / 65536.0f = -4500.0f
        // The test assertEquals("Multiply large numbers: 200.0 * 200.0 = 40000.0", 40000.0f, fpLarge1.toFloat(), DELTA);
        // should indeed be assertEquals(40000.0f, -4500.0f, DELTA) and fail.
        // The current implementation appears to be correct for multiply. The issue might be my manual overflow prediction.
        // Let's use smaller numbers that are closer to the int boundary for product * SCALE.
        // Say X*Y = 30000. 30000 * SCALE = 30000 * 65536 = 1966080000 (fits in int)
        FixedPoint16 fpA = new FixedPoint16(150.0f); // raw: 150 * 2^16
        FixedPoint16 fpB = new FixedPoint16(200.0f); // raw: 200 * 2^16
        fpA.multiply(fpB); // Expected: 150.0 * 200.0 = 30000.0
        assertEquals("Multiply: 150.0 * 200.0 = 30000.0", 30000.0f, fpA.toFloat(), DELTA);

        FixedPoint16 fpC = new FixedPoint16(0.001f);
        FixedPoint16 fpD = new FixedPoint16(0.001f);
        fpC.multiply(fpD); // 0.000001f
        // (int)(0.001f*SCALE) = -65 or similar if negative, or small positive if positive
        // (int)(0.001f*SCALE) = (int)(65.536) = 65
        // value = (65L * 65L) >> 16 = 4225L >> 16 = 0
        assertEquals("Multiply very small numbers: 0.001 * 0.001 = 0.000001", 0.000001f, fpC.toFloat(), DELTA);
        // Actual result for 0.001f * 0.001f will be 0.0f because (65*65) >> 16 = 0.
        // So the test should be:
        assertEquals("Multiply very small numbers (expect 0 due to precision): 0.001 * 0.001", 0.0f, fpC.toFloat(), DELTA);

    }

    @Test
    public void testDivide() {
        FixedPoint16 fp1 = new FixedPoint16(7.5f);
        FixedPoint16 fp2 = new FixedPoint16(3.0f);
        fp1.divide(fp2);
        assertEquals("Divide: 7.5 / 3.0 = 2.5", 2.5f, fp1.toFloat(), DELTA);

        FixedPoint16 fp3 = new FixedPoint16(-2.0f);
        fp1.divide(fp3); // 2.5 / (-2.0) = -1.25
        assertEquals("Divide: 2.5 / (-2.0) = -1.25", -1.25f, fp1.toFloat(), DELTA);
        
        FixedPoint16 fp4 = new FixedPoint16(0.5f);
        fp1.divide(fp4); // -1.25 / 0.5 = -2.5
        assertEquals("Divide: -1.25 / 0.5 = -2.5", -2.5f, fp1.toFloat(), DELTA);
        
        FixedPoint16 fp5 = new FixedPoint16(100.0f);
        FixedPoint16 fp6 = new FixedPoint16(4.0f);
        fp5.divide(fp6); // 100.0 / 4.0 = 25.0
        assertEquals("Divide: 100.0 / 4.0 = 25.0", 25.0f, fp5.toFloat(), DELTA);

        FixedPoint16 fp7 = new FixedPoint16(1.0f);
        FixedPoint16 fp8 = new FixedPoint16(3.0f);
        fp7.divide(fp8); // 1.0 / 3.0 = 0.333...
        assertEquals("Divide: 1.0 / 3.0 = 0.333...", 0.333333f, fp7.toFloat(), DELTA);
    }
    
    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        FixedPoint16 fp1 = new FixedPoint16(5.0f);
        FixedPoint16 fpZero = new FixedPoint16(0.0f);
        fp1.divide(fpZero); // Should throw ArithmeticException (division by zero)
    }

    // --- Test Other Public Methods ---

    @Test
    public void testSetFromFloat() {
        FixedPoint16 fp = new FixedPoint16();
        fp.setFromFloat(12.345f);
        assertEquals("setFromFloat: 12.345", 12.345f, fp.toFloat(), DELTA);
        fp.setFromFloat(-0.875f);
        assertEquals("setFromFloat: -0.875", -0.875f, fp.toFloat(), DELTA);
    }

    @Test
    public void testSetFromInt() {
        FixedPoint16 fp = new FixedPoint16();
        fp.setFromInt(10);
        assertEquals("setFromInt: 10", 10.0f, fp.toFloat(), DELTA);
        assertEquals("setFromInt: 10 (raw value)", 10 << FixedPoint16.SCALE_BITS, fp.value);
        fp.setFromInt(-50);
        assertEquals("setFromInt: -50", -50.0f, fp.toFloat(), DELTA);
        assertEquals("setFromInt: -50 (raw value)", -50 << FixedPoint16.SCALE_BITS, fp.value);
    }

    @Test
    public void testGetFractional() {
        FixedPoint16 fp1 = new FixedPoint16(5.678f);
        // 5.678 * SCALE = 5.678 * 65536 = 372130.048 -> 372130 (raw value)
        // Fractional part: 372130 & (SCALE-1) = 372130 & 65535 = 44450
        // 44450 / SCALE = 44450 / 65536.0f = 0.67803955f
        assertEquals("getFractional for 5.678", 0.67803955f, fp1.getFractional(), DELTA);

        FixedPoint16 fp2 = new FixedPoint16(-2.25f);
        // -2.25f * SCALE = -147456
        // In FixedPoint16, value is stored as two's complement.
        // -2.25 -> integer part -3, fractional part 0.75.
        // Or, -147456. Fractional part: (-147456 & SCALE_MASK) / SCALE
        // -147456 & 65535 = 49152.  49152 / 65536.0 = 0.75
        // This is how it's implemented and is a common way.
        assertEquals("getFractional for -2.25", 0.75f, fp2.getFractional(), DELTA);
        
        FixedPoint16 fp3 = new FixedPoint16(10.0f); // Integer, so fractional is 0
        assertEquals("getFractional for 10.0", 0.0f, fp3.getFractional(), DELTA);
    }

    @Test
    public void testMultiplyInt() {
        FixedPoint16 fp = new FixedPoint16(3.5f);
        fp.multiplyInt(3); // 3.5 * 3 = 10.5
        assertEquals("multiplyInt: 3.5 * 3 = 10.5", 10.5f, fp.toFloat(), DELTA);

        fp.multiplyInt(-2); // 10.5 * (-2) = -21.0
        assertEquals("multiplyInt: 10.5 * (-2) = -21.0", -21.0f, fp.toFloat(), DELTA);
        
        fp.multiplyInt(0); // -21.0 * 0 = 0.0
        assertEquals("multiplyInt: -21.0 * 0 = 0.0", 0.0f, fp.toFloat(), DELTA);
    }

    // --- Test Static Utility Methods ---
    @Test
    public void testStaticFromFloat() {
        int val = FixedPoint16.FromFloat(7.5f);
        assertEquals("Static FromFloat: 7.5", (int)(7.5f * FixedPoint16.SCALE), val);
    }

    @Test
    public void testStaticToFloat() {
        int val = FixedPoint16.FromInt(12); // 12 << SCALE_BITS
        val += FixedPoint16.FromFloat(0.375f); // Add fractional part
        assertEquals("Static ToFloat: 12.375", 12.375f, FixedPoint16.ToFloat(val), DELTA);
    }
    
    @Test
    public void testStaticGetFractional() {
        int val = FixedPoint16.FromFloat(9.875f); // raw: 9.875 * SCALE
        // (val & SCALE_MASK) / SCALE = (0.875*SCALE)/SCALE = 0.875
        assertEquals("Static getFractional for 9.875", 0.875f, FixedPoint16.getFractional(val), DELTA);
    }

    @Test
    public void testStaticFromInt() {
        int val = FixedPoint16.FromInt(100);
        assertEquals("Static FromInt: 100", 100 << FixedPoint16.SCALE_BITS, val);
    }

    @Test
    public void testStaticToInt() {
        int val = FixedPoint16.FromFloat(42.7f); // (int)(42.7f * SCALE)
        assertEquals("Static ToInt: 42.7 -> 42", 42, FixedPoint16.ToInt(val));
    }

    // --- Test Equality (by comparing float values due to no .equals override) ---
    @Test
    public void testEqualityComparison() {
        FixedPoint16 fp1 = new FixedPoint16(10.5f);
        FixedPoint16 fp2 = new FixedPoint16(10.5f);
        FixedPoint16 fp3 = new FixedPoint16(10.50001f); // Very close
        FixedPoint16 fp4 = new FixedPoint16(-10.5f);

        assertEquals("Equality: fp1 and fp2 should be equal", fp1.toFloat(), fp2.toFloat(), DELTA);
        // For raw values, they should be identical if constructed from same float (usually)
        assertEquals("Equality: fp1 and fp2 raw values should be equal", fp1.value, fp2.value);

        // fp1 and fp3 might be different in raw value due to precision of float constructor
        // but their float values should be very close
        assertEquals("Equality: fp1 and fp3 should be approximately equal", fp1.toFloat(), fp3.toFloat(), DELTA);
        
        // assertNotEquals is not a standard JUnit method. We check inequality like this:
        assertTrue("Inequality: fp1 and fp4 should not be equal", fp1.toFloat() != fp4.toFloat());
        assertTrue("Inequality: fp1 and fp4 raw values should not be equal", fp1.value != fp4.value);
    }
    
    // --- Test Edge Cases and Potential Issues ---
    @Test
    public void testMultiplicationOverflowPotential() {
        // Max value for FixedPoint16 is (2^31 - 1) / 2^16 approx 32767.99
        // Min value for FixedPoint16 is (-2^31) / 2^16 approx -32768.0
        // If we multiply two numbers whose product X*Y > 32767.99, the result * SCALE will overflow int.
        // Example: 200.0f * 200.0f = 40000.0f.
        // Expected raw value for 40000.0f is 40000 * 2^16 = 2,621,440,000. This overflows signed 32-bit int.
        // Max signed int is 2,147,483,647.
        // The internal multiplication is: value = (int)(((long)value)*fp.value >> SCALE_BITS);
        
        FixedPoint16 fpA = new FixedPoint16(200.0f); // value_A = 200 * 2^16 = 13107200
        FixedPoint16 fpB = new FixedPoint16(200.0f); // value_B = 200 * 2^16 = 13107200
        
        // ((long)value_A * value_B) = 13107200L * 13107200L = 17179869184000000L
        // >> SCALE_BITS (16) = 17179869184000000L / 65536 = 262144000000L
        // (int)262144000000L will overflow. The actual long value is 2.62144E11
        // Max long for intermediate product is 2^63-1. (200*2^16)^2 is (200^2)*(2^32) = 40000 * 2^32 ~ 1.7 * 10^17. This fits in long.
        // The issue is when casting the result of the shift (which is product_val / SCALE) back to int.
        // product_val / SCALE = (value_A * value_B) / SCALE = (X*SCALE * Y*SCALE) / SCALE = X*Y*SCALE
        // So, X*Y*SCALE must fit in an int.
        // 40000.0f * SCALE = 40000.0f * 65536 = 2621440000. This is the value that should be stored in 'value'.
        // Since 2621440000 is greater than Integer.MAX_VALUE (2147483647), it overflows.
        // 2621440000 in binary is 10011100011011000000000000000000 (32 bits)
        // As a signed int, this is -1673527296.
        fpA.multiply(fpB);
        // So fpA.value becomes -1673527296.
        // fpA.toFloat() = -1673527296 / 65536.0f = -25536.0f.
        
        assertEquals("Multiply causing overflow: 200.0 * 200.0. Expected wrapped value.", -25536.0f, fpA.toFloat(), DELTA);

        // Test with values whose product * SCALE just fits
        FixedPoint16 fpC = new FixedPoint16(180.0f); // 180*2^16
        FixedPoint16 fpD = new FixedPoint16(180.0f); // 180*2^16
        // Product = 180*180 = 32400.
        // Expected raw value = 32400 * 2^16 = 2123366400. This fits in int.
        fpC.multiply(fpD);
        assertEquals("Multiply near limit: 180.0 * 180.0 = 32400.0", 32400.0f, fpC.toFloat(), DELTA);
    }

    @Test
    public void testDivisionPrecision() {
        FixedPoint16 fp1 = new FixedPoint16(1.0f);
        FixedPoint16 fp2 = new FixedPoint16(1000.0f);
        fp1.divide(fp2); // 1.0 / 1000.0 = 0.001
        // Raw for 1.0f is SCALE (65536)
        // Raw for 1000.0f is 1000 * SCALE (65536000)
        // value = (long)(SCALE) << SCALE_BITS / (1000 * SCALE)
        // value = (long)(SCALE * SCALE) / (1000 * SCALE)
        // value = SCALE / 1000 = 65536 / 1000 = 65 (integer division)
        // fp1.toFloat() = 65 / SCALE = 65 / 65536.0f = 0.000991821f
        assertEquals("Division precision 1.0/1000.0", 0.000991821f, fp1.toFloat(), DELTA);

        FixedPoint16 fp3 = new FixedPoint16(FixedPoint16.FromFloat(0.0001f)); // smallest representable non-zero if possible
        // FromFloat(0.0001f) = (int)(0.0001f * 65536) = (int)(6.5536) = 6
        // So fp3.value = 6
        FixedPoint16 fp4 = new FixedPoint16(2.0f); // fp4.value = 2 * SCALE
        fp3.divide(fp4); // fp3.value becomes (6 * SCALE) / (2 * SCALE) = 3
        
        // fp3.toFloat() = 3 / SCALE = 3 / 65536.0f = 0.0000457763671875f
        float expectedValue = 3.0f / FixedPoint16.SCALE;
        assertEquals("Division of small number", expectedValue, fp3.toFloat(), DELTA);
    }
}
