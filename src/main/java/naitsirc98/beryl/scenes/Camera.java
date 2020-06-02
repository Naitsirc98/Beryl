package naitsirc98.beryl.scenes;

import naitsirc98.beryl.events.EventManager;
import naitsirc98.beryl.events.window.WindowResizedEvent;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.util.geometry.Sizec;
import naitsirc98.beryl.util.geometry.Viewport;
import naitsirc98.beryl.util.geometry.Viewportc;
import org.joml.*;

import static java.util.Objects.requireNonNull;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Maths.*;
import static org.joml.Math.max;

public final class Camera {

	public static final float MIN_PITCH = -89.0f;
	public static final float MAX_PITCH = 89.0f;

	public static final float DEFAULT_YAW = -90.0f;

	public static final float DEFAULT_NEAR_PLANE = 0.1f;
	public static final float DEFAULT_FAR_PLANE = 2048.0f;

	public static final float DEFAULT_SENSITIVITY = 0.1f;

	public static final float DEFAULT_EXPOSURE = 1.0f;


	// Projection type (2D/3D)
	private ProjectionType projectionType;
	// Viewport
	private final Viewport viewport;
	// Axis vectors
	private final Vector3f position;
	private final Vector3f forward;
	private final Vector3f up;
	private final Vector3f right;
	// Field of view
	private float maxFOV;
	private float minFOV;
	private float fov;
	// Euler angles
	private float yaw;
	private float pitch;
	private float roll;
	// Planes
	private float nearPlane;
	private float farPlane;
	// Movement
	private float sensitivity;
	// Exposure
	private float exposure;
	// Matrices
	private final Matrix4f projectionMatrix;
	private final Matrix4f viewMatrix;
	private final Matrix4f projectionViewMatrix;
	private final FrustumIntersection frustum;
	private final Vector4f[] frustumPlanes;
	// Movement
	private float lastX;
	private float lastY;

	private boolean modified;

	private SceneCameraInfo sceneCameraInfo;

	Camera(SceneCameraInfo sceneCameraInfo) {

		this.sceneCameraInfo = requireNonNull(sceneCameraInfo);

		projectionType = ProjectionType.PERSPECTIVE;

		Sizec windowSize = Window.get().size();
		viewport = new Viewport(0, 0, windowSize.width(), windowSize.height());

		position = new Vector3f();
		forward = new Vector3f(0, 0, -1);
		up = new Vector3f(0, 1, 0);
		right = new Vector3f(1, 0, 0);

		maxFOV = radians(90.0f);
		minFOV = radians(1.0f);
		fov = clamp(minFOV, maxFOV, maxFOV / 2.0f);
		yaw = DEFAULT_YAW;
		pitch = 0.0f;
		roll = 0.0f;

		nearPlane = DEFAULT_NEAR_PLANE;
		farPlane = DEFAULT_FAR_PLANE;
		sensitivity = DEFAULT_SENSITIVITY;
		exposure = DEFAULT_EXPOSURE;

		lastX = lastY = Float.MIN_VALUE;

		viewMatrix = new Matrix4f();
		projectionMatrix = new Matrix4f();
		projectionViewMatrix = new Matrix4f();

		frustum = new FrustumIntersection();

		frustumPlanes = new Vector4f[6];
		for(int i = 0;i < frustumPlanes.length;i++) {
			frustumPlanes[i] = new Vector4f();
		}

		modified = true;

		updateCameraVectors();

		EventManager.addEventCallback(WindowResizedEvent.class, e -> viewport.set(0, 0, e.width(), e.height()));
	}

	public boolean modified() {
		return modified;
	}

	public Camera lookAt(float x, float y) {

		if(x == lastX && y == lastY) {
			return this;
		}

		float xOffset = x - lastX;
		float yOffset = lastY - y;

		lastX = x;
		lastY = y;
		
		final float sensitivity = this.sensitivity;
		final float fov = max(this.fov, 1.0f);
		final float maxFOV = this.maxFOV;

		xOffset *= sensitivity / (maxFOV / fov);
		yOffset *= sensitivity / (maxFOV / fov);

		yaw += xOffset;
		pitch = clamp(MIN_PITCH, MAX_PITCH, yOffset + pitch);

		modify();

		return this;
	}
	
	public void move(Direction direction, float amount) {
		assertNonNull(direction);
		
		final Vector3f dest = new Vector3f();

		switch(direction) {
			case LEFT:
				position.add(right.mul(amount, dest).negate());
				break;
			case RIGHT:
				position.add(right.mul(amount, dest));
				break;
			case UP:
				position.add(up.mul(amount, dest));
				break;
			case DOWN:
				position.add(up.mul(amount, dest).negate());
				break;
			case FORWARD:
				position.add(forward.mul(amount, dest));
				break;
			case BACKWARD:
				position.add(forward.mul(amount, dest).negate());
				break;
			default:
				return;
		}

		modify();
	}

	public void zoom(float amount) {
		if(amount != 0) {
			fov(fov - radians(amount));
		}
	}

	public Matrix4fc projectionMatrix() {
		return projectionMatrix;
	}

	public Matrix4fc viewMatrix() {
		return viewMatrix;
	}
	
	public Matrix4fc projectionViewMatrix() {
		return projectionViewMatrix;
	}
	
	public Vector4fc[] frustumPlanes() {
		return frustumPlanes;
	}

	public ProjectionType projectionType() {
		return projectionType;
	}

	public Camera projectionType(ProjectionType projectionType) {

		if(projectionType == null) {
			Log.error("ProjectionType cannot be null");
			return this;
		}

		this.projectionType = projectionType;

		modify();

		return this;
	}

	public Viewportc viewport() {
		return viewport;
	}

	public void viewport(Viewportc viewport) {
		this.viewport.set(viewport.x(), viewport.y(), viewport.width(), viewport.height());
		modify();
	}

	public Vector3fc position() {
		return position;
	}

	public Camera position(Vector3fc position) {
		return position(position.x(), position.y(), position.z());
	}

	public Camera position(float x, float y, float z) {
		position.set(x, y, z);
		modify();
		return this;
	}

	public Vector3fc forward() {
		return forward;
	}

	public Vector3fc up() {
		return up;
	}

	public Vector3fc right() {
		return right;
	}

	public float maxFOV() {
		return maxFOV;
	}

	public Camera maxFOV(float maxFOV) {
		this.maxFOV = maxFOV;
		modify();
		return this;
	}

	public float minFOV() {
		return minFOV;
	}

	public Camera minFOV(float minFOV) {
		this.minFOV = minFOV;
		modify();
		return this;
	}

	public float fov() {
		return fov;
	}

	public Camera fov(float fov) {
		this.fov = clamp(minFOV, maxFOV, fov);
		modify();
		return this;
	}

	public float yaw() {
		return yaw;
	}

	public Camera yaw(float yaw) {
		this.yaw = yaw;
		modify();
		return this;
	}

	public float pitch() {
		return pitch;
	}

	public Camera pitch(float pitch) {
		this.pitch = pitch;
		modify();
		return this;
	}

	public float roll() {
		return roll;
	}

	public Camera roll(float roll) {
		this.roll = roll;
		modify();
		return this;
	}

	public float nearPlane() {
		return nearPlane;
	}

	public Camera nearPlane(float nearPlane) {
		this.nearPlane = nearPlane;
		modify();
		return this;
	}

	public float farPlane() {
		return farPlane;
	}

	public Camera farPlane(float farPlane) {
		this.farPlane = farPlane;
		modify();
		return this;
	}

	public float sensitivity() {
		return sensitivity;
	}

	public Camera sensitivity(float sensitivity) {
		this.sensitivity = sensitivity;
		modify();
		return this;
	}

	public float exposure() {
		return exposure;
	}

	public Camera exposure(float exposure) {
		this.exposure = exposure;
		modify();
		return this;
	}

	public Matrix4f getViewMatrix(Matrix4f viewMatrix) {

		Vector3f position = this.position;
		Vector3f forward = this.forward;
		Vector3f up = this.up;

		return viewMatrix.setLookAt(
				position.x, position.y, position.z,
				position.x + forward.x, position.y + forward.y, position.z + forward.z,
				up.x, up.y, up.z);
	}

	public Matrix4f getProjectionMatrix(Matrix4f projectionMatrix, ProjectionType projectionType) {

		if(projectionType == ProjectionType.PERSPECTIVE) {

			projectionMatrix.setPerspective(fov, viewport.aspect(), nearPlane, farPlane);

		} else {

			final float w = viewport.width();
			final float h = viewport.height();
			final float ff = fov / max((maxFOV / 2.0f), 1.0f);
			projectionMatrix.setOrtho(-ff, ff, -h/w*ff, h/w*ff, nearPlane, farPlane);
			// projection.setOrtho(-ff, ff, -ff, ff, nearPlane, farPlane);
			// projection.setOrtho(-1, 1, -1, 1, nearPlane, farPlane);
		}

		return projectionMatrix;
	}

	public FrustumIntersection frustum() {
		return frustum;
	}

	public Vector4fc frustumPlane(int index) {
		return frustumPlanes[index];
	}

	public void updateMatrices() {
		updateCameraVectors();
		getViewMatrix(viewMatrix);
		getProjectionMatrix(projectionMatrix, projectionType);
		projectionMatrix.mul(viewMatrix, projectionViewMatrix);
		getFrustumPlanes();
		frustum.set(projectionViewMatrix);
		sceneCameraInfo.update(this);
		modified = false;
	}

	private void getFrustumPlanes() {
		for(int i = 0;i < frustumPlanes.length;i++) {
			projectionViewMatrix.frustumPlane(i, frustumPlanes[i]);
		}
	}
	
	/**
	 * Calculates the front vector from the Camera's (updated) Euler Angles
	 * */
	private void updateCameraVectors() {
		
		final float yaw = radians(this.yaw);
		final float pitch = radians(this.pitch);
		final Vector3f forward = this.forward;
		
		// Calculate the new front vector
		forward.x = cos(yaw) * cos(pitch);
		forward.y = sin(pitch);
		forward.z = sin(yaw) * cos(pitch);
		
		forward.normalize();
		
		// Also re-calculate the Right and Up vector
		// Normalize the vectors, because their length gets closer to 0 the more you look up or down 
		// which results in slower movement.
		forward.cross(0.0f, 1.0f, 0.0f, right).normalize();
		right.cross(forward, up).normalize();
	}

	private void modify() {
		modified = true;
	}

	public enum Direction {
		LEFT,
		RIGHT,
		UP,
		DOWN,
		FORWARD,
		BACKWARD
	}

	public enum ProjectionType {
		ORTHOGRAPHIC,
		PERSPECTIVE
	}
}
