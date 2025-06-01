package com.codnyx.myengine;

/**
 * Represents a 16.16 fixed-point number.
 * This class provides methods for converting between floating-point numbers, integers,
 * and fixed-point representation, as well as arithmetic operations on fixed-point numbers.
 * The upper 16 bits represent the integer part, and the lower 16 bits represent the fractional part.
 */
public class FixedPoint16 
{
	/**
	 * The number of bits used for the fractional part.
	 */
	public final static int SCALE_BITS = 16;
	/**
	 * The scaling factor (2^SCALE_BITS), used to convert between floating-point/integer and fixed-point.
	 */
	public final static int SCALE = 1<<SCALE_BITS;
	/**
	 * Mask to extract the fractional part of the fixed-point number.
	 */
	public final static int SCALE_MASK = SCALE-1;
	/**
	 * The raw integer value holding the fixed-point number.
	 */
	public int value;
	
	/**
	 * Default constructor. Initializes the fixed-point value to 0.
	 */
	public FixedPoint16()
	{
		value = 0;
	}
	
	/**
	 * Constructs a FixedPoint16 from a float value.
	 * @param n The float value to convert.
	 */
	public FixedPoint16(float n)
	{
		setFromFloat(n);
	}

	/**
	 * Constructs a FixedPoint16 from an integer value.
	 * @param n The integer value to convert. The integer is treated as a whole number.
	 */
	public FixedPoint16(int n)
	{
		setFromInt(n);
	}
	
	/**
	 * Sets the fixed-point value from a float.
	 * @param n The float value to convert.
	 */
	public final void setFromFloat(float n) 
	{
		value = (int)(n*SCALE);
	}
	
	/**
	 * Converts the fixed-point value to a float.
	 * @return The float representation of this fixed-point number.
	 */
	public final float toFloat()
	{
		return ((float)value)/SCALE;
	}
	
	/**
	 * Gets the fractional part of the fixed-point number as a float.
	 * @return The fractional part as a float, in the range [0.0, 1.0).
	 */
	public final float getFractional()
	{
		return ((float)(value & SCALE_MASK))/SCALE;
	}

	/**
	 * Sets the fixed-point value from an integer.
	 * The integer is treated as a whole number and converted to its fixed-point representation.
	 * @param n The integer value.
	 */
	public final void setFromInt(int n) 
	{
		value = n << SCALE_BITS;
	}
	
	/**
	 * Converts the fixed-point value to an integer, truncating the fractional part.
	 * @return The integer part of this fixed-point number.
	 */
	public final int toInt()
	{
		// Using integer division for truncation towards zero, consistent with (int)float_val
		return value / SCALE; 
	}
	
	/**
	 * Multiplies this fixed-point number by an integer.
	 * @param n The integer to multiply by.
	 * @return This FixedPoint16 instance after multiplication, for chaining.
	 */
	public final FixedPoint16 multiplyInt(int n)
	{
		 value *= n;
		 return this;
	}
	
	/**
	 * Adds another FixedPoint16 number to this one.
	 * @param fp The FixedPoint16 number to add.
	 * @return This FixedPoint16 instance after addition, for chaining.
	 */
	public final FixedPoint16 add(FixedPoint16 fp)
	{
		value += fp.value;
		return this;
	}

	/**
	 * Subtracts another FixedPoint16 number from this one.
	 * @param fp The FixedPoint16 number to subtract.
	 * @return This FixedPoint16 instance after subtraction, for chaining.
	 */
	public final FixedPoint16 subtract(FixedPoint16 fp)
	{
		value -= fp.value;
		return this;
	}
	
	/**
	 * Multiplies this FixedPoint16 number by another FixedPoint16 number.
	 * @param fp The FixedPoint16 number to multiply by.
	 * @return This FixedPoint16 instance after multiplication, for chaining.
	 */
	public final FixedPoint16 multiply(FixedPoint16 fp)
	{
		value = (int)(((long)value)*fp.value >> SCALE_BITS);
		return this;
	}
	
	/**
	 * Divides this FixedPoint16 number by another FixedPoint16 number.
	 * @param fp The FixedPoint16 number to divide by.
	 * @return This FixedPoint16 instance after division, for chaining.
	 */
	public final FixedPoint16 divide(FixedPoint16 fp)
	{
		value = (int)(((long)value << SCALE_BITS)/fp.value);
		return this;
	}
	
	/**
	 * Static utility method to convert a float to its 16.16 fixed-point integer representation.
	 * @param n The float value to convert.
	 * @return The integer representation of the fixed-point number.
	 */
	public final static int FromFloat(float n) 
	{
		return (int)(n*SCALE);
	}
	
	/**
	 * Static utility method to convert a 16.16 fixed-point integer representation to a float.
	 * @param value The integer representation of the fixed-point number.
	 * @return The float value.
	 */
	public final static float ToFloat(int value)
	{
		return ((float)value)/SCALE;
	}
	
	/**
	 * Static utility method to get the fractional part of a 16.16 fixed-point integer representation as a float.
	 * @param value The integer representation of the fixed-point number.
	 * @return The fractional part as a float, in the range [0.0, 1.0).
	 */
	public final static float getFractional(int value)
	{
		return ((float)(value & SCALE_MASK))/SCALE;
	}

	/**
	 * Static utility method to convert an integer to its 16.16 fixed-point integer representation.
	 * The integer is treated as a whole number.
	 * @param n The integer value.
	 * @return The integer representation of the fixed-point number.
	 */
	public final static int FromInt(int n) 
	{
		return n << SCALE_BITS;
	}
	
	/**
	 * Static utility method to convert a 16.16 fixed-point integer representation to an integer,
	 * truncating the fractional part.
	 * @param value The integer representation of the fixed-point number.
	 * @return The integer part of the fixed-point number.
	 */
	public final static int ToInt(int value)
	{
		// Using integer division for truncation towards zero
		return value / SCALE;
	}

}
