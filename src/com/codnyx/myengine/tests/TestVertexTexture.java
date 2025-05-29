package com.codnyx.myengine.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.codnyx.myengine.VertexTexture;
import com.codnyx.myengine.Texture; // Superclass
import com.codnyx.myengine.Polygon;
import com.codnyx.myengine.Vertex;
import com.codnyx.myengine.MyMath; // For vector math verification

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Color; // For creating test image colors

public class TestVertexTexture {

    private static final float DELTA = 1e-5f;

    // Helper to create a simple BufferedImage with specified colors
    private BufferedImage createTestImage(int width, int height, int[] pixelData) {
        if (pixelData == null || pixelData.length != width * height) {
            throw new IllegalArgumentException("Pixel data length must match width*height.");
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] imgData = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixelData, 0, imgData, 0, pixelData.length);
        return img;
    }
    
    // Helper to assert float arrays, handling nulls
    private void assertFloatArrayEquals(String message, float[] expected, float[] actual, float delta) {
        if (expected == null && actual == null) return;
        assertNotNull(message + " - expected array was null, actual was not (or vice versa)", expected);
        assertNotNull(message + " - actual array was null, expected was not (or vice versa)", actual);
        assertEquals(message + " (array length)", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(message + " (element " + i + ")", expected[i], actual[i], delta);
        }
    }
    
    private void assertIntArrayEquals(String message, int[] expected, int[] actual) {
        if (expected == null && actual == null) return;
        assertNotNull(message + " - expected array was null, actual was not (or vice versa)", expected);
        assertNotNull(message + " - actual array was null, expected was not (or vice versa)", actual);
        assertArrayEquals(message, expected, actual);
    }


    @Test
    public void testConstructorAndFieldInitialization() {
        // Image: 2x2, power-of-two for mask testing
        // Pixels: RED, GREEN
        //         BLUE, WHITE
        int R = Color.RED.getRGB(); int G = Color.GREEN.getRGB();
        int B = Color.BLUE.getRGB(); int W = Color.WHITE.getRGB();
        BufferedImage testImg = createTestImage(2, 2, new int[]{R, G, B, W});

        // Polygon vertices (simple triangle for defining texture space)
        Vertex pV0 = new Vertex(new float[]{0.0f, 2.0f, 0.0f}); // Corresponds to texV[0]
        Vertex pV1 = new Vertex(new float[]{0.0f, 0.0f, 0.0f}); // Corresponds to texV[1] - Origin O for 3D texture space
        Vertex pV2 = new Vertex(new float[]{2.0f, 0.0f, 0.0f}); // Corresponds to texV[2]
        Polygon poly = new Polygon(new Vertex[]{pV0, pV1, pV2});

        // Texture UV coordinates (normalized) for the polygon vertices
        // texV[0] -> (0,1) top-left in UV space if Y is up
        // texV[1] -> (0,0) bottom-left in UV space (origin for 2D texture space)
        // texV[2] -> (1,0) bottom-right in UV space
        float[][] textureUVs = {
            {0.0f, 1.0f}, // For pV0
            {0.0f, 0.0f}, // For pV1
            {1.0f, 0.0f}  // For pV2
        };

        VertexTexture vt = new VertexTexture(testImg, poly, textureUVs);

        // Check image-related fields
        // private int xmask, ymask, w; protected BufferedImage image, int[] data;
        // Cannot directly access private fields without reflection, but can test their effects via getColor.
        // We can check 'w' if it were public, or infer from xmask. xmask = w - 1.
        // Test 'w' via xmask + 1
        // assertEquals("Image width 'w' check", 2, getPrivateField(vt, "w")); // If accessible
        // assertEquals("xmask check", 1, getPrivateField(vt, "xmask"));
        // assertEquals("ymask check", 1, getPrivateField(vt, "ymask"));
        // assertSame("Image should be set", testImg, getPrivateField(vt, "image"));
        // assertNotNull("Image data array should be set", getPrivateField(vt, "data"));

        // --- Verify O, U, V (3D texture space vectors) ---
        // O = pV1.point = {0,0,0}
        assertFloatArrayEquals("Texture 3D Origin O", new float[]{0.0f, 0.0f, 0.0f}, vt.O, DELTA);

        // Initial U = pV0.point - O = {0,2,0} - {0,0,0} = {0,2,0}
        // Initial V = pV2.point - O = {2,0,0} - {0,0,0} = {2,0,0}
        // Orthogonalization: V_new = V_old - ( (V_old . U_old) / (U_old . U_old) ) * U_old
        // V_old . U_old = (2,0,0) . (0,2,0) = 0. So V_new = V_old = {2,0,0} (already orthogonal)
        // Scaling: U_final = U_old / (U_old . U_old) = {0,2,0} / 4 = {0, 0.5, 0}
        //          V_final = V_new / (V_new . V_new) = {2,0,0} / 4 = {0.5, 0, 0}
        assertFloatArrayEquals("Texture 3D U vector", new float[]{0.0f, 0.5f, 0.0f}, vt.U, DELTA);
        assertFloatArrayEquals("Texture 3D V vector", new float[]{0.5f, 0.0f, 0.0f}, vt.V, DELTA);
        
        // --- Verify o, u, v (2D pixel space vectors) ---
        // o_pix = textureUVs[1] * (img_width, img_height) = (0,0) * (2,2) = {0,0}
        assertIntArrayEquals("Texture 2D origin o", new int[]{0, 0}, vt.o);

        // Initial u_pix = (textureUVs[0] - textureUVs[1]) * (img_dims)
        //               = ((0,1) - (0,0)) * (2,2) = (0,1) * (2,2) = {0,2}
        // Initial v_pix = (textureUVs[2] - textureUVs[1]) * (img_dims)
        //               = ((1,0) - (0,0)) * (2,2) = (1,0) * (2,2) = {2,0}
        // Orthogonalization (2D): v_new_2d = v_old_2d - ( (v_old_2d . u_old_2d) / (u_old_2d . u_old_2d) ) * u_old_2d
        // v_old_2d . u_old_2d = (2,0) . (0,2) = 0. So v_new_2d = v_old_2d = {2,0} (already orthogonal)
        // The 2D u,v are NOT normalized like 3D U,V in the code. They retain their pixel scale.
        assertIntArrayEquals("Texture 2D u vector", new int[]{0, 2}, vt.u);
        assertIntArrayEquals("Texture 2D v vector", new int[]{2, 0}, vt.v);
    }

    @Test
    public void testGetColor() {
        int R = Color.RED.getRGB(); int G = Color.GREEN.getRGB();
        int B = Color.BLUE.getRGB(); int W = Color.WHITE.getRGB();
        BufferedImage img = createTestImage(2, 2, new int[]{R, G, B, W}); // Power-of-two for mask wrapping

        Vertex pV0 = new Vertex(new float[]{0f,1f,0f}); Vertex pV1 = new Vertex(new float[]{0f,0f,0f}); Vertex pV2 = new Vertex(new float[]{1f,0f,0f});
        Polygon poly = new Polygon(new Vertex[]{pV0, pV1, pV2});
        float[][] textureUVs = {{0f,1f}, {0f,0f}, {1f,0f}}; // Standard UV mapping for a quad's triangle
        
        VertexTexture vt = new VertexTexture(img, poly, textureUVs);

        assertEquals("getColor(0,0)", R, vt.getColor(0,0));
        assertEquals("getColor(1,0)", G, vt.getColor(1,0));
        assertEquals("getColor(0,1)", B, vt.getColor(0,1));
        assertEquals("getColor(1,1)", W, vt.getColor(1,1));

        // Test wrapping (masking behavior)
        // xmask = 1 (binary 01), ymask = 1 (binary 01)
        // For x=2 (binary 10): 2 & 1 = 0. For y=2: 2 & 1 = 0. -> getColor(0,0)
        assertEquals("getColor(2,0) wrap", R, vt.getColor(2,0)); // (2&1, 0&1) = (0,0)
        assertEquals("getColor(0,2) wrap", R, vt.getColor(0,2)); // (0&1, 2&1) = (0,0)
        assertEquals("getColor(2,2) wrap", R, vt.getColor(2,2)); // (0,0)
        assertEquals("getColor(3,3) wrap", W, vt.getColor(3,3)); // (3&1, 3&1) = (1,1)
        assertEquals("getColor(-1,0) wrap", G, vt.getColor(-1,0)); // (-1&1, 0&1) = (1,0) (due to Java's two's complement for -1 being all 1s)
        assertEquals("getColor(0,-1) wrap", B, vt.getColor(0,-1)); // (0&1, -1&1) = (0,1)
    }

    @Test
    public void testSetImage() {
        BufferedImage img1 = createTestImage(2, 2, new int[]{Color.RED.getRGB(), Color.GREEN.getRGB(), Color.BLUE.getRGB(), Color.WHITE.getRGB()});
        BufferedImage img2 = createTestImage(4, 4, new int[16]); // Different image, 4x4 all black

        Vertex pV0 = new Vertex(new float[]{0f,1f,0f}); Vertex pV1 = new Vertex(new float[]{0f,0f,0f}); Vertex pV2 = new Vertex(new float[]{1f,0f,0f});
        Polygon poly = new Polygon(new Vertex[]{pV0, pV1, pV2});
        float[][] textureUVs = {{0f,1f}, {0f,0f}, {1f,0f}};
        
        VertexTexture vt = new VertexTexture(img1, poly, textureUVs);
        assertEquals("Initial getColor(0,0)", Color.RED.getRGB(), vt.getColor(0,0));

        // Check that O,U,V and o,u,v are based on img1 and poly
        float[] initialO = vt.O.clone();
        int[] initialo = vt.o.clone();

        vt.setImage(img2);
        // Check if image related fields (w, xmask, ymask, data) are updated
        // This will be reflected in getColor behavior for wrapping
        assertEquals("getColor(0,0) after setImage (should be from img2 - black)", 0, vt.getColor(0,0));
        // xmask for 4x4 is 3. (4&3, 0&3) = (0,0)
        assertEquals("getColor(4,0) wrap after setImage", 0, vt.getColor(4,0)); 

        // Verify that O,U,V and o,u,v are NOT recalculated by setImage
        // The constructor calculates these based on the initial image dimensions for o,u,v.
        // If setImage changes dimensions, o,u,v derived from original dimensions might be inconsistent.
        // This is a characteristic of the current implementation.
        assertFloatArrayEquals("3D Origin O should not change after setImage", initialO, vt.O, DELTA);
        assertIntArrayEquals("2D origin o SHOULD change if createTV was called by setImage implicitly, but it's not", initialo, vt.o);
        // In current VertexTexture, createTV is only called by constructor. setImage updates w, xmask, ymask, data.
        // The fields o,u,v (int[]) are set in constructor using createTV which uses the *initial* image's width/height.
        // If setImage changes image dimensions, the existing o,u,v (pixel space) become potentially mismatched
        // with the new image dimensions for any logic that might use them directly later, though getColor uses xmask/ymask from new image.
        // The U,V,O (float[]) are based on Polygon, not image dimensions, so they should remain unchanged.
    }
    
    @Test
    public void testConstructor_ZeroMagnitudeVectors() {
        // Test case where U or V vectors in 3D or 2D might become zero before normalization/scaling
        BufferedImage img = createTestImage(2, 2, new int[]{0,0,0,0});

        // Case 1: Collinear polygon vertices leading to U_world being zero vector initially
        Vertex pV0_collinear = new Vertex(new float[]{0.0f, 0.0f, 0.0f}); // pV0 = pV1
        Vertex pV1_collinear = new Vertex(new float[]{0.0f, 0.0f, 0.0f});
        Vertex pV2_collinear = new Vertex(new float[]{2.0f, 0.0f, 0.0f});
        Polygon poly_collinear_U = new Polygon(new Vertex[]{pV0_collinear, pV1_collinear, pV2_collinear});
        float[][] textureUVs = {{0f,1f}, {0f,0f}, {1f,0f}};

        VertexTexture vt_collinear_U = new VertexTexture(img, poly_collinear_U, textureUVs);
        // U = pV0 - pV1 = 0. Constructor has: if (MyMath.dotProduct(U,U) == 0) { U[0]=1; }
        // Then U is scaled by 1 / (U.U) = 1/1 = 1. So U becomes {1,0,0}
        assertFloatArrayEquals("3D U vector when pV0=pV1", new float[]{1.0f, 0.0f, 0.0f}, vt_collinear_U.U, DELTA);

        // Case 2: Collinear texture UVs leading to u_pix being zero vector initially
        Vertex pV0 = new Vertex(new float[]{0f,2f,0f});
        Vertex pV1 = new Vertex(new float[]{0f,0f,0f});
        Vertex pV2 = new Vertex(new float[]{2f,0f,0f});
        Polygon poly_ok = new Polygon(new Vertex[]{pV0, pV1, pV2});
        float[][] textureUVs_collinear_u = {
            {0.0f, 0.0f}, // texV0 = texV1
            {0.0f, 0.0f},
            {1.0f, 0.0f}
        };
        VertexTexture vt_collinear_u_pix = new VertexTexture(img, poly_ok, textureUVs_collinear_u);
        // u_pix = (texV0 - texV1) * dims = 0. Constructor has: if (dot_uu_2d == 0) { u[0]=1; }
        assertIntArrayEquals("2D u vector when texUV0=texUV1", new int[]{1, 0}, vt_collinear_u_pix.u);
    }
}
