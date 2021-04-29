package BauRobo.GUI;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import BauRobo.models.ply.*;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class GuiApplication {
	//TODO: pom file anpassen auf imports
	private String stdFilepath = "data/";
	final private String objPath = "/objects/";
	final private int maxSending = 1000;//TODO: find good value for max files sent (get allObjects)
	final MyLogManager log = new MyLogManager(50);//maxFilesize log-file = 100 lines

	public String getStdFilepath() {
		return stdFilepath;
	}

	public static void main(String[] args) {
		SpringApplication.run(GuiApplication.class, args);
	}

	/**
	 * Deletes an object ply-file.
	 * @param objectId Id of the object that should me deleted
	 * @return true if successful
	 */
	@DeleteMapping("/delete")
	public boolean delete(@RequestBody String objectId) {
		String path = stdFilepath + objPath + objectId + ".ply";
		ObjectDTO obj;
		boolean success = false;
		try {
			obj = new ObjectDTO(path);
			log.writeLog("f delete(" + obj.objID + ") undo_delete(" + obj.toString() + ")");
			success = obj.deleteObject(stdFilepath + objPath);
		} catch (Exception e) {
			String error = "there was an error while deleting an Object";
			log.writeLog("e "+ error);
			System.out.println(error);
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * undo a /delete.
	 * @param object JSON object from the ObjectDTO class
	 * @return true if successful
	 */
	public boolean undo_delete(ObjectDTO object) { //isn't used directly but invoked in update()
		String obj = newObject(object);
		return !obj.isEmpty();//true if it worked
	}

	/**
	 * changes an existing ply-object.
	 * @param object JSON object from the ObjectDTO class
	 * @return true if successful
	 */
	@PutMapping("/changeObject") //TODO: for optimisation: only send changed parts
	public boolean changeObject(@RequestBody ObjectDTO object) {
		boolean success = true;
		String path;
		if(object!=null)
			path = stdFilepath + objPath + object.objID + ".ply";
		else {
			return false;
		}
		boolean alreadyExists = Files.exists(Path.of(path));
		if(alreadyExists) {
			try {
				ObjectDTO newObj = new ObjectDTO(path);
				String oldObject = newObj.toString();
				if(object != null && !newObj.equals(object)) {
					if(object.getX() != null)
						newObj.setX(object.getX());
					if(object.getY() != null)
						newObj.setY(object.getY());
					if(object.getZ() != null)
						newObj.setZ(object.getZ());
					if(object.getVertex_indices() != null)
						newObj.setVertex_indices(object.getVertex_indices());
					if(object.objClass != null)
						newObj.objClass = object.objClass;
					if(object.color != null)
						newObj.color = object.color;
					if(object.sanding != null)
						newObj.sanding = object.sanding;
					newObj.save(path);
				}
				log.writeLog("f changeObject(" + object.toString() + ") undo_changeObject(" + oldObject + ")");
			}
			catch(Exception e) {
				String error = "Error while messing with file.";
				log.writeLog("e "+ error);
				System.out.println(error);
				e.printStackTrace();
				success = false;
			}
		}
		else {
			String error = "The changed object doesn't yet exist and thus can't be changed.";
			log.writeLog("e "+ error);
			System.out.println(error);
			success = false;
		}
		return success;//wenn erfolgreich = true
	}

	/**
	 * undo a /changeObject.
	 * @param object JSON object from the ObjectDTO class
	 * @return true if successful
	 */
	public boolean undo_changeObject(ObjectDTO object) { //isn't used directly but invoked in update()
		return changeObject(object);
	}

	/**
	 * creates a new object.
	 * @param object JSON object from the ObjectDTO class
	 * @return new object id (=filename) if successful
	 */
	@PostMapping("/newObject")
	public String newObject(@RequestBody ObjectDTO object) {
		String retId;
        boolean alreadyTaken = Files.exists(Path.of(stdFilepath + objPath + object.objID + ".ply"));

        //generate id
		if(object.objID == null || alreadyTaken || object.objID.equals("")) {
			alreadyTaken = true;
			int i=1;
			for(; alreadyTaken; i++) {

				alreadyTaken = Files.exists(Path.of(stdFilepath + objPath + i + ".ply"));
			}
			object.objID = Integer.toString(i-1);
		}

		//try to save the object
		try {
			object.save(stdFilepath + objPath + object.objID + ".ply");
			retId = object.objID;
			log.writeLog("f newObject(" + object.toString() + ") undo_newObject(" + retId + ")");
		}catch(Exception e) {
			retId = "";
			String error = "Error while saving the new Object.";
			log.writeLog("e "+ error);
			System.out.println(error);
			e.printStackTrace();
		}

		return retId;//wenn fehler = ""
	}

	/**
	 * undo a /newObject.
	 * @param objID ID of the object that was created,
	 * ID should be the same as the name of the .ply-file
	 * @return true if successful
	 */
	public boolean undo_newObject(String objID) {//isn't used directly but invoked in update()
		return delete(objID);
	}

	/**
	 * changes the standard file path the ply-files are saved and loaded from.
	 * @param folder path data/<folder>/ which has an object folder and a pointcloud.ply, "" or " " for the default "data/"
	 * @return true if successful
	 */
	@PutMapping("/changeFilepath")
	public boolean changeFilepath(@RequestBody String folder) {
		boolean success = true;
		String oldPath = "\"\"";
		if(!stdFilepath.equals("data/"))
			oldPath = stdFilepath.substring(5, stdFilepath.length() - 1);
		stdFilepath = "data/" + folder + "/";
		File file = new File(stdFilepath);
		if(folder.equals(" ") || folder.equals("") || folder.equals("\"\"")) {
			stdFilepath = "data/";
			file = new File(stdFilepath);
		}
		else if (!file.isDirectory()) {//basically = Exception
			String error = "The proposed filepath is not valid.";
			log.writeLog("e "+ error);
			System.out.println(error);
			success = false;
			stdFilepath = "data/" + oldPath + "/";
		}
		if (file.exists())
			success = true;
		if(success)
			log.writeLog("f changeFilepath(" + folder + ") undo_changeFilepath(" + oldPath + ")");
		return success;
	}

	/**
	 * undo a /changeFilepath.
	 * @param folder path data/<folder>/ which has an object folder and a pointcloud.ply, "" or " " for the default "data/"
	 * @return true if successful
	 */
	public boolean undo_changeFilepath(String folder){//isn't used directly but invoked in update()
		return changeFilepath(folder);
	}

	/**
	 * get the inventory of your current filepath.
	 * @return an array of strings with every file and directory ("File <name>" or "Directory <name>") or null if empty
	 */
	@GetMapping("/objectInventory")
	public String[] objectInventory() {
		File folder = new File(stdFilepath);
		File[] listOfFiles = folder.listFiles();
		String[] names;
		if (listOfFiles != null) {
			names = new String[listOfFiles.length];
			for (int i = 0; i < listOfFiles.length && i < maxSending*10; i++) {
			  if (listOfFiles[i].isFile()) {
				  names[i] = "File " + listOfFiles[i].getName();
			  } else if (listOfFiles[i].isDirectory()) {
				  names[i] = "Directory " + listOfFiles[i].getName();
			  }
			}
		}
		else{
			return null;
		}
		return names;
	}

	/**
	 * get a json with an array of all ObjectDTO objects in the current filepath(+objects/).
	 * @return an array of ObjectDTO in json format or null if empty
	 */
	@GetMapping("/allObjects")
	public ObjectDTO[] allObjects() {
		File folder = new File(stdFilepath + objPath);
		File[] listOfFiles = folder.listFiles();
		ObjectDTO[] objectsTmp;
		if (listOfFiles != null) {
			objectsTmp = new ObjectDTO[listOfFiles.length];
		}
		else{
			return null;
		}

		int j = 0;
		for (int i = 0; i < listOfFiles.length && i < maxSending; i++) {
			try {
				if (listOfFiles[i].isFile()) {
					ObjectDTO tmpObj = new ObjectDTO(stdFilepath + objPath + listOfFiles[i].getName());
					objectsTmp[j] =  tmpObj;
				} else if (listOfFiles[i].isDirectory()) {
					j -= 1;
				}
			}
			catch(Exception e) {
				j -= 1;
				String error = "Error while reading file " + stdFilepath + objPath + listOfFiles[i].getName();
				log.writeLog("e "+ error);
				System.out.println(error);
				e.printStackTrace();
			}
			j += 1;
		}

		ObjectDTO[] objects = new ObjectDTO[j];
		for (int i = 0; i < j; i++) {
			objects[i] = objectsTmp[i];
		}

		return objects;
	}

	/**
	 * get a single ObjectDTO in json format.
	 * @param id the id = filename of the Object
	 * @return a json of an ObjectDTO
	 */
	@GetMapping("/objectById/{id}")
	public String objectById(@PathVariable String id) {
		String path = stdFilepath + objPath + id + ".ply";
		ObjectDTO ply;
		try {
			ply = new ObjectDTO(path);
		} catch (Exception e) {
			String error = "Error while trying get object " + id + ".ply.";
			log.writeLog("e "+ error);
			e.printStackTrace();
			return null;
		}
		return ply.toString();
	}
	
	//----------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------
	//TODO 1. allow sending normal point cloud.
	/**
	 * get the pointcloud of the current filepath (file named pointcloud.ply).
	 * @return a json of an ObjectDTO
	 */
	@GetMapping("/getPointcloud")
	public String getPointcloud() {
		String path = stdFilepath + "pointcloud.ply";
		PointcloudDTO ply;
		try {
			ply = new PointcloudDTO(path);
		} catch (Exception e) {
			String error = "Error while trying to get the pointcloud.";
			log.writeLog("e "+ error);
			e.printStackTrace();
			return null;
		}
		return ply.toString();
	}
	//----------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------

	/**
	 * undoes the last step that is listed in the log-file in the root-folder.
	 * can only undo changes on the server.
	 * @return a String of the undo method or null if unsuccessful
	 */
	@PostMapping("/undo")
	public String undo(){
		String lastLog = log.readLastLog(); //"id";//objID or true/false
		lastLog = lastLog.replace(", ", ",");
		String undoMethod;
		String logType = lastLog.split(" ")[1];
		if(logType.equals("f")){
			undoMethod = lastLog.split(" ")[3];
			try {
				String method_str = undoMethod.split("\\(")[0];
				String variables_str = undoMethod.split("\\(")[1];
				variables_str = variables_str.split("\\)")[0];
				boolean returnValue;

				if(method_str.equals("undo_delete")||method_str.equals("undo_changeObject")) {
					JSONObject variables_json = new JSONObject(variables_str);
					ObjectDTO variables = new ObjectDTO(variables_json);

					Method method = GuiApplication.class.getMethod(method_str, ObjectDTO.class);
					returnValue = (Boolean) method.invoke(this, variables);
				}
				else if(method_str.equals("undo_newObject") || method_str.equals("undo_changeFilepath")) {
					Method method = GuiApplication.class.getMethod(method_str, String.class);
					returnValue = (Boolean) method.invoke(this, variables_str);
				}
				else{
					returnValue = false;
				}
				if(! returnValue) {
					System.out.println("Error while undoing.");
					undoMethod = null;
				}
			}
			catch(Exception e){
				String error = "Error while trying to undo the last step.";
				log.writeLog("e "+ error);
				System.out.println(error);
				e.printStackTrace();
				undoMethod = null;
			}
			log.deleteLastLog(2);
		}
		else if(log.countLines() == 1){
			return "";
		}
		else{
			System.out.println("The method, that is used to undo the last step, was not found.");
			undoMethod = null;
		}
		return undoMethod;
	}
}
