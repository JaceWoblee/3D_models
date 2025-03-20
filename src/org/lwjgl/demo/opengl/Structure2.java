package org.lwjgl.demo.opengl;

import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.demo.util.Color4D;
import org.lwjgl.demo.util.OGLApp;
import org.lwjgl.demo.util.OGLModel3D;
import org.lwjgl.demo.util.OGLObject;

import java.nio.FloatBuffer;

import static org.joml.Math.PI;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL20C.*;

public class Structure2 extends OGLApp<Model2> {
	public Structure2(Model2 model) {
		super(model);
		
		m_keyCallback = (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			else if (action == GLFW_PRESS || action == GLFW_REPEAT) {
				switch(key) {
				case GLFW_KEY_LEFT: model.changeYangle(0.125); break;
				case GLFW_KEY_RIGHT: model.changeYangle(-0.125); break;
				case GLFW_KEY_UP: model.changeXangle(0.125); break;
				case GLFW_KEY_DOWN: model.changeXangle(-0.125); break;
				}
			}
		};
	}
	
	public static void main(String[] args) {
		new Structure2(new Model2()).run("Cube", 640, 640, new Color4D(0.7f, 0.7f, 0.7f, 1));
	}
}

class Model2 extends OGLModel3D {
	final static double deg2rad = PI/180;

	private final Matrix3d m_vm = new Matrix3d();
	private final Vector3d m_light  = new Vector3d();
	private final FloatBuffer m_vec3f = BufferUtils.createFloatBuffer(3);
	private final FloatBuffer m_mat3f = BufferUtils.createFloatBuffer(3*3);
	private final FloatBuffer m_mat4f = BufferUtils.createFloatBuffer(4*4);

	private Side m_side;
    private double m_startTime = System.currentTimeMillis()/1000.0;
    private double m_distance = 50.0f;	// camera distance
    private double m_dxAngle = 0;		// degrees
    private double m_dyAngle = 0; 		// degrees
    private double m_xAngle = 0;		// degrees
    private double m_yAngle = 0;		// degrees
    private double m_zAngle = 0;		// degrees
    private long   m_count;				// fps

	@Override
	public void init(int width, int height) {
		super.init(width, height);
		m_side = new Side(new Color4D(0, 0, 0, 1));
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // VIEW
        V.translation(0.0, 0.0, -m_distance).rotateX(m_xAngle*deg2rad).rotateY(m_yAngle*deg2rad).rotateZ(m_zAngle*deg2rad); // V = T*Rx*Ry*Rz

        // LIGHT (view coordinate system)
        glUniform3fv(u_LIGHT, m_light.set(0.0, 0.0, 10.0).normalize().get(m_vec3f));

		//Front Right
		// FR1
		M.translation(0, -4, 3); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));
		// FR2
		M.translation(2, -3, 2); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));
		// FR3
		M.translation(4, -2, 1); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));
		// FR4
		M.translation(4, 0, 1); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));
		// FR5
		M.translation(4, 2, 1); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));
		// FRB1
		M.translation(-2, 3, 0); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));
		// FRB2
		M.translation(-4, 2, 1); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));
		// FRB3
		M.translation(-4, 0, 1); // translation = identity.translate
		drawSide(m_side.setRGBA(1, 0, 0, 1));


		//Front Left
		M.rotationYXZ(0, PI,PI).translate(0, -6, 3);
		drawSide(m_side.setRGBA(0, 1, 1, 1));

	    // fps
        m_count++;

        double theTime = System.currentTimeMillis()/1000.0;
        if (theTime >= m_startTime + 1.0) {
            System.out.format("%d fps\n", m_count); // falls die fps zu hoch sind: https://www.khronos.org/opengl/wiki/Swap_Interval#In_Windows
            m_startTime = theTime;
            m_count = 0;
        }
        
        // animation
        m_xAngle -= m_dxAngle;
        m_yAngle -= m_dyAngle;
	}
	
	public void changeXangle(double delta) {
		m_dxAngle += delta;
	}

	public void changeYangle(double delta) {
		m_dyAngle += delta;
	}
	
	private void drawSide(Side side) {
		// set geometric transformation matrices for all vertices of this model
        glUniformMatrix3fv(u_VM, false, V.mul(M, VM).normal(m_vm).get(m_mat3f));
        glUniformMatrix4fv(u_PVM, false, P.mul(VM, PVM).get(m_mat4f)); // get: stores in and returns m_mat4f
        
        // set color for all vertices of this model
        glUniform4fv(u_COLOR, side.getColor());

        // draw a quad
        side.setupPositions(m_POSITIONS);	
        side.setupNormals(m_NORMALS);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, side.getVertexCount());
    }

    private static class Side extends OGLObject {
    	final static int CoordinatesPerVertex = 3;
    	
		protected Side(Color4D color) {
			super(color);
        	
			final int nVertices = 4;
			final int nCoordinates = nVertices*CoordinatesPerVertex;
			
			// allocate vertex positions and normals
            allocatePositionBuffer(nCoordinates);
            allocateNormalBuffer(nCoordinates);
            
            // GL_TRIANGLE_STRIP because GL_QUADS are deprecated
            addVertex(0, 0, 0);
            addVertex(0, -2, 0);
            addVertex(+2, +1, -1);
            addVertex(+2, -1, -1);
            
            // bind vertex positions and normals
            bindPositionBuffer();           
            bindNormalBuffer();           
		}
    	
        private void addVertex(float x, float y, float z) {
            m_positions.put(m_vertexCount*CoordinatesPerVertex + 0, x);
            m_positions.put(m_vertexCount*CoordinatesPerVertex + 1, y);
            m_positions.put(m_vertexCount*CoordinatesPerVertex + 2, z);

            m_normals.put(m_vertexCount*CoordinatesPerVertex + 0, 0);
            m_normals.put(m_vertexCount*CoordinatesPerVertex + 1, 0);
            m_normals.put(m_vertexCount*CoordinatesPerVertex + 2, 1);

            m_vertexCount++;
        }
        
        public Side setRGBA(float r, float g, float b, float a) {
        	m_color.put(0, r);
        	m_color.put(1, g);
        	m_color.put(2, b);
        	m_color.put(3, a);
        	return this;
        }
    }

}
