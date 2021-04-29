package BauRobo.models.ply;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.io.FileWriter;
import java.nio.file.Files;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @author Lisa Mosis
 */
public class ObjectDTO extends PlyManager{

    
    public String objID;
	public String objClass;
	public String color; //hex
	public String sanding; //TODO: ask customer if only one of the options color and sanding allowed makes sense

    
	/**
     * Creates an instance of ObjectDTO from a JSON object.
     *
     * @param jo JSON object with the following structure{ x:[...],
     * y:[...],z:[...],vertex_indices[[...]...]}
     */
    public ObjectDTO(JSONObject jo) {
        super(null, null, null, null);
        JSONArray xArray = jo.getJSONArray("x");
        JSONArray yArray = jo.getJSONArray("y");
        JSONArray zArray = jo.getJSONArray("z");

        this.x = new float[xArray.length()];
        this.y = new float[yArray.length()];
        this.z = new float[zArray.length()];
        for (int i = 0; i < x.length; i++) {
            x[i] = (float) xArray.getDouble(i);
            y[i] = (float) yArray.getDouble(i);
            z[i] = (float) zArray.getDouble(i);
        }
        JSONArray facesArray = jo.getJSONArray("vertex_indices");
        int nb = facesArray.getJSONArray(0).length();
        this.vertex_indices = new int[facesArray.length()][nb];
        for (int i = 0; i < this.vertex_indices.length; i++) {
            for (int j = 0; j < nb; j++) {
                this.vertex_indices[i][j] = facesArray.getJSONArray(i).getInt(j);
            }
        }

        this.objID = jo.getString("objID");
        this.objClass = jo.getString("objClass");
        this.color = jo.getString("color");
        this.sanding = jo.getString("sanding");
        if(this.color == null)
            this.color = "";
        if(this.sanding == null)
            this.sanding = "";
    }
    
    /**
     * Creates an instance of ObjectDTO.
     * For an empty color or sanding use ""
     *
     * @param x coordinates in the X axis.
     * @param y coordinates in the Y axis.
     * @param z coordinates in the Z axis.
     * @param vertex_indices Vertex index for each face.
     * @param objID name of the object
     * @param objClass what the object represents
     * @param color the color the object is supposed to be painted
     * @param sanding the sanding the object is supposed to get
     */
    public ObjectDTO(float[] x, float[] y, float[] z, int[][] vertex_indices, String objID, String objClass, String color, String sanding) {
    	super(x,y,z,vertex_indices);
    	
        this.objID = objID;
        this.objClass = objClass;
        this.color = color;
        this.sanding = sanding;
        if(this.color == null)
            this.color = "";
        if(this.sanding == null)
            this.sanding = "";
    }
    
    /**
     * Creates an instance of ObjectDTO from a PLY file.
     *
     * @param path path of the PLY file.
     * @throws IOException if the file cannot be loaded.
     * @throws PlyException if the PLY format is not correct.
     */
    public ObjectDTO(String path) throws IOException, PlyException {
    	super(path);
        this.loadPly(path);
    }

    /**
     * Load mesh from PLY file
     *
     * @param path path of the PLY file.
     */
    private void loadPly(String path) throws IOException{
    	PLY mesh = PLY.load(Path.of(path));
    	
    	comments = mesh.comments.get(4);
        objID = comments.split(" ")[0].split("=")[1]; // TODO: exception when the id isn't the same as the filename
        objClass = comments.split(" ")[1].split("=")[1];
        try {
        	color = comments.split(" ")[2].split("=")[1];
        }catch(Exception e) {
            color = "";
        }
        try {
        	sanding = comments.split(" ")[3].split("=")[1];
        }catch(Exception e) {
        	sanding = "";
        }
        if(this.color == null)
            this.color = "";
        if(this.sanding == null)
            this.sanding = "";
    }

    /**
     * makes a JSON-like String for sending
     */
    @Override
    public String toString() {
    	JSONArray faces = new JSONArray();
        for (int[] face : vertex_indices) {
            JSONArray facesArray = new JSONArray();
            for (int i : face) {
                facesArray.put(i);
            }
            faces.put(facesArray);
        }
    	String str = "{\"objID\":\"" + this.objID + "\",\"objClass\":\"" + this.objClass + "\",\"color\":\"" + this.color + "\",\"sanding\":\"" + this.sanding + "\",\"vertex_indices\":" + faces + ",\"x\":" + Arrays.toString(x) + ",\"y\":" + Arrays.toString(y) + ",\"z\":" + Arrays.toString(z) + "}";
    	return str;
    }

    /**
     * Makes a String from this object that is formated to be saved in a .ply file
     */
    public String toPly() {
    	if((this.color == null || this.color.equals("")) && (this.sanding == null || this.sanding.equals("")))
    		this.comments = "id=" + this.objID + " class=" + this.objClass;
    	else
    		this.comments = "id=" + this.objID + " class=" + this.objClass + " color=" + this.color + " sanding=" + this.sanding;
        String ply = "ply\n"
                + "format ascii 1.0\n"
                + "comment made by JavaPointCloud library\n"
                + "comment " + this.comments + "\n"
                + "element vertex " + this.x.length + "\n"
                + "property double x\n"
                + "property double y\n"
                + "property double z\n"
                + "element face " + vertex_indices.length + "\n"
                + "property list uchar int vertex_index\n"
                + "end_header\n";

        //writes the points
        for (int i = 0; i < x.length; i++) {
            ply += x[i] + " " + y[i] + " " + z[i] + "\n";
        }
        //writes the faces
        for (int[] f : this.vertex_indices) {
            ply += f.length + " ";
            for (int i = 0; i < f.length; i++) {
                int index = f[i];
                ply += index;
                if (i + 1 == f.length) {
                    ply += "\n";
                } else {
                    ply += " ";
                }
            }
        }
        return ply;
    }

    /**
     * Save this instance into a PLY file.
     *
     * @param path the PLY file path.
     */
    @Override
    public void save(String path) throws IOException {
        String txt = this.toPly();
        FileWriter myWriter = new FileWriter(path);
        myWriter.write(txt);
        myWriter.close();

    }

    /**
     * deletes the object
     */
    public boolean deleteObject(String stdFilepath) throws IOException {
        String path = stdFilepath + objID + ".ply";
        Path filepath = Paths.get(path);
		boolean success = true;
        if(Files.notExists(filepath)){
            success = false;
            System.out.println("File does not exist");
        }
        Files.deleteIfExists(filepath);
		return success;
	}

    public float[] getX() {
        return x;
    }

    public void setX(float[] x) {
        this.x = x;
    }

    public float[] getY() {
        return y;
    }

    public void setY(float[] y) {
        this.y = y;
    }

    public float[] getZ() {
        return z;
    }

    public void setZ(float[] z) {
        this.z = z;
    }

    public int[][] getVertex_indices() {
        return vertex_indices;
    }

    public void setVertex_indices(int[][] vertex_indices) {
        this.vertex_indices = vertex_indices;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { //is the object compared to itself?
            return true;
        }
        if (obj == null) { //compared to null
            return false;
        }
        if (getClass() != obj.getClass()) { //is it the same object class
            return false;
        }
        final ObjectDTO other = (ObjectDTO) obj;
        if (!Arrays.equals(this.x, other.x)) {
            return false;
        }
        if (!Arrays.equals(this.y, other.y)) {
            return false;
        }
        if (!Arrays.equals(this.z, other.z)) {
            return false;
        }
        if (!Arrays.deepEquals(this.vertex_indices, other.vertex_indices)) {
            return false;
        }
        if (!this.objClass.equals(other.objClass)) {
            return false;
        }
        if (!this.objID.equals(other.objID)) {
            return false;
        }
        if (!this.color.equals(other.color)) {
            return false;
        }
        if (!this.sanding.equals(other.sanding)) {
            return false;
        }
        return true;
    }
}