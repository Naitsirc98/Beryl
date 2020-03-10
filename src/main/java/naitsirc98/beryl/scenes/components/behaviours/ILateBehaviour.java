package naitsirc98.beryl.scenes.components.behaviours;

public interface ILateBehaviour {

    /**
     * Called after the update and the first process task pass of its scene. At this point, all deferred operations made in
     * {@link IUpdateBehaviour#onUpdate()}, like destruction of scene objects, are completed.
     */
    void onLateUpdate();

}
