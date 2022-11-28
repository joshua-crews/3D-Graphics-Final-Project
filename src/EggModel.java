/* This is my own work*/
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import gmaths.Mat4;
import gmaths.Mat4Transform;
import gmaths.Vec3;

public class EggModel extends Model {

    private Vec3 position;

    public EggModel(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, Texture textureId1, Texture textureId2, LampLight lamp1Light, LampLight lamp2Light) {
        super(gl, camera, light, skybox, shader, material, modelMatrix, mesh, textureId1, textureId2, lamp1Light, lamp2Light);
        position = new Vec3(0.0f, 0.0f, 0.0f);
    }

    public void render(GL3 gl) {
        Mat4 newMatrix = Mat4.multiply(Mat4Transform.translate(position), getModelMatrix());
        render(gl, newMatrix);
    }

    public void setPosition(Vec3 pos) {
        this.position = pos;
    }
}
