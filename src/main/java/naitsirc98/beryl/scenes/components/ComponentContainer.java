package naitsirc98.beryl.scenes.components;

import naitsirc98.beryl.scenes.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class ComponentContainer<T extends Component, EnabledContainer extends Collection<T>, DisabledContainer extends Collection<T>> {

    private final EnabledContainer enabledComponents;
    private final DisabledContainer disabledComponents;

    public ComponentContainer(EnabledContainer enabledContainer, DisabledContainer disabledContainer) {
        this.enabledComponents = enabledContainer;
        this.disabledComponents = disabledContainer;
    }

    public void add(T component) {
        if(component.enabled()) {
            enabledComponents.add(component);
        } else {
            disabledComponents.add(component);
        }
    }

    public void remove(T component) {
        if(component.enabled()) {
            enabledComponents.remove(component);
        } else {
            disabledComponents.remove(component);
        }
    }

    public void enable(T component) {
        if(disabledComponents.remove(component)) {
            enabledComponents.add(component);
        }
    }

    public void disable(T component) {
        if(enabledComponents.remove(component)) {
            disabledComponents.add(component);
        }
    }

    public int size() {
        return enabledComponents.size() + disabledComponents.size();
    }

    public void clear() {
        enabledComponents.clear();
        disabledComponents.clear();
    }

    public EnabledContainer enabled() {
        return enabledComponents;
    }

    public DisabledContainer disabled() {
        return disabledComponents;
    }

    public static class Default<T extends Component> extends ComponentContainer<T, ArrayList<T>, HashSet<T>> {

        public Default() {
            super(new ArrayList<>(), new HashSet<>());
        }
    }

}
