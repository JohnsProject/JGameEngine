package com.johnsproject.jgameengine.rasterization;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;
import static com.johnsproject.jgameengine.util.VectorUtils.*;

import com.johnsproject.jgameengine.model.Face;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class GouraudRasterizer extends FlatRasterizer {
	
	protected final int[] red;
	protected final int[] green;
	protected final int[] blue;
	protected final int[] colorCache;
	
	public GouraudRasterizer(Shader shader) {
		super(shader);
		this.red = VectorUtils.emptyVector();
		this.green = VectorUtils.emptyVector();
		this.blue = VectorUtils.emptyVector();
		this.colorCache = VectorUtils.emptyVector();
	}
	
	protected final void setColor0(int color) {
		red[0] = ColorUtils.getRed(color) << INTERPOLATE_BIT;
		green[0] = ColorUtils.getGreen(color) << INTERPOLATE_BIT;
		blue[0] = ColorUtils.getBlue(color) << INTERPOLATE_BIT;
	}
	
	protected final void setColor1(int color) {
		red[1] = ColorUtils.getRed(color) << INTERPOLATE_BIT;
		green[1] = ColorUtils.getGreen(color) << INTERPOLATE_BIT;
		blue[1] = ColorUtils.getBlue(color) << INTERPOLATE_BIT;
	}
	
	protected final void setColor2(int color) {
		red[2] = ColorUtils.getRed(color) << INTERPOLATE_BIT;
		green[2] = ColorUtils.getGreen(color) << INTERPOLATE_BIT;
		blue[2] = ColorUtils.getBlue(color) << INTERPOLATE_BIT;
	}
	
	/**
	 * This method tells the rasterizer to draw the given {@link GeometryBuffer geometryBuffer}.
	 * This rasterizer draws a triangle using the x, y coordinates of each vertex of the geometryBuffer. 
	 * It uses linear interpolation to find out the z coordinate and the colors for each pixel.
	 * While rasterizing the geometryBuffer, for each pixel/fragment the {@link Shader#fragment} 
	 * method of this rasterizer's {@link Shader} will be called.
	 * 
	 * @param geometryBuffer
	 */
	public void draw(Face face) {
		copyFrustum(shader.getShaderBuffer().getCamera().getFrustum());
		VectorUtils.copy(location0, face.getVertex(0).getLocation());
		VectorUtils.copy(location1, face.getVertex(1).getLocation());
		VectorUtils.copy(location2, face.getVertex(2).getLocation());
		if(cull()) {
			return;
		}
		setColor0(face.getVertex(0).getShadedColor());
		setColor1(face.getVertex(1).getShadedColor());
		setColor2(face.getVertex(2).getShadedColor());
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
			swapVector(red, green, blue, 0, 1);
		}
		if (location1[VECTOR_Y] > location2[VECTOR_Y]) {
			VectorUtils.swap(location1, location2);
			swapVector(red, green, blue, 2, 1);
		}
		if (location0[VECTOR_Y] > location1[VECTOR_Y]) {
			VectorUtils.swap(location0, location1);
			swapVector(red, green, blue, 0, 1);
		}
        if (location1[VECTOR_Y] == location2[VECTOR_Y]) {
        	drawBottomTriangle();
        } else if (location0[VECTOR_Y] == location1[VECTOR_Y]) {
        	drawTopTriangle();
        } else {
            int x = location0[VECTOR_X];
            int y = location1[VECTOR_Y];
            int z = location0[VECTOR_Z];
            int r = red[0];
            int g = green[0];
            int b = blue[0];
            int dy = FixedPointUtils.divide(location1[VECTOR_Y] - location0[VECTOR_Y], location2[VECTOR_Y] - location0[VECTOR_Y]);
            int multiplier = location2[VECTOR_X] - location0[VECTOR_X];
            x += FixedPointUtils.multiply(dy, multiplier);
            multiplier = location2[VECTOR_Z] - location0[VECTOR_Z];
            z += FixedPointUtils.multiply(dy, multiplier);
            multiplier = red[2] - red[0];
            r += FixedPointUtils.multiply(dy, multiplier);
            multiplier = green[2] - green[0];
            g += FixedPointUtils.multiply(dy, multiplier);
            multiplier = blue[2] - blue[0];
            b += FixedPointUtils.multiply(dy, multiplier);
            vectorCache[VECTOR_X] = x;
            vectorCache[VECTOR_Y] = y;
            vectorCache[VECTOR_Z] = z;
            colorCache[0] = r;
            colorCache[1] = g;
            colorCache[2] = b;
            VectorUtils.swap(vectorCache, location2);
            swapCache(red, green, blue, colorCache, 2);
            drawBottomTriangle();
            VectorUtils.swap(vectorCache, location2);
            VectorUtils.swap(location0, location1);
            VectorUtils.swap(location1, vectorCache);
            swapCache(red, green, blue, colorCache, 2);
            swapVector(red, green, blue, 0, 1);
            swapCache(red, green, blue, colorCache, 1);
            drawTopTriangle();
        }
	}
	
	private void drawBottomTriangle() {
		int xShifted = location0[VECTOR_X] << FP_BIT;
		int y2y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		int y3y1 = location1[VECTOR_Y] - location0[VECTOR_Y];
		y2y1 = y2y1 == 0 ? 1 : y2y1;
		y3y1 = y3y1 == 0 ? 1 : y3y1;
        int dx1 = FixedPointUtils.divide(location1[VECTOR_X] - location0[VECTOR_X], y2y1);
        int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
        int dz1 = FixedPointUtils.divide(location1[VECTOR_Z] - location0[VECTOR_Z], y2y1);
        int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
        int dr1 = FixedPointUtils.divide(red[1] - red[0], y2y1);
        int dr2 = FixedPointUtils.divide(red[2] - red[0], y3y1);
        int dg1 = FixedPointUtils.divide(green[1] - green[0], y2y1);
        int dg2 = FixedPointUtils.divide(green[2] - green[0], y3y1);
        int db1 = FixedPointUtils.divide(blue[1] - blue[0], y2y1);
        int db2 = FixedPointUtils.divide(blue[2] - blue[0], y3y1);
        if(dx1 < dx2) {
        	int dxdx = dx2 - dx1;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
        	int dr = FixedPointUtils.divide(dr2 - dr1, dxdx);
        	int dg = FixedPointUtils.divide(dg2 - dg1, dxdx);
        	int db = FixedPointUtils.divide(db2 - db1, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int r = red[0] << FP_BIT;
            int g = green[0] << FP_BIT;
            int b = blue[0] << FP_BIT;
	        for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
	        	drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db);
	            x1 += dx1;
	            x2 += dx2;
	            z += dz1;
	            r += dr1;
	            g += dg1;
	            b += db1;
	        }
        } else {
        	int dxdx = dx1 - dx2;
        	dxdx = dxdx == 0 ? 1 : dxdx;
        	int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
        	int dr = FixedPointUtils.divide(dr1 - dr2, dxdx);
        	int dg = FixedPointUtils.divide(dg1 - dg2, dxdx);
        	int db = FixedPointUtils.divide(db1 - db2, dxdx);
        	int x1 = xShifted;
            int x2 = xShifted;
            int z = location0[VECTOR_Z] << FP_BIT;
            int r = red[0] << FP_BIT;
            int g = green[0] << FP_BIT;
            int b = blue[0] << FP_BIT;
        	for (int y = location0[VECTOR_Y]; y <= location1[VECTOR_Y]; y++) {
        		drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db);
	            x1 += dx2;
	            x2 += dx1;
	            z += dz2;
	            r += dr2;
	            g += dg2;
	            b += db2;
	        }
        }
    }
    
	private void drawTopTriangle() {
		int xShifted = location2[VECTOR_X] << FP_BIT;
		int y3y1 = location2[VECTOR_Y] - location0[VECTOR_Y];
		int y3y2 = location2[VECTOR_Y] - location1[VECTOR_Y];
		y3y1 = y3y1 == 0 ? 1 : y3y1;
		y3y2 = y3y2 == 0 ? 1 : y3y2;
		int dx1 = FixedPointUtils.divide(location2[VECTOR_X] - location0[VECTOR_X], y3y1);
		int dx2 = FixedPointUtils.divide(location2[VECTOR_X] - location1[VECTOR_X], y3y2);
		int dz1 = FixedPointUtils.divide(location2[VECTOR_Z] - location0[VECTOR_Z], y3y1);
		int dz2 = FixedPointUtils.divide(location2[VECTOR_Z] - location1[VECTOR_Z], y3y2);
		int dr1 = FixedPointUtils.divide(red[2] - red[0], y3y1);
		int dr2 = FixedPointUtils.divide(red[2] - red[1], y3y2);
		int dg1 = FixedPointUtils.divide(green[2] - green[0], y3y1);
		int dg2 = FixedPointUtils.divide(green[2] - green[1], y3y2);
		int db1 = FixedPointUtils.divide(blue[2] - blue[0], y3y1);
		int db2 = FixedPointUtils.divide(blue[2] - blue[1], y3y2);
		if (dx1 > dx2) {
			int dxdx = dx1 - dx2;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz1 - dz2, dxdx);
			int dr = FixedPointUtils.divide(dr1 - dr2, dxdx);
			int dg = FixedPointUtils.divide(dg1 - dg2, dxdx);
			int db = FixedPointUtils.divide(db1 - db2, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int r = red[2] << FP_BIT;
			int g = green[2] << FP_BIT;
			int b = blue[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db);
	            x1 -= dx1;
	            x2 -= dx2;
	            z -= dz1;
	            r -= dr1;
	            g -= dg1;
	            b -= db1;
	        }
		} else {
			int dxdx = dx2 - dx1;
			dxdx = dxdx == 0 ? 1 : dxdx;
			int dz = FixedPointUtils.divide(dz2 - dz1, dxdx);
			int dr = FixedPointUtils.divide(dr2 - dr1, dxdx);
			int dg = FixedPointUtils.divide(dg2 - dg1, dxdx);
			int db = FixedPointUtils.divide(db2 - db1, dxdx);
			int x1 = xShifted;
			int x2 = xShifted;
			int z = location2[VECTOR_Z] << FP_BIT;
			int r = red[2] << FP_BIT;
			int g = green[2] << FP_BIT;
			int b = blue[2] << FP_BIT;
	        for (int y = location2[VECTOR_Y]; y > location0[VECTOR_Y]; y--) {
	        	drawScanline(x1, x2, y, z, r, g, b, dz, dr, dg, db);
	            x1 -= dx2;
	            x2 -= dx1;
	            z -= dz2;
	            r -= dr2;
	            g -= dg2;
	            b -= db2;
	        }
		}
    }
	
	private void drawScanline(int x1, int x2, int y, int z, int r, int g, int b, int dz, int dr, int dg, int db) {
		x1 >>= FP_BIT;
		x2 >>= FP_BIT;
		int cr, cg, cb;
		for (; x1 <= x2; x1++) {
			fragment.getLocation()[VECTOR_X] = x1;
			fragment.getLocation()[VECTOR_Y] = y;
			fragment.getLocation()[VECTOR_Z] = z >> FP_BIT;
			cr = r >> FP_PLUS_INTERPOLATE_BIT;
			cg = g >> FP_PLUS_INTERPOLATE_BIT;
			cb = b >> FP_PLUS_INTERPOLATE_BIT;
			fragment.setColor(ColorUtils.toColor(cr, cg, cb));
			shader.fragment(fragment);
			z += dz;
			r += dr;
			g += dg;
			b += db;
		}
	}
}
