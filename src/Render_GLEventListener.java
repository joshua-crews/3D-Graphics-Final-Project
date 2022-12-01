import gmaths.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Render_GLEventListener implements GLEventListener {

    private final Camera camera;

    private final JPanel canvas;
    private final JSlider globalLightSlider;
    private final MainRenderer frame;

    public static boolean isRendering = false;

    /* The constructor is not used to initialise anything */
    public Render_GLEventListener(Camera camera, JPanel canvasWindow, MainRenderer frame, JSlider slider) {
        this.camera = camera;
        this.camera.setPosition(new Vec3(4f, 6f, 15f));
        this.camera.setTarget(new Vec3(0f, 5f, 0f));
        this.canvas = canvasWindow;
        this.frame = frame;
        this.globalLightSlider = slider;
    }

    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glFrontFace(GL.GL_CCW);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
        initialise(gl);
        startTime = getSeconds();
    }

    public void setLamp1Pos(int num) {
        room.setLamp1Pos(num);
    }

    public void setLamp2Pos(int num) {
        room.setLamp2Pos(num);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(x, y, width, height);
        float aspect = (float) width / (float) height;
        camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
    }

    /* Draw */
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        render(gl);
        if (!isRendering) {
            isRendering = true;
            for (Component comp : canvas.getComponents()) {
                if (comp instanceof JLabel) {
                    canvas.remove(comp);
                }
            }
            frame.redrawUI();
        }
    }

    public void dispose(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        table.dispose(gl);
        room.dispose(gl);
        light.dispose(gl);
        skybox.dispose(gl);
    }

    private Room room;
    private Table table;
    private Light light;
    private SkyboxModel skybox;

    private Texture[] texture;

    private boolean isEggJumping = false;

    private final int T_CONTAINER_DIFFUSE = 0;
    private final int T_CONTAINER_SPECULAR = 1;

    private void loadTextures(GL3 gl) {
        texture = new Texture[2];
        texture[T_CONTAINER_DIFFUSE] = TextureLibrary.loadTexture(gl, "resources/textures/container2.jpg");
        texture[T_CONTAINER_SPECULAR] = TextureLibrary.loadTexture(gl, "resources/textures/container2_specular.jpg");
    }

    public void initialise(GL3 gl) {
        loadTextures(gl);
        light = new Light(gl, globalLightSlider);
        light.setCamera(camera);
        light.setPosition(new Vec3(0.0f, 10.0f, 0.0f));
        skybox = new SkyboxModel(gl, camera, camera.getPosition());
        room = new Room(gl, camera, light, skybox);
        table = new Table(gl, camera, light, skybox, texture[T_CONTAINER_DIFFUSE], texture[T_CONTAINER_SPECULAR], room.getLamp1().getLight(), room.getLamp2().getLight());
    }

    public void render(GL3 gl) {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        light.render(gl);
        skybox.setPosition(camera.getPosition());
        skybox.setRotation(getSkyboxRotation());
        skybox.render(gl);

        table.setModelMatrix(getModelMatrix());
        table.render(gl);

        //This is all my code within the render function for the egg
        if (isEggJumping) {
            room.setEggPosition(new Vec3(0.0f, getEggPosition(), 0.0f));
            if (getEggPosition() < 0.0f) {
                isEggJumping = false;
                room.setEggPosition(new Vec3(0.0f, 0.0f, 0.0f));
            }
        } else {
            isEggJumping = shouldEggJump();
        }
        room.render(gl);
    }

    private float getSkyboxRotation() {
        double elapsedTime = getSeconds() - startTime;
        float value = 180.0f * (float) Math.toRadians(elapsedTime * 0.25f) + 180.0f;
        while (value > 360f) {
            value -= 360f;
        }
        return value;
    }

    private float getEggPosition() {
        double elapsedTime = getSeconds() - jumpTime;
        return (float) Math.sin(Math.toRadians(elapsedTime * 200f));
    }

    private boolean shouldEggJump() {
        double elapsedTime = getSeconds() - jumpTime;
        Random r = new Random();
        if (isEggJumping) {
            return false;
        }
        if (elapsedTime > nextJumpInterval) {
            jumpTime = getSeconds();
            nextJumpInterval = r.nextInt(20-8) + 8;
            return true;
        } else {
            return false;
        }
    }

    private Mat4 getModelMatrix() {
        Mat4 m = new Mat4(1);
        m = Mat4.multiply(m, Mat4Transform.translate(0, 4, 0));
        return m;
    }

    public void setLamp1OnOff() {
        room.getLamp1().setLightOnOff();
    }

    public void setLamp2OnOff() {
        room.getLamp2().setLightOnOff();
    }

    private double startTime;
    private double jumpTime;
    private int nextJumpInterval;

    private double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }

}
