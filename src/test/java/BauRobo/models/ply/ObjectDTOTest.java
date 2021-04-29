
package BauRobo.models.ply;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Lisa Mosis
 */
public class ObjectDTOTest {

    public ObjectDTOTest() {
    }

    /**
     * Tests the toString method of ObjectDTO.
     */
    @Test
    public void testToString() {
        try {
            System.out.println("toString send");
            ObjectDTO instance = new ObjectDTO("src/test/resources/ply/2.ply");
            String expResult = "{\"objID\":\"2\",\"objClass\":\"Wall\",\"color\":\"\",\"sanding\":\"\",\"vertex_indices\":[[0,2,3,1],[2,6,7,3],[6,4,5,7],[4,0,1,5],[1,3,7,5],[0,4,6,2]],\"x\":[-8.219, -8.219, -8.183, -8.183, -8.219, -8.219, -8.183, -8.183],\"y\":[0.498, 3.35, 0.498, 3.35, 0.498, 3.35, 0.498, 3.35],\"z\":[-0.005, -0.005, -0.005, -0.005, 4.515, 4.515, 4.515, 4.515]}";
            assertEquals(expResult, instance.toString());

        } catch ( IOException | PlyException ex) {
            Logger.getLogger(ObjectDTOTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

    }

    /**
     * Tests toPly method of ObjectDTO.
     */
    @Test
    public void testToPly() {
        try {
            System.out.println("toPly");
            ObjectDTO instance = new ObjectDTO("src/test/resources/ply/2.ply");
            instance.setComments("id=2 class=Wall");
            String expResult = "ply\n"
                    + "format ascii 1.0\n"
                    + "comment made by JavaPointCloud library\n"
                    + "comment id=2 class=Wall\n"
                    + "element vertex 8\n"
                    + "property double x\n"
                    + "property double y\n"
                    + "property double z\n"
                    + "element face 6\n"
                    + "property list uchar int vertex_index\n"
                    + "end_header\n"
                    + "-8.219 0.498 -0.005\n"
                    + "-8.219 3.35 -0.005\n"
                    + "-8.183 0.498 -0.005\n"
                    + "-8.183 3.35 -0.005\n"
                    + "-8.219 0.498 4.515\n"
                    + "-8.219 3.35 4.515\n"
                    + "-8.183 0.498 4.515\n"
                    + "-8.183 3.35 4.515\n"
                    + "4 0 2 3 1\n"
                    + "4 2 6 7 3\n"
                    + "4 6 4 5 7\n"
                    + "4 4 0 1 5\n"
                    + "4 1 3 7 5\n"
                    + "4 0 4 6 2\n";
            String result = instance.toPly();

            System.out.println("expected:" + expResult);
            System.out.println("result:  " + result);
            assertEquals(expResult, result);
        } catch (IOException | PlyException ex) {
            Logger.getLogger(ObjectDTOTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Tests constructor(path), save and delete methods of ObjectDTO.
     */
    @Test
    public void testSave() {
        try {
        	//test saving a new object
            System.out.println("newObject");
            String path = "src/test/resources/ply/new.ply";
            ObjectDTO instance = new ObjectDTO("src/test/resources/ply/2.ply");
            instance.objID = "new";
            System.out.println("save");
            instance.save(path);
            ObjectDTO result = new ObjectDTO("src/test/resources/ply/new.ply");
            assertEquals(instance, result);
            
            //test deleting the new object
            System.out.println("delete");
            result.deleteObject("src/test/resources/ply/");
            File tempFile = new File("src/test/resources/ply/new.ply");
            boolean exists = tempFile.exists();
            boolean expected = false;
            assertEquals(expected, exists);
            
        } catch (IOException | PlyException ex) {
            Logger.getLogger(ObjectDTOTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Tests all the constructors of ObjectDTO.
     */
    @Test
    public void testConstructors() {
        try {
            //test saving a new object
            System.out.println("constructors");
            String path = "src/test/resources/ply/2.ply";
            ObjectDTO instance1 = new ObjectDTO(path);

            float[] x = {-8.219f,-8.219f, -8.183f, -8.183f, -8.219f, -8.219f, -8.183f, -8.183f};
            float[] y = {0.498f, 3.35f, 0.498f, 3.35f, 0.498f, 3.35f, 0.498f, 3.35f};
            float[] z = {-0.005f, -0.005f, -0.005f, -0.005f, 4.515f, 4.515f, 4.515f, 4.515f};
            int[][] vertex_indices = {{0,2,3,1},{2,6,7,3},{6,4,5,7},{4,0,1,5},{1,3,7,5},{0,4,6,2}};
            ObjectDTO instance2 = new ObjectDTO(x,y,z,vertex_indices,"2","Wall","","");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("objID", "2");
            jsonObject.put("objClass", "Wall");
            jsonObject.put("color", "");
            jsonObject.put("sanding", "");
            JSONArray xArray = new JSONArray(x);
            JSONArray yArray = new JSONArray(y);
            JSONArray zArray = new JSONArray(z);
            JSONArray faces = new JSONArray();
            for (int[] face : vertex_indices) {
                JSONArray facesArray = new JSONArray();
                for (int i : face) {
                    facesArray.put(i);
                }
                faces.put(facesArray);
            }
            jsonObject.put("vertex_indices", faces);
            jsonObject.put("x", xArray);
            jsonObject.put("y", yArray);
            jsonObject.put("z", zArray);
            ObjectDTO instance3 = new ObjectDTO(jsonObject);

            assertEquals(instance1, instance2); //expected, actual
            assertEquals(instance1, instance3);

        } catch (Exception e) {
            Logger.getLogger(ObjectDTOTest.class.getName()).log(Level.SEVERE, null, e);
            fail(e.getMessage());
        }
    }
}
