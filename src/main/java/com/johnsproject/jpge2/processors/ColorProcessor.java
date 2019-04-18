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
package com.johnsproject.jpge2.processors;

import java.awt.Color;

/**
 * The ColorProcessor class contains methods for generating and performing 
 * integer sRGB color operations such as lerp, multiply and add.
 * 
 * @author John Ferraz Salomon
 *
 */
public class ColorProcessor {

	/**
	 * Some default integer sRGB colors.
	 */
	public final int WHITE = Color.white.getRGB(),
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
	public final byte COLOR_BITS = 8;
	
	/**
	 * Value representing the range of each color values. (0 - {@value #COLOR_VALUE})
	 */
	private final int COLOR_VALUE = 0xFF;
	
	private final byte GREENSHIFT = 8;
	private final byte REDSHIFT = 16;
	private final byte ALPHASHIFT = 24;

	private final MathProcessor mathProcessor;
	
	ColorProcessor(MathProcessor mathProcessor) {
		this.mathProcessor = mathProcessor;
	}
	
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
	public int generate(int a, int r, int g, int b) {
		int color = 0;
		color |= mathProcessor.clamp(a, 0, 255) << ALPHASHIFT;
		color |= mathProcessor.clamp(r, 0, 255) << REDSHIFT;
		color |= mathProcessor.clamp(g, 0, 255) << GREENSHIFT;
		color |= mathProcessor.clamp(b, 0, 255);
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
	public int generate(int r, int g, int b) {
		int color = 0;
		color |= (255) << ALPHASHIFT;
		color |= mathProcessor.clamp(r, 0, 255) << REDSHIFT;
		color |= mathProcessor.clamp(g, 0, 255) << GREENSHIFT;
		color |= mathProcessor.clamp(b, 0, 255);
		return color;
	}

	/**
	 * Returns the blue component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public int getBlue(int color) {
		return (color) & COLOR_VALUE;
	}

	/**
	 * Returns the green component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public int getGreen(int color) {
		return (color >> GREENSHIFT) & COLOR_VALUE;
	}

	/**
	 * Returns the red component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public int getRed(int color) {
		return (color >> REDSHIFT) & COLOR_VALUE;
	}

	/**
	 * Returns the alpha component of the given color. 
	 * The color should be in sRGB color space.
	 * 
	 * @param color
	 * @return
	 */
	public int getAlpha(int color) {
		return (color >> ALPHASHIFT) & COLOR_VALUE;
	}

	
	/**
	 * Returns the result of the multiplication of color and factor. 
	 * The factor should be in the range 0-{@value #COLOR_VALUE}.
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color
	 * @param factor
	 * @return
	 */
	public int multiply(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		factor += 255;
		r = (r * factor) >> 8;
		g = (g * factor) >> 8;
		b = (b * factor) >> 8;
		return generate(a, r, g, b);
	}
	
	/**
	 * Returns the result of the multiplication of color and factor. 
	 * The factor should be in the range 0-{@value #COLOR_VALUE}.
	 * 
	 * @param color
	 * @param factor
	 * @return
	 */
	public int multiplyARGB(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		factor += 255;
		r = (r * factor) >> 8;
		g = (g * factor) >> 8;
		b = (b * factor) >> 8;
		a = (a * factor) >> 8;
		return generate(a, r, g, b);
	}
	
	/**
	 * Returns a color containing the added components of color1 and color2.
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public int add(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 + r2);
		int g = (g1 + g2);
		int b = (b1 + b2);
		return generate(a1, r, g, b);
	}
	
	/**
	 * Returns a color containing the added components of color1 and color2.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public int addARGB(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 + r2);
		int g = (g1 + g2);
		int b = (b1 + b2);
		int a = (a1 + a2);
		return generate(a, r, g, b);
	}
	
	/**
	 * Returns a color containing the multiplied components of color1 and color2.
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public int multiplyColor(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 * r2) >> 8;
		int g = (g1 * g2) >> 8;
		int b = (b1 * b2) >> 8;
		return generate(a1, r, g, b);
	}
	
	/**
	 * Returns a color containing the multiplied components of color1 and color2.
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public int multiplyColorARGB(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 * r2) >> 8;
		int g = (g1 * g2) >> 8;
		int b = (b1 * b2) >> 8;
		int a = (a1 * a2) >> 8;
		return generate(a, r, g, b);
	}
	
	/**
	 * Returns a color that is the linear interpolation of color1 and color2 by the given factor. 
	 * The factor should be in the range 0-{@value #COLOR_VALUE}. 
	 * This method only changes the RGB values of the color.
	 * 
	 * @param color1
	 * @param color2
	 * @param factor
	 * @return
	 */
	public int lerp(int color1, int color2, int factor) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 + (((r2 - r1) * factor) >> 8));
		int g = (g1 + (((g2 - g1) * factor) >> 8));
		int b = (b1 + (((b2 - b1) * factor) >> 8));
		return generate(a1, r, g, b);
	}
	
	/**
	 * Returns a color that is the linear interpolation of color1 and color2 by the given factor. 
	 * The factor should be in the range 0-{@value #COLOR_VALUE}.
	 * 
	 * @param color1
	 * @param color2
	 * @param factor
	 * @return
	 */
	public int lerpARGB(int color1, int color2, int factor) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 + (((r2 - r1) * factor) >> 8));
		int g = (g1 + (((g2 - g1) * factor) >> 8));
		int b = (b1 + (((b2 - b1) * factor) >> 8));
		int a = (a1 + (((a2 - a1) * factor) >> 8));
		return generate(a, r, g, b);
	}
}
