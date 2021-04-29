package BauRobo.models.ply;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import static BauRobo.models.ply.PLYType.FLOAT32;

/**
 * @author Lisa Mosis
 */
public class PointcloudDTO {

    float[] x;
    float[] y;
    float[] z;
    float[] intensity;

    /**
     * Load pointcloud from PLY file
     *
     * @param path path of the PLY file.
     */
    public PointcloudDTO(String path) throws IOException, PlyException {
        this.loadPly(path);
    }

    /**
     * Load pointcloud from PLY file
     *
     * @param path path of the PLY file.
     */
    private void loadPly(String path) throws IOException, PlyException {
        PLY mesh = PLY.load(Path.of(path));
        PLYElementList vertex = mesh.elements("vertex");
        try {
            this.x = vertex.property(FLOAT32, "x");
            this.y = vertex.property(FLOAT32, "y");
            this.z = vertex.property(FLOAT32, "z");
            this.intensity = vertex.property(FLOAT32, "intensity");
            if (this.y.length != this.x.length || this.x.length != this.z.length|| this.x.length != this.intensity.length) {
                throw new PlyException("the arrays x, y and z have not the same size!");
            }
        } catch (java.lang.IllegalArgumentException e) {
            double[] dx = vertex.property(PLYType.FLOAT64, "x");
            double[] dy = vertex.property(PLYType.FLOAT64, "y");
            double[] dz = vertex.property(PLYType.FLOAT64, "z");
            double[] dIntensity = vertex.property(PLYType.FLOAT64, "intensity");
            if (dy.length != dx.length || dx.length != dz.length|| dx.length != dIntensity.length) {
                throw new PlyException("the arrays x, y and z have not the same size!");
            }
            this.x = new float[dx.length];
            this.y = new float[dx.length];
            this.z = new float[dx.length];
            this.intensity = new float[dx.length];
            for (int i = 0; i < dz.length; i++) {
                this.z[i] = (float) dz[i];
                this.x[i] = (float) dx[i];
                this.y[i] = (float) dy[i];
                this.intensity[i] = (float) dIntensity[i];
            }
        }
    }

    /**
     * Makes a JSON-like String from the Object for sending
     */
    @Override
    public String toString() {
        String str = "{\"x\":" + Arrays.toString(x) + ",\"y\":" + Arrays.toString(y) + ",\"z\":" + Arrays.toString(z) +",\"intensity\":" + Arrays.toString(intensity) + "}";
        return str;
    }

}
