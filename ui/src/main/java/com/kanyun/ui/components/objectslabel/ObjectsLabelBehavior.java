package com.kanyun.ui.components.objectslabel;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import java.util.List;

public class ObjectsLabelBehavior extends BehaviorBase<ObjectsLabel> {

    /**
     * Create a new BehaviorBase for the given control. The Control must not
     * be null.
     *
     * @param control     The control. Must not be null.
     * @param keyBindings The key bindings that should be used with this behavior.
     */
    public ObjectsLabelBehavior(ObjectsLabel control, List<KeyBinding> keyBindings) {
        super(control, keyBindings);
    }


}
