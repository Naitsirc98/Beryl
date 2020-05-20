package naitsirc98.beryl.scenes.environment;

import naitsirc98.beryl.logging.Log;
import naitsirc98.beryl.meshes.views.WaterMeshView;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class SceneEnhancedWater {

    public static final int MAX_ENHANCED_WATER_VIEWS = 1;

    private final WaterMeshView[] enhancedWaterViews;

    public SceneEnhancedWater() {
        enhancedWaterViews = new WaterMeshView[MAX_ENHANCED_WATER_VIEWS];
    }

    public boolean empty() {
        for(WaterMeshView waterMeshView : enhancedWaterViews) {
            if(waterMeshView == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isEnhanced(WaterMeshView waterMeshView) {
        return alreadyEnhanced(waterMeshView);
    }

    public SceneEnhancedWater setEnhancedWaterView(WaterMeshView waterMeshView) {
        return setEnhancedWaterView(0, waterMeshView);
    }

    public SceneEnhancedWater setEnhancedWaterView(int waterUnit, WaterMeshView waterMeshView) {
        if(alreadyEnhanced(waterMeshView)) {
            Log.error("WaterMeshView " + waterMeshView + " is already marked as enhanced");
            return this;
        }
        enhancedWaterViews[waterUnit] = waterMeshView;
        return this;
    }

    public void clear() {
        Arrays.fill(enhancedWaterViews, null);
    }

    public Stream<WaterMeshView> stream() {
        return Arrays.stream(enhancedWaterViews).filter(Objects::nonNull);
    }

    private boolean alreadyEnhanced(WaterMeshView waterMeshView) {
        for(WaterMeshView waterView : enhancedWaterViews) {
            if(Objects.equals(waterView, waterMeshView)) {
                return true;
            }
        }
        return false;
    }
}
