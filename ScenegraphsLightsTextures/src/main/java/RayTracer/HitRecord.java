package RayTracer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import util.TextureImage;

public class HitRecord {
    public float t, shininess;
    public Vector4f ambient, diffuse, specular,intersect, normal;
    public Vector3f texCoords;
    public TextureImage texImg;

    public HitRecord() {

    }

    public HitRecord(float t, Vector4f ambient, Vector4f diffuse, Vector4f specular, float shininess, Vector4f intersect, Vector4f normal, Vector3f texCoords, TextureImage texImg) {
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
