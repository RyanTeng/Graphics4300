import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Material;
import util.ObjImporter;
import util.ObjectInstance;
import util.PolygonMesh;


import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 * <p>
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {
    private int WINDOW_WIDTH, WINDOW_HEIGHT;
    private Stack<Matrix4f> modelView;
    private Matrix4f projection;
    private float trackballRadius;
    private float camX;
    private float camY;
    private float camZ;
    private float camXSpeed;
    private float camYSpeed;
    private float camZSpeed;
    private float thetaX;
    private float thetaXSpeed;
    private float thetaY;
    private float thetaYSpeed;
    private float thetaZ;
    private float thetaZSpeed;
    private char lastKeyPressed;
    private int toggle;

    private util.ShaderProgram program;
    private util.ShaderLocationsVault shaderLocations;
    private int projectionLocation;
    private sgraph.IScenegraph<VertexAttrib> scenegraph;
    private util.ObjectInstance camera;


    public View() {
        projection = new Matrix4f();
        modelView = new Stack<>();
        trackballRadius = 100;
        scenegraph = null;
        camX = 0;
        camY = 0;
        camZ = 100;
        camXSpeed = 0;
        camYSpeed = 0;
        camZSpeed = 0;
        thetaX = (float)-Math.PI/2;
        thetaXSpeed = 0;
        thetaY = (float)-Math.PI/2;
        thetaYSpeed = 0;
        thetaZ = (float)Math.PI/2;
        thetaZSpeed = 0;
        toggle = 0;
    }

    public void initScenegraph(GLAutoDrawable gla, InputStream in) throws Exception {
        GL3 gl = gla.getGL().getGL3();

        if (scenegraph != null)
            scenegraph.dispose();

        program.enable(gl);

        scenegraph = sgraph.SceneXMLReader.importScenegraph(in
                , new VertexAttribProducer());

        sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
        renderer.setContext(gla);
        Map<String, String> shaderVarsToVertexAttribs = new HashMap<String, String>();
        shaderVarsToVertexAttribs.put("vPosition", "position");
        shaderVarsToVertexAttribs.put("vNormal", "normal");
        shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
        renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
        scenegraph.setRenderer(renderer);
        program.disable(gl);
    }

    public void init(GLAutoDrawable gla) throws Exception {
        GL3 gl = gla.getGL().getGL3();


        //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
        program = new util.ShaderProgram();

        program.createProgram(gl, "shaders/default.vert", "shaders/default"
                + ".frag");

        shaderLocations = program.getAllShaderVariables(gl);

        //get input variables that need to be given to the shader program
        projectionLocation = shaderLocations.getLocation("projection");

        camera = readObj(gl, "src/main/resources/models/Digi_Cam.obj");
    }

    @SuppressWarnings("Duplicates")
    public void draw(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();

        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(gl.GL_DEPTH_TEST);

        program.enable(gl);

        while (!modelView.empty()) {
            modelView.pop();
        }

        modelView.push(new Matrix4f());

        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));

        camX += camXSpeed;
        camY += camYSpeed;
        camZ += camZSpeed;
        thetaX += thetaXSpeed / trackballRadius;
        thetaY += thetaYSpeed / trackballRadius;
        thetaZ += thetaZSpeed / trackballRadius;

        if (toggle % 2 == 1) {
            modelView.peek().lookAt(new Vector3f(0, 100, 100), new Vector3f(0, 50, 0), new Vector3f(0, 1, 0));
            gl.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            scenegraph.draw(modelView);
            gl.glFlush();

            modelView.peek().lookAt(new Vector3f(camX, camY, camZ),
                    new Vector3f(camX + (float) Math.cos(thetaX), camY + (float) Math.cos(thetaY), camZ + (float) Math.sin(thetaX) + (float) Math.sin(thetaY)),
                    new Vector3f((float) Math.cos(thetaZ), (float) Math.sin(thetaZ), 0));
            drawCamera(gla, gl);

            gl.glViewport(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
            scenegraph.draw(modelView);
            gl.glFlush();

        }
        else {
            modelView.peek().lookAt(new Vector3f(camX, camY, camZ),
                    new Vector3f(camX + (float) Math.cos(thetaX), camY + (float) Math.cos(thetaY), camZ + (float) Math.sin(thetaX) + (float) Math.sin(thetaY)),
                    new Vector3f((float) Math.cos(thetaZ), (float) Math.sin(thetaZ), 0));
            gl.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            scenegraph.draw(modelView);
            gl.glFlush();

            modelView.peek().lookAt(new Vector3f(0, 100, 100), new Vector3f(0, 50, 0), new Vector3f(0, 1, 0));
            gl.glViewport(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
            scenegraph.draw(modelView);
            gl.glFlush();
        }

        program.disable(gl);


    }

    private void drawCamera(GLAutoDrawable gla, GL3 gl) {
        FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
        FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);
        Matrix4f camMat = new Matrix4f();
        Material mat = new Material();
        camMat.lookAt(new Vector3f(camX, camY, camZ),
                new Vector3f(camX + (float) Math.cos(thetaX), camY + (float) Math.cos(thetaY), camZ + (float) Math.sin(thetaX) + (float) Math.sin(thetaY)),
                new Vector3f((float) Math.cos(thetaZ), (float) Math.sin(thetaZ), 0));

        camMat.scale(20,20f,20f);

        mat.setAmbient(0.5f, 0.5f, 0.5f);
        mat.setDiffuse(1, 1, 1);
        mat.setSpecular(1f, 1f, 1f);

        //pass the projection matrix to the shader
        gl.glUniformMatrix4fv(
                shaderLocations.getLocation("projection"),
                1, false, projection.get(fb16));

        //pass the modelview matrix to the shader
        gl.glUniformMatrix4fv(
                shaderLocations.getLocation("modelview"),
                1, false, camMat.get(fb16));

        //send the color of the triangle
        gl.glUniform4fv(
                shaderLocations.getLocation("vColor")
                , 1, mat.getAmbient().get(fb4));

        camera.draw(gla);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == ' ') {
            toggle++;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                camXSpeed = 1;
                break;
            case KeyEvent.VK_LEFT:
                camXSpeed = -1;
                break;
            case KeyEvent.VK_UP:
                camYSpeed = 1;
                break;
            case KeyEvent.VK_DOWN:
                camYSpeed = -1;
                break;
            case KeyEvent.VK_A:
                thetaXSpeed = -1;
                break;
            case KeyEvent.VK_D:
                thetaXSpeed = 1;
                break;
            case KeyEvent.VK_W:
                thetaYSpeed = 1;
                break;
            case KeyEvent.VK_S:
                thetaYSpeed = -1;
                break;
            case KeyEvent.VK_F:
                thetaZSpeed = -1;
                break;
            case KeyEvent.VK_C:
                thetaZSpeed = 1;
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                camXSpeed = 0;
                break;
            case KeyEvent.VK_LEFT:
                camXSpeed = 0;
                break;
            case KeyEvent.VK_UP:
                camYSpeed = 0;
                break;
            case KeyEvent.VK_DOWN:
                camYSpeed = 0;
                break;
            case KeyEvent.VK_A:
                thetaXSpeed = 0;
                break;
            case KeyEvent.VK_D:
                thetaXSpeed = 0;
                break;
            case KeyEvent.VK_W:
                thetaYSpeed = 0;
                break;
            case KeyEvent.VK_S:
                thetaYSpeed = 0;
                break;
            case KeyEvent.VK_F:
                thetaZSpeed = 0;
                break;
            case KeyEvent.VK_C:
                thetaZSpeed = 0;
                break;
        }
    }

    private util.ObjectInstance readObj(GL3 gl, String file) throws FileNotFoundException {
        InputStream in;
        PolygonMesh tmesh;
        in = new FileInputStream(file);

        tmesh = ObjImporter.importFile(new VertexAttribProducer(), in, true);

        Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

        shaderToVertexAttribute.put("vPosition", "position");


        return new ObjectInstance(gl,
                program,
                shaderLocations,
                shaderToVertexAttribute,
                tmesh, new
                String(file));
    }

    public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
        GL gl = gla.getGL();
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
        gl.glViewport(0, 0, width, height);

        projection = new Matrix4f().perspective((float) Math.toRadians(120.0f), (float) width / height, 0.1f, 10000.0f);
        // proj = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

    }

    public void dispose(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();

    }


}
