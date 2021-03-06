package com.scs.overwatch.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.overwatch.Overwatch;
import com.scs.overwatch.Settings;
import com.scs.overwatch.components.ICollideable;
import com.scs.overwatch.components.IProcessable;
import com.scs.overwatch.modules.GameModule;

public abstract class AbstractPlatform extends PhysicalEntity implements IProcessable, ICollideable {

	public static final float HEIGHT = 0.3f;

	public AbstractPlatform(Overwatch _game, GameModule _module, float x, float y, float z, float w, float d, float rotDegrees) {
		super(_game, _module, "AbstractPlatform");

		Box box1 = new Box(w/2, HEIGHT/2, d/2);
		//box1.scaleTextureCoordinates(new Vector2f(WIDTH, HEIGHT));
		Geometry geometry = new Geometry("AbstractPlatform", box1);
		//TextureKey key3 = new TextureKey("Textures/crate.png");
		TextureKey key3 = null;
		if (Settings.NEON) {
			key3 = new TextureKey("Textures/tron_blue.jpg");
		} else {
			key3 = new TextureKey("Textures/boxes and crates/3.png");
			
		}
			
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material floor_mat = null;
		if (Settings.LIGHTING) {
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
		} else {
			floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			floor_mat.setTexture("ColorMap", tex3);
		}
		geometry.setMaterial(floor_mat);
		//floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		//geometry.setQueueBucket(Bucket.Transparent);

		this.main_node.attachChild(geometry);
		if (rotDegrees != 0) {
			float rads = (float)Math.toRadians(rotDegrees);
			main_node.rotate(0, rads, 0);
		}
		main_node.setLocalTranslation(x+(w/2), y+(HEIGHT/2), z+0.5f);

		floor_phy = new RigidBodyControl(1);
		main_node.addControl(floor_phy);
		module.getBulletAppState().getPhysicsSpace().add(floor_phy);

		geometry.setUserData(Settings.ENTITY, this);
		floor_phy.setUserObject(this);

		module.addEntity(this);

		floor_phy.setKinematic(true);
		//floor_phy.setKinematicSpatial(geometry);

	}


/*	@Override
	public void process(float tpf) {
		//this.floor_phy.setPhysicsLocation(location)
	}
*/
/*
	@Override
	public void remove() {
		super.remove();
		this.module.bulletAppState.getPhysicsSpace().remove(this.floor_phy);

	}
*/

}
