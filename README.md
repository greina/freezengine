# FreeZ Engine (freezengine)
==============================

**FreeZ Engine** is a lightweight 3D software renderer written in Java from scratch. It serves as a practical implementation of fundamental 3D rendering algorithms, making it suitable for educational purposes, hobbyist projects, or anyone interested in the mechanics of 3D graphics.

The engine focuses on clarity and simplicity while demonstrating core concepts like polygon rasterization, transformations, lighting, and texture mapping.

## Core Features

FreeZ Engine implements several key 3D graphics techniques:

*   **Software Rasterization:** Utilizes a scanline algorithm for rendering polygons.
*   **Z-Buffering:** Implements depth buffering for accurate hidden surface removal.
*   **Transformations:** Supports perspective and affine (model/view) transformations.
*   **Model Loading:** Capable of parsing and rendering Wavefront OBJ (`.obj`) 3D models.
*   **Texture Mapping:** Basic support for applying 2D textures to 3D surfaces.
*   **Gouraud Shading:** Smooth shading by interpolating colors across polygon faces.
*   **Backface Culling:** Optimizes rendering by ignoring polygons facing away from the camera.
*   **Frustum Clipping:** Clips geometry against near and far Z-planes.
*   **Fixed-Point Arithmetic:** Includes options for calculations using `FixedPoint16.java`, potentially offering performance benefits or compatibility with systems having limited floating-point support.
*   **Swing-Based Display:** Uses a simple Java Swing framework for windowing and displaying rendered frames.

## Getting Started / How to Run Demos

This project is written in Java and can be imported into any standard Java IDE (e.g., Eclipse, IntelliJ IDEA).

**1. Import the Project:**
   *   Clone or download the repository.
   *   Import the project as an existing Java project into your IDE.

**2. Run a Demo (Example: `ObjTestLauncher`):**

   There are several test launchers in the `src/com/codnyx/myengine/testlaunchers/` directory. Here's how to run the `ObjTestLauncher` which displays a rotating cow model:

   *   **Using an IDE (Recommended):**
      *   Navigate to the `src/com/codnyx/myengine/testlaunchers/ObjTestLauncher.java` file.
      *   Right-click on the file and select "Run As" > "Java Application" (or your IDE's equivalent).
      *   This will start a Swing window displaying the demo. The cow model (`cow.obj`) is loaded from the `res/` directory (embedded in classpath).

   *   **Using Eclipse Launch Configurations:**
      *   The `runconf/` directory contains pre-configured `.launch` files for Eclipse.
      *   In Eclipse, you can import these launch configurations or right-click on `ObjTestLauncher.launch` and select "Run As" > "ObjTestLauncher".

   *   **What to Expect:**
      *   The `ObjTestLauncher` demo shows a 3D cow model continuously rotating around its Y-axis. You can control the zoom using the UP/DOWN arrow keys or W/S keys.

**3. Explore Other Demos:**
   *   Check out other launchers in the `src/com/codnyx/myengine/testlaunchers/` directory, such as:
      *   `TextureTestLauncher.java`: Demonstrates texture mapping.
      *   `TestZBuffering.java`: Shows Z-buffering in action.
      *   `PolyTestLauncher.java`: A simple polygon rendering test.
   *   The `runconf/` directory also contains corresponding `.launch` files for these.

## Key Components

For those interested in diving into the code, here are some of the key classes and packages:

*   `src/com/codnyx/myengine/`: The core package for the engine.
    *   `PolygonRenderer.java`: The heart of the rendering pipeline, implementing scanline conversion, Z-buffering, shading, and texturing.
    *   `Mesh.java`, `Polygon.java`, `Vertex.java`: Classes representing the 3D geometry (meshes, polygons, and vertices).
    *   `ObjParser.java`: Handles the parsing of Wavefront OBJ model files.
    *   `Texture.java`: Manages texture data and operations.
    *   `AffineTransformation.java`, `PerspectiveTransformation.java`: Implement 3D mathematical transformations.
    *   `DirectionalLight.java`: Provides basic directional lighting.
    *   `FixedPoint16.java`: Implements 16.16 fixed-point arithmetic.
*   `src/com/codnyx/myengine/testlaunchers/`: Contains various executable demo applications (e.g., `ObjTestLauncher`, `TextureTestLauncher`) that showcase different features of the engine.
*   `res/`: Contains resource files, such as `.obj` models and texture images, used by the demos.
*   `runconf/`: Includes Eclipse `.launch` configurations for easily running the demos.

## Visuals

To further showcase the capabilities of FreeZ Engine, consider adding a screenshot or a GIF animation of one of the demos in action (e.g., the `ObjTestLauncher` with the rotating cow, or the `TextureTestLauncher`). Visual examples can greatly enhance the project's presentation!
