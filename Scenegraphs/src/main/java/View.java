import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;


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
    private Matrix4f projection, trackballTransform;
    private float trackballRadius;
    private Vector2f mousePos;
    private float camX;
    private float camY;
    private float camZ;
    private float camXSpeed;
    private float camYSpeed;
    private float camZSpeed;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float theta;
    private float thetaSpeed;
    private char lastKeyPressed;


    private util.ShaderProgram program;
    private util.ShaderLocationsVault shaderLocations;
    private int projectionLocation;
    private sgraph.IScenegraph<VertexAttrib> scenegraph;


    public View() {
        projection = new Matrix4f();
        modelView = new Stack<Matrix4f>();
        trackballRadius = 1000;
        trackballTransform = new Matrix4f();
        scenegraph = null;
        camX = 2;
        camY = 2;
        camZ = 100;
        camXSpeed = 0;
        camYSpeed = 0;
        camZSpeed = 0;
        rotX = 0;
        rotY = 0;
        rotZ = 0;
        theta = 0;
        thetaSpeed = 0;
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
    }


    public void draw(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();

        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(gl.GL_DEPTH_TEST);


        program.enable(gl);

        while (!modelView.empty())
            modelView.pop();

        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
        modelView.push(new Matrix4f());
        trackballTransform = new Matrix4f().rotate(theta / trackballRadius, rotX, rotY, rotZ);
        modelView.peek().lookAt(new Vector3f(camX, camY, camZ), new Vector3f(camX, camY, 0), new Vector3f(0, 1, 0))
                .mul(trackballTransform);
        camX += camXSpeed;
        camY += camYSpeed;
        camZ += camZSpeed;
        theta += thetaSpeed;



        /*
         *Supply the shader with all the matrices it expects.
         */
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));
        //return;


        //gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES

        scenegraph.draw(modelView);
        /*
         *OpenGL batch-processes all its OpenGL commands.
         *  *The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
         *
         *This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to
         *finish drawing.
         *
         *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
         */
        gl.glFlush();

        program.disable(gl);


    }

    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
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
                rotY = 1;
                thetaSpeed = -1;
                break;
            case KeyEvent.VK_D:
                rotY = 1;
                thetaSpeed = 1;
                break;
            case KeyEvent.VK_W:
                rotX = 1;
                thetaSpeed = -1;
                break;
            case KeyEvent.VK_S:
                rotX = 1;
                thetaSpeed = 1;
                break;
            case KeyEvent.VK_F:
                rotZ = 1;
                thetaSpeed = -1;
                break;
            case KeyEvent.VK_C:
                rotZ = 1;
                thetaSpeed = 1;
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
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
                thetaSpeed = 0;
                break;
            case KeyEvent.VK_D:
                thetaSpeed = 0;
                break;
            case KeyEvent.VK_W:
                thetaSpeed = 0;
                break;
            case KeyEvent.VK_S:
                thetaSpeed = 0;
                break;
            case KeyEvent.VK_F:
                thetaSpeed = 0;
                break;
            case KeyEvent.VK_C:
                thetaSpeed = 0;
                break;
        }
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
