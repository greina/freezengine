package com.codnyx.myengine;

public class FixedPoint 
{
	public final static int SCALE_BITS = 16;
	public final static int SCALE = 1<<SCALE_BITS;
	public final static int SCALE_MASK = SCALE-1;
	public int value;
	
	public FixedPoint()
	{
		value = 0;
	}
	
	public FixedPoint(float n)
	{
		setFromFloat(n);
	}

	public FixedPoint(int n)
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
	
	public final FixedPoint multiplyInt(int n)
	{
		 value *= n;
		 return this;
	}
	
	public final FixedPoint add(FixedPoint fp)
	{
		value += fp.value;
		return this;
	}

	public final FixedPoint subtract(FixedPoint fp)
	{
		value -= fp.value;
		return this;
	}
	
	public final FixedPoint multiply(FixedPoint fp)
	{
		value = (int)(((long)value)*fp.value >> SCALE_BITS);
		return this;
	}
	
	public final FixedPoint divide(FixedPoint fp)
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
