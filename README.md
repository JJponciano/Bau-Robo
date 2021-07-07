# Bau-Robo

## 3D data acquisition (Scanner 3D)

Relevant as stand alone Device:
- Terrestrial laser scanner
    - Leica RTC 360 - 40000€ (Ideal device)
    - Leica BLK 360 - 16000€ (cost-efficient device)
    - other potenzial terrestrial laserscanner?
        -  Timms ( large amounts of rooms)

Maybe relevant on combination with a robot:
- Time of flight / Lidar camera (Tof/lidar)
    -  L515 ss
    -  other Tof

Not relevant:
- Go pro  with Meshroom
- iphone
- ipad


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
- GetMeanNormal : characterizes the mean normal of a segment.
- GetPointCount : characterizes the point number of a point cloud.
- GetMeanColor : characterizes the mean color of a segment.
- GetResolution : characterizes the resolution of a point cloud.
- GetDistance : characterizes the euclidean distance between two segments.
- GetAltitude : characterizes the altitude of a point cloud.
- GetDistance : characterizes the euclidean distance between two segments.
- isParallele: characterizes the parallelisms of two segments
- isBetween: characterizes a segments for which its position is between two other segments.
- isAligned: characterize a segments align with other segments.
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

## Segmentation strategy

Relevant DL approaches:
- Ni, H.; Lin, X.; Zhang, J.; Ni, H.; Lin, X.; Zhang, J. Classification of ALS Point Cloud with Improved Point Cloud Segmentation and Random Forests. Remote Sens. 2017, 9, 288.

- Ghorpade, V.K.; Checchin, P.; Malaterre, L.; Trassoudaine, L. 3D shape representation with spatial probabilistic distribution of intrinsic shape keypoints. EURASIP J. Adv. Signal Process.
2017, 2017, 52.

- Bueno, M.; Martínez-Śanchez, J.; Gonźalez-Jorge, H.; Lorenzo, H. Detection of geometric
keypoints and its application to point cloud coarse registration. In Proceedings of the International Archives of the Photogrammetry, Remote Sensing and Spatial Information Sciences - ISPRS Archives; ISPRS: Prague, Czech Republic, 2016; Vol. 41, pp. 187–194.

- Vetrivel, A.; Gerke, M.; Kerle, N.; Nex, F.; Vosselman, G. Disaster damage detection through synergistic use of deep learning and 3D point cloud features derived from very high resolution oblique aerial images, and multiple-kernel-learning. ISPRS J. Photogramm. Remote Sens. 2018, 140, 45–59.

- Shen, Y.; Feng, C.; Yang, Y.; Tian, D. Mining Point Cloud Local Structures by Kernel Correlation and Graph Pooling. In Proceedings of the Conference on Computer Vision and Pattern Recognition (CVPR); Salt Lake City, United States, 2018; pp. 4548–4557.

- Qi, C.R.; Su, H.; Mo, K.; Guibas, L.J. PointNet: Deep learning on point sets for 3D classification and segmentation. In Proceedings of the Conference on Computer Vision and Pattern Recognition (CVPR); Honolulu, Hawaii, United States, 2017; pp. 77–85.

- Nurunnabi, A.; Belton, D.; West, G. Robust Segmentation for Large Volumes of Laser Scanning Three-Dimensional Point Cloud Data. IEEE Trans. Geosci. Remote Sens. 2016, 54, 4790–4805.

- Lawin, F.J.; Danelljan, M.; Tosteberg, P.; Bhat, G.; Khan, F.S.; Felsberg, M. Deep projective 3D semantic segmentation. In Proceedings of the Computer Analysis of Images and Patterns (CAIP); Ystad, Sweden, 2017; pp. 95–107.

- Shen, Y.; Feng, C.; Yang, Y.; Tian, D. Mining Point Cloud Local Structures by Kernel Correlation and Graph Pooling. In Proceedings of the Conference on Computer Vision and Pattern Recognition (CVPR); Salt Lake City, United States, 2018; pp. 4548–4557.

- Qi, C.R.; Su, H.; Mo, K.; Guibas, L.J. PointNet: Deep learning on point sets for 3D classification and segmentation. In Proceedings of the Conference on Computer Vision and Pattern Recognition (CVPR); Honolulu, Hawaii, United States, 2017; pp. 77–85.

- Nurunnabi, A.; Belton, D.; West, G. Robust Segmentation for Large Volumes of Laser Scanning Three-Dimensional Point Cloud Data. IEEE Trans. Geosci. Remote Sens. 2016, 54, 4790–4805.

- Lawin, F.J.; Danelljan, M.; Tosteberg, P.; Bhat, G.; Khan, F.S.; Felsberg, M. Deep projective 3D semantic segmentation. In Proceedings of the Computer Analysis of Images and Patterns (CAIP); Ystad, Sweden, 2017; pp. 95–107.

### Most accurrate methods for planar object: 

Voxel-Based 3D Point Cloud Semantic Segmentation: Unsupervised Geometric and Relationship Featuring

* Poux, Florent & Billen, Roland. (2019). Voxel-Based 3D Point Cloud Semantic Segmentation: Unsupervised Geometric and Relationship Featuring vs Deep Learning Methods. ISPRS International Journal of Geo-Information. 8. 213. 10.3390/ijgi8050213. 
