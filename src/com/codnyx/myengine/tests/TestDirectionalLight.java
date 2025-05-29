package com.codnyx.myengine.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.Arrays;

import org.junit.Test;

import com.codnyx.myengine.DirectionalLight;

/**
 * JUnit test class for the DirectionalLight class.
 * This class verifies the correct functionality of setting and getting
 * properties of a directional light source, such as its direction and color.
 */
public class TestDirectionalLight {

    /**
     * Tests the default constructor of DirectionalLight.
     * Checks if a new DirectionalLight instance is created with default values:
     * - color: Color.white
     * - position: {0, 0, 0}
     * - diffuse_coeff: 0
     * - ambient_coeff: 0
     * - specular_coeff: 0
     */
    @Test
    public void testDefaultConstructor() {
        DirectionalLight light = new DirectionalLight();
        assertNotNull("DirectionalLight instance should not be null", light);
        assertEquals("Default color should be white", Color.white, light.color);
        assertArrayEquals("Default position should be {0,0,0}", new float[]{0,0,0}, light.position, 0.0f);
        assertEquals("Default diffuse_coeff should be 0", 0, light.diffuse_coeff, 0.0f);
        assertEquals("Default ambient_coeff should be 0", 0, light.ambient_coeff, 0.0f);
        assertEquals("Default specular_coeff should be 0", 0, light.specular_coeff, 0.0f);
    }

    /**
     * Tests setting and getting the light direction.
     * It assigns various valid direction vectors to the light's position field
     * and verifies that the field is updated correctly.
     */
    @Test
    public void testSetGetDirection() {
        DirectionalLight light = new DirectionalLight();
        
        float[] direction1 = {1.0f, 0.0f, 0.0f};
        light.position = direction1;
        assertArrayEquals("Direction should be " + Arrays.toString(direction1), direction1, light.position, 0.0f);

        float[] direction2 = {0.0f, -1.0f, 0.0f};
        light.position = direction2;
        assertArrayEquals("Direction should be " + Arrays.toString(direction2), direction2, light.position, 0.0f);

        float[] direction3 = {0.5f, 0.5f, -0.707f}; // Example normalized vector
        light.position = direction3;
        assertArrayEquals("Direction should be " + Arrays.toString(direction3), direction3, light.position, 0.0001f); // Added delta for float comparison

        float[] direction4 = {0f, 0f, 0f}; // Zero vector
        light.position = direction4;
        assertArrayEquals("Direction should be " + Arrays.toString(direction4), direction4, light.position, 0.0f);
    }

    /**
     * Tests setting and getting the light color.
     * It assigns various Color objects to the light's color field
     * and verifies that the field is updated correctly.
     */
    @Test
    public void testSetGetColor() {
        DirectionalLight light = new DirectionalLight();

        Color color1 = Color.RED;
        light.color = color1;
        assertEquals("Color should be RED", color1, light.color);

        Color color2 = new Color(0, 255, 0); // Green
        light.color = color2;
        assertEquals("Color should be GREEN", color2, light.color);

        Color color3 = new Color(128, 128, 255); // A custom light blue
        light.color = color3;
        assertEquals("Color should be custom light blue", color3, light.color);
    }
    
    /**
     * Tests setting and getting the light intensity coefficients.
     * It assigns various float values to the ambient, diffuse, and specular
     * coefficient fields and verifies they are updated correctly.
     */
    @Test
    public void testSetGetIntensityCoefficients() {
        DirectionalLight light = new DirectionalLight();

        // Test ambient coefficient
        float ambient1 = 0.25f;
        light.ambient_coeff = ambient1;
        assertEquals("Ambient coefficient should be " + ambient1, ambient1, light.ambient_coeff, 0.001f);

        float ambient2 = 1.0f;
        light.ambient_coeff = ambient2;
        assertEquals("Ambient coefficient should be " + ambient2, ambient2, light.ambient_coeff, 0.001f);

        // Test diffuse coefficient
        float diffuse1 = 0.7f;
        light.diffuse_coeff = diffuse1;
        assertEquals("Diffuse coefficient should be " + diffuse1, diffuse1, light.diffuse_coeff, 0.001f);
        
        float diffuse2 = 0.0f;
        light.diffuse_coeff = diffuse2;
        assertEquals("Diffuse coefficient should be " + diffuse2, diffuse2, light.diffuse_coeff, 0.001f);

        // Test specular coefficient
        float specular1 = 0.5f;
        light.specular_coeff = specular1;
        assertEquals("Specular coefficient should be " + specular1, specular1, light.specular_coeff, 0.001f);

        float specular2 = 1.0f; // Max typical value
        light.specular_coeff = specular2;
        assertEquals("Specular coefficient should be " + specular2, specular2, light.specular_coeff, 0.001f);
        
        // Test all coefficients together
        light.ambient_coeff = 0.1f;
        light.diffuse_coeff = 0.6f;
        light.specular_coeff = 0.9f;
        assertEquals("Ambient coefficient should be 0.1", 0.1f, light.ambient_coeff, 0.001f);
        assertEquals("Diffuse coefficient should be 0.6", 0.6f, light.diffuse_coeff, 0.001f);
        assertEquals("Specular coefficient should be 0.9", 0.9f, light.specular_coeff, 0.001f);
    }
}
