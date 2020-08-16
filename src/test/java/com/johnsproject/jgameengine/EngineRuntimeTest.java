package com.johnsproject.jgameengine;

import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_BIT;
import static com.johnsproject.jgameengine.util.FixedPointUtils.FP_ONE;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
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
import com.johnsproject.jgameengine.model.Mesh;
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
public class EngineRuntimeTest implements EngineListener, EngineKeyListener {

	private static final boolean SHOW_ENGINE_STATISTICS = true;
	private static final boolean SHOW_DIRECTIONAL_LIGHT_SHADOW_MAP = false;
	private static final boolean SHOW_SPOT_LIGHT_SHADOW_MAP = false;
	
	private static final boolean ENABLE_SHADOW_MAPPING = false;
	
	private static final boolean USE_FLAT_SHADING = false;
	private static final boolean USE_GOURAUD_SHADING = false;
	private static final boolean USE_PHONG_SHADING = false;
	
	private static final int WINDOW_W = 1920;
	private static final int WINDOW_H = 1080;
	private static final int RENDER_W = (WINDOW_W * 100) / 100;
	private static final int RENDER_H = (WINDOW_H * 100) / 100;
	
	private static final int CAMERA_TRANSLATION_SPEED = FP_ONE / 10;
	
	private final FrameBuffer frameBuffer = new FrameBuffer(RENDER_W, RENDER_H);
	private final EngineWindow window = new EngineWindow(frameBuffer);
	private final GraphicsEngine graphicsEngine = new GraphicsEngine(frameBuffer);
	private final InputEngine inputEngine = new InputEngine();
	private final EngineStatistics engineStats = new EngineStatistics(window);
	
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
		new EngineRuntimeTest();
	}
	
	EngineRuntimeTest() {		
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
		window.setSize(WINDOW_W, WINDOW_H);
		inputEngine.addEngineKeyListener(this);
		Engine.getInstance().setScene(scene);
		cameraTranslation = VectorUtils.emptyVector();
		cameraTranslationSpeed = CAMERA_TRANSLATION_SPEED;
		
		graphicsEngine.getShaders().clear();
		graphicsEngine.addShader(new DirectionalLightShadowShader());
		graphicsEngine.addShader(new SpotLightShadowShader()); 
		graphicsEngine.addShader(basicShader);
		graphicsEngine.addShader(basicThreadedShader);
		graphicsEngine.addShader(flatShader);
		graphicsEngine.addShader(gouraudShader);
		graphicsEngine.addShader(phongShader);
		graphicsEngine.setDefaultShader(gouraudShader);

		final int width = 200, height = 15;
		final int panelHeight = 240, checkboxHeight = 15;
		final int x = 10, startY = 10, deltaY = 20;
		int y = 0;

		final Panel panel = new Panel();
		panel.setBounds(x, 160, width, panelHeight);

		Label title = new Label("Shader settings");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		title.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		panel.add(title);
		
		y++;
		final CheckboxGroup checkboxGroup = new CheckboxGroup();
		Checkbox checkbox = new Checkbox("basic shader", checkboxGroup, false);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleShaderChange(basicShader));
		panel.add(checkbox);
		
		y++;
		checkbox = new Checkbox("basic threaded shader", checkboxGroup, false);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleShaderChange(basicThreadedShader));
		panel.add(checkbox);
		
		y++;
		checkbox = new Checkbox("flat shader", checkboxGroup, false);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleShaderChange(flatShader));
		panel.add(checkbox);
		
		y++;
		checkbox = new Checkbox("gouraud shader", checkboxGroup, true);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleShaderChange(gouraudShader));
		panel.add(checkbox);
		
		y++;
		checkbox = new Checkbox("phong shader", checkboxGroup, false);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleShaderChange(phongShader));
		panel.add(checkbox);
		
		y++;
		checkbox = new Checkbox("shadows", true);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleShadowChange());
		panel.add(checkbox);

		y++;
		title = new Label("Light settings");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		title.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		panel.add(title);

		y++;
		checkbox = new Checkbox("directional light", true);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleLightChange(directionalLight));
		panel.add(checkbox);
		
		y++;
		checkbox = new Checkbox("spot light", true);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleLightChange(spotLight));
		panel.add(checkbox);
		
		y++;
		checkbox = new Checkbox("point light", true);
		checkbox.setBounds(x, (y * deltaY) + startY, width, checkboxHeight);
		checkbox.addItemListener(handleLightChange(pointLight));
		panel.add(checkbox);
		
		window.add(panel, 0);
	}
	
	public ItemListener handleShaderChange(final Shader shader) {
		return new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					graphicsEngine.setDefaultShader(shader);
			}
		};
	}
	
	public ItemListener handleShadowChange() {
		return new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				final boolean enableShadows = e.getStateChange() == ItemEvent.SELECTED;
				directionalLight.setShadow(enableShadows);
				spotLight.setShadow(enableShadows);
				pointLight.setShadow(enableShadows);
			}
		};
	}
	
	public ItemListener handleLightChange(final Light light) {
		return new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				light.setActive(e.getStateChange() == ItemEvent.SELECTED);
			}
		};
	}
	
	private Scene loadScene() {		
		try {
			Texture texture = new Texture(FileUtils.loadImage(this.getClass().getResourceAsStream("/JohnsProjectLogo.png")));
			
			Scene scene = new Scene();
			model = OBJImporter.parseResource(this.getClass().getClassLoader(), "DefaultTest.obj");
			model.getMesh().getMaterial("Material.006").setTexture(texture);
			scene.addModel(model);
			
			camera = new Camera("Camera", new Transform());
			camera.getTransform().worldTranslate(0, FP_ONE * 10, FP_ONE * 15);
			camera.getTransform().worldRotate(FP_ONE * -35, 0, 0);
			scene.addCamera(camera);
			cameraTransform = camera.getTransform();
			
			directionalLight = new Light("DirectionalLight", new Transform());
			directionalLight.getTransform().worldRotate(FP_ONE * -90, 0, 0);
			scene.addLight(directionalLight);
			scene.setMainDirectionalLight(directionalLight);
			
			spotLight = new Light("SpotLight", new Transform());
			spotLight.getTransform().worldTranslate(0, FP_ONE, FP_ONE * 8);
			spotLight.setType(LightType.SPOT);
			spotLight.setSpotSize(FP_ONE * 90);
			spotLight.setInnerSpotSize(FP_ONE * 80);
			scene.addLight(spotLight);
			
			pointLight = new Light("PointLight", new Transform());
			pointLight.getTransform().worldTranslate(-FP_ONE * 2, 0, 0);
			pointLight.setType(LightType.POINT);
			scene.addLight(pointLight);
			
			System.gc();
			return scene;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Scene();
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
			final int x = -((mouseY - (WINDOW_H >> 1)) >> 1) << FP_BIT;
			final int y = -((mouseX - (WINDOW_W >> 1)) >> 1) << FP_BIT;
			cameraTransform.setRotation(x, y, 0);
		}
	}
}
