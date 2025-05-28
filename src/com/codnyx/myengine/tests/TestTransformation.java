package com.codnyx.myengine.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.codnyx.myengine.AffineTransformation;

/**
 * JUnit test class for {@link com.codnyx.myengine.AffineTransformation}.
 * This class verifies the correctness of affine transformations, particularly focusing
 * on the invertibility of various transformations (rotation, translation, scaling, and mixed sequences).
 * It ensures that applying a transformation and then its inverse to a point returns the original point,
 * within a defined floating-point threshold.
 */
public class TestTransformation {

	/** Default threshold for float comparisons in tests. */
	private static final float INITIAL_THRESHOLD = .0001f;
	/** Current threshold for float comparisons, can be modified by {@link TransformatorGenerator#init()}. */
	private float threshold = INITIAL_THRESHOLD;
	
	/** Reusable {@link AffineTransformation} instance for tests. */
	AffineTransformation t = new AffineTransformation();

	/**
	 * Interface for generating different types of affine transformations for testing.
	 */
	interface TransformatorGenerator
	{
		/** Initializes the generator, potentially resetting state or thresholds. */
		void init();
		/** Applies a specific transformation to the given {@link AffineTransformation} object. */
		void transform(AffineTransformation t);
	}

	/** Generates random Z-axis rotations. */
	TransformatorGenerator rotZ = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		}
		@Override
		public void transform(AffineTransformation t) {
			t.rotateZ((float)(Math.random()*Math.PI*2));
		}
	};

	/** Generates random Y-axis rotations. */
	TransformatorGenerator rotY = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		}
		@Override
		public void transform(AffineTransformation t) {
			t.rotateY((float)(Math.random()*Math.PI*2));
		}
	};
	
	/** Generates random X-axis rotations. */
	TransformatorGenerator rotX = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		}
		@Override
		public void transform(AffineTransformation t) {
			t.rotateX((float)(Math.random()*Math.PI*2));
		}
	};

	/** Generates random translations. */
	TransformatorGenerator tr = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		}
		@Override
		public void transform(AffineTransformation t) {
			float x = (float)Math.random()*10;
			float y = (float)Math.random()*10;
			float z = (float)Math.random()*10;
			t.translateTo(x,y,z);
		}
	};

	/** Generates random scaling operations. Ensures scaling factors are non-zero. */
	TransformatorGenerator scale = new TransformatorGenerator() {
		public void init() 
		{
			threshold = INITIAL_THRESHOLD;
		}
		@Override
		public void transform(AffineTransformation t) {
			float x = (float)Math.random()*1.9f + 0.1f; // Avoid zero scale factor
			float y = (float)Math.random()*1.9f + 0.1f; // Avoid zero scale factor
			float z = (float)Math.random()*1.9f + 0.1f; // Avoid zero scale factor
			t.scaleOf(x,y,z);
		}
	};
	
	/** 
	 * Generates a sequence of mixed random transformations (rotations, scaling, translations).
	 * Resets the transformation to identity every 25 operations to prevent excessive error accumulation.
	 * Uses a slightly higher comparison threshold due to potential accumulation of floating point errors.
	 */
	TransformatorGenerator mixed = new TransformatorGenerator() {
		private int counter;

		public void init() 
		{
			threshold = INITIAL_THRESHOLD * 10f; // Increased threshold for mixed operations
			counter = 0;
		}
		
		@Override
		public void transform(AffineTransformation t) {
			if(++counter % 25 == 0) // Reset periodically
			{
				t.loadIdentity();
				// counter = 0; // Not strictly necessary to reset counter here, % handles it
			}
			int choice = (int) (Math.random()*5);
			boolean log = false; // Set to true for debugging transformation sequence
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
	
	/**
	 * Tests the invertibility of Z-axis rotations.
	 * Applies a random Z rotation, transforms a point, then inverse-transforms the result
	 * and checks if it matches the original point.
	 */
	@Test
	public void testRotZ() {
		TransformatorGenerator tg = rotZ;
		tg.init();
		testTransformation(tg);
	}

	/**
	 * Tests the invertibility of Y-axis rotations.
	 */
	@Test
	public void testRotY()
	{
		TransformatorGenerator tg = rotY;
		tg.init();
		testTransformation(tg);
	}
	
	/**
	 * Tests the invertibility of X-axis rotations.
	 */
	@Test
	public void testRotX()
	{
		TransformatorGenerator tg = rotX;
		tg.init();
		testTransformation(tg);
	}
	
	/**
	 * Tests the invertibility of translations.
	 */
	@Test
	public void testTraslation() // Typo: should be testTranslation
	{
		TransformatorGenerator tg = tr;
		tg.init();
		testTransformation(tg);
	}

	/**
	 * Tests the invertibility of scaling operations.
	 */
	@Test
	public void testScaling()
	{
		TransformatorGenerator tg = scale;
		tg.init();
		testTransformation(tg);
	}
	
	/**
	 * Tests the invertibility of a sequence of mixed random transformations.
	 * This is a more rigorous test for the stability and correctness of the inverse operations
	 * when multiple transformations are combined.
	 */
	@Test
	public void testMixed()
	{
		TransformatorGenerator tg = mixed;
		tg.init();
		testTransformation(tg);
	}
	
	/**
	 * Core test logic for verifying transformation invertibility.
	 * It performs 100 runs. In each run:
	 * <ol>
	 *   <li>A random 3D point is generated.</li>
	 *   <li>A transformation is applied to the current {@link AffineTransformation} matrix {@code t}
	 *       using the provided {@code TransformatorGenerator g}.</li>
	 *   <li>The random point is transformed by {@code t}.</li>
	 *   <li>The transformed point is then inverse-transformed by {@code t}.</li>
	 *   <li>Each component of the resulting point is compared against the original point's components,
	 *       asserting they are equal within the current {@link #threshold}.</li>
	 * </ol>
	 * The main transformation matrix {@code t} is reset to identity at the beginning of this method.
	 * 
	 * @param g The {@link TransformatorGenerator} that defines the type of transformation to test.
	 */
	public void testTransformation(TransformatorGenerator g)
	{
		t.loadIdentity(); // Start with a clean identity matrix for each test type
		float[] point = {0,0,0};
		float[] result = {0,0,0}; // Stores transformed point
		float[] inverted_result = {0,0,0}; // Stores point after inverse transform

		for(int run = 1; run <= 100; run++)
		{
			// Generate a random point
			for(int i = 0; i < 3; i++)
				point[i] = (float)Math.random()*10;
			
			// Apply a new transformation to matrix 't'
			g.transform(t); 
			
			// Transform the point
			t.transform(point, result);
			// Inverse transform the result
			t.inverse_transform(result, inverted_result); // Assuming inverse_transform can write to a different array
			
			// Check if the inverse-transformed point matches the original point
			for(int i = 0; i < 3; i ++)
			{
				// System.out.println(String.format("Run %d, Comp %d: Original=%.4f, Inverted=%.4f, Diff=%.4f, Threshold=%.4f", run, i, point[i], inverted_result[i], Math.abs(point[i]-inverted_result[i]), threshold));
				assertEquals("Transformation invertibility failed for component " + i + " in run " + run,
							 point[i], inverted_result[i], threshold);
			}
		}
	}
}
