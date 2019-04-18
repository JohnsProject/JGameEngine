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
package com.johnsproject.jpge2.dto;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jpge2.Engine;

public class Texture {
	
	private int[] image;
	private int width, height;
	
	public Texture (int width, int height){
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
		this.image = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		this.width = img.getWidth();
		this.height = img.getHeight();
	}
	
	public Texture (String path) throws IOException{
		BufferedImage img = Engine.getInstance().getProcessor().getFileProcessor().loadImage(path);
		this.image = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		this.width = img.getWidth();
		this.height = img.getHeight();
	}
	
	public Texture (String path, int width, int height) throws IOException{
		BufferedImage img = Engine.getInstance().getProcessor().getFileProcessor().loadImage(path, width, height);
		this.image = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		this.width = img.getWidth();
		this.height = img.getHeight();
	}
	
	public Texture (InputStream stream) throws IOException{
		BufferedImage img = Engine.getInstance().getProcessor().getFileProcessor().loadImage(stream);
		this.image = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		this.width = img.getWidth();
		this.height = img.getHeight();
	}
	
	public Texture (InputStream stream, int width, int height) throws IOException{
		BufferedImage img = Engine.getInstance().getProcessor().getFileProcessor().loadImage(stream, width, height);
		this.image = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		this.width = img.getWidth();
		this.height = img.getHeight();
	}
	
	public void setPixel(int x, int y, int color){
		image[x + (y * width)] = color;
	}
	
	public int getPixel(int x, int y){
		return image[x + (y * width)];
	}
	
	public int[] getPixels() {
		return image;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
}
