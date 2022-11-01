package render.code;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainRenderer {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);

    public static void main(String[] args) {
        Renderer renderer = new Renderer("My 3D Project");
        renderer.getContentPane().setPreferredSize(dimension);
        renderer.pack();
        renderer.setVisible(true);
        renderer.getCanvas().requestFocusInWindow();
    }


}

class Renderer extends JFrame {

    private GLCanvas canvas;
    private GLEventListener glEventListener;
    private final FPSAnimator animator;

    public Renderer(String title) {
        super(title);
        GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));
        canvas = new GLCanvas(glcapabilities);
        Camera camera = new Camera(Camera.DEFAULT_POSITION, Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
        glEventListener = new Renderer_GLEventListener(camera);
        canvas.addGLEventListener(glEventListener);
        canvas.addMouseMotionListener(new MouseListener(camera));
        canvas.addKeyListener(new KeyboardListener(camera));
        getContentPane().add(canvas, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                animator.stop();
                remove(canvas);
                dispose();
                System.exit(0);
            }
        });
        animator = new FPSAnimator(canvas, 60);
        animator.start();
    }

    public GLCanvas getCanvas () {
        return this.canvas;
    }

    public GLEventListener getGlEventListener() {
        return this.glEventListener;
    }

    public FPSAnimator getAnimator() {
        return this.animator;
    }
}

class KeyboardListener extends KeyAdapter {
    private Camera camera;

    public KeyboardListener(Camera camera) {
        this.camera = camera;
    }

    public void keyPressed(KeyEvent e) {
        Camera.Movement m = Camera.Movement.NO_MOVEMENT;
        switch (e.getKeyCode()) {
            //TODO Bad controls given by prof that we have to use, but I dont like them for now
            /*case KeyEvent.VK_LEFT:  m = Camera.Movement.LEFT;  break;
            case KeyEvent.VK_RIGHT: m = Camera.Movement.RIGHT; break;
            case KeyEvent.VK_UP:    m = Camera.Movement.UP;    break;
            case KeyEvent.VK_DOWN:  m = Camera.Movement.DOWN;  break;
            case KeyEvent.VK_A:  m = Camera.Movement.FORWARD;  break;
            case KeyEvent.VK_Z:  m = Camera.Movement.BACK;  break;*/
            case KeyEvent.VK_A:  m = Camera.Movement.LEFT;  break;
            case KeyEvent.VK_D: m = Camera.Movement.RIGHT; break;
            case KeyEvent.VK_Q:    m = Camera.Movement.UP;    break;
            case KeyEvent.VK_E:  m = Camera.Movement.DOWN;  break;
            case KeyEvent.VK_W:  m = Camera.Movement.FORWARD;  break;
            case KeyEvent.VK_S:  m = Camera.Movement.BACK;  break;
        }
        camera.keyboardInput(m);
    }
}

class MouseListener extends MouseMotionAdapter {

    private Point lastpoint;
    private Camera camera;

    public MouseListener(Camera camera) {
        this.camera = camera;
    }

    public void mouseDragged(MouseEvent e) {
        Point ms = e.getPoint();
        float sensitivity = 0.001f;
        float dx=(float) (ms.x-lastpoint.x)*sensitivity;
        float dy=(float) (ms.y-lastpoint.y)*sensitivity;
        if (e.getModifiers()==MouseEvent.BUTTON1_MASK)
            camera.updateYawPitch(dx, -dy);
        lastpoint = ms;
    }

    public void mouseMoved(MouseEvent e) {
        lastpoint = e.getPoint();
    }
}
