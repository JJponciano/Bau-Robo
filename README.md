# Bau-Robo

## Scanner

Relevant for small non planar object:
- Leica RTC 360

Relevant for planar object (chauir, table, windows, dors, furniture):
- ipad


Only for Big planar object (wall, celling, ground)
- L515 ss
- Timms

Not relevant:
- Go pro  with Meshroom
- Faro
- iphone


## Algorithms

Simplification:
- Octree
- Kdtree


Segmentation Algorithm:
- Normal estimation
- Color group prediction
- Normal filtering
- Color filtering
- Normal Region growing
- Color Region growing
- Kmean
- RANSAC

Feature extraction:

- GetHeight : characterizes the height of a segment.
- GetWidth : characterizes the width of a segment.
- GetLength : characterizes the length of a segment.
- GetVolume : characterizes the volume of a segment.
- GetArea : characterizes the area of a segment.
- GetLocation : characterizes the location of a segment.
- GetPointCount : characterizes the point number of a point cloud. GetMeanColor : characterizes the mean color of a segment. 
- GetMeanNormal : characterizes the mean normal of a segment. 
- GetResolution : characterizes the resolution of a point cloud. GetDistance : characterizes the euclidean distance between two segments.

## Object 

Geometric categories:
- regular
    - Planar
    - Cylindrical
- irregular

List of objects to recognize in the point clouds:
-  Floor
-  Beam
-  Wooden beam
-  Board
-  Bookcase
-  Ceiling
-  Chair
-  Column
-  Door
-  Rooom
-  Sofa
-  Stairs
-  Table
-  Wall
    - Plywood wall.
    - Concrete wall.
    - Plasterboard wall.
-  Window
-  Noise
    -  Cuts in the walls
    -  Junction between two plates  
- Window frame
- Door frame
- Cable
- Power socket
- Light fixture
- WDoor handle
- Window handle
- Radiator
- Bathroom furniture
  - Washbasin
  - Toilet
  - Shower
- Switch 
- Junction box
- Water pipes
- Floor tile
- Stairs
- Stair railing
- Coat rack
- Fireplace
- Exhaust duct
- VMC
- Air conditioner
- Heat pump outlet.
- Valve
