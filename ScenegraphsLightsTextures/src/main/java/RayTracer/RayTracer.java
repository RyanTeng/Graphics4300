package RayTracer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
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
            color = shade(ray, record, scene.getLights(mv));
        }
        return color;
    }

    private boolean closestIntersect(Ray ray, Stack<Matrix4f> mv, IScenegraph scene, HitRecord record) {
        scene.getRoot().closestIntersect(ray, record);
        return record.t != 0;
    }

    private Color shade(Ray ray, HitRecord record, ArrayList<Light> lights) {
        Vector4f lightVec,viewVec,reflectVec;
        Vector4f normalView;
        Vector4f ambient,diffuse,specular;
        float nDotL,rDotV;
        Vector4f resultant = new Vector4f();
        for (int i=0;i<lights.size();i++)
        {
            if (lights.get(i).getPosition().w!=0)
                lightVec = lights.get(i).getPosition().normalize().sub(record.intersect);
            else
                lightVec = lights.get(i).getPosition().normalize().mul(-1);

            Vector4f spotdirection = (lights.get(i).getSpotDirection().normalize());
            if ((-1 * lightVec.dot(spotdirection))<lights.get(i).getSpotCutoff())
                continue;

            Vector4f tNormal = record.normal;
            normalView = tNormal.normalize();


            nDotL = normalView.dot(lightVec);

            viewVec = record.intersect.mul(-1);
            viewVec = viewVec.normalize();

            reflectVec = lightVec.mul(-1).sub(normalView.mul(2 * lightVec.mul(-1).dot(normalView.normalize())));
            reflectVec = reflectVec.normalize();

            rDotV = Float.max((reflectVec.dot(viewVec)),0.0f);

            ambient = record.ambient.mul(new Vector4f(lights.get(i).getAmbient(),0));
            diffuse = record.diffuse.mul(new Vector4f(lights.get(i).getDiffuse(), 0).mul(Float.max(nDotL,0)));
            if (nDotL>0)
                specular = record.specular.mul(new Vector4f(lights.get(i).getSpecular(), 0).mul((float)Math.pow(rDotV, record.shininess)));
            else
                specular = new Vector4f(0,0,0, 0);
           resultant = ambient.add(diffuse).add(specular);
        }
        return new Color(resultant.x, resultant.y, resultant.z);
    }
}
