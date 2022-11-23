package render.code;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import render.code.gmaths.Mat4;
import render.code.gmaths.Mat4Transform;
import render.code.gmaths.Vec3;

public class SkyboxModel{
    private Model[] boxSide;
    private Camera camera;
    private Texture[] textureMap = new Texture[6];
    private float size = 100f;

    private final Vec3 baseColor = new Vec3(1f, 1f, 1f);
    private final Material material = new Material(baseColor, baseColor, new Vec3(0.0f, 0.0f, 0.0f), 0.01f);
    private final String vertexShaderPath = "src/render/code/shaders/vs_skybox.txt";
    private final String fragmentShaderPath = "src/render/code/shaders/fs_skybox.txt";

    private Vec3 position;

    public SkyboxModel(GL3 gl, Camera c, Vec3 pos) {
        camera = c;
        loadTextures(gl);
        boxSide = new Model[6];
        boxSide[0] = makeWall0(gl);
        boxSide[1] = makeWall1(gl);
        boxSide[2] = makeWall2(gl);
        boxSide[3] = makeWall3(gl);
        boxSide[4] = makeWall4(gl);
        boxSide[5] = makeWall5(gl);
        position = pos;
    }

    private void loadTextures(GL3 gl) {
        textureMap[0] = TextureLibrary.loadTexture(gl, "src/resources/textures/skybox_textures/ny.png");
        textureMap[1] = TextureLibrary.loadTexture(gl, "src/resources/textures/skybox_textures/px.png");
        textureMap[2] = TextureLibrary.loadTexture(gl, "src/resources/textures/skybox_textures/nx.png");
        textureMap[3] = TextureLibrary.loadTexture(gl, "src/resources/textures/skybox_textures/nz.png");
        textureMap[4] = TextureLibrary.loadTexture(gl, "src/resources/textures/skybox_textures/pz.png");
        textureMap[5] = TextureLibrary.loadTexture(gl, "src/resources/textures/skybox_textures/py.png");
    }

    private Model makeWall0(GL3 gl) {
        //floor
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
        return new Model(gl, camera, shader, material, modelMatrix, mesh, textureMap[0]);
    }

    private Model makeWall1(GL3 gl) {
        //right
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(-90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(size*0.5f,size*0.5f,0), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
        return new Model(gl, camera, shader , material, modelMatrix, mesh, textureMap[1]);
    }

    private Model makeWall2(GL3 gl) {
        //left
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(-size*0.5f,size*0.5f,0), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
        return new Model(gl, camera, shader, material, modelMatrix, mesh, textureMap[2]);
    }

    private Model makeWall3(GL3 gl) {
        //behind
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(180), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(0,size*0.5f,size*0.5f), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
        return new Model(gl, camera, shader, material, modelMatrix, mesh, textureMap[3]);
    }

    private Model makeWall4(GL3 gl) {
        //facing
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(0,size*0.5f,-(size*0.5f)), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
        return new Model(gl, camera, shader, material, modelMatrix, mesh, textureMap[4]);
    }

    private Model makeWall5(GL3 gl) {
        //top
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(180), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(0,size,0), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, vertexShaderPath, fragmentShaderPath);
        return new Model(gl, camera, shader, material, modelMatrix, mesh, textureMap[5]);
    }

    public void render(GL3 gl) {
        for (Model model : boxSide) {
            Mat4 newMatrix = Mat4.multiply(Mat4Transform.translate(position), model.getModelMatrix());
            newMatrix = Mat4.multiply(Mat4Transform.translate(0f, -30f, 0f), newMatrix);
            model.render(gl, newMatrix);
        }
    }

    public void dispose(GL3 gl) {
        for (Model model : boxSide) {
            model.dispose(gl);
        }
    }

    public void setPosition(Vec3 pos) {
        position = pos;
    }
}
