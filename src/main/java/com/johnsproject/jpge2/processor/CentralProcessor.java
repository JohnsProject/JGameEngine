package com.johnsproject.jpge2.processor;

public class CentralProcessor {

	private final ColorProcessor colorProcessor;
	private final FileProcessor fileProcessor;
	private final GraphicsProcessor graphicsProcessor;
	private final MathProcessor mathProcessor;
	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;
	private final TextureProcessor textureProcessor;

	public CentralProcessor() {
		this.fileProcessor = new FileProcessor();
		this.mathProcessor = new MathProcessor();
		this.colorProcessor = new ColorProcessor(mathProcessor);
		this.matrixProcessor = new MatrixProcessor(mathProcessor);
		this.vectorProcessor = new VectorProcessor(mathProcessor);
		this.textureProcessor = new TextureProcessor(fileProcessor);
		this.graphicsProcessor = new GraphicsProcessor(mathProcessor, matrixProcessor, vectorProcessor);
	}

	public ColorProcessor getColorProcessor() {
		return colorProcessor;
	}

	public FileProcessor getFileProcessor() {
		return fileProcessor;
	}

	public GraphicsProcessor getGraphicsProcessor() {
		return graphicsProcessor;
	}

	public MathProcessor getMathProcessor() {
		return mathProcessor;
	}

	public MatrixProcessor getMatrixProcessor() {
		return matrixProcessor;
	}

	public VectorProcessor getVectorProcessor() {
		return vectorProcessor;
	}

	public TextureProcessor getTextureProcessor() {
		return textureProcessor;
	}
}
