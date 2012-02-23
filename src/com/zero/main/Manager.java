package com.zero.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;

import com.zero.objects.Entity;

public class Manager {

	private static Manager manager;
	private CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<Entity>();
	private HashMap<String, Audio> sounds;
	protected World world = null;
	protected GameContainer container = null;
	protected OBBViewportTransform transform;
	
	private Manager() {
		sounds = new HashMap<String, Audio>();
		try {
			Audio laserSound = AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("res/laser.ogg"));
			sounds.put("laser", laserSound);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void render(GameContainer container, Graphics g) {
		for (Entity entity : entities) {
			entity.draw();
		}
	}

	public void update(GameContainer container, int delta) {
		for (Entity entity : entities) {
			entity.update(container, delta);
		}
	}
	
	public void addEntity(Entity entity) {
			entities.add(entity);
	}
	
	public void removeEntity(Entity entity) {
		entities.remove(entity);
		System.out.println("Remove entity: " + entity.toString());
//		try {
//			entity.destroy();
//		} catch (SlickException e) {
//			e.printStackTrace();
//		}
	}
	
	public void playSound(String key, Float pitch, Float gain, Boolean loop) {
		Audio temp = sounds.get(key);
		if (temp != null) {
			temp.playAsSoundEffect(pitch, gain, loop);
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

	public GameContainer getContainer() {
		return container;
	}

	public void setContainer(GameContainer container) {
		this.container = container;
		transform = new OBBViewportTransform();
	    transform.setYFlip(true);
	    transform.setExtents(container.getWidth() / 2, container.getHeight() / 2);
	}
	
	public Vec2 translateCoordsToWorld(Float x, Float y) {
		Vec2 result = new Vec2(-(container.getWidth() / 2) + x,  container.getHeight() / 2 - y);
		return result;
	}
	
	public Vec2 translateCoordsToScreen(Vec2 coordWorld) {
		Vec2 screen = new Vec2();
        transform.getWorldToScreen(coordWorld, screen);
        return screen;
	}
	
	public Vec2 translateCoordsToScreen(Vec2 coordWorld, Float offsetX, Float offsetY) {
		Vec2 center = translateCoordsToScreen(coordWorld);
		center.x -= offsetX;
		center.y -= offsetY;
		
		return center;
	}
}
