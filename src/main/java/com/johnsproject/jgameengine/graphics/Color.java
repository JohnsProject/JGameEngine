package com.johnsproject.jgameengine.graphics;

import java.awt.image.BufferedImage;

import com.johnsproject.jgameengine.math.Fixed;

/**
 * The ColorUtils class contains methods for generating integer sRGB colors 
 * and performing integer sRGB color operations such as lerp, multiply and add.
 * 
 * @author John Ferraz Salomon
 */
public final class Color {

	/**
	 * Some default integer sRGB colors.
	 */
	public static final int WHITE = java.awt.Color.white.getRGB(),
							BLUE = java.awt.Color.blue.getRGB(),
							CYAN = java.awt.Color.cyan.getRGB(),
							GREEN = java.awt.Color.green.getRGB(),
							ORANGE = java.awt.Color.orange.getRGB(),
							RED = java.awt.Color.red.getRGB(),
							YELLOW = java.awt.Color.yellow.getRGB(),
							MAGENTA = java.awt.Color.magenta.getRGB(),
							PINK = java.awt.Color.pink.getRGB(),
							GRAY = java.awt.Color.gray.getRGB(),
							BLACK = java.awt.Color.black.getRGB();
	
	
	/**
	 * Bits count representing each color value.
	 */
	public static final byte COLOR_BITS = 8;
	
	/**
	 * Value representing the range of each color value. (0 - {@value #COLOR_ONE})
	 */
	public static final int COLOR_ONE = (1 << COLOR_BITS) - 1;
	
	/**
	 * Number representing the BufferedImage color type that the ColorProcessor handles.
	 */
	public static final byte COLOR_TYPE = BufferedImage.TYPE_INT_ARGB;
	
	private static final byte GREENSHIFT = COLOR_BITS;
	private static final byte REDSHIFT = COLOR_BITS * 2;
	private static final byte ALPHASHIFT = COLOR_BITS * 3;
	
	private Color() { }
	
	/**
	 * Returns a integer sRGB color. 
	 * (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue)
	 * 
	 * @param a
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int toColor(int a, int r, int g, int b) {
		int color = 0;
		color |= Math.min(COLOR_ONE, Math.max(a, 0)) << ALPHASHIFT;
		color |= Math.min(COLOR_ONE, Math.max(r, 0)) << REDSHIFT;
		color |= Math.min(COLOR_ONE, Math.max(g, 0)) << GREENSHIFT;
		color |= Math.min(COLOR_ONE, Math.max(b, 0));
		return color;
	}

	/**
	 * Returns a integer sRGB color, with alpha = 255. 
	 * (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue)
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int toColor(int r, int g, int b) {
		int color = 0;
		color |= (255) << ALPHASHIFT;
		color |= Math.min(COLOR_ONE, Math.max(r, 0)) << REDSHIFT;
		color |= Math.min(COLOR_ONE, Math.max(g, 0)) << GREENSHIFT;
		color |= Math.min(COLOR_ONE, Math.max(b, 0));
		return color;
	}

	/**
	 * Returns the blue component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public static int getBlue(int color) {
		return (color) & COLOR_ONE;
	}

	/**
	 * Returns the green component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public static int getGreen(int color) {
		return (color >> GREENSHIFT) & COLOR_ONE;
	}

	/**
	 * Returns the red component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public static int getRed(int color) {
		return (color >> REDSHIFT) & COLOR_ONE;
	}

	/**
	 * Returns the alpha component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public static int getAlpha(int color) {
		return (color >> ALPHASHIFT) & COLOR_ONE;
	}

	
	/**
	 * Returns the result of the multiplication of color and factor. 
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color
	 * @param factor a fixed point value between 0 and {@link Fixed#FP_ONE}
	 * @return
	 */
	public static int multiply(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		r = Fixed.multiply(r, factor);
		g = Fixed.multiply(g, factor);
		b = Fixed.multiply(b, factor);
		return toColor(a, r, g, b);
	}
	
	/**
	 * Returns the result of the multiplication of color and factor.
	 * 
	 * @param color
	 * @param factor a fixed point value between 0 and {@link Fixed#FP_ONE}
	 * @return
	 */
	public static int multiplyARGB(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		r = Fixed.multiply(r, factor);
		g = Fixed.multiply(g, factor);
		b = Fixed.multiply(b, factor);
		a = Fixed.multiply(a, factor);
		return toColor(a, r, g, b);
	}
	
	/**
	 * Returns a color containing the added components of color1 and color2.
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static int add(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 + r2);
		int g = (g1 + g2);
		int b = (b1 + b2);
		return toColor(a1, r, g, b);
	}
	
	/**
	 * Returns a color containing the added components of color1 and color2.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static int addARGB(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 + r2);
		int g = (g1 + g2);
		int b = (b1 + b2);
		int a = (a1 + a2);
		return toColor(a, r, g, b);
	}
	
	/**
	 * Returns a color containing the multiplied components of color1 and color2.
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static int multiplyColor(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 * r2) >> COLOR_BITS;
		int g = (g1 * g2) >> COLOR_BITS;
		int b = (b1 * b2) >> COLOR_BITS;
		return toColor(a1, r, g, b);
	}
	
	/**
	 * Returns a color containing the multiplied components of color1 and color2.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static int multiplyColorARGB(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 * r2) >> COLOR_BITS;
		int g = (g1 * g2) >> COLOR_BITS;
		int b = (b1 * b2) >> COLOR_BITS;
		int a = (a1 * a2) >> COLOR_BITS;
		return toColor(a, r, g, b);
	}
	
	/**
	 * Returns a color that is the linear interpolation of color1 and color2 by the given factor. 
	 * The factor should be in the range 0-{@value #COLOR_ONE}. 
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color1
	 * @param color2
	 * @param factor
	 * @return
	 */
	public static int lerp(int color1, int color2, int factor) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 + (((r2 - r1) * factor) >> COLOR_BITS));
		int g = (g1 + (((g2 - g1) * factor) >> COLOR_BITS));
		int b = (b1 + (((b2 - b1) * factor) >> COLOR_BITS));
		return toColor(a1, r, g, b);
	}
	
	/**
	 * Returns a color that is the linear interpolation of color1 and color2 by the given factor. 
	 * The factor should be in the range 0-{@value #COLOR_ONE}.
	 * 
	 * @param color1
	 * @param color2
	 * @param factor
	 * @return
	 */
	public static int lerpARGB(int color1, int color2, int factor) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 + (((r2 - r1) * factor) >> COLOR_BITS));
		int g = (g1 + (((g2 - g1) * factor) >> COLOR_BITS));
		int b = (b1 + (((b2 - b1) * factor) >> COLOR_BITS));
		int a = (a1 + (((a2 - a1) * factor) >> COLOR_BITS));
		return toColor(a, r, g, b);
	}
	
	/**
	 * Returns a string containing the data of the given color.
	 * 
	 * @param color
	 * @return
	 */
	public static String toString(int color) {
		int r1 = getRed(color), g1 = getGreen(color), b1 = getBlue(color), a1 = getAlpha(color);
		return "(" + a1 + ", " + g1 + ", " + b1 + ", " + r1 + ")";
	}
}
