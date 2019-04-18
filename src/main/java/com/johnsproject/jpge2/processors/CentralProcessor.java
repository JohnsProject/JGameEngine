package com.johnsproject.jpge2.processors;

public class CentralProcessor {

	private final ColorProcessor colorProcessor;
	private final FileProcessor fileProcessor;
	private final GraphicsProcessor graphicsProcessor;
	private final MathProcessor mathProcessor;
	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;

	public CentralProcessor() {
		this.fileProcessor = new FileProcessor();
		this.mathProcessor = new MathProcessor();
		this.colorProcessor = new ColorProcessor(mathProcessor);
		this.matrixProcessor = new MatrixProcessor(mathProcessor);
		this.vectorProcessor = new VectorProcessor(mathProcessor);
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
}
