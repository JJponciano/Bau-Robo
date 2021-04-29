package BauRobo.models.ply;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Lisa Mosis
 */
public class PointcloudDTOTest {

    /**
     * Tests the toString method of ObjectDTO.
     */
    @Test
    public void testToString() {
        try {
            System.out.println("toString");
            PointcloudDTO instance = new PointcloudDTO("src/test/resources/ply/pointcloud.ply");
            String expResult = "{\"x\":[0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122],\"y\":[1.02563, 1.03735, 1.05347, 1.14282, 1.22241, 1.2644, 1.28198, 1.28735, 1.3064, 1.32104],\"z\":[-1.32056, -1.32153, -1.32056, -1.31274, -1.3186, -1.31763, -1.31714, -1.31665, -1.31714, -1.31274],\"intensity\":[0.30353, 0.31598, 0.29255, 0.29914, 0.29596, 0.26764, 0.27448, 0.29718, 0.27936, 0.27961]}";
            String result = instance.toString();
            assertEquals(expResult, result);

        } catch ( IOException | PlyException ex) {
            Logger.getLogger(ObjectDTOTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

    }
}
