package com.codnyx.myengine;

import java.awt.Color;

/**
 * Represents a directional light source in the 3D scene.
 * Directional lights are assumed to be infinitely far away, so all light rays are parallel.
 * The light has a color and intensity coefficients for ambient, diffuse, and specular components.
 */
public class DirectionalLight 
{
	/**
	 * The color of the light. Defaults to white.
	 */
	public Color color = Color.white;
	/**
	 * The direction of the light. Note that this is often represented as a vector
	 * pointing from the scene towards the light source.
	 * The values are typically normalized.
	 */
	public float[] position = {0,0,0}; // TODO: Rename to 'direction' for clarity if it represents a direction vector
	/**
	 * The coefficient for the diffuse component of this light.
	 * Determines how much this light contributes to the diffuse reflection of materials.
	 * Value should typically be between 0 and 1.
	 */
	public float diffuse_coeff = 0;
	/**
	 * The coefficient for the ambient component of this light.
	 * Determines how much this light contributes to the overall ambient illumination of the scene.
	 * Value should typically be between 0 and 1.
	 */
	public float ambient_coeff = 0;
	/**
	 * The coefficient for the specular component of this light.
	 * Determines how much this light contributes to the specular highlights on materials.
	 * Value should typically be between 0 and 1.
	 */
	public float specular_coeff = 0;
	

}
