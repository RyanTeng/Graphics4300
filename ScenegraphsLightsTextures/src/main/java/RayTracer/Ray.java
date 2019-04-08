package RayTracer;

import org.joml.Vector3f;

public class Ray {
    public Vector3f posn;
    public Vector3f direction;

    public Ray(float x, float y, float z, float xdir, float ydir, float zdir) {
        posn = new Vector3f(x, y, z);
        direction = new Vector3f(xdir, ydir, zdir);
    }
    public Ray(Vector3f posn, Vector3f dir) {
        this.posn = posn;
        this.direction = dir.normalize();
    }



}
