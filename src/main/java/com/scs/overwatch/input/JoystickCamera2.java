package com.scs.overwatch.input;

import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.JoystickButton;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.scs.overwatch.Overwatch;
import com.scs.overwatch.Settings;

public class JoystickCamera2 extends FlyByCamera implements IInputDevice, RawInputListener {

	private static final float LOOK_UD_ADJ = Overwatch.properties.GetGamepadUpDownAdjust();// .4f;//.75f;//.5f;
	private static final float MOVE_SPEED = Overwatch.properties.GetGamepadMoveSpeed();// 5;
	private static final float DEADZONE = Overwatch.properties.GetGamepadDeadZone();// 0.0015f;
	private static final float TURN_SPEED = Overwatch.properties.GetGamepadTurnSpeed();// 100f;//150f;

	protected Joystick joystick;
	private float fwdVal, backVal, leftVal, rightVal;
	private Vector2f joyPos = new Vector2f();
	private Vector2f joyPosDir = new Vector2f();
	private boolean jump = false, shoot = false, ability1 = false, cycleAbility = false;
	private int id;
	private float prevLeft, prevRight, prevUp, prevDown;

	public JoystickCamera2(Camera _cam, Joystick _joystick, InputManager _inputManager) {
		super(_cam);

		super.initialUpVec = Vector3f.UNIT_Y;

		this.inputManager = _inputManager;
		this.joystick = _joystick;
		id = joystick.getJoyId();

		this.inputManager.addRawInputListener(this);

		inputManager.addListener(this, "jFLYCAM_Left"+id);
		inputManager.addListener(this, "jFLYCAM_Right"+id);
		inputManager.addListener(this, "jFLYCAM_Up"+id);
		inputManager.addListener(this, "jFLYCAM_Down"+id);

		inputManager.addListener(this, "jFLYCAM_StrafeLeft" + id);
		inputManager.addListener(this, "jFLYCAM_StrafeRight" + id);
		inputManager.addListener(this, "jFLYCAM_Forward" + id);
		inputManager.addListener(this, "jFLYCAM_Backward" + id);

		mapJoystick(joystick, id);

		this.registerWithInput(this.inputManager);
		super.setEnabled(true);
	}


	protected void mapJoystick( Joystick joystick, int id ) {
		// Map it differently if there are Z axis
		if( joystick.getAxis( JoystickAxis.Z_ROTATION ) != null && joystick.getAxis( JoystickAxis.Z_AXIS ) != null ) {
			// Make the left stick move
			joystick.getXAxis().assignAxis( "jFLYCAM_StrafeRight"+id, "jFLYCAM_StrafeLeft"+id );
			joystick.getYAxis().assignAxis( "jFLYCAM_Backward"+id, "jFLYCAM_Forward"+id );

			// And the right stick control the camera                       
			joystick.getAxis( JoystickAxis.Z_ROTATION ).assignAxis( "jFLYCAM_Down"+id, "jFLYCAM_Up"+id );
			joystick.getAxis( JoystickAxis.Z_AXIS ).assignAxis(  "jFLYCAM_Right"+id, "jFLYCAM_Left"+id );
		} else {             
			joystick.getPovXAxis().assignAxis("jFLYCAM_StrafeRight"+id, "jFLYCAM_StrafeLeft"+id);
			joystick.getPovYAxis().assignAxis("jFLYCAM_Forward"+id, "jFLYCAM_Backward"+id);
			joystick.getXAxis().assignAxis("jFLYCAM_Right"+id, "jFLYCAM_Left"+id);
			joystick.getYAxis().assignAxis("jFLYCAM_Down"+id, "jFLYCAM_Up"+id);
		}
	}


	@Override
	public float getFwdValue() {
		return this.fwdVal;
	}


	@Override
	public float getBackValue() {
		return this.backVal;
	}


	@Override
	public float getStrafeLeftValue() {
		return this.leftVal;
	}


	@Override
	public float getStrafeRightValue() {
		return this.rightVal;
	}


	@Override
	public boolean isJumpPressed() {
		return jump;
	}


	@Override
	public boolean isShootPressed() {
		return shoot;
	}


	@Override
	public boolean isAbilityOtherPressed() {
		return ability1;
	}


	@Override
	public boolean isSelectNextAbilityPressed() {
		return this.cycleAbility;
	}

	@Override
	public void onAnalog(String name, float value, float tpf) {
		if (!enabled) {			
			return;
		}

		//----------- CAMERA DIRECTION

		float tmp = value;

		if (name.equals("jFLYCAM_Left" + id)) {
			if (Settings.DEBUG_GAMEPAD_DIV_TPF) {
				value /= tpf;
			}

			/*if (Settings.DEBUG_GAMEPAD_TURNING) {
				Vector3f pos = this.avatar.gamepadTest.getLocalTranslation();
				float newX = value * 10000;
				//Settings.p("X=" + newX);
				this.avatar.gamepadTest.setPosition(200f - newX, pos.y);
			}*/
			if (Settings.GAMEPAD_USE_AVG) {
				value = (value + prevLeft) / 2;
				prevLeft = value; //tmp;
			}
			if (Settings.DEBUG_GAMEPAD_MULT_VALUE) {
				rotateCamera(value * value * TURN_SPEED, initialUpVec);
			} else {
				rotateCamera(value * TURN_SPEED, initialUpVec);
			}
		} else if (name.equals("jFLYCAM_Right" + id)) {
			if (Settings.DEBUG_GAMEPAD_DIV_TPF) {
				value /= tpf;
			}

			/*if (Settings.DEBUG_GAMEPAD_TURNING) {
				Vector3f pos = this.avatar.gamepadTest.getLocalTranslation();
				float newX = value * 10000;
				//Settings.p("X=" + newX);
				this.avatar.gamepadTest.setPosition(200f + newX, pos.y);
			}*/
			if (Settings.GAMEPAD_USE_AVG) {
				value = (value + prevRight) / 2;
				prevRight = value; //tmp;
			}
			if (Settings.DEBUG_GAMEPAD_MULT_VALUE) {
				rotateCamera(-value * value * TURN_SPEED, initialUpVec);
			} else {
				rotateCamera(-value * TURN_SPEED, initialUpVec);
			}
		} else if (name.equals("jFLYCAM_Up" + id)) {
			if (Settings.DEBUG_GAMEPAD_DIV_TPF) {
				value /= tpf;
			}

			if (Settings.GAMEPAD_USE_AVG) {
				value = (value + prevUp) / 2;
				prevUp = value; //tmp;
			}
			if (Settings.DEBUG_GAMEPAD_MULT_VALUE) {
				rotateCamera(-value*value * LOOK_UD_ADJ * TURN_SPEED * (invertY ? -1 : 1), cam.getLeft());
			} else {
				rotateCamera(-value * LOOK_UD_ADJ * TURN_SPEED * (invertY ? -1 : 1), cam.getLeft());
			}
		} else if (name.equals("jFLYCAM_Down" + id)) {
			if (Settings.DEBUG_GAMEPAD_DIV_TPF) {
				value /= tpf;
			}

			if (Settings.GAMEPAD_USE_AVG) {
				value = (value + prevDown) / 2;
				prevDown = value; //tmp;
			}
			if (Settings.DEBUG_GAMEPAD_MULT_VALUE) {
				rotateCamera(value*value * LOOK_UD_ADJ * TURN_SPEED * (invertY ? -1 : 1), cam.getLeft());
			} else {
				rotateCamera(value * LOOK_UD_ADJ * TURN_SPEED * (invertY ? -1 : 1), cam.getLeft());
			}
			//----------- MOVEMENT

		} else if (name.equals("jFLYCAM_Forward" + id)) {
			value -= DEADZONE;
			if (value > 0) {
				//Settings.p("value=" + value);
				joyPos.x = value;
			} else {
				joyPos.x = 0;
			}
		} else if (name.equals("jFLYCAM_Backward" + id)) {
			value -= DEADZONE;
			if (value > 0) {
				joyPos.x = -value;
			} else {
				joyPos.x = 0;
			}
		} else if (name.equals("jFLYCAM_StrafeLeft" + id)) {
			value -= DEADZONE;
			if (value > 0) {
				joyPos.y = -value;
			} else {
				joyPos.y = 0;
			}
		} else if (name.equals("jFLYCAM_StrafeRight" + id)) {
			value -= DEADZONE;
			if (value > 0) {
				joyPos.y = value;
			} else {
				joyPos.y = 0;
			}
		}
		this.calcValues();
	}


	private void calcValues() {
		joyPosDir.set(joyPos.x, joyPos.y);
		float length = Math.min(1, joyPosDir.length());
		joyPosDir.normalizeLocal();

		float angle = joyPosDir.getAngle();
		float x = FastMath.cos(angle) * length;
		float y = FastMath.sin(angle) * length;  

		fwdVal = 0;
		backVal = 0;
		leftVal = 0;
		rightVal = 0;

		if (x > 0) {
			fwdVal = Math.min(1, x * MOVE_SPEED);
			//Settings.p("fwdVal=" + fwdVal);
		} else {
			backVal = Math.min(1, -x * MOVE_SPEED);
			//Settings.p("backVal=" + backVal);
		}
		if (y > 0) {
			rightVal = Math.min(1, y * MOVE_SPEED);
		} else {
			leftVal = Math.min(1, -y * MOVE_SPEED);
		}
	}


	// Raw Input Listener ------------------------

	@Override
	public void onJoyAxisEvent(JoyAxisEvent evt) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.jme3.input.RawInputListener#onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent)
	 * 1 = X
	 * 2 = O
	 * 5 = R1
	 * 7 = R2
	 */
	@Override
	public void onJoyButtonEvent(JoyButtonEvent evt) {
		Joystick stick = evt.getButton().getJoystick();
		if (stick == joystick) {
			JoystickButton button = evt.getButton();
			//Settings.p("button.getButtonId()=" + button.getButtonId());
			if (button.getButtonId() == 1) {
				this.jump = evt.isPressed();
			} else if (button.getButtonId() == 5 || button.getButtonId() == 7) {
				this.shoot = evt.isPressed();
			} else if (button.getButtonId() == 2) {
				this.ability1 = evt.isPressed();
			} else if (button.getButtonId() == 3) {
				this.cycleAbility = evt.isPressed();
			}

		}
	}

	public void beginInput() {}
	public void endInput() {}
	public void onMouseMotionEvent(MouseMotionEvent evt) {}
	public void onMouseButtonEvent(MouseButtonEvent evt) {}
	public void onKeyEvent(KeyInputEvent evt) {}
	public void onTouchEvent(TouchEvent evt) {}


	// End of Raw Input Listener

}
