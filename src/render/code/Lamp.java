package render.code;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import render.code.gmaths.Mat4;
import render.code.gmaths.Mat4Transform;
import render.code.gmaths.Vec3;
import render.code.objects.LampArm;
import render.code.objects.LampBase;
import render.code.objects.LampHead;

public class Lamp {
    private LampLight light;
    private Light sceneLight;
    private Camera camera;
    private SkyboxModel skybox;
    private Texture texture_lamp_albeido;
    private Texture texture_lamp_specular;

    private Vec3 position;
    private Vec3 baseRotation = new Vec3(0f, 0f, 0f);
    private Vec3 firstArmRotation = new Vec3( 0f, 0f, 30f);
    private Vec3 secondArmRotation = new Vec3(0f, 0f, -60f);
    private Vec3 headRotation = new Vec3(0f, 0f, 0f);
    private Vec3 firstArmOffset = new Vec3(0f, 0f, 0f);
    private Vec3 secondArmOffset = new Vec3(0f, 0f, 0f);
    private Vec3 headOffset = new Vec3(0f, 0f, 0f);

    private LampWithHead lampWithHead;

    private final Material material = new Material(new Vec3(1.0f, 1.0f, 1.0f), new Vec3(1.0f, 1.0f, 1.0f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);

    public Lamp (GL3 gl, Camera c, Light l, SkyboxModel s, Vec3 pos) {
        camera = c;
        sceneLight = l;
        light = new LampLight(gl);
        light.setCamera(camera);
        skybox = s;
        position = pos;
        loadTextures(gl);
        lampWithHead = new LampWithHead(gl);
        pos.y = pos.y + 3.7f;
        pos.x = pos.x + 0.4f;
        light.setPosition(pos);
    }

    private void loadTextures(GL3 gl) {
        texture_lamp_albeido = TextureLibrary.loadTexture(gl, "src/resources/textures/Lamp_Color.jpg");
        texture_lamp_specular = TextureLibrary.loadTexture(gl, "src/resources/textures/Lamp_Specular.jpg");
    }

    public void render(GL3 gl) {
        lampWithHead.render(gl);
        light.render(gl);
    }

    public void dispose(GL3 gl) {
        lampWithHead.dispose(gl);
        light.dispose(gl);
    }

    public void setBaseRotation(Vec3 rot) {
        baseRotation = rot;
    }

    public void setFirstArmRotation(Vec3 rot) {
        firstArmRotation = rot;
    }

    public void setSecondArmRotation(Vec3 rot) {
        secondArmRotation = rot;
    }

    public void setHeadRotation(Vec3 rot) {
        headRotation = rot;
    }

    public void setFirstArmOffset(Vec3 offset) {
        firstArmOffset = offset;
    }

    public void setSecondArmOffset(Vec3 offset) {
        secondArmOffset = offset;
    }

    public void setHeadOffset(Vec3 offset) {
        headOffset = offset;
    }

    class LampWithHead {
        private LampAllArms base;

        private Model head;

        private LampWithHead (GL3 gl) {
            base = new LampAllArms(gl);
            head = makeLampHead(gl);
        }

        private Model makeLampHead(GL3 gl) {
            Mesh lampBaseModel = new Mesh(gl, LampHead.vertices.clone(), LampHead.indices.clone());
            Shader shader = new Shader(gl, "src/render/code/shaders/vs_sphere_04.txt", "src/render/code/shaders/fs_sphere_04.txt");

            Mat4 modelMatrix = makeModelMatrix();

            return new Model(gl, camera, sceneLight, skybox, shader, material, modelMatrix, lampBaseModel, texture_lamp_albeido, texture_lamp_specular);
        }

        private Mat4 makeModelMatrix() {
            Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(1,1,1), Mat4Transform.translate(0.0f,0.0f,0.0f));
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(headRotation.x), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(headRotation.y), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(headRotation.z), modelMatrix);
            modelMatrix = Mat4.multiply(base.getModelMatrix(), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.translate(1.2f, 0.9f, 0.0f), modelMatrix);
            return Mat4.multiply(Mat4Transform.translate(headOffset), modelMatrix);
        }

        public void render(GL3 gl) {
            base.render(gl);
            head.setModelMatrix(makeModelMatrix());
            head.render(gl, makeModelMatrix());
        }

        public void dispose(GL3 gl) {
            base.dispose(gl);
            head.render(gl);
        }
    }

    class LampAllArms {
        private LampBaseFirstArm base;

        private Model arm;

        private LampAllArms (GL3 gl) {
            base = new LampBaseFirstArm(gl);
            arm = makeLampArm(gl);
        }

        private Model makeLampArm(GL3 gl) {
            Mesh lampBaseModel = new Mesh(gl, LampArm.vertices.clone(), LampArm.indices.clone());
            Shader shader = new Shader(gl, "src/render/code/shaders/vs_sphere_04.txt", "src/render/code/shaders/fs_sphere_04.txt");

            Mat4 modelMatrix = makeModelMatrix();

            return new Model(gl, camera, sceneLight, skybox, shader, material, modelMatrix, lampBaseModel, texture_lamp_albeido, texture_lamp_specular);
        }

        public Mat4 getModelMatrix() {
            return arm.getModelMatrix();
        }

        private Mat4 makeModelMatrix() {
            Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(1,1,1), Mat4Transform.translate(0.0f,0.0f,0.0f));
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(secondArmRotation.x), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(secondArmRotation.y), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(secondArmRotation.z), modelMatrix);
            modelMatrix = Mat4.multiply(base.getModelMatrix(), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.translate(0.0f, 1.7f, 0.0f), modelMatrix);
            return Mat4.multiply(Mat4Transform.translate(secondArmOffset), modelMatrix);
        }

        public void render(GL3 gl) {
            base.render(gl);
            arm.setModelMatrix(makeModelMatrix());
            arm.render(gl, makeModelMatrix());
        }

        public void dispose(GL3 gl) {
            base.dispose(gl);
            arm.render(gl);
        }
    }

    class LampBaseFirstArm {

        private LampBaseStart base;
        private Model arm;

        private LampBaseFirstArm (GL3 gl) {
            base = new LampBaseStart(gl);
            arm = makeLampArm(gl);
        }

        private Model makeLampArm(GL3 gl) {
            Mesh lampBaseModel = new Mesh(gl, LampArm.vertices.clone(), LampArm.indices.clone());
            Shader shader = new Shader(gl, "src/render/code/shaders/vs_sphere_04.txt", "src/render/code/shaders/fs_sphere_04.txt");

            Mat4 modelMatrix = makeModelMatrix();

            return new Model(gl, camera, sceneLight, skybox, shader, material, modelMatrix, lampBaseModel, texture_lamp_albeido, texture_lamp_specular);
        }

        public Mat4 getModelMatrix() {
            return arm.getModelMatrix();
        }

        private Mat4 makeModelMatrix() {
            Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(1,1,1), Mat4Transform.translate(0.0f,0.0f,0.0f));
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(firstArmRotation.x), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(firstArmRotation.y), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(firstArmRotation.z), modelMatrix);
            modelMatrix = Mat4.multiply(base.getModelMatrix(), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.translate(-0.8f, 1.1f, 0.0f), modelMatrix);
            return Mat4.multiply(Mat4Transform.translate(firstArmOffset), modelMatrix);
        }

        public void render(GL3 gl) {
            base.render(gl);
            arm.setModelMatrix(makeModelMatrix());
            arm.render(gl, makeModelMatrix());
        }

        public void dispose(GL3 gl) {
            base.dispose(gl);
            arm.render(gl);
        }
    }

    class LampBaseStart {

        private Model lampBase;

        private LampBaseStart (GL3 gl) {
            lampBase = makeLampBase(gl);
        }

        private Model makeLampBase(GL3 gl) {
            Mesh lampBaseModel = new Mesh(gl, LampBase.vertices.clone(), LampBase.indices.clone());
            Shader shader = new Shader(gl, "src/render/code/shaders/vs_tt_05.txt", "src/render/code/shaders/fs_tt_05.txt");

            Mat4 modelMatrix = makeModelMatrix();

            return new Model(gl, camera, sceneLight, skybox, shader, material, modelMatrix, lampBaseModel, texture_lamp_albeido, texture_lamp_specular);
        }

        private Mat4 makeModelMatrix() {
            Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(1,1,1), Mat4Transform.translate(0.0f,0.0f,0.0f));
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(baseRotation.x), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(baseRotation.y), modelMatrix);
            modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(baseRotation.z), modelMatrix);
            return Mat4.multiply(Mat4Transform.translate(position), modelMatrix);
        }

        public Mat4 getModelMatrix() {
            return lampBase.getModelMatrix();
        }

        public void render(GL3 gl) {
            lampBase.setModelMatrix(makeModelMatrix());
            lampBase.render(gl, makeModelMatrix());
        }

        public void dispose(GL3 gl) {
            lampBase.dispose(gl);
        }
    }
}
