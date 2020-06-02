# Beryl
A Java Framework for building real time graphics applications. This is my final project of the Software Engineering degree at [ULPGC](https://www.ulpgc.es/).

![Logo](img/beryl_logo.jpg) 

Beryl is a framework purely written in Java to develop high performance graphics applications.
The main purpose of this project is to demonstrate the potential of the Java platform in making this kind of applications.

I have designed Beryl to run 3D desktop games and simulations, focusing on readability and performance, taking advantage of new hardware capabilities.
One of the goals is the support of multiple graphics APIs, especially [Vulkan](https://www.khronos.org/vulkan/).

To achieve this, I worked with modern OpenGL techniques, following the AZDO philosophy ([Approaching Zero Drive Overhead](https://www.slideshare.net/CassEveritt/approaching-zero-driver-overhead)), that is,
reducing the driver's work as much as possible, while putting much more responsibility on the application code. In other words, using OpenGL like Vulkan.

## Features

It supports multiple light sources, cascaded shadow maps, terrain generation, water, 3D sound, dynamic skybox, fog, 3D model loading, and various shading models, supporting Phong and PBR (Physically Based Rendering) Metallic-Roughness for now.
All of these functionalities are going to be continually improved in the future, as well as adding many others.

I have develop some example scenes to test all features out:

### Open World Scenes

You can develop outdoor 3D environments very easily with Beryl, like, for example, a beautiful forest:

![Forest daytime](img/forest_day.png)

<sup>*Forest daytime, 1000 trees, water and shadows*</sup>

Maybe you prefer adventuring into the woods at night with a flashlight:

![Forest at night](img/forest_night.png)

<sup>*Forest at night, player porting a flashlight*</sup>

You can't imagine how creepy becomes a forest with some fog around...

![Forest with dense fog](img/forest_fog.png)

<sup>*Forest with dense fog*</sup>

### Indoor scenes

You can also render interiors, like your dream bedroom:

![Room](img/room.png)

<sup>*Room scene, rendering with PBR*</sup>

### Lighting and texture mapping

Using 2 textures and the sun light, you can simulate the Earth rotation and see the lights on the dark side!

![Sun and Earth 1](img/earth_day.png)

<sup>*Earth, light side*</sup>

![Sun and Earth 2](img/earth_night.png) 

<sup>*Earth, dark side*</sup>

### Physically Based Rendering scenes

Create incredible scenes with physically accurate algorithms and HDR environments to make the scene feel real.

![PBR Materials](img/pbr_materials.png)

<sup>*Different PBR materials, rendered with IBL*</sup>

![PBR Rusted Iron](img/pbr_rusted_iron.png)

<sup>*Rusted iron reflecting night lights*</sup>

![PBR Revolver](img/pbr_revolver.png)

<sup>*PBR Model with metallic workflow: the amazing [Cerberus](https://artisaverb.info/Cerberus.html) model*</sup>

### Performance

Beryl uses bindless textures, frustum culling and multithreading drawing command generation with indirect rendering. This means that multiple
objects are drawn at once, boosting the performance up to 400% faster in some scenes.

![Stress Test](img/50000_cubes.png)

<sup>*Stress Test. 50000 cubes are drawn, all of them rotating each frame, and 1000 cubes are getting
 destroyed and created every 100 ms. I get 105 FPS on GTX 970*</sup>

## Framework architecture

The framework contains individual, single responsibility systems, that can be divided in 3 different levels:

![Beryl Systems](img/BerylSystems.png)

<sup>*Beryl Systems*</sup>

Each system can depend on other systems in the same or lower levels, but never on higher layers. This makes a hierarchical architecture that
correctly defines the order in which these systems have to be initialized and terminated.    