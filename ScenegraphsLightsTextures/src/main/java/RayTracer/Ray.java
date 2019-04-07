package RayTracer;

public class Ray {
    public Vector posn;
    public Vector direction;

    public Ray(float x, float y, float z, float xdir, float ydir, float zdir) {
        posn = new Vector(x, y, z);
        direction = new Vector(xdir, ydir, zdir);
    }

}
