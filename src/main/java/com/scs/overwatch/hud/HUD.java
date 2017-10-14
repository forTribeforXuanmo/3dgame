package com.scs.overwatch.hud;

import java.util.ArrayList;
import java.util.List;

import ssmith.lang.NumberFunctions;

import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;
import com.scs.overwatch.Overwatch;
import com.scs.overwatch.Settings;
import com.scs.overwatch.components.IEntity;
import com.scs.overwatch.components.IProcessable;
import com.scs.overwatch.components.IShowOnHUD;
import com.scs.overwatch.gui.TextArea;
import com.scs.overwatch.modules.GameModule;

/*
 * Positioning text = the co-ords of BitmapText are for the top-left of the first line of text, and they go down from there.
 * 
 */
public class HUD extends Node implements IEntity, IProcessable {

	public TextArea log_ta;
	public float hud_width, hud_height;

	private int playerId;
	private Camera cam;
	private Geometry damage_box;
	private ColorRGBA dam_box_col = new ColorRGBA(1, 0, 0, 0.0f);
	private boolean process_damage_box;
	private List<Picture> targetting_reticules = new ArrayList<>();
	private Overwatch game;
	private GameModule module;
	private BitmapText abilityGun, abilityOther, score, haveBall, accuracy;
	public BitmapText helpText;
	private float showHelpUntil = 5;

	public HUD(Overwatch _game, GameModule _module, float xBL, float yBL, float w, float h, BitmapFont font_small, int id, Camera _cam) {
		super("HUD");

		game = _game;
		module =_module;
		hud_width = w;
		hud_height = h;
		playerId = id;
		cam = _cam;

		super.setLocalTranslation(xBL, yBL, 0);

		/*health = new BitmapText(font_small, false);
		health.setLocalTranslation(10, hud_height-20, 0);
		this.attachChild(health);
		this.setHealth(100);*/

		score = new BitmapText(font_small, false);
		score.setLocalTranslation(10, hud_height-15, 0);
		this.attachChild(score);
		this.setScore(0);

		abilityGun = new BitmapText(font_small, false);
		abilityGun.setColor(ColorRGBA.Green);
		abilityGun.setLocalTranslation(10, hud_height-30, 0);
		this.attachChild(abilityGun);

		abilityOther = new BitmapText(font_small, false);
		abilityOther.setColor(ColorRGBA.Green);
		abilityOther.setLocalTranslation(10, hud_height-45, 0);
		this.attachChild(abilityOther);

		haveBall = new BitmapText(font_small, false);
		haveBall.setLocalTranslation(10, hud_height-60, 0);
		this.attachChild(haveBall);

		accuracy = new BitmapText(font_small, false);
		accuracy.setLocalTranslation(10, hud_height-75, 0);
		this.attachChild(accuracy);

		helpText = new BitmapText(font_small, false);
		abilityOther.setColor(ColorRGBA.Red);
		helpText.setText(GameModule.HELP_TEXT);
		helpText.setLocalTranslation(10, hud_height-75, 0);
		this.attachChild(helpText);

		// Damage box
		{
			Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", this.dam_box_col);
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			damage_box = new Geometry("damagebox", new Quad(w, h));
			damage_box.move(0, 0, 0);
			damage_box.setMaterial(mat);
			this.attachChild(damage_box);
		}

		/*if (Settings.DEBUG_HUD) {
			log_ta = new TextArea("log", font_small, 6, "TEXT TEST_" + id);
			log_ta.setLocalTranslation(0, hud_height/2, 0);
			this.attachChild(log_ta);

			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", new ColorRGBA(1, 1, 0, 0.5f));
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			Geometry testBox = new Geometry("testBox", new Quad(w/2, h/2));
			testBox.move(10, 10, 0);
			testBox.setMaterial(mat);
			this.attachChild(testBox);

			/*Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			//mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			Texture t = game.getAssetManager().loadTexture("Textures/text/hit.png");
			//t.setWrap(WrapMode.Repeat);
			mat.setTexture("DiffuseMap", t);
			Geometry geom = new Geometry("Billboard", new Quad(w, h));
			geom.setMaterial(mat);
			geom.move(0, 0, 0);
			//geom.setQueueBucket(Bucket.Transparent);
			//geom.setLocalTranslation(-w/2, -h/2, 0);
			this.attachChild(geom);*/
		/*
			Picture pic = new Picture("HUD Picture");
			pic.setImage(game.getAssetManager(), "Textures/text/hit.png", true);
			pic.setWidth(w);
			pic.setHeight(h);
			//pic.setPosition(settings.getWidth()/4, settings.getHeight()/4);
			this.attachChild(pic);
		}*/


		this.updateGeometricState();

		this.setModelBound(new BoundingBox());
		this.updateModelBound();

		module.addEntity(this);

	}


	@Override
	public void process(float tpf) {
		if (showHelpUntil > 0) {
			showHelpUntil -= tpf;
			if (showHelpUntil <= 0) {
				this.helpText.removeFromParent();
			}
		}
		
		if (Settings.DEBUG_HUD) {
			this.abilityGun.setText("" + NumberFunctions.rnd(1000,  9999));
			this.score.setText("" + NumberFunctions.rnd(1000,  9999));
		}

		// Test reticle
		if (Settings.DEBUG_TARGETTER) {
			int id = 0;
			for (IEntity entity : module.entities) {
				if (entity instanceof IShowOnHUD) {
					if (this.targetting_reticules.size() <= id) {
						this.addTargetter();
					}
					IShowOnHUD soh = (IShowOnHUD) entity;
					Picture pic = this.targetting_reticules.get(id);
					pic.setCullHint(CullHint.Inherit);

					Vector3f screen_pos = cam.getScreenCoordinates(soh.getLocation()); // todo - cache vec3f, don't create each time
					pic.setPosition(screen_pos.x, screen_pos.y);
					id++;
				}
			}

			// Hide the rest
			for (int i=id ; i<this.targetting_reticules.size() ; i++) {
				Picture pic = this.targetting_reticules.get(id);
				pic.setCullHint(CullHint.Always);

			}

		}

		if (process_damage_box) {
			this.dam_box_col.a -= (tpf/2);
			if (dam_box_col.a <= 0) {
				dam_box_col.a = 0;
				process_damage_box = false;
			}
		}



	}


	public void log(String s) {
		this.log_ta.addLine(s);
	}


	public void setScore(float s) {
		this.score.setText("SCORE: " + (int)s);
	}


	/*public void setHealth(float h) {
		if (!Settings.DEBUG_HUD) {
			this.health.setText("HEALTH: " + (int)h);
		} else {
			this.health.setText("THIS IS PLAYER " + playerId);
		}
	}*/


	public void setAbilityGunText(String s) {
		this.abilityGun.setText(s);
	}


	public void setAbilityOtherText(String s) {
		this.abilityOther.setText(s);
	}


	public void setAccuracy(int a)  {
		this.accuracy.setText("Accuracy: " + a + "%");
	}
	
	
	public void updateHasBall(boolean a) {
		if (a) {
			this.haveBall.setText("YOU HAVE THE BALL");
		} else {
			this.haveBall.setText("");
		}
	}


	public void showDamageBox() {
		process_damage_box = true;
		this.dam_box_col.a = .5f;
		this.dam_box_col.r = 1f;
		this.dam_box_col.g = 0f;
		this.dam_box_col.b = 0f;
	}


	public void showCollectBox() {
		process_damage_box = true;
		this.dam_box_col.a = .3f;
		this.dam_box_col.r = 0f;
		this.dam_box_col.g = 1f;
		this.dam_box_col.b = 1f;
	}


	private void addTargetter() {
		Picture targetting_reticule = new Picture("HUD Picture");
		targetting_reticule.setImage(game.getAssetManager(), "Textures/circular_recticle.png", true);
		float crosshairs_w = cam.getWidth()/10;
		targetting_reticule.setWidth(crosshairs_w);
		float crosshairs_h = cam.getHeight()/10;
		targetting_reticule.setHeight(crosshairs_h);
		this.attachChild(targetting_reticule);

		this.targetting_reticules.add(targetting_reticule);
	}
}
