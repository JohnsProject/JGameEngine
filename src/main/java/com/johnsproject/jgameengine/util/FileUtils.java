package com.johnsproject.jgameengine.util;

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
import java.io.Serializable;

import javax.imageio.ImageIO;

/**
 * The FileUtils class contains methods for writing/reading files from the file system.
 * 
 * @author John Ferraz Salomon
 */
public final class FileUtils {

	private FileUtils() {}
	
	/**
	 * Reads the content of the file at the specified path and returns it.
	 * 
	 * @param path of the file
	 * @return String representation of the data in the specified file.
	 * @throws IOException If the file does not exist, is a directory rather than a regular file,
	 * or for some other reason cannot be opened for reading.
	 */
	public static String readFile(String path) throws IOException {
		String content = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(path);
			content = readStream(fileInputStream);
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		return content;
	}

	/**
	 * Writes the object to the file at the specified path using serialization.
	 * Only objects that implement the {@link Serializable} interface can be written.
	 * 
	 * @param path file path.
	 * @param obj object to write. Has to implement Serializable interface.
	 * @throws IOException If an I/O error occurs while writing stream header. 
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
	 * Reads the object from the file at the specified path using serialization.
	 * Only objects written using {@link #writeObjectToFile}
	 *  or {@link ObjectOutputStream} can be parsed.
	 * 
	 * @param path file path.
	 * @return The object in the specified file.
	 * @throws IOException If an I/O error occurs while reading stream header.
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
	 * Reads the content of the specified {@link InputStream} and returns it.
	 * 
	 * @param stream InputStream to read from.
	 * @return String representation of the data in the specified InputStream.
	 * @throws IOException If an I/O error occurs.
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
	 * Loads the image at the specified path and returns it as a {@link BufferedImage}.
	 * 
	 * @param path image path.
	 * @return Loaded image as a BufferedImage.
	 * @throws IOException If an error occurs while loading the image.
	 */
	public static BufferedImage loadImage(String path) throws IOException {
		BufferedImage image = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(path);
			BufferedImage tmp = ImageIO.read(fileInputStream);
			image = new BufferedImage(tmp.getWidth(), tmp.getHeight(), ColorUtils.COLOR_TYPE);
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
	 * Loads an image from the specified {@link InputStream} and returns it as a {@link BufferedImage}.
	 * 
	 * @param stream InputStream to read from.
	 * @return Loaded image as a BufferedImage.
	 * @throws IOException If an error occurs while loading the image.
	 */
	public static BufferedImage loadImage(InputStream stream) throws IOException {
		BufferedImage image = null;
		try {
			BufferedImage tmp = ImageIO.read(stream);
			image = new BufferedImage(tmp.getWidth(), tmp.getHeight(), ColorUtils.COLOR_TYPE);
			image.createGraphics().drawImage(tmp, 0, 0, null);
			image.createGraphics().dispose();
		} finally {}
		image.flush();
		return image;
	}
	
	/**
	 * Loads the image at the specified path and returns it as a {@link BufferedImage} 
	 * with the specified size.
	 * 
	 * @param path image path.
	 * @param width destination width.
	 * @param height destination height.
	 * @return Loaded image as a BufferedImage.
	 * @throws IOException If an error occurs while loading the image.
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
		BufferedImage resized = new BufferedImage(width, height, ColorUtils.COLOR_TYPE);
		resized.createGraphics().drawImage(tmp, 0, 0, null);
		resized.createGraphics().dispose();
		return resized;
	}
	
	/**
	 * Loads an image from the specified {@link InputStream} and returns it as a {@link BufferedImage} 
	 * with the specified size.
	 * 
	 * @param stream InputStream to read from.
	 * @param width destination width.
	 * @param height destination height.
	 * @return Loaded image as a BufferedImage.
	 * @throws IOException If an error occurs while loading the image.
	 */
	public static BufferedImage loadImage(InputStream stream, int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(1, 1, 1);
		try {
			image = ImageIO.read(stream);
		} finally { }
		Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height, ColorUtils.COLOR_TYPE);
		resized.createGraphics().drawImage(tmp, 0, 0, null);
		resized.createGraphics().dispose();
		return resized;
	}
	
}
