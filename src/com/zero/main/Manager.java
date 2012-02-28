package com.zero.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.zero.objects.Entity;



public class Manager implements ContactListener {

	//Pixel to meter ratio
	public static final int PTM = 32;
	public static final float CAMERA_EDGE = 3f;

	private static Manager manager;
	private CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<Entity>();
	private ArrayList<Entity> needsToBeRemoved = new ArrayList<Entity>();
	private ArrayList<Entity> needsToBeAdded = new ArrayList<Entity>();
	private HashMap<String, Sound> sounds;

	protected World world = null;
	protected SpriteBatch batch = null;
	protected RayHandler lightEngine = null;
	protected OrthographicCamera camera;
	protected Entity cameraController;

	protected Map _map;
	private Texture texture;       
	private TextureRegion[] regions = new TextureRegion[4]; // #2

	private Manager() {
		loadSounds();
	}

	public void setMap(Map map) {
		texture = new Texture(Gdx.files.internal("res/hero.png"));
		//batch = new SpriteBatch();
		regions[0] = new TextureRegion(texture, 0, 0, 32, 32);          // #3
		regions[1] = new TextureRegion(texture, 32, 0, 32, 32);    // #4
		regions[2] = new TextureRegion(texture, 0, 32, 32, 32);         // #5
		regions[3] = new TextureRegion(texture, 32, 32, 32, 32);    // #6
		this._map = map;
	}

	public Map getMap() {
		return this._map;
	}


	public void render() {
		int x;
		int y;
		CopyOnWriteArrayList<Tile> map = this._map.getMap();

		for(Tile tile : map) {
			x =  tile.getX();
			y =  tile.getY();
			if(tile.getType() == 0 || tile.getType() == 1) {

				batch.draw(regions[tile.getType()], x, y, 0, 0, 32f, 32f, 1f / (float)Manager.PTM, 1f / (float)Manager.PTM, 90, true);
			} 
		}

		for (Entity entity : entities) {
			entity.draw();
		}
	}

	public void update(float delta) {

		for (Entity entity : needsToBeRemoved) {
			removeEntity(entity);
		}

		needsToBeRemoved.clear();
		
		for (Entity entity : entities) {
			entity.update(delta);
		}
		
		if(!world.isLocked()) {
			for(Entity entity : needsToBeAdded) {
				this.addEntity(entity);
			}
			needsToBeAdded.clear();
		}
	}

	public void addEntity(Entity entity) {
		entities.add(entity);
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
		if (entity.getBody() != null) {
			world.destroyBody(entity.getBody());
			entity.removeLights();
		}
	}

	public Sound playSound(String key, Float pitch, Float gain, Boolean loop) {
		Sound temp = sounds.get(key);

		if (temp != null) {
			long id;
			if (loop) {
				id = temp.loop(gain);
			} else {
				id = temp.play(gain);
			}
			temp.setPitch(id, pitch);
		}

		return temp;
	}

	public void stopSound(String key) {
		Sound temp = sounds.get(key);
		if (temp != null) {
			temp.stop();
		}
	}

	public static Manager getInstance() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public Vector2 translateCoordsToWorld(Float x, Float y) {
		Vector2 result = new Vector2(-(Gdx.graphics.getWidth() / 2) + x,  Gdx.graphics.getHeight() / 2 - y);
		return result;
	}

	public Vector2 translateCoordsToScreen(Vector2 coordWorld) {
		Float screenX = coordWorld.x;
		Float screenY = -coordWorld.y;
		return new Vector2(screenX, screenY);
	}

	public Vector2 translateCoordsToScreen(Vector2 coordWorld, Float offsetX, Float offsetY) {
		Vector2 center = translateCoordsToScreen(coordWorld);
		center.x -= offsetX;
		center.y -= offsetY;

		return center;
	}

	private void loadSounds() {
		sounds = new HashMap<String, Sound>();

		Sound laser = (Sound) Gdx.audio.newSound(Gdx.files.internal("res/laser.ogg"));
		sounds.put("laser", laser); 

		Sound thruster = (Sound) Gdx.audio.newSound(Gdx.files.internal("res/thrust.ogg"));
		sounds.put("thruster", thruster); 

		Sound hit = (Sound) Gdx.audio.newSound(Gdx.files.internal("res/hit.ogg"));
		sounds.put("hit", hit);
		
		Sound turbo = (Sound) Gdx.audio.newSound(Gdx.files.internal("res/turbo.ogg"));
		sounds.put("turbo", turbo);
	}

	@Override
	public void beginContact(Contact contact) {
		
	}

	public void addEntityNext(Entity entity) {
		needsToBeAdded.add(entity);
	}
	
	public void removeEntityNex(Entity entity) {
		needsToBeRemoved.add(entity);
	}
	
	@Override
	public void endContact(Contact contact) {
		Body a = contact.getFixtureA().getBody();
		Body b = contact.getFixtureB().getBody();

		  if (b.getUserData() != null 
		    && a.getUserData() != null 
		    && needsToBeRemoved.indexOf((Entity)b.getUserData()) == -1) {

		   Entity caller = (Entity)b.getUserData();
		   Entity receiver = (Entity)a.getUserData();
		   Boolean shouldRemove = caller.collision(receiver);

		   if (shouldRemove) {
		    needsToBeRemoved.add(caller);
		   }
		  }
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}

	public SpriteBatch getBatch() {
		return batch;
	}

	public void setBatch(SpriteBatch batch) {
		this.batch = batch;
	}

	public RayHandler getLightEngine() {
		return lightEngine;
	}

	public void setLightEngine(RayHandler lightEngine) {
		this.lightEngine = lightEngine;
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public void setCamera(OrthographicCamera camera) {
		this.camera = camera;
	}
	
	public void clampCameraTo(Entity controller) {
		this.cameraController = controller;
	}
	
	public void updateCameraPosition() {
		if (camera == null || cameraController == null || cameraController.getBody() == null) {
			return;
		}
		
		Ray posBorder = camera.getPickRay(Gdx.graphics.getWidth(), 0);
		
		Vector2 translateV = new Vector2();
		
		if (posBorder.origin.x - cameraController.getBody().getPosition().x < CAMERA_EDGE) {
			translateV.x = CAMERA_EDGE - (posBorder.origin.x - cameraController.getBody().getPosition().x);
		} 
		if (posBorder.origin.y - cameraController.getBody().getPosition().y < CAMERA_EDGE) {
			translateV.y = CAMERA_EDGE - (posBorder.origin.y - cameraController.getBody().getPosition().y);
		}	
		
		camera.translate(translateV.x, translateV.y, 0f);
		translateV.set(0f, 0f);
		
		Ray negBorder = camera.getPickRay(0, Gdx.graphics.getHeight());
		if ( Math.abs(negBorder.origin.x - cameraController.getBody().getPosition().x) < CAMERA_EDGE) {
			translateV.x = -(CAMERA_EDGE - Math.abs(negBorder.origin.x - cameraController.getBody().getPosition().x));
		}
		if ( Math.abs(negBorder.origin.y - cameraController.getBody().getPosition().y) < CAMERA_EDGE) {
			translateV.y = -(CAMERA_EDGE - Math.abs(negBorder.origin.y - cameraController.getBody().getPosition().y));
		} 
		camera.translate(translateV.x, translateV.y, 0f);
		camera.update();
	}
}
