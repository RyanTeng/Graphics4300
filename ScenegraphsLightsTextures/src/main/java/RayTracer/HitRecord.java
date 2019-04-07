package RayTracer;

import util.TextureImage;

public class HitRecord {
    public float t, ambient, diffuse, specular, shininess;
    public Vector intersect;
    public Vector normal;
    public Vector texCoords;
    public TextureImage texImg;

    public HitRecord(float t, float ambient, float diffuse, float specular, float shininess, Vector intersect, Vector normal, Vector texCoords, TextureImage texImg) {
        t = t;
        ambient = ambient;
        diffuse = diffuse;
        specular = specular;
        shininess = shininess;
        intersect = intersect;
        normal = normal;
        texCoords = texCoords;
        texImg = texImg;
    }
}
