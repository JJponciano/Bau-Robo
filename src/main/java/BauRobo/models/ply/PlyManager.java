/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package BauRobo.models.ply;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import static BauRobo.models.ply.PLYType.FLOAT32;
import static BauRobo.models.ply.PLYType.LIST;
import static BauRobo.models.ply.PLYType.UINT8;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 */
public class PlyManager {

    float[] x; //TODO: float or double
    float[] y;
    float[] z;
    int[][] vertex_indices;
    String comments = "";

    /**
     * Creates an instance of PlyManager from a JSON object.
     *
     * @param jo JSON object with the following structure{ x:[...],
     * y:[...],z:[...],faces[[...]...]}
     */
    public PlyManager(JSONObject jo) {
        JSONArray xArray = jo.getJSONArray("x");
        JSONArray yArray = jo.getJSONArray("y");
        JSONArray zArray = jo.getJSONArray("z");

        this.x = new float[xArray.length()];
        this.y = new float[yArray.length()];
        this.z = new float[zArray.length()];
        for (int i = 0; i < x.length; i++) {
                x[i] = Float.parseFloat(xArray.getString(i));
                y[i] = Float.parseFloat(yArray.getString(i));
                z[i] = Float.parseFloat(zArray.getString(i));
        }
        JSONArray facesArray = jo.getJSONArray("vertex_indices");
        int nb = facesArray.getJSONArray(0).length();
        this.vertex_indices = new int[facesArray.length()][nb];
        for (int i = 0; i < this.vertex_indices.length; i++) {
            for (int j = 0; j < nb; j++) {
                this.vertex_indices[i][j] = facesArray.getJSONArray(i).getInt(j);
            }
        }
    }

    /**
     * Creates an instance of PlyManager.
     *
     * @param x coordinates in the X axis.
     * @param y coordinates in the Y axis.
     * @param z coordinates in the Z axis.
     * @param vertex_indices Vertex index for each face.
     */
    PlyManager(float[] x, float[] y, float[] z, int[][] vertex_indices) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vertex_indices = vertex_indices;
    }

    /**
     * Creates an instance of PlyManage from a PLY file.
     *
     * @param path path of the PLY file.
     * @throws IOException if the file cannot be loaded.
     * @throws PlyException if the PLY format is not correct.
     */
    public PlyManager(String path) throws IOException, PlyException {
        this.loadPly(path);
    }

    /**
     * Load mesh from PLY file
     *
     * @param path path of the PLY file.
     */
    private void loadPly(String path) throws IOException, PlyException {
        PLY mesh = PLY.load(Path.of(path));
        PLYElementList vertex = mesh.elements("vertex");
        PLYElementList face = mesh.elements("face");
        try {
            this.x = vertex.property(FLOAT32, "x");
            this.y = vertex.property(FLOAT32, "y");
            this.z = vertex.property(FLOAT32, "z");
            if (this.y.length != this.x.length || this.x.length != this.z.length) {
                throw new PlyException("the arrays x, y and z have not the same size!");
            }
        } catch (java.lang.IllegalArgumentException e) {
            double[] dx = vertex.property(PLYType.FLOAT64, "x");
            double[] dy = vertex.property(PLYType.FLOAT64, "y");
            double[] dz = vertex.property(PLYType.FLOAT64, "z");
            if (dy.length != dx.length || dx.length != dz.length) {
                throw new PlyException("the arrays x, y and z have not the same size!");
            }
            this.x = new float[dx.length];
            this.y = new float[dx.length];
            this.z = new float[dx.length];
            for (int i = 0; i < dz.length; i++) {
                this.z[i] = (float) dz[i];
                this.x[i] = (float) dx[i];
                this.y[i] = (float) dy[i];
            }
        }
        this.vertex_indices = face.property(LIST(UINT8, PLYType.INT32), "vertex_index");
    }

    /**
     * Gets JSON object corresponding to this instance.
     *
     * @return JSON object with the following structure{ x:[...],
     * y:[...],z:[...],vertex_indices[...]}
     */
    public JSONObject asJSON() {
        JSONObject jo = new JSONObject();

        JSONArray xArray = new JSONArray(Arrays.toString(x));
        JSONArray yArray = new JSONArray(Arrays.toString(y));
        JSONArray zArray = new JSONArray(Arrays.toString(z));
        JSONArray faces = new JSONArray();
        for (int[] face : this.vertex_indices) {
            JSONArray facesArray = new JSONArray();
            for (int j = 0; j < face.length; j++) {
                facesArray.put(face[j]);
            }
            faces.put(facesArray);
        }
        jo.put("x", xArray);
        jo.put("y", yArray);
        jo.put("z", zArray);
        jo.put("vertex_indices", faces);
        return jo;
    }

    @Override
    public String toString() {
        String ply = "ply\n"
                + "format ascii 1.0\n"
                + "comment made by JavaPointCloud library\n"
                + "comment " + this.comments + "\n"
                + "element vertex " + this.x.length + "\n"
                + "property float x\n"
                + "property float y\n"
                + "property float z\n"
                + "element face " + this.vertex_indices.length + "\n"
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
    public void save(String path) throws IOException {
        String txt = this.toString();
        final File fileio = new File(path);
        final Charset charset = Charset.forName("UTF8");
        BufferedWriter writer = Files.newBufferedWriter(fileio.toPath(), charset);
        writer.write(txt, 0, txt.length());

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
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Arrays.hashCode(this.x);
        hash = 67 * hash + Arrays.hashCode(this.y);
        hash = 67 * hash + Arrays.hashCode(this.z);
        hash = 67 * hash + Arrays.deepHashCode(this.vertex_indices);
        hash = 67 * hash + Objects.hashCode(this.comments);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlyManager other = (PlyManager) obj;
        if (!Objects.equals(this.comments, other.comments)) {
            return false;
        }
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
        return true;
    }

}
