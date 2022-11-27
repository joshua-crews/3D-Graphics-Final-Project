package render.code;

import render.code.gmaths.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;

public class Model {

    private Mesh mesh;
    private Texture textureId1;
    private Texture textureId2;
    private Material material;
    private Shader shader;
    private Mat4 modelMatrix;
    private Camera camera;
    private Light light;
    private SkyboxModel skybox;

    public Model(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, Texture textureId1, Texture textureId2) {
        this.mesh = mesh;
        this.material = material;
        this.modelMatrix = modelMatrix;
        this.shader = shader;
        this.camera = camera;
        this.light = light;
        this.skybox = skybox;
        this.textureId1 = textureId1;
        this.textureId2 = textureId2;
    }

    public Model(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, Texture textureId1) {
        this(gl, camera, light, skybox, shader, material, modelMatrix, mesh, textureId1, null);
    }

    public Model(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh) {
        this(gl, camera, light, skybox, shader, material, modelMatrix, mesh, null, null);
    }

    public Model(GL3 gl, Camera camera, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, Texture textureId1) {
        this(gl, camera, null, null, shader, material, modelMatrix, mesh, textureId1, null);
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