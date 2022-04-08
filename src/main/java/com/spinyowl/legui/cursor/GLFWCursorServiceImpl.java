package com.spinyowl.legui.cursor;

import com.spinyowl.legui.system.context.Context;
import com.spinyowl.legui.system.context.GLFWContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.spinyowl.legui.system.context.NvgBasedContext;
import org.lwjgl.glfw.GLFW;

public class GLFWCursorServiceImpl implements CursorService {

  private Map<Cursor, Long> cursors = new ConcurrentHashMap<>();

  private static Long createCursor(Cursor cs) {
    return GLFW.nglfwCreateCursor(cs.getImageReference(), cs.getxHot(), cs.getyHot());
  }

  @Override
  public void setCursor(Cursor cursor, Context context) {
    if (cursor == null) {
      return;
    }
    long glfwWindow = ((GLFWContext) context).getGlfwWindow();
    if (cursor == Cursor.ARROW) {
      GLFW.glfwSetCursor(glfwWindow,
          GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
      return;
    }
    if (cursor == Cursor.H_RESIZE) {
      GLFW.glfwSetCursor(glfwWindow,
          GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
      return;
    }
    if (cursor == Cursor.V_RESIZE) {
      GLFW.glfwSetCursor(glfwWindow,
          GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR));
      return;
    }
    if (cursor == Cursor.CROSSHAIR) {
      GLFW.glfwSetCursor(glfwWindow,
          GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR));
      return;
    }
    if (cursor == Cursor.HAND) {
      GLFW.glfwSetCursor(glfwWindow,
          GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));
      return;
    }
    if (cursor == Cursor.IBEAM) {
      GLFW.glfwSetCursor(glfwWindow,
          GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR));
      return;
    }

    GLFW.glfwSetCursor(glfwWindow,
        cursors.computeIfAbsent(cursor, GLFWCursorServiceImpl::createCursor));
  }
}
