package com.zero.main;

import java.util.concurrent.CopyOnWriteArrayList;

public class GenerateMap {

	protected CopyOnWriteArrayList<Tile> map = new CopyOnWriteArrayList<Tile>();
		

	public void generate(int width, int height) {
		
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				Tile tile = new Tile("x = "+i+" y = "+j);
				tile.setPosition(i, j);
				int sum = i + j;
				if(sum == 0 ){
					tile.setType(0);	
				} else if((sum % 3) == 0) {
					tile.setType(1);	
					
				} else {
					tile.setType(2);	
					
				}
				this.map.add(tile);
				
			}
		}
		
		
	}
	
	public CopyOnWriteArrayList<Tile> getMap() {
		return this.map;
	}
	
}
