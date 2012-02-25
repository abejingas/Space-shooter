package com.zero.objects;


import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.zero.main.PolygonParser;

public class Plane extends Entity 
{	
	public Plane(String ref, Float x, Float y) throws SlickException {
		super(ref, x, y);
		angleDifference = 180;
	}

	public static final int SHOT_DELAY = 200;
	public static final float ROTATE_SPEED_FACTOR = 0.03f;
	
	//@TODO: TO IMPLEMENT MAX!
	//public static final float THRUSTER_MAX = 1f;
    public static final float THRUSTER_FACTOR = 1.5f;
    public static final float REV_THRUSTER_FACTOR = 0.5f;
        
	private Boolean shotDelayOn = false;
	int shotCounter = 0;

	@Override
	public void updatePosition(GameContainer container, int delta) {
		
		//Parse user input, shouldn't be here!
		Input input = container.getInput();
		if (shotDelayOn && shotCounter < SHOT_DELAY) {
			shotCounter += delta;
		} else if(shotDelayOn && shotCounter >= SHOT_DELAY) {
			shotDelayOn = false;
			shotCounter = 0;
		}
                    
		if (input.isKeyDown(Input.KEY_A)) {
			body.applyAngularImpulse(50f);
		}
		if (input.isKeyDown(Input.KEY_D)) {
			body.applyAngularImpulse(-50f);
		}
		
		if (input.isKeyDown(Input.KEY_W)) {
			body.applyLinearImpulse(getThrustVector(false), body.getWorldCenter());
			manager.playSoundIfNotStarted("thruster", 1f, 0.2f, true);
		} else if(input.isKeyPressed(Input.KEY_W)) {
			manager.stopSound("thruster");
		}
		
		if(input.isKeyDown(Input.KEY_S)) {
			body.applyLinearImpulse(getThrustVector(true), body.getWorldCenter());
			manager.playSoundIfNotStarted("thruster", 3f, 0.1f, true);
		} else if(input.isKeyPressed(Input.KEY_S)) {
			manager.stopSound("thruster");
		}
		
		if (input.isKeyDown(Input.KEY_SPACE) && !shotDelayOn) {
			
				Vec2 point = body.getWorldPoint(new Vec2(0, -88) );
				
				try {
					Bullet box = new Bullet("res/laser.png", point.x, point.y, body.getAngle(), this);
					manager.addEntity(box);
				} catch (SlickException e) {
					e.printStackTrace();
				}
				
				shotDelayOn = true;
		}
	}
	
	private Vec2 getThrustVector(Boolean reverse) {
		double rads = body.getAngle() + Math.toRadians(90);
		
		double factor;
		if (reverse) {
			factor = REV_THRUSTER_FACTOR;
		} else {
			factor = THRUSTER_FACTOR;
		}
		
		//x + d * cos(a)  y + d.sin(a)
		double x = factor * Math.cos(rads);
		double y = factor * Math.sin(rads);
		
		Vec2 vector = new Vec2((float)x, (float)y);
		if (!reverse) {
			return vector.mul(-1f);
		}
		
		return vector; 
	}
        
    //Create physic based structures, body, shape, fixture
    //and registers physics body to physics world
	@Override
	public void createPhysicsBody() {
		bodyDef = new BodyDef();
		bodyDef.position = manager.translateCoordsToWorld(x, y);
		bodyDef.type = BodyType.DYNAMIC;
		body = manager.getWorld().createBody(bodyDef);
	
		PolygonParser pp = new PolygonParser();
		
		pp.parseEntity("plane", body);
		
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.2f);
        body.setTransform(body.getPosition(), (float)Math.toRadians(180));
        body.setUserData(this);
	}

	@Override
	public Boolean collision(Entity with) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void hit() {
		// TODO Auto-generated method stub
		
	}
}
