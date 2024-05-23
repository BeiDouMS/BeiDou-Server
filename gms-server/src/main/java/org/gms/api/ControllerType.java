package org.gms.api;

import lombok.Getter;
import org.gms.api.controller.TestController;

@Getter
public enum ControllerType {
    TEST("test", new TestController())
    ;

    private final String key;
    private final BaseController controller;

    ControllerType(String key, BaseController controller) {
        this.key = key;
        this.controller = controller;
    }

    public static BaseController ofController(String key) {
        for (ControllerType value : values()) {
            if (value.getKey().equals(key)) {
                return value.getController();
            }
        }
        return null;
    }
}
