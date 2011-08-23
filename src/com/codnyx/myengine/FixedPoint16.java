package com.codnyx.myengine;

public class FixedPoint16 
{
	public final static int SCALE_BITS = 16;
	public final static int SCALE = 1<<SCALE_BITS;
	public final static int SCALE_MASK = SCALE-1;
	public int value;
	
	public FixedPoint16()
	{
		value = 0;
	}
	
	public FixedPoint16(float n)
	{
		setFromFloat(n);
	}

	public FixedPoint16(int n)
	{
		setFromInt(n);
	}
	
	public final void setFromFloat(float n) 
	{
		value = (int)(n*SCALE);
	}
	
	public final float toFloat()
	{
		return ((float)value)/SCALE;
	}
	
	public final float getFractional()
	{
		return ((float)(value & SCALE_MASK))/SCALE;
	}

	public final void setFromInt(int n) 
	{
		value = n << SCALE_BITS;
	}
	
	public final int toInt()
	{
		return value>>SCALE_BITS;
	}
	
	public final FixedPoint16 multiplyInt(int n)
	{
		 value *= n;
		 return this;
	}
	
	public final FixedPoint16 add(FixedPoint16 fp)
	{
		value += fp.value;
		return this;
	}

	public final FixedPoint16 subtract(FixedPoint16 fp)
	{
		value -= fp.value;
		return this;
	}
	
	public final FixedPoint16 multiply(FixedPoint16 fp)
	{
		value = (int)(((long)value)*fp.value >> SCALE_BITS);
		return this;
	}
	
	public final FixedPoint16 divide(FixedPoint16 fp)
	{
		value = (int)(((long)value << SCALE_BITS)/fp.value);
		return this;
	}
	
	public final static int FromFloat(float n) 
	{
		return (int)(n*SCALE);
	}
	
	public final static float ToFloat(int value)
	{
		return ((float)value)/SCALE;
	}
	
	public final static float getFractional(int value)
	{
		return ((float)(value & SCALE_MASK))/SCALE;
	}

	public final static int FromInt(int n) 
	{
		return n << SCALE_BITS;
	}
	
	public final static int ToInt(int value)
	{
		return value>>SCALE_BITS;
	}

}
