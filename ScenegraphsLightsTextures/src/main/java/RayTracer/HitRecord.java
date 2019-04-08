package RayTracer;

import org.joml.Vector3f;
import util.TextureImage;

public class HitRecord {
    public float t, shininess;
    public Vector3f intersect, normal, ambient, diffuse, specular;
    public Vector3f texCoords;
    public TextureImage texImg;

    public HitRecord() {

    }

    public HitRecord(float t, Vector3f ambient, Vector3f diffuse, Vector3f specular, float shininess, Vector3f intersect, Vector3f normal, Vector3f texCoords, TextureImage texImg) {
        this.t = t;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
        this.intersect = intersect;
        this.normal = normal;
        this.texCoords = texCoords;
        this.texImg = texImg;
    }
}
