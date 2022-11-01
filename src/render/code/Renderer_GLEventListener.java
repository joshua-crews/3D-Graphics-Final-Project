package render.code;

import render.code.gmaths.*;

import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import render.code.scene.objects.TableTop;

public class Renderer_GLEventListener implements GLEventListener {
    private Shader shader;
    private final Camera camera;

    public Renderer_GLEventListener(Camera camera) {
        this.camera = camera;
    }

    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        initialise(gl);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(x, y, width, height);
        float aspect = (float)width/(float)height;
        camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
    }

    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        render(gl);
    }

    public void dispose(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glDeleteBuffers(1, vertexBufferId, 0);
        gl.glDeleteVertexArrays(1, vertexArrayId, 0);
        gl.glDeleteBuffers(1, elementBufferId, 0);
        gl.glDeleteBuffers(1, textureId1, 0);
    }
    private int[] textureId1 = new int[1];

    public void initialise(GL3 gl) {
        shader = new Shader(gl, "src/render/code/vs_V01.txt", "src/render/code/fs_V01.txt");
        fillBuffers(gl);
        textureId1 = TextureLibrary.loadTexture(gl, "src/resources/textures/table_wood_texture.jpg");
    }

    public void render(GL3 gl) {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        Mat4 projectionMatrix = camera.getPerspectiveMatrix();
        Mat4 viewMatrix = camera.getViewMatrix();

        shader.use(gl);
        shader.setFloatArray(gl, "view", viewMatrix.toFloatArrayForGLSL());
        shader.setFloatArray(gl, "projection", projectionMatrix.toFloatArrayForGLSL());

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId1[0]);

        Mat4 modelMatrix = getModelMatrix();
        Mat4 mvpMatrix = Mat4.multiply(projectionMatrix, Mat4.multiply(viewMatrix, modelMatrix));

        shader.setFloatArray(gl, "model", modelMatrix.toFloatArrayForGLSL());
        shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

        gl.glBindVertexArray(vertexArrayId[0]);
        gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
        gl.glBindVertexArray(0);
    }

    private Mat4 getModelMatrix() {
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(modelMatrix, Mat4Transform.translate(0f,0f,0f));
        return modelMatrix;
    }

    final float[] vertices = TableTop.getVertices();
    final int[] indices = TableTop.getIndices();

    private final int vertexStride = 8;
    private final int vertexXYZFloats = 3;
    private final int vertexColourFloats = 3;
    private final int vertexTexFloats = 2;

    private final int[] vertexBufferId = new int[1];
    private final int[] vertexArrayId = new int[1];
    private final int[] elementBufferId = new int[1];

    private void fillBuffers(GL3 gl) {
        gl.glGenVertexArrays(1, vertexArrayId, 0);
        gl.glBindVertexArray(vertexArrayId[0]);
        gl.glGenBuffers(1, vertexBufferId, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);
        FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);

        int stride = vertexStride;
        int numXYZFloats = vertexXYZFloats;
        int offset = 0;
        gl.glVertexAttribPointer(0, numXYZFloats, GL.GL_FLOAT, false, stride*Float.BYTES, offset);
        gl.glEnableVertexAttribArray(0);

        int numColorFloats = vertexColourFloats;
        offset = numXYZFloats*Float.BYTES;
        gl.glVertexAttribPointer(1, numColorFloats, GL.GL_FLOAT, false, stride*Float.BYTES, offset);
        gl.glEnableVertexAttribArray(1);

        offset = (numXYZFloats+numColorFloats)*Float.BYTES;
        gl.glVertexAttribPointer(2, vertexTexFloats, GL.GL_FLOAT, false, stride*Float.BYTES, offset);
        gl.glEnableVertexAttribArray(2);

        gl.glGenBuffers(1, elementBufferId, 0);
        IntBuffer ib = Buffers.newDirectIntBuffer(indices);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, (long) Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);
        gl.glBindVertexArray(0);
    }

}