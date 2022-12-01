import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import gmaths.Mat4;
import gmaths.Mat4Transform;
import gmaths.Vec3;
import objects.TableLeg;
import objects.TableTop;

class Table {

    private final Model tableTop;

    private final Model[] tableLegs;

    public Table(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Texture t_diffuse, Texture t_specular, LampLight lamp1Light, LampLight lamp2Light) {
        //Tabletop
        Mesh mesh = new Mesh(gl, TableTop.vertices.clone(), TableTop.indices.clone());
        Shader shader = new Shader(gl, "shaders/vs_cube_04.txt", "shaders/fs_cube_04.txt");
        Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        // diffuse and specular textures
        tableTop = new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular, lamp1Light, lamp2Light);

        //Table leg
        mesh = new Mesh(gl, TableLeg.vertices.clone(), TableLeg.indices.clone());
        tableLegs = new Model[]{new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular, lamp1Light, lamp2Light),
                new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular, lamp1Light, lamp2Light),
                new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular, lamp1Light, lamp2Light),
                new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular, lamp1Light, lamp2Light)};
    }

    public void setModelMatrix(Mat4 m) {
        tableTop.setModelMatrix(m);
        m = Mat4.multiply(m, Mat4Transform.translate(0, -2, 0));
        for (int i = 0; i < tableLegs.length; i++) {
            final float distanceValue = 1.75f;
            switch (i) {
                case 0:
                    m = Mat4.multiply(m, Mat4Transform.translate(distanceValue, 0, distanceValue));
                    tableLegs[i].setModelMatrix(m);
                    m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue, 0, -distanceValue));
                    break;
                case 1:
                    m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue, 0, distanceValue));
                    tableLegs[i].setModelMatrix(m);
                    m = Mat4.multiply(m, Mat4Transform.translate(distanceValue, 0, -distanceValue));
                    break;
                case 2:
                    m = Mat4.multiply(m, Mat4Transform.translate(distanceValue, 0, -distanceValue));
                    tableLegs[i].setModelMatrix(m);
                    m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue, 0, distanceValue));
                    break;
                case 3:
                    m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue, 0, -distanceValue));
                    tableLegs[i].setModelMatrix(m);
                    m = Mat4.multiply(m, Mat4Transform.translate(distanceValue, 0, distanceValue));
                    break;
            }

        }
    }

    public void render(GL3 gl) {
        tableTop.render(gl);
        for (Model tableLeg : tableLegs) {
            tableLeg.render(gl);
        }
    }

    public void dispose(GL3 gl) {
        tableTop.dispose(gl);
        for (Model tableLeg : tableLegs) {
            tableLeg.dispose(gl);
        }
    }
}