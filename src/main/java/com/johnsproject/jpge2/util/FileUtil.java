package com.johnsproject.jpge2.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import com.johnsproject.jpge2.processor.ColorProcessor;

public final class FileUtil {

	private FileUtil() {}
	
	/**
	 * Reads the content of the file at the given path and returns it.
	 * 
	 * @param fileName file path.
	 * @return content of given file.
	 * @throws IOException
	 */
	public static String readFile(String fileName) throws IOException {
		String content = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(fileName);
			content = readStream(fileInputStream);
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		return content;
	}

	/**
	 * Writes the object to the file at the given path using serialization.
	 * 
	 * @param path file path.
	 * @param obj object to write. Needs to be serializable.
	 * @throws IOException
	 */
	public static void writeObjectToFile(String path, Object obj) throws IOException {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
			out.writeObject(obj);
			out.close();
			fileOutputStream.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads the object from the file at the given path using serialization.
	 * 
	 * @param path file path.
	 * @throws IOException
	 */
	public static Object readObjectFromFile(String path) throws IOException {
		Object result = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileInputStream);
			result = in.readObject();
			in.close();
			fileInputStream.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Reads the content of the given {@link InputStream} and returns it.
	 * 
	 * @param stream {@link InputStream} to read from.
	 * @return content of the given {@link InputStream}.
	 * @throws IOException
	 */
	public static String readStream(InputStream stream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

		StringBuilder stringBuilder = new StringBuilder();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			stringBuilder.append(line);
			stringBuilder.append("\n");
		}

		in.close();
		return stringBuilder.toString();
	}

	/**
	 * Loads the image at the given path and returns it as a {@link BufferedImage}.
	 * 
	 * @param path image path.
	 * @return loaded image as a {@link BufferedImage}.
	 * @throws IOException
	 */
	public static BufferedImage loadImage(String path) throws IOException {
		BufferedImage image = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(path);
			BufferedImage tmp = ImageIO.read(fileInputStream);
			image = new BufferedImage(tmp.getWidth(), tmp.getHeight(), ColorProcessor.COLOR_TYPE);
			image.createGraphics().drawImage(tmp, 0, 0, null);
			image.createGraphics().dispose();
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		image.flush();
		return image;
	}
	
	/**
	 * Loads an image from the given {@link InputStream} and returns it as a {@link BufferedImage}.
	 * 
	 * @param stream {@link InputStream} to read from.
	 * @return loaded image as a {@link BufferedImage}.
	 * @throws IOException
	 */
	public static BufferedImage loadImage(InputStream stream) throws IOException {
		BufferedImage image = null;
		try {
			BufferedImage tmp = ImageIO.read(stream);
			image = new BufferedImage(tmp.getWidth(), tmp.getHeight(), ColorProcessor.COLOR_TYPE);
			image.createGraphics().drawImage(tmp, 0, 0, null);
			image.createGraphics().dispose();
		} finally {}
		image.flush();
		return image;
	}
	
	/**
	 * Loads the image at the given path and returns it as a {@link BufferedImage} 
	 * with the given size.
	 * 
	 * @param path image path.
	 * @param width destination width.
	 * @param height destination height.
	 * @return loaded image as a {@link BufferedImage}.
	 * @throws IOException
	 */
	public static BufferedImage loadImage(String path, int width, int height) throws IOException {
		BufferedImage image = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(path);
			image = ImageIO.read(fileInputStream);
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height, ColorProcessor.COLOR_TYPE);
		resized.createGraphics().drawImage(tmp, 0, 0, null);
		resized.createGraphics().dispose();
		return resized;
	}
	
	/**
	 * Loads an image from the given {@link InputStream} and returns it as a {@link BufferedImage} 
	 * with the given size.
	 * 
	 * @param stream {@link InputStream} to read from.
	 * @param width destination width.
	 * @param height destination height.
	 * @return loaded image as a {@link BufferedImage}.
	 * @throws IOException
	 */
	public static BufferedImage loadImage(InputStream stream, int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(1, 1, 1);
		try {
			image = ImageIO.read(stream);
		} finally { }
		Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height, ColorProcessor.COLOR_TYPE);
		resized.createGraphics().drawImage(tmp, 0, 0, null);
		resized.createGraphics().dispose();
		return resized;
	}
	
}
