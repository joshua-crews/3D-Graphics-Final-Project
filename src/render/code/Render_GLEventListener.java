package render.code;

import render.code.gmaths.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;
import render.code.objects.Egg;
import render.code.objects.TableLeg;
import render.code.objects.TableTop;

public class Render_GLEventListener implements GLEventListener {
  
  private static final boolean DISPLAY_SHADERS = false;
  private Camera camera;
    
  /* The constructor is not used to initialise anything */
  public Render_GLEventListener(Camera camera) {
    this.camera = camera;
    this.camera.setPosition(new Vec3(4f,6f,15f));
    this.camera.setTarget(new Vec3(0f,5f,0f));
  }
  
  // ***************************************************
  /*
   * METHODS DEFINED BY GLEventListener
   */

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {   
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); 
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled' so needs to be enabled
    gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
  }
  
  /* Called to indicate the drawing surface has been moved and/or resized  */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    float aspect = (float)width/(float)height;
    camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    table.dispose(gl);
    room.dispose(gl);
    light.dispose(gl);
    skybox.dispose(gl);
  }

  // ***************************************************
  /* THE SCENE
   * Now define all the methods to handle the scene.
   * This will be added to in later examples.
   */

  private Room room;
  private Table table;
  private Light light;
  private SkyboxModel skybox;

  private Texture[] texture;   // array of textures
  
  private final int T_CONTAINER_DIFFUSE = 0;
  private final int T_CONTAINER_SPECULAR = 1;

  private void loadTextures(GL3 gl) {
    texture = new Texture[2];
    texture[T_CONTAINER_DIFFUSE] = TextureLibrary.loadTexture(gl, "src/resources/textures/container2.jpg");
    texture[T_CONTAINER_SPECULAR] = TextureLibrary.loadTexture(gl, "src/resources/textures/container2_specular.jpg");
  }

  public void initialise(GL3 gl) {
    loadTextures(gl);
    light = new Light(gl);
    light.setCamera(camera);
    skybox = new SkyboxModel(gl);
    skybox.setCamera(camera);
    Vec3 basecolor = new Vec3(0.5f, 0.5f, 0.5f);
    Material material = new Material(basecolor, basecolor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
    room = new Room(gl, camera, light, skybox);
    table = new Table(gl, camera, light, skybox, texture[T_CONTAINER_DIFFUSE], texture[T_CONTAINER_SPECULAR]);
  }
  
  public void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    light.setPosition(getLightPosition());  // changing light position each frame
    light.render(gl);
    skybox.setPosition(getSkyboxPosition());
    skybox.render(gl);

    table.setModelMatrix(getModelMatrix(0));
    table.render(gl);
    
    room.render(gl);
  }
  
  // The light's position is continually being changed, so needs to be calculated for each frame.
  private Vec3 getLightPosition() {
    double elapsedTime = getSeconds()-startTime;
    float x = 5.0f*(float)(Math.sin(Math.toRadians(elapsedTime*50)));
    float y = 6.0f;
    float z = 5.0f*(float)(Math.cos(Math.toRadians(elapsedTime*50)));
    return new Vec3(x,y,z);
  }

  private Vec3 getSkyboxPosition() {
    double elapsedTime = getSeconds()-startTime;
    float x = 0.0f;
    float y = (2.0f*(float)(Math.cos(Math.toRadians(elapsedTime*50))))+5.0f;
    float z = 0.0f;
    return new Vec3(x,y,z);
  }

  // This method is used to set a random position for each container 
  // and a rotation based on the elapsed time.
  private Mat4 getModelMatrix(int i) {
    Mat4 m = new Mat4(1);
    m = Mat4.multiply(m, Mat4Transform.translate(0,4,0));
    return m;
  }
  
    // ***************************************************
  /* TIME
   */ 
  
  private double startTime;
  
  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }
  
}

// I've used an inner class here. A separate class would be better.

class Room {

  private Model[] wall;
  private Camera camera;
  private Light light;
  private SkyboxModel skybox;
  private Texture t0,t1;
  private float size = 16f;
  private Texture texture_granite;
  private Texture texture_wall;
  private Texture texture_egg_albedo;
  private Texture texture_egg_specular;
  private Model egg;

  public Room(GL3 gl, Camera c, Light l, SkyboxModel s) {
    camera = c;
    light = l;
    skybox = s;
    this.t0 = t0;
    this.t1 = t1;
    loadTextures(gl);
    wall = new Model[4];
    wall[0] = makeWall0(gl);
    wall[1] = makeWall1(gl);
    wall[2] = makeWall2(gl);
    wall[3] = makeWall3(gl);
    egg = makeEgg(gl);
  }

  private void loadTextures(GL3 gl) {
    texture_wall = TextureLibrary.loadTexture(gl, "src/resources/textures/wall.jpg");
    texture_granite = TextureLibrary.loadTexture(gl, "src/resources/textures/granite.jpg");
    texture_egg_albedo = TextureLibrary.loadTexture(gl, "src/resources/textures/Egg_Texture.png");
    texture_egg_specular = TextureLibrary.loadTexture(gl, "src/resources/textures/specular_egg.jpg");
  }
 
  private Model makeWall0(GL3 gl) {
    // grey basecolor with main colour given by texture map
    Vec3 basecolor = new Vec3(0.5f, 0.5f, 0.5f); // grey
    Material material = new Material(basecolor, basecolor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
    //create floor
    Mat4 modelMatrix = new Mat4(1);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "src/render/code/shaders/vs_tt_05.txt", "src/render/code/shaders/fs_tt_05.txt");
    return new Model(gl, camera, light, skybox, shader, material, modelMatrix, mesh, texture_granite);
  }

  private Model makeWall1(GL3 gl) {
    // grey basecolor with main colour given by texture map
    Vec3 basecolor = new Vec3(0.5f, 0.5f, 0.5f); // grey
    Material material = new Material(basecolor, basecolor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
    // back wall
    Mat4 modelMatrix = new Mat4(1);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(-90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(size*0.5f,size*0.5f,0), modelMatrix);
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "src/render/code/shaders/vs_tt_05.txt", "src/render/code/shaders/fs_tt_05.txt");
    return new Model(gl, camera, light, skybox, shader , material, modelMatrix, mesh, texture_wall);
  }

  private Model makeWall2(GL3 gl) {
    Vec3 baseColor = new Vec3(0.5f, 0.5f, 0.5f);
    Material material = new Material(baseColor, baseColor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
    // side wall
    Mat4 modelMatrix = new Mat4(1);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(180), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-size*0.5f,size*0.5f,0), modelMatrix);
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "src/render/code/shaders/vs_tt_05.txt", "src/render/code/shaders/fs_tt_05.txt");
    return new Model(gl, camera, light, skybox, shader, material, modelMatrix, mesh, texture_wall);
  }

  private Model makeWall3(GL3 gl) {
    Vec3 baseColor = new Vec3(0.5f, 0.5f, 0.5f);
    Material material = new Material(baseColor, baseColor, new Vec3(0.3f, 0.3f, 0.3f), 4.0f);
    // forward wall
    Mat4 modelMatrix = new Mat4(1);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(size,1f,size), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(180), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
    //modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(0,size*0.5f,size*0.5f), modelMatrix);
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "src/render/code/shaders/vs_tt_05.txt", "src/render/code/shaders/fs_tt_05.txt");
    return new Model(gl, camera, light, skybox, shader, material, modelMatrix, mesh, texture_wall);
  }

  private Model makeEgg(GL3 gl) {
    Mesh eggModel = new Mesh(gl, Egg.vertices.clone(), Egg.indices.clone());
    Shader shader = new Shader(gl, "src/render/code/shaders/vs_sphere_04.txt", "src/render/code/shaders/fs_sphere_04.txt");

    Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(2,2,2), Mat4Transform.translate(-0.6f,0.0f,0.0f));
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(0,4,0), modelMatrix);

    return new Model(gl, camera, light, skybox, shader, material, modelMatrix, eggModel, texture_egg_albedo, texture_egg_specular);
  }

  public void render(GL3 gl) {
    for (Model model : wall) {
      model.render(gl);
    }
    skybox.render(gl);
    egg.render(gl);
  }

  public void dispose(GL3 gl) {
    for (Model model : wall) {
      model.dispose(gl);
    }
    skybox.render(gl);
    egg.render(gl);
  }
}

class Table {

  private Model tableTop;

  private Model[] tableLegs;

  public Table(GL3 gl, Camera camera, Light light, SkyboxModel skybox, Texture t_diffuse, Texture t_specular) {
    //Tabletop
    Mesh mesh = new Mesh(gl, TableTop.vertices.clone(), TableTop.indices.clone());
    Shader shader = new Shader(gl, "src/render/code/shaders/vs_cube_04.txt", "src/render/code/shaders/fs_cube_04.txt");
    Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    // diffuse and specular textures
    tableTop = new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular);

    //Table leg
    mesh = new Mesh(gl, TableLeg.vertices.clone(), TableLeg.indices.clone());
    tableLegs = new Model[]{new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular),
                            new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular),
                            new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular),
                            new Model(gl, camera, light, skybox, shader, material, new Mat4(1), mesh, t_diffuse, t_specular)};
  }

  public void setModelMatrix(Mat4 m) {
    tableTop.setModelMatrix(m);
    m = Mat4.multiply(m, Mat4Transform.translate(0,-2,0));
    for (int i = 0; i < tableLegs.length; i++) {
      final float distanceValue = 1.75f;
      switch (i) {
        case 0:
          m = Mat4.multiply(m, Mat4Transform.translate(distanceValue,0,distanceValue));
          tableLegs[i].setModelMatrix(m);
          m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue,0,-distanceValue));
          break;
        case 1:
          m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue,0,distanceValue));
          tableLegs[i].setModelMatrix(m);
          m = Mat4.multiply(m, Mat4Transform.translate(distanceValue,0,-distanceValue));
          break;
        case 2:
          m = Mat4.multiply(m, Mat4Transform.translate(distanceValue,0,-distanceValue));
          tableLegs[i].setModelMatrix(m);
          m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue,0,distanceValue));
          break;
        case 3:
          m = Mat4.multiply(m, Mat4Transform.translate(-distanceValue,0,-distanceValue));
          tableLegs[i].setModelMatrix(m);
          m = Mat4.multiply(m, Mat4Transform.translate(distanceValue,0,distanceValue));
          break;
      }

    }
  }

  public void render(GL3 gl) {
    tableTop.render(gl);
    for (Model tableLeg: tableLegs) {
      tableLeg.render(gl);
    }
  }

  public void dispose(GL3 gl) {
    tableTop.dispose(gl);
    for (Model tableLeg: tableLegs) {
      tableLeg.dispose(gl);
    }
  }
}