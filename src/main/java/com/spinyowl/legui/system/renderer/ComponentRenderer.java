package com.spinyowl.legui.system.renderer;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.system.context.GLFWContext;

/**
 * Component renderer base.
 */
public abstract class ComponentRenderer<C extends Component> {

  public void initialize() {
    // this method should be reimplemented if need to initialize some data in renderer before it can be used
    // called only once
  }

  public void render(C component, GLFWContext context) {
    renderComponent(component, context);
  }

  public abstract void renderComponent(C component, GLFWContext context);

  public void destroy() {
    // this method should be reimplemented if need to destroy some data in renderer before exit
  }
}