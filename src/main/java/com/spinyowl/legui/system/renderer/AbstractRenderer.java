package com.spinyowl.legui.system.renderer;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.Layer;
import com.spinyowl.legui.system.context.GLFWContext;

/**
 * Base of main renderer which called by renderer thread.
 */
public abstract class AbstractRenderer implements Renderer {

  protected abstract void preRender(GLFWContext context);

  protected abstract void postRender(GLFWContext context);

  public void render(Frame display, GLFWContext context) {
    preRender(context);
    for (Layer layer : display.getAllLayers()) {
      RendererProvider.getInstance().getComponentRenderer(layer.getClass()).render(layer, context);
    }
    postRender(context);
  }

}
