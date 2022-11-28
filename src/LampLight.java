import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import gmaths.Mat4;
import gmaths.Mat4Transform;
import gmaths.Vec3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class LampLight {

    private Material material;
    private Vec3 position;
    private Vec3 rotation;
    private Shader shader;
    private Camera camera;

    private int state = 0;

    private boolean isOn = true;

    public LampLight(GL3 gl, Vec3 rotation) {
        material = new Material();
        material.setAmbient(0.3f, 0.3f, 0.3f);
        material.setDiffuse(0.7f, 0.7f, 0.7f);
        material.setSpecular(0.7f, 0.7f, 0.7f);
        position = new Vec3(3f, 2f, 1f);
        this.rotation = rotation;

        fillBuffers(gl);
        shader = new Shader(gl, "shaders/vs_light_01.txt", "shaders/fs_light_01.txt");
    }

    public void setState(int s) {
        this.state = s;
    }

    public int getState() {
        return this.state;
    }

    public void setRotation(Vec3 rot) {
        rotation = rot;
    }

    public void setPosition(Vec3 v) {
        position.x = v.x;
        position.y = v.y;
        position.z = v.z;
    }

    public Vec3 getPosition() {
        return position;
    }

    public Material getMaterial() {
        return material;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void render(GL3 gl) {
        if (isOn) {
            Mat4 model = new Mat4(1);
            model = Mat4.multiply(Mat4Transform.rotateAroundX(rotation.x), model);
            model = Mat4.multiply(Mat4Transform.rotateAroundY(rotation.y), model);
            model = Mat4.multiply(Mat4Transform.rotateAroundZ(rotation.z), model);
            model = Mat4.multiply(Mat4Transform.scale(0.3f, 0.3f, 0.3f), model);
            model = Mat4.multiply(Mat4Transform.translate(position), model);

            Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), model));

            shader.use(gl);
            shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

            gl.glBindVertexArray(vertexArrayId[0]);

            gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
            gl.glBindVertexArray(0);
        }
    }

    public void setOn(boolean isEnabled) {
        isOn = isEnabled;
    }
    public boolean isOn() {
        return isOn;
    }

    public void dispose(GL3 gl) {
        gl.glDeleteBuffers(1, vertexBufferId, 0);
        gl.glDeleteVertexArrays(1, vertexArrayId, 0);
        gl.glDeleteBuffers(1, elementBufferId, 0);
    }

    private final float[] vertices = new float[] {
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f,  0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f,  0.5f, -0.5f,
            0.5f,  0.5f,  0.5f
    };

    private final int[] indices =  new int[] {
            0,1,3,
            3,2,0,
            4,6,7,
            7,5,4,
            1,5,7,
            7,3,1,
            6,4,0,
            0,2,6,
            0,4,5,
            5,1,0,
            2,3,7,
            7,6,2
    };

    private int vertexStride = 3;
    private int vertexXYZFloats = 3;

    private int[] vertexBufferId = new int[1];
    private int[] vertexArrayId = new int[1];
    private int[] elementBufferId = new int[1];

    private void fillBuffers(GL3 gl) {
        gl.glGenVertexArrays(1, vertexArrayId, 0);
        gl.glBindVertexArray(vertexArrayId[0]);
        gl.glGenBuffers(1, vertexBufferId, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);
        FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);

        int stride = vertexStride;
        int numXYZFloats = vertexXYZFloats;
        int offset = 0;
        gl.glVertexAttribPointer(0, numXYZFloats, GL.GL_FLOAT, false, stride * Float.BYTES, offset);
        gl.glEnableVertexAttribArray(0);

        gl.glGenBuffers(1, elementBufferId, 0);
        IntBuffer ib = Buffers.newDirectIntBuffer(indices);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, (long) Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);
    }

}