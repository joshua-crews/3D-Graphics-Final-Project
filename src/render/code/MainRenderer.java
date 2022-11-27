package render.code;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class MainRenderer extends JFrame {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    //The UI height, ensuring it is never over the height to not cause issues
    private static final int GUI_HEIGHT = 100;
    private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);
    private GLCanvas canvas;
    private Render_GLEventListener glEventListener;
    private final FPSAnimator animator;
    private JPanel canvasWindow;

    public static void main(String[] args) {
        if (GUI_HEIGHT >= HEIGHT) {
            System.out.println("The GUI height may not be larger than the window height!");
            System.exit(1);
        }
        MainRenderer b1 = new MainRenderer("3D Graphics Project");
        b1.getContentPane().setPreferredSize(dimension);
        b1.pack();
        b1.setVisible(true);
        b1.canvas.requestFocusInWindow();
    }

    public MainRenderer(String textForTitleBar) {
        super(textForTitleBar);
        canvasWindow = new JPanel(new GridLayout(1, 1, 0, 0));
        Dimension dimension = new Dimension(WIDTH, HEIGHT - GUI_HEIGHT);
        canvasWindow.setMaximumSize(dimension);
        canvasWindow.setMinimumSize(dimension);
        canvasWindow.setPreferredSize(dimension);
        canvasWindow.add(new JLabel("Loading..."));
        JPanel gui = setUpGUI();
        setUpCanvas((JSlider) gui.getComponent(3));
        canvasWindow.add(canvas);
        getContentPane().add(canvasWindow, BorderLayout.PAGE_START);
        getContentPane().add(gui);
        addWindowListener(new windowHandler());
        animator = new FPSAnimator(canvas, 60);
        animator.start();
    }

    public void redrawUI() {
        getContentPane().repaint();
        getContentPane().validate();
    }

    private JPanel setUpGUI() {
        JPanel window = new JPanel(new GridLayout(2, 4, 0, 0));
        Dimension dimension = new Dimension(WIDTH, GUI_HEIGHT);
        window.setMaximumSize(dimension);
        window.setMinimumSize(dimension);
        window.setPreferredSize(dimension);

        JButton lamp1Pos1But = new JButton("Lamp 1, Pos 1");
        JButton lamp1Pos2But = new JButton("Lamp 1, Pos 2");
        JButton lamp1Pos3But = new JButton("Lamp 1, Pos 3");

        lamp1Pos1But.addActionListener(e -> glEventListener.setLamp1Pos(0));
        lamp1Pos2But.addActionListener(e -> glEventListener.setLamp1Pos(1));
        lamp1Pos3But.addActionListener(e -> glEventListener.setLamp1Pos(2));

        window.add(lamp1Pos1But);
        window.add(lamp1Pos2But);
        window.add(lamp1Pos3But);
        JSlider globalLightLevel = new JSlider();
        globalLightLevel.setValue(100);
        window.add(globalLightLevel);

        JButton lamp2Pos1But = new JButton("Lamp 2, Pos 1");
        JButton lamp2Pos2But = new JButton("Lamp 2, Pos 2");
        JButton lamp2Pos3But = new JButton("Lamp 2, Pos 3");

        lamp2Pos1But.addActionListener(e -> glEventListener.setLamp2Pos(0));
        lamp2Pos2But.addActionListener(e -> glEventListener.setLamp2Pos(1));
        lamp2Pos3But.addActionListener(e -> glEventListener.setLamp2Pos(2));

        window.add(lamp2Pos1But);
        window.add(lamp2Pos2But);
        window.add(lamp2Pos3But);
        JPanel panel = new JPanel(new GridLayout(1, 2, 0, 0));
        panel.add(new JButton("Lamp 1 On/Off"));
        panel.add(new JButton("Lamp 2 On/Off"));
        window.add(panel);

        return window;
    }

    private void setUpCanvas(JSlider slider) {
        GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));
        canvas = new GLCanvas(glcapabilities);
        Camera camera = new Camera(Camera.DEFAULT_POSITION,
                Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
        glEventListener = new Render_GLEventListener(camera, canvasWindow, this, slider);
        canvas.addGLEventListener(glEventListener);
        canvas.addMouseMotionListener(new MyMouseInput(camera));
        canvas.addKeyListener(new MyKeyboardInput(camera));
    }

    private class windowHandler extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            animator.stop();
            remove(canvas);
            dispose();
            System.exit(0);
        }
    }
}

class MyKeyboardInput extends KeyAdapter {
    private Camera camera;

    public MyKeyboardInput(Camera camera) {
        this.camera = camera;
    }

    public void keyPressed(KeyEvent e) {
        Camera.Movement m = Camera.Movement.NO_MOVEMENT;
        switch (e.getKeyCode()) {
            //TODO Bad controls given by prof that we have to use, but I don't like them for now
      /*case KeyEvent.VK_LEFT:  m = Camera.Movement.LEFT;  break;
      case KeyEvent.VK_RIGHT: m = Camera.Movement.RIGHT; break;
      case KeyEvent.VK_UP:    m = Camera.Movement.UP;    break;
      case KeyEvent.VK_DOWN:  m = Camera.Movement.DOWN;  break;
      case KeyEvent.VK_A:  m = Camera.Movement.FORWARD;  break;
      case KeyEvent.VK_Z:  m = Camera.Movement.BACK;  break;*/
            case KeyEvent.VK_A:
                m = Camera.Movement.LEFT;
                break;
            case KeyEvent.VK_D:
                m = Camera.Movement.RIGHT;
                break;
            case KeyEvent.VK_Q:
                m = Camera.Movement.UP;
                break;
            case KeyEvent.VK_E:
                m = Camera.Movement.DOWN;
                break;
            case KeyEvent.VK_W:
                m = Camera.Movement.FORWARD;
                break;
            case KeyEvent.VK_S:
                m = Camera.Movement.BACK;
                break;
        }
        camera.keyboardInput(m);
    }
}

class MyMouseInput extends MouseMotionAdapter {
    private Point lastpoint;
    private Camera camera;

    public MyMouseInput(Camera camera) {
        this.camera = camera;
    }

    /**
     * mouse is used to control camera position
     *
     * @param e instance of MouseEvent
     */
    public void mouseDragged(MouseEvent e) {
        Point ms = e.getPoint();
        float sensitivity = 0.001f;
        float dx = (float) (ms.x - lastpoint.x) * sensitivity;
        float dy = (float) (ms.y - lastpoint.y) * sensitivity;
        if (e.getModifiers() == MouseEvent.BUTTON1_MASK)
            camera.updateYawPitch(dx, -dy);
        lastpoint = ms;
    }

    public void mouseMoved(MouseEvent e) {
        lastpoint = e.getPoint();
    }

}