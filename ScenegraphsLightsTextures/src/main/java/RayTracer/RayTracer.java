package RayTracer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import sgraph.INode;
import sgraph.IScenegraph;
import util.Light;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Stack;

public class RayTracer {
    public BufferedImage raytrace(int width, int height, Stack<Matrix4f> modelview, Vector3f camPos, IScenegraph scenegraph) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Vector3f posn = camPos;
                Vector3f dir = new Vector3f(x, y, -camPos.z);
                Ray ray = new Ray(posn, dir);

                Color color = raycast(ray, modelview, scenegraph);
                img.setRGB(x, y, color.getRGB());
            }
        }
        return img;
    }

    private Color raycast(Ray ray, Stack<Matrix4f> mv, IScenegraph scene) {
        HitRecord record = new HitRecord();
        boolean hit = closestIntersect(ray, mv, scene, record);
        Color color = Color.BLACK;
        if (hit) {
            //TODO: write a getLights function which returns all the lights in a scene
            Color color = shade(ray, record, scene.getLights());
        }
        return color;
    }

    private boolean closestIntersect(Ray ray, Stack<Matrix4f> mv, IScenegraph scene, HitRecord record) {
        //TODO: write a function in node that calculates whether or not the ray collides with what is inside the node.

    }

    private Color shade(Ray ray, HitRecord record, ArrayList<Light> lights) {

    }
}
