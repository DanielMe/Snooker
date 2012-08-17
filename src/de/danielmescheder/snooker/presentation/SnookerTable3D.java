package de.danielmescheder.snooker.presentation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.light.PointLight;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Line.Mode;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Sphere;
import com.jme.scene.shape.Tube;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.bui.BLabel;
import com.jmex.bui.BStyleSheet;
import com.jmex.bui.BWindow;
import com.jmex.bui.BuiSystem;
import com.jmex.bui.PolledRootNode;
import com.jmex.bui.layout.GroupLayout;
import com.jmex.bui.provider.DefaultResourceProvider;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Player;
import de.danielmescheder.snooker.domain.Pocket;
import de.danielmescheder.snooker.domain.PocketCorner;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.domain.Tile;
import de.danielmescheder.snooker.gameflow.GameState;

/**
 * This implementation of a {@link TablePresentation} is responsible for
 * creating a graphical three-dimensional representation of a snooker game using
 * the JMonkeyEngine {@link http://www.jMonkeyEngine.com}
 * 
 */
public class SnookerTable3D extends TablePresentation {

	private GameState state;
	private Table table;

	protected Node tableNode = new Node();
	protected Node cueBallNode = new Node();
	protected Node objectBalls = new Node();
	protected Map<BilliardBall, Node> ballNodes;
	protected Cylinder cue;
	protected Node cueNode;
	protected Node aimingPointNode;
	protected Node visualizationNode;
	protected Node ballMarkings;

	private static ShadowedRenderPass sPass = new ShadowedRenderPass();
	private boolean ready, showingAimLine, showMarkings;

	protected float cushionWidth;
	protected float railWidth;
	protected BLabel scoreLabel;
	protected BWindow mainMenu;

	protected ColorRGBA tableGreen = new ColorRGBA(.01f, .5f, .01f, 1);
	protected ColorRGBA railBrown = new ColorRGBA(.25f, .25f, .01f, 1);
	protected ColorRGBA pocketRailBrown = new ColorRGBA(.4f, .4f, .02f, 1);
	protected ColorRGBA cueBrown = new ColorRGBA(.6f, .4f, .11f, 1);

	private MaterialState tableMat, voidMat, railMat, pocketRailMat,
			markingMat, cueMat, pointMat, ghostBallMat, lineMat;
	private TextureState railsState, cushionMatTex, pocketRailState,
			tableCloth;

	private boolean tileLinesSet = false;

	// private Line aimLine = new Line("AimLine",new Vector3f[]{new
	// Vector3f(0,0,0), new Vector3f(0,0,100f)},null,null,null);

	public SnookerTable3D(GameState state) {
		this.state = state;
		this.table = state.getTable();
		ballNodes = new HashMap<BilliardBall, Node>();
		ready = false;
		stencilBits = 4; // we need a minimum stencil buffer at least.
	}

	/**
	 * builds the scene.
	 * 
	 * @see com.jme.app.BaseGame#initGame()
	 */
	@Override
	protected void simpleInitGame() {
		display.getRenderer().setBackgroundColor(ColorRGBA.black);
		createMaterials();
		createTextures();
		tableNode = createTableNode(table);

		// initState(state);

		// tableNode.attachChild(cueBallNode);
		// tableNode.attachChild(objectBalls);

		input.setEnabled(false);

		createGUI();

		// cameraControl = createTableChaser(tableNode);

		rootNode.attachChild(tableNode); // Put it in the scene graph
		rootNode.attachChild(BuiSystem.getRootNode());
		rootNode.setRenderState(createLighting());

		sPass.add(rootNode);
		sPass.addOccluder(tableNode);
		sPass.setRenderShadows(false);
		sPass.setLightingMethod(ShadowedRenderPass.LightingMethod.Additive);

		rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);

		pManager.add(sPass);

		cam.setUp(new Vector3f(0, 0, 1));
		cameraControl = null;
		ready = true;
	}

	@Override
	public void initBalls()
	{
		for (Node ballNode : ballNodes.values())
		{
			ballNode.removeFromParent();
		}
		cueBallNode = addBall(state.getCueBall());
		ballNodes.put(state.getCueBall(), cueBallNode);
		// objectBalls.detachAllChildren();
		for (BilliardBall ball : state.getBalls()) {
			if (!ball.equals(state.getCueBall())) {
				ballNodes.put(ball, addBall(ball));
			}
		}
		tableNode.updateRenderState();
	}

	public void updateScores() {
		if (state.getPlayers() == null) {
			return;
		}
		StringBuffer text = new StringBuffer();
		for (Player p : state.getPlayers()) {
			if (p == state.getCurrentPlayer()) {
				text.append("   [ ");
			} else {
				text.append("       ");
			}
			text.append(p.getName() + " | " + p.getScore());
			if (p == state.getCurrentPlayer()) {
				text.append(" ]   ");
			} else {
				text.append("       ");
			}
		}
		scoreLabel.setText(text.toString());

	}

	protected void createGUI() {
		BStyleSheet style;
		try {
			style = new BStyleSheet(new FileReader("style/style.bss"),
					new DefaultResourceProvider());
			BuiSystem.init(new PolledRootNode(timer, input), style);

			BuiSystem.addWindow(createMainMenu());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected BWindow createMainMenu() {
		mainMenu = new BWindow(BuiSystem.getStyle(), GroupLayout.makeHStretch());
		mainMenu.setSize(this.settings.getWidth(), 20);
		mainMenu.setLocation(0, this.settings.getHeight() - 30);
		// mainMenu.setStyleClass("scoreWindow");
		scoreLabel = new BLabel("");
		// scoreLabel = new BLabel("", "scoreLabel");
		mainMenu.add(scoreLabel);
		return mainMenu;
	}

	protected Node createTableNode(Table table) {
		Node tableNode = new Node("Table node");
		// tableNode.setLocalTranslation(new Vector3f(0,0,0));

		float tableWidth = table.getWidth();
		float tableLength = table.getLength();

		cushionWidth = table.getPocketCornerRadius();
		railWidth = table.getPocketRadius() * 2f;

		float minX = -tableWidth / 2f;
		float maxX = tableWidth / 2f;
		float minY = -tableLength / 2f;
		float maxY = tableLength / 2f;

		float minZ = -.05f;
		float maxZ = .05f;

		float pocketRadius = table.getPocketRadius();
		float pocketCornerRadius = table.getPocketCornerRadius();

		float pocketRailWidth = railWidth + cushionWidth - 2 * pocketRadius;

		/*
		 * TABLE CLOTH
		 */

		Box b = new Box("Table", new Vector3f(0, 0, -.001f), tableWidth / 2f
				+ table.getPocketRadius(), tableLength / 2f
				+ table.getPocketRadius(), .001f);
		// Give the box a bounds object to allow it to be culled
		b.setModelBound(new BoundingBox());
		// Calculate the best bounds for the object you gave it
		b.updateModelBound();

		b.setRenderState(tableMat);
		b.setRenderState(tableCloth);
		tableNode.attachChild(b);

		Box ground = new Box("Table", new Vector3f(0, 0, -.001f), tableWidth
				/ 2f + cushionWidth + railWidth - pocketRailWidth, tableLength
				/ 2f + cushionWidth + railWidth - pocketRailWidth, .001f);
		ground.getLocalTranslation().setZ(-.001f);
		ground.setRenderState(voidMat);
		tableNode.attachChild(ground);

		/*
		 * CUSHIONS
		 */

		float middleCushionOffset = pocketRadius + pocketCornerRadius;
		float cornerCushionOffset = FastMath.sqrt(2) * pocketRadius
				+ (FastMath.sqrt(2) - 1) * pocketCornerRadius;

		float cushionMinX = minX + cornerCushionOffset;
		float cushionMaxX = maxX - cornerCushionOffset;

		float cushionMinY = minY + cornerCushionOffset;
		float cushionMaxY = maxY - cornerCushionOffset;

		// names are wrong because x dimension is mirrored?
		Box bottomCushion = new Box("Bottom cushion", new Vector3f(cushionMinX,
				minY - cushionWidth, minZ), new Vector3f(cushionMaxX, minY,
				maxZ));
		Box topCushion = new Box("Top cushion", new Vector3f(cushionMinX, maxY,
				minZ), new Vector3f(cushionMaxX, maxY + cushionWidth, maxZ));
		Box bottomLeftCushion = new Box("Bottom left cushion", new Vector3f(
				minX - cushionWidth, cushionMinY, minZ), new Vector3f(minX,
				-middleCushionOffset, maxZ));
		Box topLeftCushion = new Box("Top left cushion", new Vector3f(minX
				- cushionWidth, middleCushionOffset, minZ), new Vector3f(minX,
				cushionMaxY, maxZ));
		Box bottomRightCushion = new Box("Bottom right cushion", new Vector3f(
				maxX, cushionMinY, minZ), new Vector3f(maxX + cushionWidth,
				-middleCushionOffset, maxZ));
		Box topRightCushion = new Box("Top right cushion", new Vector3f(maxX,
				middleCushionOffset, minZ), new Vector3f(maxX + cushionWidth,
				cushionMaxY, maxZ));

		Box[] cushions = new Box[] { bottomCushion, topCushion,
				bottomLeftCushion, topLeftCushion, bottomRightCushion,
				topRightCushion };

		MaterialState cushionMat = tableMat;

		for (Box cushion : cushions) {
			cushion.setRenderState(cushionMat);
			cushion.setRenderState(cushionMatTex);
			tableNode.attachChild(cushion);
		}

		/*
		 * RAILS
		 */

		float cornerRailOffset = FastMath.sqrt(2) * pocketRadius
				- (2 - FastMath.sqrt(2)) * pocketCornerRadius;

		float railMinX = minX + cornerRailOffset;
		float railMaxX = maxX - cornerRailOffset;

		float railMinY = minY + cornerRailOffset;
		float railMaxY = maxY - cornerRailOffset;

		// names are wrong because x dimension is mirrored?
		Box bottomRail = new Box("Bottom rail", new Vector3f(railMinX, minY
				- cushionWidth - railWidth, minZ), new Vector3f(railMaxX, minY
				- cushionWidth, maxZ));
		Box topRail = new Box("Top rail", new Vector3f(railMinX, maxY
				+ cushionWidth, minZ), new Vector3f(railMaxX, maxY
				+ cushionWidth + railWidth, maxZ));
		Box bottomLeftRail = new Box("Bottom left rail", new Vector3f(minX
				- cushionWidth - railWidth, railMinY, minZ), new Vector3f(minX
				- cushionWidth, -pocketRadius, maxZ));
		Box topLeftRail = new Box("Top left rail", new Vector3f(minX
				- cushionWidth - railWidth, pocketRadius, minZ), new Vector3f(
				minX - cushionWidth, railMaxY, maxZ));
		Box bottomRightRail = new Box("Bottom right rail", new Vector3f(maxX
				+ cushionWidth, railMinY, minZ), new Vector3f(maxX
				+ cushionWidth + railWidth, -pocketRadius, maxZ));
		Box topRightRail = new Box("Top right rail", new Vector3f(maxX
				+ cushionWidth, pocketRadius, minZ), new Vector3f(maxX
				+ cushionWidth + railWidth, railMaxY, maxZ));

		Box[] rails = new Box[] { bottomRail, topRail, bottomLeftRail,
				topLeftRail, bottomRightRail, topRightRail };

		for (Box rail : rails) {
			rail.setRenderState(railMat);
			rail.setRenderState(railsState);
			tableNode.attachChild(rail);
		}

		/*
		 * POCKETS
		 */

		for (Pocket pocket : table.getPockets()) {
			Cylinder pocketNode = new Cylinder("Pocket", 15, 30, pocket
					.getRadius(), .005f, true);
			pocketNode.setLocalTranslation(pocket.getPosition().subtract(
					new Vector3f(tableWidth / 2f, tableLength / 2f, 0)));
			pocketNode.setRenderState(voidMat);
			tableNode.attachChild(pocketNode);
		}

		/*
		 * POCKET CORNERS
		 */

		for (PocketCorner pocketCorner : table.getPocketCorners()) {
			Cylinder pocketCornerNode = new Cylinder("Pocket corner", 15, 15,
					pocketCorner.getRadius(), maxZ, true);
			pocketCornerNode.setLocalTranslation(pocketCorner.getPosition()
					.subtract(
							new Vector3f(tableWidth / 2f, tableLength / 2f,
									-maxZ / 2f + .0001f)));
			pocketCornerNode.setRenderState(cushionMat);
			pocketCornerNode.setRenderState(cushionMatTex);
			tableNode.attachChild(pocketCornerNode);
		}

		/*
		 * POCKET RAILS
		 */

		int k = 0;
		Box pocketRail;
		Vector3f point1, point2;
		for (int x : new int[] { -1, 1 }) {
			for (int y : new int[] { -1, 1 }) {
				point1 = new Vector3f(
						// x*(tableWidth/2f + cushionWidth + railWidth -
						// pocketRailWidth),
						x * (tableWidth / 2f + cushionWidth + railWidth),
						y * (tableLength / 2f + cushionWidth + railWidth), minZ);
				point2 = new Vector3f(
						x * (tableWidth / 2f - cornerRailOffset),
						y
								* (tableLength / 2f + cushionWidth + railWidth - pocketRailWidth),
						maxZ);

				pocketRail = new Box("Pocket rail " + k++, new Vector3f(Math
						.min(point1.x, point2.x), Math.min(point1.y, point2.y),
						Math.min(point1.z, point2.z)), new Vector3f(Math.max(
						point1.x, point2.x), Math.max(point1.y, point2.y), Math
						.max(point1.z, point2.z)));

				pocketRail.setRenderState(pocketRailMat);
				pocketRail.setRenderState(pocketRailState);
				tableNode.attachChild(pocketRail);

				point1 = new Vector3f(
						x * (tableWidth / 2f + cushionWidth + railWidth),
						y
								* (tableLength / 2f + cushionWidth + railWidth - pocketRailWidth),
						minZ);
				point2 = new Vector3f(
						x
								* (tableWidth / 2f + cushionWidth + railWidth - pocketRailWidth),
						y * (tableLength / 2f - cornerRailOffset), maxZ);

				pocketRail = new Box("Pocket rail " + k++, new Vector3f(Math
						.min(point1.x, point2.x), Math.min(point1.y, point2.y),
						Math.min(point1.z, point2.z)), new Vector3f(Math.max(
						point1.x, point2.x), Math.max(point1.y, point2.y), Math
						.max(point1.z, point2.z)));

				pocketRail.setRenderState(pocketRailMat);
				pocketRail.setRenderState(pocketRailState);

				tableNode.attachChild(pocketRail);
			}

			point1 = new Vector3f(x
					* (tableWidth / 2f + cushionWidth + railWidth),
					-pocketRadius, minZ);
			point2 = new Vector3f(
					x
							* (tableWidth / 2f + cushionWidth + railWidth - pocketRailWidth),
					pocketRadius, maxZ);

			pocketRail = new Box("Pocket rail " + k++, new Vector3f(Math.min(
					point1.x, point2.x), Math.min(point1.y, point2.y), Math
					.min(point1.z, point2.z)), new Vector3f(Math.max(point1.x,
					point2.x), Math.max(point1.y, point2.y), Math.max(point1.z,
					point2.z)));

			pocketRail.setRenderState(pocketRailMat);
			pocketRail.setRenderState(pocketRailState);

			tableNode.attachChild(pocketRail);
		}

		/*
		 * MARKINGS
		 */

		Line baulkLine = new Line("baulkLine", new Vector3f[] {
				new Vector3f(-tableWidth / 2f, -tableLength / 2f
						+ table.getBaulkLineDistance(), .0001f),
				new Vector3f(tableWidth / 2f, -tableLength / 2f
						+ table.getBaulkLineDistance(), .0001f) }, null, null,
				null);
		baulkLine.setLineWidth(3f);
		baulkLine.setAntialiased(true);

		// ColorRGBA markingColor = ColorRGBA.white;
		// ColorRGBA markingColor = new ColorRGBA(.5f, .9f, .5f, .5f);

		baulkLine.setRenderState(markingMat);

		tableNode.attachChild(baulkLine);

		// sample some points for the half circle
		int samples = 50;
		Vector3f[] halfCircle = new Vector3f[samples + 1];
		float step = (float) ((-Math.PI) / samples);
		float radius = table.getDRadius();
		for (int i = 0; i < halfCircle.length; i++) {
			double t = step * i;
			float x = (float) (radius * Math.cos(t));
			float y = (float) (radius * Math.sin(t));
			halfCircle[i] = new Vector3f(x, y, 0);
		}
		Line d = new Line("HalfCircle", halfCircle, null, null, null);
		d.setMode(Mode.Connected);
		d.setLocalTranslation(0, -tableLength / 2f
				+ table.getBaulkLineDistance(), 0);

		d.setLineWidth(3f);
		d.setAntialiased(true);

		d.setRenderState(markingMat);

		tableNode.attachChild(d);

		return tableNode;
	}

	private ColorRGBA getBallColor(BilliardBall ball) {
		ColorRGBA color;
		switch (ball.getType()) {
		case CUE:
			color = new ColorRGBA(.9f, .9f, .9f, 1);
			break;
		case RED:
			color = new ColorRGBA(1f, 0.0f, 0.0f, 1);
			break;
		case YELLOW:
			color = new ColorRGBA(1f, 1f, 0, 1);
			break;
		case GREEN:
			color = ColorRGBA.green.clone();
			break;
		case BROWN:
			color = ColorRGBA.brown.clone();
			break;
		case BLUE:
			color = ColorRGBA.blue.clone();
			break;
		case PINK:
			color = ColorRGBA.pink.clone().multLocal(.8f);
			break;
		case BLACK:
			color = new ColorRGBA(.05f, .05f, .05f, 1);
			break;
		default:
			color = ColorRGBA.gray;
		}
		return color;
	}

	protected Node addBall(BilliardBall ball) {
		ColorRGBA color = getBallColor(ball);
		Node ballNode = createBallNode((-table.getWidth() / 2f)
				+ ball.getPosition().x, (-table.getLength() / 2f)
				+ ball.getPosition().y, ball.getRadius(), color);
		tableNode.attachChild(ballNode);
		return ballNode;
	}

	protected Node createBallNode(float x, float y, float radius,
			ColorRGBA ballColor) {
		Node ballNode = new Node("Ball");
		Sphere s = new Sphere("Ball Sphere", 30, 30, radius);
		s.setModelBound(new BoundingBox());
		s.updateModelBound();

		// s.setVBOInfo(new VBOInfo(true));

		MaterialState ballMat = display.getRenderer().createMaterialState();

		ColorRGBA typeColor = ballColor;

		ballMat.setAmbient(ColorRGBA.darkGray.clone());
		ballMat.setDiffuse(typeColor.clone());
		ballMat.setSpecular(ColorRGBA.lightGray.clone());
		ballMat.setShininess(128.0f);
		ballMat.setEmissive(ColorRGBA.black.clone());
		ballMat.setEnabled(true);

		s.setRenderState(ballMat);
		// sPass.addOccluder(s);

		ballNode.setLocalTranslation(new Vector3f(x, y, radius));
		ballNode.attachChild(s);
		return ballNode;
	}

	protected LightState createLighting() {
		/** Set up a basic, default light. */
		PointLight light = new PointLight();
		// light.setAngle(25);
		light.setLocation(new Vector3f(0, 3f, 4.5f));
		light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.5f));
		light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
		light.setAttenuate(true);

		light.setEnabled(true);
		light.setShadowCaster(true);

		/** Set up a basic, default light. */
		PointLight light2 = new PointLight();
		// light.setAngle(25);
		light2.setLocation(new Vector3f(0, -3f, 4.5f));
		light2.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.5f));
		light2.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
		light2.setAttenuate(true);
		light2.setEnabled(true);
		light2.setShadowCaster(true);

		/** Attach the light to a lightState and the lightState to rootNode. */
		LightState lightState = display.getRenderer().createLightState();
		lightState.setEnabled(true);
		lightState.attach(light);
		lightState.attach(light2);
		lightState.setSeparateSpecular(true);

		return lightState;
	}

	@Override
	protected void simpleUpdate() {
		super.simpleUpdate();
		pManager.updatePasses(tpf);
		updateScores();
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public Node getCueBallNode() {
		return cueBallNode;
	}

	@Override
	public Node getTableNode() {
		return tableNode;
	}

	@Override
	public void removeCue() {
		tableNode.detachChild(cueNode);
	}

	@Override
	public Node setCueToBall(Node ball) {
		cue = new Cylinder("Cue", 30, 30, .006f, .6f, true);

		cue.setRenderState(cueMat);
		cue.setCastsShadows(false);

		Sphere aimingPoint = new Sphere("Aiming point", 5, 5, .0025f);

		// aimingPoint.setCastsShadows(false);
		aimingPoint.setRenderState(pointMat);

		cueNode = new Node();
		aimingPointNode = new Node();
		cueNode.setLocalTranslation(ball.getLocalTranslation());
		cueNode.attachChild(cue);
		cueNode.attachChild(aimingPointNode);

		tableNode.attachChild(cueNode);
		aimingPointNode.attachChild(aimingPoint);
		aimingPoint.setLocalTranslation(0, 0, -table.getBallRadius());
		cueNode.getLocalRotation().fromAngles(FastMath.PI / 2, 0, FastMath.PI);

		cue.setLocalTranslation(0, 0, -.35f);

		cueNode.updateRenderState();

		return cueNode;
	}

	@Override
	public void rotateCue(float x, float y) {
		cueNode.getLocalRotation().fromAngles(y + FastMath.PI / 2, 0,
				x + FastMath.PI);
	}

	@Override
	public void transposeCue(float x, float y) {
		cue.getLocalTranslation().x = -x;
		cue.getLocalTranslation().y = y;
		aimingPointNode.getLocalRotation().fromAngles(
				FastMath.asin(y / table.getBallRadius()),
				-FastMath.asin(-x / table.getBallRadius()), 0);

	}

	@Override
	public void setCueDistance(float dist) {
		cue.getLocalTranslation().z = -.35f - dist;
	}

	@Override
	public void updateBall(BilliardBall ball) {
		Node b = ballNodes.get(ball);
		b.getLocalTranslation().x = ball.getPosition().x - table.getWidth() / 2;
		b.getLocalTranslation().y = ball.getPosition().y - table.getLength()
				/ 2;
	}



	@Override
	public void showShotVisualization(boolean enabled) {
		showingAimLine = enabled;
		if (!enabled) {
			rootNode.detachChild(visualizationNode);

		}
	}

	@Override
	public void updateShotVisualization(ArrayList<ArrayList<Vector3f>> points) {
		if (showingAimLine) {
			rootNode.detachChild(visualizationNode);
			visualizationNode = new Node("VisNode");
			rootNode.attachChild(visualizationNode);
			for (int i = 0; i < points.size(); i++) {
				ArrayList<Vector3f> list = points.get(i);
				Vector3f[] pointsList = new Vector3f[1];
				pointsList = list.toArray(pointsList);

				Line aimLine = new Line("AimLine " + i, pointsList, null, null,
						null);
				aimLine.setLineWidth(1);
				aimLine.setAntialiased(false);
				aimLine.setMode(Mode.Connected);
				visualizationNode.attachChild(aimLine);
				aimLine.setRenderState(lineMat);
				aimLine.updateRenderState();

				Sphere ghostSphere = new Sphere("Ghost Ball Sphere " + i, 30,
						30, table.getBallRadius());
				visualizationNode.attachChild(ghostSphere);
				ghostSphere.setModelBound(new BoundingBox());
				ghostSphere.updateModelBound();
				ghostSphere.setRenderState(ghostBallMat);
				ghostSphere.updateRenderState();
				ghostSphere.setCastsShadows(true);

				BlendState alphaState = DisplaySystem.getDisplaySystem()
						.getRenderer().createBlendState();
				alphaState.setBlendEnabled(true);
				alphaState
						.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
				alphaState
						.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
				alphaState.setTestEnabled(true);
				alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
				alphaState.setEnabled(true);

				ghostSphere.setRenderState(alphaState);
				ghostSphere.updateRenderState();

				ghostSphere.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

				ghostSphere
						.setLocalTranslation(pointsList[pointsList.length - 1]);
			}

		}

	}

	@Override
	public void updateTileVisualization(Set<BilliardBall> balls) {
		rootNode.detachChild(visualizationNode);
		visualizationNode = new Node("VisNode");
		rootNode.attachChild(visualizationNode);
		for (BilliardBall b : balls) {

			Vector3f x1 = b.getPosition().clone();
			x1.x -= table.getWidth() * .5f;
			x1.y -= table.getLength() * .5f;
			x1.z = b.getRadius();

			for (Tile t : b.getTiles()) {
				Vector3f x2 = new Vector3f(t.getFromX() + .5f * t.getWidth(), t
						.getFromY()
						+ .5f * t.getLength(), b.getRadius());

				x2.x -= table.getWidth() * .5f;
				x2.y -= table.getLength() * .5f;

				Vector3f[] parray = { x1, x2 };
				Line l = new Line("foo", parray, null, null, null);

				l.setLineWidth(1);
				l.setAntialiased(false);
				l.setMode(Mode.Connected);
				visualizationNode.attachChild(l);
				l.setRenderState(lineMat);
				l.updateRenderState();
			}
		}
	}

	public void showMarkings(boolean enabled) {
		showMarkings = enabled;
		if (!enabled) {
			rootNode.detachChild(ballMarkings);
		}
	}

	private Spatial getMarking(BilliardBall ball, float markingRadius) {
		Node ballNode = this.getBallNode(ball);
		//		
		Tube marking = new Tube("Marking " + ball.getID(), markingRadius,
				markingRadius - 0.003f, 1 / 6f * ball.getRadius());
		Quaternion q = new Quaternion();
		q.fromAngleAxis((float) Math.PI / 2f, new Vector3f(1, 0, 0));
		Vector3f translation = ballNode.getLocalTranslation().clone();
		translation.z = marking.getHeight();
		marking.setLocalRotation(q);
		marking.setLocalTranslation(translation);
		marking.setModelBound(new BoundingBox());

		return marking;
	}

	private MaterialState getMarkingMat(BilliardBall ball) {

		ColorRGBA newColor = this.getBallColor(ball).clone();

		MaterialState markingMat = display.getRenderer().createMaterialState();

		markingMat.setAmbient(ColorRGBA.darkGray.clone());
		markingMat.setDiffuse(newColor);
		markingMat.setSpecular(ColorRGBA.lightGray);
		markingMat.setShininess(100f);
		markingMat.setEmissive(ColorRGBA.black.clone());
		markingMat.setEnabled(true);

		return markingMat;
	}

	public void markBalls(Set<BilliardBall> balls) {
		rootNode.detachChild(ballMarkings);
		ballMarkings = new Node("BallMarkings");
		rootNode.attachChild(ballMarkings);
		for (BilliardBall ball : balls) {
			float markingRadius = ball.getRadius() + 0.005f;
			Spatial marking = getMarking(ball, markingRadius);
			MaterialState markingMat = getMarkingMat(ball);
			ballMarkings.attachChild(marking);
			marking.setRenderState(markingMat);
			marking.updateRenderState();
		}

	}

	public void makeShine(Set<BilliardBall> balls)
	{
		for (Node ballNode : ballNodes.values())
		{
			((MaterialState) ballNode.getChild("Ball Sphere").getRenderState(RenderState.StateType.Material)).setAmbient(ColorRGBA.darkGray.clone());
		}
		for (BilliardBall ball : balls)
		{
			((MaterialState) ballNodes.get(ball).getChild("Ball Sphere").getRenderState(RenderState.StateType.Material)).setAmbient(ColorRGBA.gray.clone());			
		}
	}

	private void createMaterials() {
		tableMat = display.getRenderer().createMaterialState();
		tableMat.setAmbient(ColorRGBA.darkGray);
		tableMat.setDiffuse(tableGreen.clone());
		tableMat.setShininess(0);
		tableMat.setEmissive(ColorRGBA.black.clone());
		tableMat.setEnabled(true);

		voidMat = display.getRenderer().createMaterialState();
		voidMat.setAmbient(ColorRGBA.black);
		voidMat.setDiffuse(ColorRGBA.black.clone());
		voidMat.setSpecular(ColorRGBA.black);
		voidMat.setShininess(0);
		voidMat.setEmissive(ColorRGBA.black.clone());
		voidMat.setEnabled(true);

		railMat = display.getRenderer().createMaterialState();
		railMat.setAmbient(ColorRGBA.darkGray);
		railMat.setDiffuse(railBrown.clone());
		railMat.setSpecular(ColorRGBA.darkGray);
		railMat.setShininess(90f);
		railMat.setEmissive(ColorRGBA.black.clone());
		railMat.setEnabled(true);

		pocketRailMat = display.getRenderer().createMaterialState();
		pocketRailMat.setAmbient(ColorRGBA.darkGray);
		pocketRailMat.setDiffuse(pocketRailBrown.clone());
		pocketRailMat.setSpecular(ColorRGBA.darkGray);
		pocketRailMat.setShininess(20f);
		pocketRailMat.setEmissive(ColorRGBA.black.clone());
		pocketRailMat.setEnabled(true);

		markingMat = display.getRenderer().createMaterialState();
		markingMat.setAmbient(ColorRGBA.lightGray.clone());
		markingMat.setDiffuse(ColorRGBA.darkGray.clone());
		markingMat.setShininess(50f);
		markingMat.setEmissive(ColorRGBA.black);
		markingMat.setEnabled(true);

		cueMat = display.getRenderer().createMaterialState();
		cueMat.setAmbient(ColorRGBA.darkGray);
		cueMat.setDiffuse(cueBrown);
		cueMat.setSpecular(ColorRGBA.brown);
		cueMat.setShininess(0);
		cueMat.setEmissive(ColorRGBA.black);
		cueMat.setEnabled(true);

		pointMat = display.getRenderer().createMaterialState();
		pointMat.setAmbient(ColorRGBA.red.clone());
		pointMat.setDiffuse(ColorRGBA.red.clone());
		pointMat.setSpecular(ColorRGBA.red.clone());
		pointMat.setShininess(.5f);
		pointMat.setEmissive(ColorRGBA.red.clone());
		pointMat.setEnabled(true);

		ghostBallMat = display.getRenderer().createMaterialState();
		float opacityAmount = 0.000001f;
		ghostBallMat.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, opacityAmount));
		ghostBallMat.setDiffuse(new ColorRGBA(.9f, .9f, .9f, opacityAmount));
		ghostBallMat
				.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, opacityAmount));
		ghostBallMat.setShininess(128.0f);
		ghostBallMat
				.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, opacityAmount));
		ghostBallMat.setEnabled(true);
		ghostBallMat.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);

		lineMat = display.getRenderer().createMaterialState();
		lineMat.setAmbient(ColorRGBA.yellow);
		lineMat.setDiffuse(ColorRGBA.black.clone());
		lineMat.setSpecular(ColorRGBA.black);
		lineMat.setShininess(0);
		lineMat.setEmissive(ColorRGBA.black.clone());
		lineMat.setEnabled(true);

	}

	private void createTextures() {
		tableCloth = display.getRenderer().createTextureState();
		Texture cloth = TextureManager.loadTexture("./textures/felt_01.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear);
		cloth.setScale(new Vector3f(1f, 1f, 0));
		cloth.setWrap(Texture.WrapMode.Repeat);
		tableCloth.setTexture(cloth);
		tableCloth.setEnabled(true);

		pocketRailState = display.getRenderer().createTextureState();
		Texture leather = TextureManager.loadTexture("./textures/corner.gif",
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear);
		leather.setScale(new Vector3f(1f, 1f, 0));
		leather.setWrap(Texture.WrapMode.Repeat);
		pocketRailState.setTexture(leather);
		pocketRailState.setEnabled(true);

		cushionMatTex = display.getRenderer().createTextureState();
		Texture felt2 = TextureManager.loadTexture("./textures/felt_02.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear);
		// cloth.setScale(new Vector3f(1f,0.5f,0));
		felt2.setWrap(Texture.WrapMode.Repeat);
		cushionMatTex.setTexture(felt2);
		cushionMatTex.setEnabled(true);

		railsState = display.getRenderer().createTextureState();
		Texture wood1 = TextureManager.loadTexture("./textures/wood1.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear);

		railsState.setTexture(wood1);
		railsState.setEnabled(true);

	}

	@Override
	public Node getBallNode(BilliardBall b) {
		return ballNodes.get(b);
	}

	public void setTiles(Tile[][] tilesArrays) {
		if (!tileLinesSet) {
			tileLinesSet = true;
			for (Tile[] tiles : tilesArrays) {
				for (Tile tile : tiles) {
					Vector3f x = new Vector3f(tile.getLowerBoundX(), tile
							.getLowerBoundY(), 0);
					Vector3f x2 = new Vector3f(tile.getLowerBoundX(), tile
							.getLowerBoundY()
							+ tile.getLength(), 0);

					Vector3f y = new Vector3f(tile.getUpperBoundX(), tile
							.getUpperBoundY(), 0);
					Vector3f y2 = new Vector3f(tile.getUpperBoundX()
							- tile.getWidth(), tile.getUpperBoundY(), 0);

					for (Vector3f point : new Vector3f[] { x, x2, y, y2 }) {
						point.x -= table.getWidth() / 2f;
						point.y -= table.getLength() / 2f;
					}

					Vector3f[] linePoints = new Vector3f[] { x, x2 };
					Line tileLine = new Line("tileLine ", linePoints, null,
							null, null);
					tileLine.setLineWidth(1);
					tileLine.setAntialiased(true);
					tileLine.setMode(Mode.Connected);
					// tileLine.setDefaultColor(ColorRGBA.black);
					rootNode.attachChild(tileLine);
					tileLine.setRenderState(voidMat);
					tileLine.updateRenderState();

					Vector3f[] linePoints2 = new Vector3f[] { y, y2 };
					Line tileLine2 = new Line("tileLine ", linePoints2, null,
							null, null);
					tileLine2.setLineWidth(1);
					tileLine2.setAntialiased(true);
					tileLine2.setMode(Mode.Connected);
					// tileLine2.setDefaultColor(ColorRGBA.black);

					rootNode.attachChild(tileLine2);
					tileLine2.setRenderState(voidMat);
					tileLine2.updateRenderState();
				}
			}
		}

	}

}
