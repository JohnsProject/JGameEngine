package com.johnsproject.jgameengine;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineKeyListener;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.io.OBJImporter;
import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Light;
import com.johnsproject.jgameengine.model.LightType;
import com.johnsproject.jgameengine.model.Model;
import com.johnsproject.jgameengine.model.Scene;
import com.johnsproject.jgameengine.model.Texture;
import com.johnsproject.jgameengine.model.Transform;
import com.johnsproject.jgameengine.shading.BasicShader;
import com.johnsproject.jgameengine.shading.BasicThreadedShader;
import com.johnsproject.jgameengine.shading.DirectionalLightShadowShader;
import com.johnsproject.jgameengine.shading.FlatShader;
import com.johnsproject.jgameengine.shading.ForwardShaderBuffer;
import com.johnsproject.jgameengine.shading.GouraudShader;
import com.johnsproject.jgameengine.shading.PhongShader;
import com.johnsproject.jgameengine.shading.Shader;
import com.johnsproject.jgameengine.shading.SpotLightShadowShader;
import com.johnsproject.jgameengine.util.FileUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

@SuppressWarnings("unused")
public class ModelViewer implements EngineListener, EngineKeyListener {

	private static final boolean SHOW_ENGINE_STATISTICS = true;
	private static final boolean SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP = false;
	private static final boolean SHOW_SPOT_LIGHT_SHADOW_MAP = false;
	
	private static final boolean ENABLE_SHADOW_MAPPING = false;
	
	private static final boolean USE_FLAT_SHADING = false;
	private static final boolean USE_GOURAUD_SHADING = false;
	private static final boolean USE_PHONG_SHADING = false;
	
	private static final int PANEL_WIDTH = 180;
	private static final int PANEL_HEIGHT = 250;
	private static final int PANEL_X = 10;
	private static final int CHECKBOX_HEIGHT = 15;
	private static final int DELTA_Y = 20;
	
	private static final int WINDOW_WIDTH = 1920;
	private static final int WINDOW_HEIGHT = 1080;
	
	private static final int CAMERA_TRANSLATION_SPEED = FP_ONE / 10;
	
	private final FrameBuffer frameBuffer;
	private final EngineWindow window;
	private final GraphicsEngine graphicsEngine;
	private final InputEngine inputEngine = new InputEngine();
	private final EngineStatistics engineStats;
	
	private final BasicShader basicShader = new BasicShader();
	private final BasicThreadedShader basicThreadedShader = new BasicThreadedShader();
	private final FlatShader flatShader = new FlatShader();
	private final GouraudShader gouraudShader = new GouraudShader();
	private final PhongShader phongShader = new PhongShader();
	
	private final Scene scene;
	private Camera camera;
	private Model model;
	private Light directionalLight;
	private Light spotLight;
	private Light pointLight;

	private Transform cameraTransform;
	private int[] cameraTranslation;
	private int cameraTranslationSpeed;
	
	public static void main(String[] args) {
		int width = WINDOW_WIDTH;
		int height = WINDOW_HEIGHT;
		int scaling = 100;
		for (int i = 0; i < args.length; i++) {
			final String setting = args[i];
			if(setting.contains("window")) {
				final String[] resolution = setting.replace("window", "").split("x");
				width = Integer.parseInt(resolution[0]);
				height = Integer.parseInt(resolution[1]);
			}
			else if(setting.contains("render")) {
				scaling = Integer.parseInt(setting.replace("render", ""));
			}
		}
		new ModelViewer(width, height, scaling);
	}
	
	ModelViewer(int width, int height, int scaling) {		
		frameBuffer = new FrameBuffer((width * scaling) / 100, (height * scaling) / 100);
		window = new EngineWindow(frameBuffer);
		graphicsEngine = new GraphicsEngine(frameBuffer);
		engineStats = new EngineStatistics(window);
		window.setSize(width, height);
		
		scene = loadScene();
		Engine.getInstance().addEngineListener(this);
		Engine.getInstance().addEngineListener(graphicsEngine);
		Engine.getInstance().addEngineListener(inputEngine);
		Engine.getInstance().addEngineListener(window);
		if(SHOW_ENGINE_STATISTICS) {
			Engine.getInstance().addEngineListener(engineStats);
		}
		Engine.getInstance().start();	
	}
	
	public void initialize(EngineEvent e) {
		inputEngine.addEngineKeyListener(this);
		Engine.getInstance().setScene(scene);
		cameraTranslation = VectorUtils.emptyVector();
		cameraTranslationSpeed = CAMERA_TRANSLATION_SPEED;
		
		initializeShaders();	
		initializeUI();
	}
	
	private void initializeShaders() {
		graphicsEngine.getShaders().clear();
		graphicsEngine.addShader(new DirectionalLightShadowShader());
		graphicsEngine.addShader(new SpotLightShadowShader()); 
		graphicsEngine.addShader(basicShader);
		graphicsEngine.addShader(basicThreadedShader);
		graphicsEngine.addShader(flatShader);
		graphicsEngine.addShader(gouraudShader);
		graphicsEngine.addShader(phongShader);
		graphicsEngine.setDefaultShader(gouraudShader);
	}
	
	private void initializeUI() {
		final Panel panel = new Panel();
		panel.setBounds(PANEL_X, 160, PANEL_WIDTH, PANEL_HEIGHT);
		panel.setLayout(null);
		
		int y = -1;		
		y = addShaderSettings(y, panel);
		y = addLightSettings(y, panel);
		y = addLoadModelButton(y, panel);
		window.add(panel, 0);
		addHelp();
	}
	
	private int addShaderSettings(int y, Panel panel) {
		y = createTitle("Shader settings", y, panel);
		final CheckboxGroup checkboxGroup = new CheckboxGroup();
		y = createCheckbox("basic shader", false, y, panel, checkboxGroup, handleShaderChange(basicShader));
		y = createCheckbox("basic threaded shader", false, y, panel, checkboxGroup, handleShaderChange(basicThreadedShader));
		y = createCheckbox("flat shader", false, y, panel, checkboxGroup, handleShaderChange(flatShader));
		y = createCheckbox("gouraud shader", true, y, panel, checkboxGroup, handleShaderChange(gouraudShader));
		y = createCheckbox("phong shader", false, y, panel, checkboxGroup, handleShaderChange(phongShader));
		y = createCheckbox("shadows", true, y, panel, null, handleShadowChange());
		return y;
	}
	
	private int addLightSettings(int y, Panel panel) {
		y = createTitle("Light settings", y, panel);
		y = createCheckbox("directional light", true, y, panel, null, handleLightChange(directionalLight));
		y = createCheckbox("spot light", true, y, panel, null, handleLightChange(spotLight));
		y = createCheckbox("point light", true, y, panel, null, handleLightChange(pointLight));
		return y;
	}
	
	private int addLoadModelButton(int y, Panel panel) {
		final FileDialog fileDialog = new FileDialog(window, "Load model");
		fileDialog.setFile("*.obj");
		y++;
		final Button button = new Button("Load model");
		button.setBounds(PANEL_X, y * DELTA_Y, PANEL_WIDTH - 10, CHECKBOX_HEIGHT * 2);
		button.addActionListener(loadModel(fileDialog));
		panel.add(button);
		return y;
	}
	
	private void addHelp() {
		String help = "== HELP ==\n\n";
		help += "Use W, A, S, D, E, Y keys to move and shift to speed up\n\n";
		help += "Press R key and move the mouse to rotate the camera\n\n";
		final TextArea textArea = new TextArea(help, 0, 0, TextArea.SCROLLBARS_NONE);
		textArea.setBounds(PANEL_WIDTH + 11, 30, PANEL_WIDTH, 130);
		textArea.setEditable(false);		
		window.add(textArea, 0);
	}
	
	private int createCheckbox(String text, boolean state, int y, Panel panel,
									CheckboxGroup checkboxGroup, ItemListener listener) {
		y++;
		Checkbox checkbox = null;
		if(checkboxGroup == null)
			checkbox = new Checkbox(text, state);
		else
			checkbox = new Checkbox(text, checkboxGroup, state);
		checkbox.setBounds(PANEL_X, y * DELTA_Y, PANEL_WIDTH, CHECKBOX_HEIGHT);
		checkbox.addItemListener(listener);
		panel.add(checkbox);
		return y;
	}
	
	private int createTitle(String text, int y, Panel panel) {
		y++;
		final Label title = new Label(text);
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		title.setBounds(PANEL_X, y * DELTA_Y, PANEL_WIDTH, CHECKBOX_HEIGHT);
		panel.add(title);
		return y;
	}
	
	private ItemListener handleShaderChange(final Shader shader) {
		return new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					graphicsEngine.setDefaultShader(shader);
			}
		};
	}
	
	private ItemListener handleShadowChange() {
		return new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				final boolean enableShadows = e.getStateChange() == ItemEvent.SELECTED;
				directionalLight.setShadow(enableShadows);
				spotLight.setShadow(enableShadows);
				pointLight.setShadow(enableShadows);
			}
		};
	}
	
	private ItemListener handleLightChange(final Light light) {
		return new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				light.setActive(e.getStateChange() == ItemEvent.SELECTED);
			}
		};
	}
	
	private ActionListener loadModel(final FileDialog fileDialog) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileDialog.setVisible(true);
				final String directory = fileDialog.getDirectory();
				final String file = fileDialog.getFile();
				if ((directory != null) && (file != null)) {
					try {
						final String path = directory + file;
						scene.getModels().clear();
						model = OBJImporter.parse(path);
						scene.addModel(model);
						System.gc();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		};
	} 
	
	private Scene loadScene() {		
		try {
			final Scene scene = new Scene();
			loadModel(scene);
			createCamera(scene);
			createDirectionalLight(scene);
			createSpotLight(scene);
			createPointLight(scene);
			System.gc();
			return scene;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Scene();
	}
	
	private void loadModel(Scene scene) throws IOException {
		final Texture texture = new Texture(FileUtils.loadImage(this.getClass().getResourceAsStream("/JohnsProjectLogo.png")));
		model = OBJImporter.parseResource(this.getClass().getClassLoader(), "DefaultTest.obj");
		model.getMesh().getMaterial("Material.006").setTexture(texture);
		scene.addModel(model);
	}
	
	private void createCamera(Scene scene) {
		camera = new Camera("Camera", new Transform());
		camera.getTransform().worldTranslate(0, FP_ONE * 10, FP_ONE * 15);
		camera.getTransform().worldRotate(FP_ONE * -35, 0, 0);
		scene.addCamera(camera);
		cameraTransform = camera.getTransform();
	}
	
	private void createDirectionalLight(Scene scene) {
		directionalLight = new Light("DirectionalLight", new Transform());
		directionalLight.getTransform().worldRotate(FP_ONE * -90, 0, 0);
		scene.addLight(directionalLight);
		scene.setMainDirectionalLight(directionalLight);
	}
	
	private void createSpotLight(Scene scene) {
		spotLight = new Light("SpotLight", new Transform());
		spotLight.getTransform().worldTranslate(0, FP_ONE, FP_ONE * 8);
		spotLight.setType(LightType.SPOT);
		spotLight.setSpotSize(FP_ONE * 90);
		spotLight.setInnerSpotSize(FP_ONE * 80);
		scene.addLight(spotLight);
	}
	
	private void createPointLight(Scene scene) {
		pointLight = new Light("PointLight", new Transform());
		pointLight.getTransform().worldTranslate(-FP_ONE * 2, 0, 0);
		pointLight.setType(LightType.POINT);
		scene.addLight(pointLight);
	}

	public void dynamicUpdate(EngineEvent e) {		
		final ForwardShaderBuffer shaderBuffer = (ForwardShaderBuffer) graphicsEngine.getShaderBuffer();
		if(SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP || SHOW_SPOT_LIGHT_SHADOW_MAP) {
			Texture shadowMap = null;
			if(SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP) {
				shadowMap = shaderBuffer.getDirectionalShadowMap();
			}
			else if(SHOW_SPOT_LIGHT_SHADOW_MAP) {
				shadowMap = shaderBuffer.getSpotShadowMap();
			}
			for (int y = 0; y < shadowMap.getHeight(); y++) {
				for (int x = 0; x < shadowMap.getWidth(); x++) {
					int depth = shadowMap.getPixel(x, y) >> 9;
					int color = com.johnsproject.jgameengine.util.ColorUtils.toColor(depth, depth, depth);
					if(shaderBuffer.getCamera() != null)
						shaderBuffer.getCamera().getRenderTarget().getColorBuffer().setPixel(x, y, color);		
				}
			}
		}
	}
	
	public void fixedUpdate(EngineEvent e) { }

	public int getLayer() {
		if(SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP || SHOW_SPOT_LIGHT_SHADOW_MAP) {
			return GRAPHICS_ENGINE_LAYER + 1;
		}
		return DEFAULT_LAYER;
	}

	public void keyTyped(KeyEvent e) {
		
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			cameraTranslationSpeed = CAMERA_TRANSLATION_SPEED * 2;
		}
		if(e.getKeyCode() == KeyEvent.VK_P) {
			Engine.getInstance().stop();
		}
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			cameraTranslationSpeed = CAMERA_TRANSLATION_SPEED / 2;
		}
		if(e.getKeyCode() == KeyEvent.VK_P) {
			Engine.getInstance().start();
		}
	}
	
	public void keyHold(KeyEvent e) {
		VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_ZERO);
		if(e.getKeyCode() == KeyEvent.VK_W) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_FORWARD);
		}
		if(e.getKeyCode() == KeyEvent.VK_A) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_LEFT);
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_RIGHT);
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_BACK);
		}
		if(e.getKeyCode() == KeyEvent.VK_E) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_UP);
		}
		if(e.getKeyCode() == KeyEvent.VK_Y) {
			VectorUtils.copy(cameraTranslation, VectorUtils.VECTOR_DOWN);
		}
		if(!VectorUtils.equals(cameraTranslation, VectorUtils.VECTOR_ZERO)) {
			VectorUtils.multiply(cameraTranslation, cameraTranslationSpeed);
			cameraTransform.localTranslate(cameraTranslation);
		}
		if(e.getKeyCode() == KeyEvent.VK_R) {
			final int mouseX = (int)inputEngine.getMouseLocation().getX();
			final int mouseY = (int)inputEngine.getMouseLocation().getY();
			final int x = -((mouseY - (WINDOW_HEIGHT >> 1)) >> 1) << FP_BIT;
			final int y = -((mouseX - (WINDOW_WIDTH >> 1)) >> 1) << FP_BIT;
			cameraTransform.setRotation(x, y, 0);
		}
	}
}
