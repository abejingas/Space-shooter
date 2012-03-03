package com.zero.main;


import org.lwjgl.opengl.Display;

import box2dLight.RayHandler;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.zero.objects.Enemy;
import com.zero.objects.Player;

public class SpaceShooter implements ApplicationListener {

	private final static int MAX_FPS = 200;
	private final static int MIN_FPS = 120;
	public final static float TIME_STEP = 1f / MAX_FPS;
	private final static float MAX_STEPS = 1f + MAX_FPS / MIN_FPS;
	private final static float MAX_TIME_PER_FRAME = TIME_STEP * MAX_STEPS;
	private final static int VELOCITY_ITERS = 4;
	private final static int POSITION_ITERS = 4;

	float physicsTimeLeft;

	private SpriteBatch spriteBatch;
	private World world;
	private Box2DDebugRenderer renderer;
	private Manager manager;
	private RayHandler lightEngine;
	private Player player;
	private Enemy enemy;
	private BitmapFont font;
	private Matrix4 normalProjection = new Matrix4();

	@Override
	public void create() {
		world = new World(new Vector2(0, 0), true);
		renderer = new Box2DDebugRenderer(true, true, true, true);
		spriteBatch = renderer.batch;

		this.createLights();	
		manager = Manager.getInstance();

		manager.setWorld(world);
		manager.setBatch(spriteBatch);
		manager.setLightEngine(lightEngine);

		world.setContactListener(manager);
		
		player = new Player();
		manager.clampCameraTo(player.getShip());
		
		enemy = new Enemy();		
	
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
	}

	private void createLights() {
		RayHandler.setColorPrecisionHighp();
		RayHandler.setGammaCorrection(true);

		lightEngine = new RayHandler(world);
		lightEngine.setAmbientLight(0.3f);
		lightEngine.setCulling(true);
		lightEngine.setBlur(true);
		lightEngine.setBlurNum(10);
	}

	@Override
	public void resize(int width, int height) {}

	@Override
	public void render() {
		//Some strange way to limit fps
		Display.sync(200);
		manager.updateCameraPosition();
		
		boolean stepped = fixedStep(Gdx.graphics.getDeltaTime());
		
		float delta  = Gdx.graphics.getDeltaTime();
		player.update(delta);
		enemy.update(delta);
		manager.update(delta);
		
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		
		//renderer.render(world, manager.getCamera(true).combined);
		
		manager.switchCamera(false);
		spriteBatch.begin();
			manager.render();
		spriteBatch.end();

		
		this.renderLights(stepped);
		manager.switchCamera(false);
		spriteBatch.begin();
			manager.renderEmmiters();
		spriteBatch.end();
		
		
		spriteBatch.setProjectionMatrix(normalProjection);
		spriteBatch.begin();

		
		font.setColor(Color.WHITE);
		font.draw(spriteBatch, "FPS: " + Integer.toString(Gdx.graphics.getFramesPerSecond())
				+ " - GLes 2.0: " + Gdx.graphics.isGL20Available()
				+ " - Heap size: "
				+ Math.round(Gdx.app.getJavaHeap() / 1024 / 1024) + " M"
				+ " - Native heap size: "
				+ Math.round(Gdx.app.getNativeHeap() / 1024 / 1024) + " M", 10, 20);
		
		if (player.getShip().getEnergyLevel() < 30) {
			font.setColor(Color.RED);
		} else {
			font.setColor(Color.BLUE);
		}
		
		font.draw(spriteBatch, "Energy level: " + player.getShip().getEnergyLevel(), 10, Gdx.graphics.getHeight() - 20f);
		spriteBatch.end();
	}

	private void renderLights(Boolean worldSteped) {
		if (worldSteped) {
			lightEngine.update();
		}

		OrthographicCamera camera = manager.getCamera(true);
		
		lightEngine.setCombinedMatrix(camera.combined, camera.position.x,
				camera.position.y, camera.viewportWidth * camera.zoom,
				camera.viewportHeight * camera.zoom);

		lightEngine.render();
	}

	private boolean fixedStep(float delta) {
		physicsTimeLeft += delta;
		if (physicsTimeLeft > MAX_TIME_PER_FRAME)
			physicsTimeLeft = MAX_TIME_PER_FRAME;

		boolean stepped = false;
		while (physicsTimeLeft >= TIME_STEP) {
			world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);
			physicsTimeLeft -= TIME_STEP;
			stepped = true;
		}
		return stepped;
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void dispose() {}
}
