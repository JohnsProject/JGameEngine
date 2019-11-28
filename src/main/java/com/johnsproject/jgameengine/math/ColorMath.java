/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jgameengine.math;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * The ColorLibrary class contains methods for generating integer sRGB colors 
 * and performing integer sRGB color operations such as lerp, multiply and add.
 * 
 * @author John Ferraz Salomon
 */
public final class ColorMath {

	/**
	 * Some default integer sRGB colors.
	 */
	public static final int WHITE = Color.white.getRGB(),
							BLUE = Color.blue.getRGB(),
							CYAN = Color.cyan.getRGB(),
							GREEN = Color.green.getRGB(),
							ORANGE = Color.orange.getRGB(),
							RED = Color.red.getRGB(),
							YELLOW = Color.yellow.getRGB(),
							MAGENTA = Color.magenta.getRGB(),
							PINK = Color.pink.getRGB(),
							GRAY = Color.gray.getRGB(),
							BLACK = Color.black.getRGB();
	
	
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
	
	private ColorMath() { }
	
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
	 * The factor should be in the range 0-{@value #COLOR_ONE}.
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color
	 * @param factor
	 * @return
	 */
	public static int multiply(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		factor += 1;
		r = (r * factor) >> COLOR_BITS;
		g = (g * factor) >> COLOR_BITS;
		b = (b * factor) >> COLOR_BITS;
		return toColor(a, r, g, b);
	}
	
	/**
	 * Returns the result of the multiplication of color and factor. 
	 * The factor should be in the range 0-{@value #COLOR_ONE}.
	 * 
	 * @param color
	 * @param factor
	 * @return
	 */
	public static int multiplyARGB(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		factor += 1;
		r = (r * factor) >> COLOR_BITS;
		g = (g * factor) >> COLOR_BITS;
		b = (b * factor) >> COLOR_BITS;
		a = (a * factor) >> COLOR_BITS;
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
