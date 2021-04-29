package BauRobo.GUI;

import BauRobo.models.ply.ObjectDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Lisa Mosis
 */
@SpringBootTest
class GuiApplicationTest {
	GuiApplication controller;
	String stdFilepath = "data/tests/objects/";

	@BeforeEach
	public void initializeQueryParser(){
		this.controller = new GuiApplication();
		controller.changeFilepath("tests");
	}

	@Test
	void contextLoads() {
	}

	/**
	 * Tests the delete and undo method
	 */
	@Test
	public void testDelete() {
		System.out.println("delete Object");
		String id = "2";
		String objPath = stdFilepath + id + ".ply";
		ObjectDTO expected = null;
		try {
			expected = new ObjectDTO(objPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		controller.delete(id);
		File tempFile = new File(objPath);
		boolean exists = tempFile.exists();
		assertFalse(exists);

		System.out.println("undo");
		controller.undo();
		ObjectDTO result = null;
		try {
			result = new ObjectDTO(objPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(expected, result);
	}

	/**
	 * Tests the delete when unsuccessful
	 */
	@Test
	public void testDelete_error() {
		System.out.println("delete missing Object");
		String id = "404";
		boolean success = controller.delete(id);
		assertFalse(success);
	}

	/**
	 * Tests the changeObject and undo method
	 */
	@Test
	public void testChangeObject() {
		System.out.println("change Object");
		String id = "3";
		String objPath = stdFilepath + id + ".ply";
		ObjectDTO expected = null;
		ObjectDTO changed = null;
		try {
			expected = new ObjectDTO(objPath);
			changed = new ObjectDTO(objPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		changed.objClass = "something_Different";
		controller.changeObject(changed);
		ObjectDTO result = null;
		try {
			result = new ObjectDTO(objPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(changed, result);

		System.out.println("undo");
		controller.undo();
		ObjectDTO result2 = null;
		try {
			result2 = new ObjectDTO(objPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(expected, result2);
	}

	/**
	 * Tests the changeObject for missing objects
	 */
	@Test
	public void testChangeObject_error() {
		System.out.println("change Object with error");
		String id = "4";
		String objPath1 = stdFilepath + id + ".ply";
		ObjectDTO changed = null;
		try {
			changed = new ObjectDTO(objPath1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		changed.objClass = "something_Different";
		changed.objID = "404";
		boolean success = controller.changeObject(changed);
		assertFalse(success);
	}

	/**
	 * Tests the newObject and undo method
	 */
	@Test
	public void testNewObject() {
		System.out.println("new Object");
		String id = "4";
		String objPath = stdFilepath + id + ".ply";
		String objPath1 = stdFilepath + "tmp" + ".ply";
		ObjectDTO expected1 = null;
		ObjectDTO expected2 = null;
		ObjectDTO expected3 = null;
		try {
			expected1 = new ObjectDTO(objPath);
			expected2 = new ObjectDTO(objPath);
			expected3 = new ObjectDTO(objPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		expected1.objID = "tmp"; //valid new name
		expected2.objID = ""; //no id given
		expected3.objID = "4"; //already taken id
		controller.newObject(expected1);
		String id2 = controller.newObject(expected2);
		String id3 = controller.newObject(expected3);
		ObjectDTO result1 = null;
		ObjectDTO result2 = null;
		ObjectDTO result3 = null;
		String objPath2 = stdFilepath + id2 + ".ply";
		String objPath3 = stdFilepath + id3 + ".ply";
		try {
			result1 = new ObjectDTO(objPath1);
			result2 = new ObjectDTO(objPath2);
			result3 = new ObjectDTO(objPath3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//valid new id:
		File tempFile1 = new File(objPath1);
		boolean exists1 = tempFile1.exists();
		assertTrue(exists1); //save successful?
		assertEquals(expected1, result1); //save has the right data and format for reading
		//no id given:
		File tempFile2 = new File(objPath2);
		boolean exists2 = tempFile2.exists();
		assertTrue(exists2);
		assertEquals(expected2, result2);
		//already taken id
		File tempFile3 = new File(objPath3);
		boolean exists3 = tempFile3.exists();
		assertTrue(exists3);
		assertEquals(expected3, result3);

		System.out.println("undo");
		controller.undo();
		controller.undo();
		controller.undo();
		exists1 = tempFile1.exists();
		assertFalse(exists1);
		exists2 = tempFile2.exists();
		assertFalse(exists2);
		exists3 = tempFile3.exists();
		assertFalse(exists3);
	}

	/**
	 * Tests the changeFilepath and  undo method
	 */
	@Test
	public void testChangeFilepath() {
		System.out.println("changeFilepath");
		String existingFolder = "tests/objects/testordner";
		String nonexistantFolder = "doesNotExist";

		//for existing folder
		boolean response1 = controller.changeFilepath(existingFolder);
		File file1 = new File("data/" + existingFolder);
		boolean expected1 = file1.isDirectory();
		assertEquals(expected1, response1); //is the response right?
		assertEquals("data/" + existingFolder + "/", controller.getStdFilepath()); //is the path right?

		System.out.println("undo");
		controller.undo();
		assertEquals("data/tests/",controller.getStdFilepath()); //is the path right?

		//for non-existant folder
		System.out.println("changeFilepath_error");
		boolean response2 = controller.changeFilepath(nonexistantFolder);
		File file2 = new File(existingFolder);
		boolean expected2 = file2.isDirectory();
		assertEquals(expected2, response2); //is the response right

		assertEquals("data/tests/",controller.getStdFilepath()); //is the path still right?
	}

	/**
	 * Tests the objectInventory method
	 */
	@Test
	public void testObjectInventory() {
		System.out.println("objectInventory");
		String expected = "Directory objectsFile pointcloud.ply";
		String[] response = controller.objectInventory();
		String result = "";
		for (String s : response) {
			result += s;
		}
		assertEquals(expected, result);
	}

	/**
	 * Tests the allObjects method
	 */
	@Test
	public void testAllObjects() {
		System.out.println("allObjects");
		String expected = controller.objectById("2") + controller.objectById("3") + controller.objectById("4") + controller.objectById("5");
		ObjectDTO[] response = controller.allObjects();
		String result = "";
		for (ObjectDTO objectDTO : response) {
			result += objectDTO.toString();
		}
		assertEquals(expected, result);
	}
	
	/**
	 * Tests the objectsById method
	 */
	@Test
	public void testObjectsById() {
		System.out.println("objectsById");
		String expected = "{\"objID\":\"5\",\"objClass\":\"Wall\",\"color\":\"\",\"sanding\":\"\",\"vertex_indices\":[[0,2,3,1],[2,6,7,3],[6,4,5,7],[4,0,1,5],[1,3,7,5],[0,4,6,2]],\"x\":[-8.219, -8.219, -8.183, -8.183, -8.219, -8.219, -8.183, -8.183],\"y\":[0.498, 3.35, 0.498, 3.35, 0.498, 3.35, 0.498, 3.35],\"z\":[-0.005, -0.005, -0.005, -0.005, 4.515, 4.515, 4.515, 4.515]}";
		String response = controller.objectById("5");
		assertEquals(expected, response);
	}

	/**
	 * Tests the objectsById method with an error
	 */
	@Test
	public void testObjectsById_error() {
		System.out.println("objectsById error");
		String response = controller.objectById("404");
		assertNull(response);
	}

	/**
	 * Tests the getPointcloud method
	 */
	@Test
	public void testGetPointcloud() {
		System.out.println("getPointcloud");
		String expected = "{\"x\":[0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122, 0.00122],\"y\":[1.02563, 1.03735, 1.05347, 1.14282, 1.22241, 1.2644, 1.28198, 1.28735, 1.3064, 1.32104],\"z\":[-1.32056, -1.32153, -1.32056, -1.31274, -1.3186, -1.31763, -1.31714, -1.31665, -1.31714, -1.31274],\"intensity\":[0.30353, 0.31598, 0.29255, 0.29914, 0.29596, 0.26764, 0.27448, 0.29718, 0.27936, 0.27961]}";
		String response = controller.getPointcloud();
		assertEquals(expected, response);
	}

	/**
	 * Tests the getPointcloud method with an error
	 */
	@Test
	public void testGetPointcloud_error() {
		System.out.println("getPointcloud error");
		controller.changeFilepath("tests/objects/testordner");
		String response = controller.getPointcloud();
		assertNull(response);
	}

}
