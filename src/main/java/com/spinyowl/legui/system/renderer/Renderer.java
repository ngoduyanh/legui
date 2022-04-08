package com.spinyowl.legui.system.renderer;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.system.context.GLFWContext;


public interface Renderer {

  void initialize();

  void render(Frame frame, GLFWContext context);

  void destroy();
}
