import gmaths.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;
import objects.Egg;
import objects.TableLeg;
import objects.TableTop;

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

class Room {

    private final Model[] wall;
    private final Camera camera;
    private final Light light;
    private final SkyboxModel skybox;
    private final float size = 16f;
    private Texture texture_floor;
    private Texture texture_wall;

    //All lamp and egg code is mine
    private Texture texture_wall_specular;
    private Texture texture_egg_albedo;
    private Texture texture_egg_specular;
    private final EggModel egg;
    private final Lamp lamp1;
    private final Lamp lamp2;

    private final Vec3[] lamp1Pos1Rotation = {
            new Vec3(0f, 0f, 0f),
            new Vec3(0f, 0f, 30f),
            new Vec3(0f, 0f, -60f),
            new Vec3(0f, 0f, 0f)
    };

    private final Vec3[] lamp1Pos2Rotation = {
            new Vec3(0f, 0f, 0f),
            new Vec3(0f, 0f, 0f),
            new Vec3(0f, 0f, 0f),
            new Vec3(0f, 0f, 0f)
    };

    private final Vec3[] lamp1Pos3Rotation = {
            new Vec3(0f, 30f, 0f),
            new Vec3(0f, -30f, -40f),
            new Vec3(0f, 0f, 80f),
            new Vec3(0f, 45f, 20f)
    };

    private final Vec3[] lamp2Pos1Rotation = {
            new Vec3(0f, 180f, 0f),
            new Vec3(0f, 0f, 30f),
            new Vec3(0f, 0f, -60f),
            new Vec3(0f, 0f, 0f)
    };

    private final Vec3[] lamp2Pos2Rotation = {
            new Vec3(0f, 180f, 0f),
            new Vec3(0f, 80f, -30f),
            new Vec3(-30f, 0f, 0f),
            new Vec3(0f, -90f, 0f)
    };

    private final Vec3[] lamp2Pos3Rotation = {
            new Vec3(90f, 180f, 0f),
            new Vec3(-10f, -30f, -40f),
            new Vec3(-45f, 0f, -90f),
            new Vec3(30f, 180f, 0f)
    };

    public Room(GL3 gl, Camera c, Light l, SkyboxModel s) {
        camera = c;
        light = l;
        skybox = s;
        loadTextures(gl);
        lamp1 = new Lamp(gl, c, l, s, new Vec3(-5.0f, -3.5f, -2.0f), new Vec3(0.0f, 0.0f, 60.0f));
        lamp2 = new Lamp(gl, c, l, s, new Vec3(5.0f, -3.5f, 0.5f), new Vec3(0.0f, 0.0f, -60.0f));
        lamp1.setLamp1Light(lamp1.getLight());
        lamp1.setLamp2Light(lamp2.getLight());
        lamp2.setLamp1Light(lamp1.getLight());
        lamp2.setLamp2Light(lamp2.getLight());
        setLamp1Pos(0);
        setLamp2Pos(0);
        wall = new Model[4];
        wall[0] = makeWall0(gl);
        wall[1] = makeWall1(gl);
        wall[2] = makeWall2(gl);
        wall[3] = makeWall3(gl);
        egg = makeEgg(gl);
    }

    public Lamp getLamp1() {
        return lamp1;
    }

    public Lamp getLamp2() {
        return lamp2;
    }

    public void setLamp1Pos(int num) {
        switch (num) {
            case 0:
                lamp1.setBaseRotation(lamp1Pos1Rotation[0]);
                lamp1.setFirstArmRotation(lamp1Pos1Rotation[1]);
                lamp1.setSecondArmRotation(lamp1Pos1Rotation[2]);
                lamp1.setHeadRotation(lamp1Pos1Rotation[3]);
                lamp1.setFirstArmOffset(new Vec3(0.0f, 0.0f, 0.0f));
                lamp1.setSecondArmOffset(new Vec3(0.0f, 0.0f, 0.0f));
                lamp1.setHeadOffset(new Vec3(0.0f, 0.0f, 0.0f));
                Vec3 lightPosition = new Vec3(lamp1.getPosition().x, lamp1.getPosition().y, lamp1.getPosition().z);
                lightPosition.y += 3.7f;
                lightPosition.x += 0.4f;
                lamp1.setLightPosition(lightPosition);
                lamp1.setLightRotation(new Vec3(0.0f, 0.0f, 60.0f));
                lamp1.getLight().setState(0);
                break;
            case 1:
                lamp1.setBaseRotation(lamp1Pos2Rotation[0]);
                lamp1.setFirstArmRotation(lamp1Pos2Rotation[1]);
                lamp1.setSecondArmRotation(lamp1Pos2Rotation[2]);
                lamp1.setHeadRotation(lamp1Pos2Rotation[3]);
                lamp1.setFirstArmOffset(new Vec3(0.5f, 0.0f, 0.0f));
                lamp1.setSecondArmOffset(new Vec3(0.0f, 0.0f, 0.0f));
                lamp1.setHeadOffset(new Vec3(-0.5f, 0.0f, 0.0f));
                lightPosition = new Vec3(lamp1.getPosition().x, lamp1.getPosition().y, lamp1.getPosition().z);
                lightPosition.y += 3.7f;
                lightPosition.x += 0.4f;
                lamp1.setLightPosition(lightPosition);
                lamp1.setLightRotation(new Vec3(0.0f, 0.0f, 0.0f));
                lamp1.getLight().setState(1);
                break;
            case 2:
                lamp1.setBaseRotation(lamp1Pos3Rotation[0]);
                lamp1.setFirstArmRotation(lamp1Pos3Rotation[1]);
                lamp1.setSecondArmRotation(lamp1Pos3Rotation[2]);
                lamp1.setHeadRotation(lamp1Pos3Rotation[3]);
                lamp1.setFirstArmOffset(new Vec3(1.0f, -0.1f, 0.0f));
                lamp1.setSecondArmOffset(new Vec3(-0.1f, -0.3f, -0.5f));
                lamp1.setHeadOffset(new Vec3(-2.0f, 0.0f, -0.7f));
                lightPosition = new Vec3(lamp1.getPosition().x, lamp1.getPosition().y, lamp1.getPosition().z);
                lightPosition.y += 3.3f;
                lightPosition.x -= 0.7f;
                lightPosition.z -= 1.2f;
                lamp1.setLightPosition(lightPosition);
                lamp1.setLightRotation(new Vec3(15.0f, -10.0f, 20.0f));
                lamp1.getLight().setState(2);
                break;
        }
    }

    public void setLamp2Pos(int num) {
        switch (num) {
            case 0:
                lamp2.setBaseRotation(lamp2Pos1Rotation[0]);
                lamp2.setFirstArmRotation(lamp2Pos1Rotation[1]);
                lamp2.setSecondArmRotation(lamp2Pos1Rotation[2]);
                lamp2.setHeadRotation(lamp2Pos1Rotation[3]);
                lamp2.setFirstArmOffset(new Vec3(1.8f, 0.0f, 0.0f));
                lamp2.setSecondArmOffset(new Vec3(0.0f, 0.0f, 0.0f));
                lamp2.setHeadOffset(new Vec3(-2.2f, 0.0f, 0.0f));
                Vec3 lightPosition = new Vec3(lamp2.getPosition().x, lamp2.getPosition().y, lamp2.getPosition().z);
                lightPosition.y += 3.7f;
                lamp2.setLightPosition(lightPosition);
                lamp2.setLightRotation(new Vec3(0.0f, 0.0f, -60.0f));
                lamp2.getLight().setState(0);
                break;
            case 1:
                lamp2.setBaseRotation(lamp2Pos2Rotation[0]);
                lamp2.setFirstArmRotation(lamp2Pos2Rotation[1]);
                lamp2.setSecondArmRotation(lamp2Pos2Rotation[2]);
                lamp2.setHeadRotation(lamp2Pos2Rotation[3]);
                lamp2.setFirstArmOffset(new Vec3(0.7f, 0.0f, 0.0f));
                lamp2.setSecondArmOffset(new Vec3(-0.5f, 0.0f, 0.0f));
                lamp2.setHeadOffset(new Vec3(-1.5f, 0.5f, 0.0f));
                lightPosition = new Vec3(lamp2.getPosition().x, lamp2.getPosition().y, lamp2.getPosition().z);
                lightPosition.y += 4.2f;
                lightPosition.x -= 0.9f;
                lamp2.setLightPosition(lightPosition);
                lamp2.setLightRotation(new Vec3(0.0f, 0.0f, 0.0f));
                lamp2.getLight().setState(1);
                break;
            case 2:
                lamp2.setBaseRotation(lamp2Pos3Rotation[0]);
                lamp2.setFirstArmRotation(lamp2Pos3Rotation[1]);
                lamp2.setSecondArmRotation(lamp2Pos3Rotation[2]);
                lamp2.setHeadRotation(lamp2Pos3Rotation[3]);
                lamp2.setFirstArmOffset(new Vec3(1.8f, -1.1f, 0.8f));
                lamp2.setSecondArmOffset(new Vec3(0.0f, -1.6f, 1.4f));
                lamp2.setHeadOffset(new Vec3(-2.0f, -0.7f, 0.2f));
                lightPosition = new Vec3(lamp2.getPosition().x, lamp2.getPosition().y, lamp2.getPosition().z);
                lightPosition.y += 0.3f;
                lightPosition.x += 0.2f;
                lightPosition.z += 2.5f;
                lamp2.setLightPosition(lightPosition);
                lamp2.setLightRotation(new Vec3(0.0f, 45.0f, -5.0f));
                lamp2.getLight().setState(2);
                break;
        }
    }

    private void loadTextures(GL3 gl) {
        texture_wall = TextureLibrary.loadTexture(gl, "resources/textures/wood_wall_color.png");
        texture_wall_specular = TextureLibrary.loadTexture(gl, "resources/textures/wood_wall_specular.jpg");
        texture_floor = TextureLibrary.loadTexture(gl, "resources/textures/wood_floor.png");
        texture_egg_albedo = TextureLibrary.loadTexture(gl, "resources/textures/Egg_Texture.png");
        texture_egg_specular = TextureLibrary.loadTexture(gl, "resources/textures/specular_egg.jpg");
    }

    private Model makeWall0(GL3 gl) {
        Vec3 baseColor = new Vec3(0.5f, 0.5f, 0.5f);
        Material material = new Material(baseColor, baseColor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
        //create floor
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, "shaders/vs_tt_05.txt", "shaders/fs_tt_05.txt");
        return new Model(gl, camera, light, skybox, shader, material, modelMatrix, mesh, texture_floor, lamp1.getLight(), lamp2.getLight());
    }

    private Model makeWall1(GL3 gl) {
        Vec3 baseColor = new Vec3(0.5f, 0.5f, 0.5f);
        Material material = new Material(baseColor, baseColor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
        // back wall
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(-90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(size * 0.5f, size * 0.5f, 0), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, "shaders/vs_tt_05.txt", "shaders/fs_tt_05.txt");
        return new Model(gl, camera, light, skybox, shader, material, modelMatrix, mesh, texture_wall, texture_wall_specular, lamp1.getLight(), lamp2.getLight());
    }

    private Model makeWall2(GL3 gl) {
        Vec3 baseColor = new Vec3(0.5f, 0.5f, 0.5f);
        Material material = new Material(baseColor, baseColor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
        // side wall
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(180), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(-size * 0.5f, size * 0.5f, 0), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, "shaders/vs_tt_05.txt", "shaders/fs_tt_05.txt");
        return new Model(gl, camera, light, skybox, shader, material, modelMatrix, mesh, texture_wall, texture_wall_specular, lamp1.getLight(), lamp2.getLight());
    }

    //This function, while copied from the previous ones, is from my own doing
    private Model makeWall3(GL3 gl) {
        Vec3 baseColor = new Vec3(0.5f, 0.5f, 0.5f);
        Material material = new Material(baseColor, baseColor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
        // forward wall
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(180), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
        //modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(0, size * 0.5f, size * 0.5f), modelMatrix);
        Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
        Shader shader = new Shader(gl, "shaders/vs_tt_05.txt", "shaders/fs_tt_05.txt");
        return new Model(gl, camera, light, skybox, shader, material, modelMatrix, mesh, texture_wall, texture_wall_specular, lamp1.getLight(), lamp2.getLight());
    }

    private EggModel makeEgg(GL3 gl) {
        Mesh eggModel = new Mesh(gl, Egg.vertices.clone(), Egg.indices.clone());
        Shader shader = new Shader(gl, "shaders/vs_sphere_04.txt", "shaders/fs_sphere_04.txt");

        Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(2, 2, 2), Mat4Transform.translate(-0.6f, 0.0f, 0.0f));
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(0, 4, 0), modelMatrix);

        return new EggModel(gl, camera, light, skybox, shader, material, modelMatrix, eggModel, texture_egg_albedo, texture_egg_specular, lamp1.getLight(), lamp2.getLight());
    }

    public void setEggPosition(Vec3 pos) {
        egg.setPosition(pos);
    }

    public void render(GL3 gl) {
        for (Model model : wall) {
            model.render(gl);
        }
        egg.render(gl);
        lamp1.render(gl);
        lamp2.render(gl);
    }

    public void dispose(GL3 gl) {
        for (Model model : wall) {
            model.dispose(gl);
        }
        egg.dispose(gl);
        lamp1.dispose(gl);
        lamp2.dispose(gl);
    }
}

//Table class is all mine
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