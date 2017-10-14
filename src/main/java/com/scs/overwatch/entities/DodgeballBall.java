package com.scs.overwatch.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.overwatch.Overwatch;
import com.scs.overwatch.Settings;
import com.scs.overwatch.components.IBullet;
import com.scs.overwatch.components.ICanShoot;
import com.scs.overwatch.components.ICollideable;
import com.scs.overwatch.components.IMustRemainInArena;
import com.scs.overwatch.modules.GameModule;

public class DodgeballBall extends PhysicalEntity implements IBullet, IMustRemainInArena {

	private static final float RAD = 0.2f;

	public ICanShoot shooter;
	private boolean live = true;
	private float timeLeft = 2f;
	private Geometry ball_geo;

	public DodgeballBall(Overwatch _game, GameModule _module, ICanShoot _shooter) {
		super(_game, _module, "DodgeballBall");

		this.shooter = _shooter;

		ball_geo = getBall(game);
		
		ball_geo.setQueueBucket(Bucket.Transparent);

		this.main_node.attachChild(ball_geo);
		game.getRootNode().attachChild(this.main_node);
		/** Position the cannon ball  */
		if (shooter != null) {
			ball_geo.setLocalTranslation(shooter.getLocation().add(shooter.getShootDir().multLocal(PlayersAvatar.PLAYER_RAD*2)));
		}
		floor_phy = new RigidBodyControl(.3f);
		ball_geo.addControl(floor_phy);
		module.getBulletAppState().getPhysicsSpace().add(floor_phy);
		/** Accelerate the physical ball to shoot it. */
		if (shooter != null) {
			floor_phy.setLinearVelocity(shooter.getShootDir().mult(25));
		}
		this.getMainNode().setUserData(Settings.ENTITY, this);
		floor_phy.setUserObject(this);
		module.addEntity(this);

		floor_phy.setRestitution(.9f); // Bouncy
		floor_phy.setCcdMotionThreshold(RAD*2);

	}


	public static Geometry getBall(Overwatch game) {
		Sphere sphere = new Sphere(16, 16, RAD, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		/** Create a cannon ball geometry and attach to scene graph. */
		Geometry ball = new Geometry("cannon ball", sphere);

		TextureKey key3 = new TextureKey( "Textures/cells3.png");
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		Material floor_mat = null;
		if (Settings.LIGHTING) {
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
		} else {
			floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			floor_mat.setTexture("ColorMap", tex3);
		}
		ball.setMaterial(floor_mat);
		floor_mat.getAdditionalRenderState().setDepthTest(false);
		return ball;
	}
	
	
	@Override
	public void process(float tpf) {
		if (live) {
			this.timeLeft -= tpf;
			if (this.timeLeft < 0) {
				this.setUnlive();
			}
		}

		//Settings.p("Dodgeball pos=" + this.floor_phy.getPhysicsLocation());
		// Check if fallen off edge
		if (this.floor_phy.getPhysicsLocation().y < -5f) {
			Settings.p("Dodgeball has fallen off the edge" + this.floor_phy.getPhysicsLocation());
			
			// Relaunch
			this.remove();
			module.createDodgeballBall();
		}


	}

	
	public void setUnlive() {
		// Set tex to dark
		TextureKey key3 = new TextureKey( "Textures/mud.png");
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		this.ball_geo.getMaterial().setTexture("DiffuseMap", tex3);

		live = false;

	}

	
	@Override
	public ICanShoot getShooter() {
		return shooter;
	}


	@Override
	public void collidedWith(ICollideable other) {
		if (other instanceof PlayersAvatar) {
			PlayersAvatar av = (PlayersAvatar) other;
			Settings.p(this + " collided with " + other);
			if (live) {
				if (other != this.shooter) {
					av.hitByBullet(this);
					if (getShooter() != null) {
						getShooter().hasSuccessfullyHit(av);
					}
					
					// Relaunch
					this.remove();
					module.createDodgeballBall();
				}
			} else if (av.getHasBall() == false) {
				av.setHasBall(true);
				this.remove();
			}
		}
	}


	@Override
	public float getDamageCaused() {
		return live ? 1 : 0;
	}


	@Override
	public void respawn() {
		module.createDodgeballBall();
	}


}
