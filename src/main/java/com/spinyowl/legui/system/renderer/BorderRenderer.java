package com.spinyowl.legui.system.renderer;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.style.Border;
import com.spinyowl.legui.system.context.GLFWContext;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Border renderer base.
 */
public abstract class BorderRenderer<B extends Border> {

  private final AtomicBoolean initialized = new AtomicBoolean(false);

  public void initialize() {
    // should be overrided
  }

  public void render(B border, Component component, GLFWContext context) {
    if (!initialized.getAndSet(true)) {
      initialize();
    }
    renderBorder(border, component, context);
  }

  public abstract void renderBorder(B border, Component component, GLFWContext context);

  public void destroy() {
    // should be overrided
  }
}
