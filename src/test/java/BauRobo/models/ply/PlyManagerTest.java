/* Copyright (C) Jean-Jacques Ponciano <jean-jacques@ponciano.info>, 2015-2020, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package BauRobo.models.ply;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 */
public class PlyManagerTest {

    public PlyManagerTest() {
    }

    /**
     * Test of asJSON method, of class PlyManager.
     */
    @Test
    public void testAsJSON() {
        try {
            System.out.println("asJSON");
            //test with PLY path as parameter
            //L:convert .ply from filepath to JSON
            PlyManager instance = new PlyManager("src/test/resources/ply/door_lite.ply");
            JSONObject expResult = new JSONObject("{\"vertex_indices\":[[0,2,3,1],[2,6,7,3],[6,4,5,7],[4,0,1,5],[1,3,7,5],[0,4,6,2]],\"x\":[0.14721443,0.14721443,0.2149918,0.2149918,0.9563941,0.9563941,1.0241715,1.0241715],\"y\":[-2.3072064,-2.3072064,-2.381095,-2.381095,-1.5649514,-1.5649514,-1.63884,-1.63884],\"z\":[-1.31274,0.87622,-1.31274,0.87622,-1.31274,0.87622,-1.31274,0.87622]}");
            JSONObject result = instance.asJSON();
            assertEquals(expResult.toString(), result.toString());

            //test with JSON as parameter
            //L:plyManager instance from JSONObject
            instance= new PlyManager(expResult);
            result = instance.asJSON();
            assertEquals(expResult.toString(), result.toString());
        } catch (JSONException | IOException | PlyException ex) {
            Logger.getLogger(PlyManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

    }

    /**
     * Test of toString method, of class PlyManager.
     */
    @Test
    public void testToString() {
        try {
            System.out.println("toString");
            //L:PlyManager toString entspricht dem Inhalt von .ply file
            PlyManager instance = new PlyManager("src/test/resources/ply/door_lite.ply");
            instance.setComments("id=0 class=door");
            String expResult = "ply\n"
                    + "format ascii 1.0\n"
                    + "comment made by JavaPointCloud library\n"
                    + "comment id=0 class=door\n"
                    + "element vertex 8\n"
                    + "property float x\n"
                    + "property float y\n"
                    + "property float z\n"
                    + "element face 6\n"
                    + "property list uchar int vertex_index\n"
                    + "end_header\n"
                    + "0.14721443 -2.3072064 -1.31274\n"
                    + "0.14721443 -2.3072064 0.87622\n"
                    + "0.2149918 -2.381095 -1.31274\n"
                    + "0.2149918 -2.381095 0.87622\n"
                    + "0.9563941 -1.5649514 -1.31274\n"
                    + "0.9563941 -1.5649514 0.87622\n"
                    + "1.0241715 -1.63884 -1.31274\n"
                    + "1.0241715 -1.63884 0.87622\n"
                    + "4 0 2 3 1\n"
                    + "4 2 6 7 3\n"
                    + "4 6 4 5 7\n"
                    + "4 4 0 1 5\n"
                    + "4 1 3 7 5\n"
                    + "4 0 4 6 2\n";
            String result = instance.toString();
            assertEquals(expResult, result);
        } catch (IOException | PlyException ex) {
            Logger.getLogger(PlyManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of save method, of class PlyManager.
     */
    @Test
    public void testSave() throws Exception {
        try {
            System.out.println("save");
            String path = "test.ply";
            PlyManager instance = new PlyManager("src/test/resources/ply/door_lite.ply");
            instance.save(path); 	//L: saves a PlyManager as .ply file under the given path and filename
            PlyManager result = new PlyManager("src/test/resources/ply/door_lite.ply");
            assertEquals(instance, result);
        } catch (IOException | PlyException ex) {
            Logger.getLogger(PlyManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

}
