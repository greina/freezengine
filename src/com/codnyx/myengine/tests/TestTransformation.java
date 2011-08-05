package com.codnyx.myengine.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.codnyx.myengine.AffineTransformation;

public class TestTransformation {

	private static final float INITIAL_THRESHOLD = .0001f;
	private float threshold = INITIAL_THRESHOLD;
	
	AffineTransformation t = new AffineTransformation();
	TransformatorGenerator rotZ = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		};
		@Override
		public void transform(AffineTransformation t) {
			t.rotateZ((float)(Math.random()*Math.PI*2));
		}
	};

	TransformatorGenerator rotY = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		};
		@Override
		public void transform(AffineTransformation t) {
			t.rotateY((float)(Math.random()*Math.PI*2));
		}
	};
	

	TransformatorGenerator rotX = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		};
		@Override
		public void transform(AffineTransformation t) {
			t.rotateX((float)(Math.random()*Math.PI*2));
		}
	};

	TransformatorGenerator tr = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		};
		@Override
		public void transform(AffineTransformation t) {
			float x = (float)Math.random()*10;
			float y = (float)Math.random()*10;
			float z = (float)Math.random()*10;
			t.translateTo(x,y,z);
		}
	};

	TransformatorGenerator scale = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		};
		@Override
		public void transform(AffineTransformation t) {
			float x = (float)Math.random()*2;
			float y = (float)Math.random()*2;
			float z = (float)Math.random()*2;
			t.scaleOf(x,y,z);
		}
	};
	
	TransformatorGenerator mixed = new TransformatorGenerator() {
		private int counter;

		public void init() 
		{
			threshold = INITIAL_THRESHOLD*10;
		};
		
		@Override
		public void transform(AffineTransformation t) {
			if(++counter % 25 == 0)
			{
				t.loadIdentity();
				counter = 0;
			}
			int choice = (int) (Math.random()*5);
			boolean log = false;
			switch(choice)
			{
			case 0: rotX.transform(t); if(log) System.out.println("Rotating x"); break;
			case 1: rotY.transform(t); if(log) System.out.println("Rotating y"); break;
			case 2: rotZ.transform(t); if(log) System.out.println("Rotating z"); break;
			case 3: scale.transform(t); if(log) System.out.println("Scaling"); break;
			case 4: tr.transform(t); if(log) System.out.println("Translating"); break;
			}
			
		}
	};
	
	
	
	
	@Test
	public void testRotZ() {
		TransformatorGenerator tg = rotZ;
		tg.init();
		testTransformation(tg);
	}

	@Test
	public void testRotY()
	{
		TransformatorGenerator tg = rotY;
		tg.init();
		testTransformation(tg);
	}
	
	@Test
	public void testRotX()
	{
		TransformatorGenerator tg = rotX;
		tg.init();
		testTransformation(tg);
	}

	
	@Test
	public void testTraslation()
	{
		TransformatorGenerator tg = tr;
		tg.init();
		testTransformation(tg);
	}


	@Test
	public void testScaling()
	{
		TransformatorGenerator tg = scale;
		tg.init();
		testTransformation(tg);
	}
	
	@Test
	public void testMixed()
	{
		TransformatorGenerator tg = mixed;
		tg.init();
		testTransformation(tg);
	}
	
	
	public void testTransformation(TransformatorGenerator g)
	{
		t.loadIdentity();
		float[] point = {0,0,0};
		float[] result = {0,0,0};
		for(int run = 1; run <= 100; run++)
		{
			for(int i = 0; i < 3; i++)
				point[i] = (float)Math.random()*10;
//			System.out.println(String.format("Run %d, point(%.3f, %.3f, %.3f)", run, point[0], point[1], point[2]));
			g.transform(t);
			t.transform(point, result);
			t.inverse_transform(result);
			for(int i = 0; i < 3; i ++)
			{
//				System.out.println(String.format("%.4f - %.4f = %.4f < %.4f", point[i], result[i], point[i]-result[i], threshold));
				assertEquals(point[i], result[i],threshold);
			}
		}
	}

	interface TransformatorGenerator
	{
		void init();
		void transform(AffineTransformation t);
	}

}
