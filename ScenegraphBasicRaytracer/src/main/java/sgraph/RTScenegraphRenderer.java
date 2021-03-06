package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import util.Light;
import util.Material;
import util.PolygonMesh;
import util.TextureImage;

/**
 * Created by ashesh on 4/12/2016.
 */
public class RTScenegraphRenderer implements IScenegraphRenderer {
    private List<Light> lights;
    /**
     * A map to store all the textures
     */
    private Map<String, TextureImage> textures;

    public RTScenegraphRenderer() {
        textures = new TreeMap<String, TextureImage>();
    }

    @Override
    public void setContext(Object obj) throws IllegalArgumentException {
        throw new IllegalArgumentException("Not valid for this renderer");
    }

    @Override
    public void initShaderProgram(util.ShaderProgram shaderProgram, Map<String, String> shaderVarsToVertexAttribs) {
        throw new IllegalArgumentException("Not valid for this renderer");

    }

    @Override
    public int getShaderLocation(String name) {
        throw new IllegalArgumentException("Not valid for this renderer");

    }

    @Override
    public void addMesh(String name, PolygonMesh mesh) throws Exception {

    }

    public void initLightsInShader(List<Light> lights) {
        throw new IllegalArgumentException("Not valid for this renderer");
    }

    @Override
    public void draw(INode root, Stack<Matrix4f> modelView) {
        int i, j;
        int width = 800;
        int height = 800;
        float FOVY = 120.0f;
        Ray rayView = new Ray();

        this.lights = root.getLightsInView(modelView);

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        rayView.start = new Vector4f(0, 0, 0, 1);
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                /*
                 create ray in view coordinates
                 start point: 0,0,0 always!
                 going through near plane pixel (i,j)
                 So 3D location of that pixel in view coordinates is
                 x = i-width/2
                 y = j-height/2
                 z = -0.5*height/tan(FOVY)
                */
                rayView.direction = new Vector4f(i - 0.5f * width,
                        j - 0.5f * height,
                        -0.5f * height / (float) Math.tan(Math.toRadians(0.5 * FOVY)),
                        0.0f);

                float prevRefIdx = 1;
                HitRecord hitA = new HitRecord();
                raycast(rayView, root, modelView, hitA);
                Color colorA = new Color(getRaytracedColor(hitA, root, modelView).getRGB());

                Color colorR = new Color(0, 0, 0);
                if (hitA.material.getReflection() > 0) {
                    Ray reflectRay = new Ray();
                    reflectRay.start = hitA.point;
                    reflectRay.direction = rayView.direction.sub(hitA.normal.mul(2 * (hitA.normal.dot(rayView.direction))));
                    reflectRay.direction.x *= ((0.8f + (float) Math.random() / 5));
                    reflectRay.direction.y *= ((0.8f + (float) Math.random() / 5));
                    reflectRay.direction.z *= ((0.8f + (float) Math.random() / 5));
                    HitRecord hitR = new HitRecord();
                    raycast(reflectRay, root, modelView, hitR);
                    if (hitR.intersected()) {
                        colorR = new Color(getRaytracedColor(hitR, root, modelView).getRGB());
                    }
                }

                Color colorT = new Color(0, 0, 0);
                if (hitA.material.getTransparency() > 0) {
                    Ray refractRay = new Ray();
                    refractRay.start = hitA.point;
                    float thetaI = (float) Math.cos(-hitA.normal.dot(rayView.direction));
                    float refractRatio = hitA.material.getRefractiveIndex() / prevRefIdx;
                    refractRay.direction = rayView.direction.mul(refractRatio)
                            .add(hitA.normal.mul(refractRatio * thetaI - (float)
                                    (Math.cos(Math.sqrt(1 - Math.sin(refractRatio * Math.sin(Math.sqrt(1 - Math.cos(thetaI) * Math.cos(thetaI))))
                                            * Math.sin(refractRatio * Math.sin(Math.sqrt(1 - Math.cos(thetaI) * Math.cos(thetaI)))))))));
                    HitRecord hitT = new HitRecord();
                    raycast(refractRay, root, modelView, hitT);
                    colorT = getRaytracedColor(hitT, root, modelView);

                }
                float absorption = hitA.material.getAbsorption();
                float reflection = hitA.material.getReflection();
                float transparency = hitA.material.getTransparency();

                Color color = blend(colorT, blend(colorA, colorR, absorption / 1f, reflection / 1f), transparency / 1f, absorption + transparency / 1f);

                output.setRGB(i, height - 1 - j, color.getRGB());
            }
        }

        OutputStream outStream = null;

        try {
            outStream = new FileOutputStream("output/raytrace.png");
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not write raytraced image!");
        }

        try {
            ImageIO.write(output, "png", outStream);
            outStream.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write raytraced image!");
        }

    }

    public static Color blend(Color c0, Color c1, float weight0, float weight1) {
        double r = weight0 * c0.getRed() + weight1 * c1.getRed();
        double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
        double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
        double a = Math.max(c0.getAlpha(), c1.getAlpha());

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    private void raycast(Ray rayView, INode root, Stack<Matrix4f> modelView, HitRecord hitRecord) {
        root.intersect(rayView, modelView, hitRecord);
    }

    private Color getRaytracedColor(HitRecord hitRecord, INode root, Stack<Matrix4f> modelview) {
        if (hitRecord.intersected())
            return shade(hitRecord.point, hitRecord.normal, hitRecord.material,
                    hitRecord.textureName, hitRecord.texcoord, root, modelview);
        else
            return new Color(0, 0, 0);
    }

    private Color shade(Vector4f point, Vector4f normal, Material material,
                        String textureName, Vector2f texcoord, INode root, Stack<Matrix4f> modelview) {
        Vector3f color = new Vector3f(0, 0, 0);

        for (int i = 0; i < lights.size(); i++) {
            Vector3f lightVec;
            Vector3f spotdirection = new Vector3f(
                    lights.get(i).getSpotDirection().x,
                    lights.get(i).getSpotDirection().y,
                    lights.get(i).getSpotDirection().z);


            if (spotdirection.length() > 0)
                spotdirection = spotdirection.normalize();

            if (lights.get(i).getPosition().w != 0) {
                lightVec = new Vector3f(
                        lights.get(i).getPosition().x - point.x,
                        lights.get(i).getPosition().y - point.y,
                        lights.get(i).getPosition().z - point.z);
            } else {
                lightVec = new Vector3f(
                        -lights.get(i).getPosition().x,
                        -lights.get(i).getPosition().y,
                        -lights.get(i).getPosition().z);
            }
            lightVec = lightVec.normalize();
            HitRecord hitS = new HitRecord();
            Ray shadowRay = new Ray();
            shadowRay.start = point;
            shadowRay.direction = new Vector4f(lightVec, 0);
            shadowRay.direction.x *= ((0.8f + (float) Math.random() / 5));
            shadowRay.direction.y *= ((0.8f + (float) Math.random() / 5));
            shadowRay.direction.z *= ((0.8f + (float) Math.random() / 5));
            raycast(shadowRay, root, modelview, hitS);

            if (hitS.intersected()) {
                continue;
            }

            /* if point is not in the light cone of this light, move on to next light */
            if (new Vector3f(lightVec).negate().dot(spotdirection) <= Math.cos(Math.toRadians(lights.get(i).getSpotCutoff())))
                continue;


            Vector3f normalView = new Vector3f(normal.x, normal.y, normal.z).normalize();

            float nDotL = normalView.dot(lightVec);

            Vector3f viewVec = new Vector3f(point.x, point.y, point.z).negate();
            viewVec = viewVec.normalize();

            Vector3f reflectVec = new Vector3f(lightVec).negate().reflect(normalView);
            reflectVec = reflectVec.normalize();

            float rDotV = Math.max(reflectVec.dot(viewVec), 0.0f);

            Vector3f ambient = new Vector3f(
                    material.getAmbient().x * lights.get(i).getAmbient().x,
                    material.getAmbient().y * lights.get(i).getAmbient().y,
                    material.getAmbient().z * lights.get(i).getAmbient().z);

            Vector3f diffuse = new Vector3f(
                    material.getDiffuse().x * lights.get(i).getDiffuse().x * Math.max(nDotL, 0),
                    material.getDiffuse().y * lights.get(i).getDiffuse().y * Math.max(nDotL, 0),
                    material.getDiffuse().z * lights.get(i).getDiffuse().z * Math.max(nDotL, 0));
            Vector3f specular;
            if (nDotL > 0) {
                specular = new Vector3f(
                        material.getSpecular().x * lights.get(i).getSpecular().x * (float) Math.pow(rDotV, material.getShininess()),
                        material.getSpecular().y * lights.get(i).getSpecular().y * (float) Math.pow(rDotV, material.getShininess()),
                        material.getSpecular().z * lights.get(i).getSpecular().z * (float) Math.pow(rDotV, material.getShininess()));
            } else {
                specular = new Vector3f(0, 0, 0);
            }
            color = new Vector3f(color).add(ambient).add(diffuse).add(specular);
        }

        if (textures.containsKey(textureName)) {
            Vector4f colorFromTexture = textures.get(textureName).getColor(texcoord.x, 1 - texcoord.y);
            color = color.mul(colorFromTexture.x, colorFromTexture.y, colorFromTexture.z);
        }

        color.x = Math.min(color.x, 1);
        color.y = Math.min(color.y, 1);
        color.z = Math.min(color.z, 1);

        return new Color((int) (255 * color.x), (int) (255 * color.y), (int) (255 * color.z));
    }

    @Override
    public void drawMesh(String name, Material material, String textureName, Matrix4f transformation) {
        throw new IllegalArgumentException("Not valid for this renderer");
    }

    @Override
    public void addTexture(String name, String path) {
        TextureImage image = null;
        String imageFormat = path.substring(path.indexOf('.') + 1);
        try {
            image = new TextureImage(path, imageFormat, name);
        } catch (IOException e) {
            throw new IllegalArgumentException("Texture " + path + " cannot be read!");
        }
        textures.put(name, image);
    }

    @Override
    public void dispose() {

    }
}
