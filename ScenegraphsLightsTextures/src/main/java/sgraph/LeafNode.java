package sgraph;

import RayTracer.HitRecord;
import RayTracer.Ray;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Stack;

/**
 * This node represents the leaf of a scene graph. It is the only type of node that has
 * actual geometry to render.
 *
 * @author Amit Shesh
 */
public class LeafNode extends AbstractNode {
    /**
     * The name of the object instance that this leaf contains. All object instances are stored
     * in the scene graph itself, so that an instance can be reused in several leaves
     */
    protected String objInstanceName;
    /**
     * The material associated with the object instance at this leaf
     */
    protected util.Material material;

    protected String textureName;

    public LeafNode(String instanceOf, IScenegraph graph, String name) {
        super(graph, name);
        this.objInstanceName = instanceOf;
    }


    /*
     *Set the material of each vertex in this object
     */
    @Override
    public void setMaterial(util.Material mat) {
        material = new util.Material(mat);
    }

    /**
     * Set texture ID of the texture to be used for this leaf
     *
     * @param name
     */
    @Override
    public void setTextureName(String name) {
        textureName = name;
    }

    @Override
    public void closestIntersect(Ray ray, Stack<Matrix4f> mv, HitRecord record) {
        if (objInstanceName.equals("box")) {
            if (ray.direction.x == 0 || ray.direction.y == 0 || ray.direction.z == 0 ||
                    (-0.5f <= ray.posn.x / ray.direction.x) && (ray.posn.x / ray.direction.x <= 0.5f) &&
                            (-0.5f <= ray.posn.y / ray.direction.y) && (ray.posn.y / ray.direction.y <= 0.5f) &&
                            (-0.5f <= ray.posn.z / ray.direction.z) && (ray.posn.z / ray.direction.z <= 0.5f)) {
                float tempMin = (float) Math.min((-0.5 - ray.posn.x) / ray.direction.x, (-0.5 - ray.posn.y) / ray.direction.y);
                float tempMax = (float) Math.max((0.5 - ray.posn.x) / ray.direction.x, (0.5 - ray.posn.y) / ray.direction.y);
                record.t = (float) Math.min(tempMin, (-0.5 - ray.posn.z) / ray.direction.z);
                System.out.println(record.t);
                record.intersect = new Vector4f(
                        ray.posn.x + record.t * ray.direction.x,
                        ray.posn.y + record.t * ray.direction.y,
                        ray.posn.z + record.t * ray.direction.z,
                        0);
                record.intersect = record.intersect.normalize();
                //bottom face
                if (record.intersect.x <= 0 && record.intersect.y <= 0 && record.intersect.z <= 0) {
                    record.normal = new Vector4f(0, 0, -1, 0);
                }
                //top face
                if (record.intersect.x >= 0 && record.intersect.y >= 0 && record.intersect.z >= 0) {
                    record.normal = new Vector4f(0, 0, 1, 0);
                }
                //back face
                if (record.intersect.x >= 0 && record.intersect.y <= 0 && record.intersect.z <= 0) {
                    record.normal = new Vector4f(-1, 0, 0, 0);
                }
                //front face
                if (record.intersect.x <= 0 && record.intersect.y <= 0 && record.intersect.z <= 0) {
                    record.normal = new Vector4f(1, 0, 0, 0);
                }
                //left face
                if (record.intersect.x <= 0 && record.intersect.y >= 0 && record.intersect.z <= 0) {
                    record.normal = new Vector4f(0, -1, 0, 0);
                }
                //right face
                if (record.intersect.x <= 0 && record.intersect.y >= 0 && record.intersect.z <= 0) {
                    record.normal = new Vector4f(0, 1, 0, 0);
                }
                record.normal = new Vector4f(0, 0, 0, 0);
                record.ambient = material.getAmbient();
                record.diffuse = material.getDiffuse();
                record.shininess = material.getShininess();
                record.specular = material.getSpecular();
            }


        } else if (objInstanceName.equals("sphere"))   {

            record.ambient = material.getAmbient();
            record.diffuse = material.getDiffuse();
            record.shininess = material.getShininess();
            record.specular = material.getSpecular();
        }

    }

    /*
     * gets the material
     */
    public util.Material getMaterial() {
        return material;
    }

    @Override
    public INode clone() {
        LeafNode newclone = new LeafNode(this.objInstanceName, scenegraph, name);
        newclone.setMaterial(this.getMaterial());
        return newclone;
    }


    /**
     * Delegates to the scene graph for rendering. This has two advantages:
     * <ul>
     * <li>It keeps the leaf light.</li>
     * <li>It abstracts the actual drawing to the specific implementation of the scene graph renderer</li>
     * </ul>
     *
     * @param context   the generic renderer context {@link sgraph.IScenegraphRenderer}
     * @param modelView the stack of modelview matrices
     * @throws IllegalArgumentException
     */
    @Override
    public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView) throws IllegalArgumentException {
        if (objInstanceName.length() > 0) {
            context.drawMesh(objInstanceName, material, textureName, modelView.peek());
        }
    }


}
