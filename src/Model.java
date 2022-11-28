import gmaths.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;

public class Model {

    private final Mesh mesh;
    private final Texture textureId1;
    private final Texture textureId2;
    private final Material material;
    private final Shader shader;
    private Mat4 modelMatrix;
    private final Camera camera;
    private final Light light;
    private final SkyboxModel skybox;
    private final LampLight lamp1Light;
    private final LampLight lamp2Light;

    public Model(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, Texture textureId1, Texture textureId2, LampLight lamp1Light, LampLight lamp2Light) {
        this.mesh = mesh;
        this.material = material;
        this.modelMatrix = modelMatrix;
        this.shader = shader;
        this.camera = camera;
        this.light = light;
        this.skybox = skybox;
        this.textureId1 = textureId1;
        this.textureId2 = textureId2;
        this.lamp1Light = lamp1Light;
        this.lamp2Light = lamp2Light;
    }

    public Model(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, Texture textureId1, LampLight lamp1Light, LampLight lamp2Light) {
        this(gl, camera, light, skybox, shader, material, modelMatrix, mesh, textureId1, null, lamp1Light, lamp2Light);
    }

    public Model(GL3 gl, Camera camera, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, Texture textureId1, LampLight lamp1Light, LampLight lamp2Light) {
        this(gl, camera, null, null, shader, material, modelMatrix, mesh, textureId1, null, lamp1Light, lamp2Light);
    }

    public void setModelMatrix(Mat4 m) {
        modelMatrix = m;
    }

    public Mat4 getModelMatrix() {
        return modelMatrix;
    }

    public void render(GL3 gl, Mat4 modelMatrix) {
        Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), modelMatrix));
        shader.use(gl);
        shader.setFloatArray(gl, "model", modelMatrix.toFloatArrayForGLSL());
        shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

        shader.setVec3(gl, "viewPos", camera.getPosition());

        if (light != null) {
            shader.setVec3(gl, "light.position", light.getPosition());
            Vec3 ambient = light.getMaterial().getAmbient();
            float multiplier = light.getLightLevel();
            ambient.x *= multiplier;
            ambient.y *= multiplier;
            ambient.z *= multiplier;
            shader.setVec3(gl, "light.ambient", ambient);
            Vec3 diffuse = light.getMaterial().getDiffuse();
            diffuse.x *= multiplier;
            diffuse.y *= multiplier;
            diffuse.z *= multiplier;
            shader.setVec3(gl, "light.diffuse", diffuse);
            Vec3 specular = light.getMaterial().getSpecular();
            specular.x *= multiplier;
            specular.y *= multiplier;
            specular.z *= multiplier;
            shader.setVec3(gl, "light.specular", specular);
        }

        shader.setVec3(gl, "material.ambient", material.getAmbient());
        shader.setVec3(gl, "material.diffuse", material.getDiffuse());
        shader.setVec3(gl, "material.specular", material.getSpecular());
        shader.setFloat(gl, "material.shininess", material.getShininess());


        if (lamp1Light != null && lamp2Light != null) {
            if (lamp1Light.isOn()) {
                shader.setVec3(gl, "spotlight_one.position", lamp1Light.getPosition());
                Vec3 light1Front;
                switch (lamp1Light.getState()) {
                    case 1:
                        light1Front = Vec3.subtract(new Vec3(90f, 0f, 0f), lamp1Light.getPosition());
                        break;
                    case 2:
                        light1Front = Vec3.subtract(new Vec3(90f, 10f, -105f), lamp1Light.getPosition());
                        break;
                    default:
                        light1Front = Vec3.subtract(new Vec3(90f, -30f, 0f), lamp1Light.getPosition());
                        break;
                }
                light1Front.normalize();
                shader.setVec3(gl, "spotlight_one.direction", light1Front);
                shader.setFloat(gl, "spotlight_one.cutOff", (float) Math.cos(12.43));
                shader.setVec3(gl, "spotlight_one.ambient", lamp1Light.getMaterial().getAmbient());
                shader.setVec3(gl, "spotlight_one.diffuse", lamp1Light.getMaterial().getDiffuse());
                shader.setVec3(gl, "spotlight_one.specular", lamp1Light.getMaterial().getSpecular());
            } else {
                shader.setVec3(gl, "spotlight_one.position", new Vec3(0f, 0f, 0f));
                shader.setVec3(gl, "spotlight_one.direction", new Vec3(0f, 0f, 0f));
                shader.setFloat(gl, "spotlight_one.cutOff", (float) Math.cos(12.43));
                shader.setVec3(gl, "spotlight_one.ambient", new Vec3(0f, 0f, 0f));
                shader.setVec3(gl, "spotlight_one.diffuse", new Vec3(0f, 0f, 0f));
                shader.setVec3(gl, "spotlight_one.specular", new Vec3(0f, 0f, 0f));
            }

            if (lamp2Light.isOn()) {
                shader.setVec3(gl, "spotlight_two.position", lamp2Light.getPosition());
                Vec3 light2Front;
                switch (lamp2Light.getState()) {
                    case 1:
                        light2Front = Vec3.subtract(new Vec3(-90f, 0f, 0f), lamp2Light.getPosition());
                        break;
                    case 2:
                        light2Front = Vec3.subtract(new Vec3(-90f, 0f, -45f), lamp2Light.getPosition());
                        break;
                    default:
                        light2Front = Vec3.subtract(new Vec3(-90f, -30f, 0f), lamp2Light.getPosition());
                        break;
                }
                light2Front.normalize();
                shader.setVec3(gl, "spotlight_two.direction", light2Front);
                shader.setFloat(gl, "spotlight_two.cutOff", (float) Math.cos(12.43));
                shader.setVec3(gl, "spotlight_two.ambient", lamp2Light.getMaterial().getAmbient());
                shader.setVec3(gl, "spotlight_two.diffuse", lamp2Light.getMaterial().getDiffuse());
                shader.setVec3(gl, "spotlight_two.specular", lamp2Light.getMaterial().getSpecular());
            } else {
                shader.setVec3(gl, "spotlight_two.position", new Vec3(0f, 0f, 0f));
                shader.setVec3(gl, "spotlight_two.direction", new Vec3(0f, 0f, 0f));
                shader.setFloat(gl, "spotlight_two.cutOff", (float) Math.cos(12.43));
                shader.setVec3(gl, "spotlight_two.ambient", new Vec3(0f, 0f, 0f));
                shader.setVec3(gl, "spotlight_two.diffuse", new Vec3(0f, 0f, 0f));
                shader.setVec3(gl, "spotlight_two.specular", new Vec3(0f, 0f, 0f));
            }
        }

        if (textureId1 != null) {
            shader.setInt(gl, "first_texture", 0);  // be careful to match these with GL_TEXTURE0 and GL_TEXTURE1
            gl.glActiveTexture(GL.GL_TEXTURE0);
            textureId1.bind(gl);  // uses JOGL Texture class
        }
        if (textureId2 != null) {
            shader.setInt(gl, "second_texture", 1);
            gl.glActiveTexture(GL.GL_TEXTURE1);
            textureId2.bind(gl);  // uses JOGL Texture class
        }
        mesh.render(gl, light != null || skybox != null);
    }

    public void render(GL3 gl) {
        render(gl, modelMatrix);
    }

    public void dispose(GL3 gl) {
        mesh.dispose(gl);
        if (textureId1 != null) textureId1.destroy(gl);
        if (textureId2 != null) textureId2.destroy(gl);
    }

}