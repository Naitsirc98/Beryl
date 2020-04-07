package naitsirc98.beryl.scenes.components.camera;

import naitsirc98.beryl.graphics.rendering.RenderingPath;
import naitsirc98.beryl.graphics.rendering.RenderingPaths;
import naitsirc98.beryl.graphics.window.Window;
import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.scenes.Component;
import naitsirc98.beryl.scenes.components.math.Transform;
import naitsirc98.beryl.util.Color;
import naitsirc98.beryl.util.geometry.Sizec;
import naitsirc98.beryl.util.geometry.Viewport;
import naitsirc98.beryl.util.geometry.Viewportc;
import org.joml.*;

import static naitsirc98.beryl.scenes.components.camera.ProjectionType.PERSPECTIVE;
import static naitsirc98.beryl.util.Asserts.assertNonNull;
import static naitsirc98.beryl.util.Maths.*;
import static naitsirc98.beryl.util.types.TypeUtils.getOrElse;
import static org.joml.Math.max;
import static org.joml.Math.min;

public final class Camera extends Component<Camera> {

	public static final float MIN_PITCH = -89.0f;
	public static final float MAX_PITCH = 89.0f;

	public static final float DEFAULT_YAW = -90.0f;

	public static final float DEFAULT_NEAR_PLANE = 0.1f;
	public static final float DEFAULT_FAR_PLANE = 4096.0f;

	public static final float DEFAULT_SENSITIVITY = 0.1f;

	public static final float DEFAULT_EXPOSURE = 1.0f;

	// Projection type (2D/3D)
	private ProjectionType projectionType;
	// Viewport
	private Viewport viewport;
	// Axis vectors
	private Vector3f forward;
	private Vector3f up;
	private Vector3f right;
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
	// Rendering path
	private RenderingPath renderingPath;
	// Clear color
	private Color clearColor;
	// Matrices
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	private Matrix4f projectionViewMatrix;
	private FrustumIntersection frustum;
	// Movement
	private float lastX;
	private float lastY;

	private boolean modified;

	private Camera() {

	}

	@Override
	protected void init() {
		super.init();

		projectionType = PERSPECTIVE;
		Sizec windowSize = Window.get().size();
		viewport = new Viewport(0, 0, windowSize.width(), windowSize.height());
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

		renderingPath = RenderingPaths.defaultRenderingPath();

		clearColor = Color.BLACK;

		viewMatrix = new Matrix4f();
		projectionMatrix = new Matrix4f();
		projectionViewMatrix = new Matrix4f();
		frustum = new FrustumIntersection();

		modified = true;

		updateCameraVectors();
	}

	public boolean modified() {
		return modified || transform().modified();
	}

	private void modify() {
		modified = true;
	}

	public Camera lookAt(float x, float y) {
		assertNotDeleted();

		if(x == lastX && y == lastY) {
			return this;
		}

		float xOffset = x - lastX;
		float yOffset = lastY - y;

		lastX = x;
		lastY = y;
		
		final float sensitivity = this.sensitivity;
		final float fov = max(this.fov, 0.000001f);
		final float maxFOV = this.maxFOV;

		xOffset *= sensitivity / (maxFOV / fov);
		yOffset *= sensitivity / (maxFOV / fov);

		yaw += xOffset;
		pitch = clamp(MIN_PITCH, MAX_PITCH, yOffset + pitch);

		updateCameraVectors();

		modify();

		return this;
	}
	
	public void move(Direction direction, float amount) {
		assertNotDeleted();
		assertNonNull(direction);
		
		final Vector3f dest = new Vector3f();

		switch(direction) {
			case LEFT:
				transform().translate(right.mul(amount, dest).negate());
				break;
			case RIGHT:
				transform().translate(right.mul(amount, dest));
				break;
			case UP:
				transform().translate(up.mul(amount, dest));
				break;
			case DOWN:
				transform().translate(up.mul(amount, dest).negate());
				break;
			case FORWARD:
				transform().translate(forward.mul(amount, dest));
				break;
			case BACKWARD:
				transform().translate(forward.mul(amount, dest).negate());
				break;
			default:
				return;
		}

		modify();
	}

	public void zoom(float amount) {
		assertNotDeleted();
		if(amount != 0) {
			fov(fov - radians(amount));
		}
	}

	public Matrix4fc projectionMatrix() {
		assertNotDeleted();
		return projectionMatrix;
	}

	public Matrix4fc viewMatrix() {
		assertNotDeleted();
		return viewMatrix;
	}
	
	public Matrix4fc projectionViewMatrix() {
		assertNotDeleted();
		return projectionViewMatrix;
	}
	
	public FrustumIntersection frustum() {
		assertNotDeleted();
		return frustum;
	}

	public Transform transform() {
		return requires(Transform.class);
	}

	public ProjectionType projectionType() {
		assertNotDeleted();
		return projectionType;
	}

	public Camera projectionType(ProjectionType projectionType) {
		assertNotDeleted();

		if(projectionType == null) {
			Log.error("ProjectionType cannot be null");
			return this;
		}

		this.projectionType = projectionType;
		modify();

		return this;
	}

	public Viewportc viewport() {
		assertNotDeleted();
		return viewport;
	}

	public void viewport(Viewportc viewport) {
		assertNotDeleted();
		this.viewport.set(viewport.x(), viewport.y(), viewport.width(), viewport.height());
		modify();
	}

	public Vector3f forward() {
		assertNotDeleted();
		return forward;
	}

	public Vector3f up() {
		assertNotDeleted();
		return up;
	}

	public Vector3f right() {
		assertNotDeleted();
		return right;
	}

	public float maxFOV() {
		assertNotDeleted();
		return maxFOV;
	}

	public Camera maxFOV(float maxFOV) {
		assertNotDeleted();
		this.maxFOV = maxFOV;
		modify();
		return this;
	}

	public float minFOV() {
		assertNotDeleted();
		return minFOV;
	}

	public Camera minFOV(float minFOV) {
		this.minFOV = minFOV;
		modify();
		return this;
	}

	public float fov() {
		assertNotDeleted();
		return fov;
	}

	public Camera fov(float fov) {
		assertNotDeleted();
		this.fov = clamp(minFOV, maxFOV, fov);
		modify();
		return this;
	}

	public float yaw() {
		assertNotDeleted();
		return yaw;
	}

	public Camera yaw(float yaw) {
		assertNotDeleted();
		this.yaw = yaw;
		modify();
		return this;
	}

	public float pitch() {
		assertNotDeleted();
		return pitch;
	}

	public Camera pitch(float pitch) {
		assertNotDeleted();
		this.pitch = pitch;
		modify();
		return this;
	}

	public float roll() {
		assertNotDeleted();
		return roll;
	}

	public Camera roll(float roll) {
		assertNotDeleted();
		this.roll = roll;
		modify();
		return this;
	}

	public float nearPlane() {
		assertNotDeleted();
		return nearPlane;
	}

	public Camera nearPlane(float nearPlane) {
		assertNotDeleted();
		this.nearPlane = nearPlane;
		modify();
		return this;
	}

	public float farPlane() {
		assertNotDeleted();
		return farPlane;
	}

	public Camera farPlane(float farPlane) {
		assertNotDeleted();
		this.farPlane = farPlane;
		modify();
		return this;
	}

	public float sensitivity() {
		assertNotDeleted();
		return sensitivity;
	}

	public Camera sensitivity(float sensitivity) {
		assertNotDeleted();
		this.sensitivity = sensitivity;
		modify();
		return this;
	}

	public float exposure() {
		assertNotDeleted();
		return exposure;
	}

	public Camera exposure(float exposure) {
		assertNotDeleted();
		this.exposure = exposure;
		modify();
		return this;
	}

	public RenderingPath renderingPath() {
		assertNotDeleted();
		return renderingPath;
	}

	public Camera renderingPath(RenderingPath renderingPath) {
		assertNotDeleted();

		if(renderingPath == null) {
			Log.error("RenderingPath cannot be null");
			return this;
		}

		this.renderingPath = renderingPath;

		return this;
	}

	public Color clearColor() {
		return clearColor;
	}

	public Camera clearColor(Color clearColor) {
		this.clearColor = getOrElse(clearColor, Color.BLACK);
		return this;
	}

	void update() {
		recalculateView();
		recalculateProjection();
		projectionMatrix.mul(viewMatrix, projectionViewMatrix);
		frustum.set(projectionViewMatrix);
		modified = false;
	}

	private void recalculateView() {
		Vector3fc position = transform().position();
		Vector3f forward = this.forward;
		Vector3f up = this.up;

		viewMatrix.setLookAt(
				position.x(), position.y(), position.z(),
				position.x() + forward.x, position.y() + forward.y, position.z() + forward.z,
				up.x, up.y, up.z);
	}

	private void recalculateProjection() {

		if(projectionType == PERSPECTIVE) {
			projectionMatrix.setPerspective(fov, viewport.aspect(), nearPlane, farPlane);
		} else {
			final float w = viewport.width();
			final float h = viewport.height();
			final float ff = fov / max((maxFOV / 2.0f), 1.0f);
			projectionMatrix.setOrtho(-ff, ff, -h/w*ff, h/w*ff, nearPlane, farPlane);
			// projection.setOrtho(-ff, ff, -ff, ff, nearPlane, farPlane);
			// projection.setOrtho(-1, 1, -1, 1, nearPlane, farPlane);
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

	@Override
	public Class<? extends Component> type() {
		return Camera.class;
	}

	@Override
	protected void onEnable() {

	}

	@Override
	protected void onDisable() {

	}

	@Override
	protected Camera self() {
		return this;
	}

	@Override
	protected void onDestroy() {
		forward = null;
		up = null;
		right = null;
		projectionMatrix = null;
		viewMatrix = null;
		projectionViewMatrix = null;
		frustum = null;
		viewport = null;
		projectionType = null;
		renderingPath = null;
	}

	public enum Direction {
		LEFT,
		RIGHT,
		UP,
		DOWN,
		FORWARD,
		BACKWARD
	}

}
