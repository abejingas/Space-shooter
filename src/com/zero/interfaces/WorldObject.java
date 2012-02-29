package com.zero.interfaces;

import com.zero.ammunition.Ammunition;

public interface WorldObject {
	public void draw();
	public boolean collision(WorldObject with);
	public void firedAt(Ammunition bullet);
	public void update(float delta);
	public boolean dispose();
}
