# JGameEngine

is a lightweight 3D game engine written in Java.

## TODO
* [x] No dependencies
* [x] Java 1.5+ compatibility
* [x] Fixed point math only
* [ ] Multithreading
* [x] Graphics engine
* [ ] Physics engine
* [ ] Audio engine
* [ ] Networking engine
* [x] Input engine

Graphics engine
* [x] Scanline triangle rasterization (linear interpolation, no perspective needed as triangles can't be too big anyway because of clipping)
* [x] Shaders (flat, gouraud and phong shaders)
* [x] Multithreaded shaders
* [x] Point, directional and spot lights
* [x] Directional and spot light shadow mapping
* [x] Skeletal animation (experimental)
* [ ] Skybox
* [x] Custom Wavefront OBJ `.obj` importer
* [ ] Animation importer

Input engine
* [x] Keyboard
* [x] Mouse
* [ ] Gamepad
* [ ] Touch

## Screenshots
Flat shading

![Screenshot](Images/Example1.PNG "Example1")

Gouraud shading

![Screenshot](Images/Example2.PNG "Example2")

Phong shading

![Screenshot](Images/Example3.PNG "Example3")