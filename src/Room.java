import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import gmaths.Mat4;
import gmaths.Mat4Transform;
import gmaths.Vec3;
import objects.Egg;

public class Room {

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
        wall = new Model[5];
        wall[0] = makeWall0(gl);
        wall[1] = makeWall1(gl);
        wall[2] = makeWall2(gl);
        wall[3] = makeWall3(gl);
        wall[4] = makeWindowFrame(gl);
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

    private Model makeWindowFrame(GL3 gl) {
        Vec3 baseColor = new Vec3(0.5f, 0.5f, 0.5f);
        Material material = new Material(baseColor, baseColor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
        // forward wall
        Mat4 modelMatrix = new Mat4(1);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(0, size * 0.5f, -(size * 0.5f)), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(1f, 0.05f, 1f), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate(0, size - (float)(size * 0.05), 0), modelMatrix);
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