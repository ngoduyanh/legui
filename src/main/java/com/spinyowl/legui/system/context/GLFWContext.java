package com.spinyowl.legui.system.context;

import static com.spinyowl.legui.system.renderer.nvg.NvgRenderer.NVG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_ICONIFIED;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.config.Configuration;
import com.spinyowl.legui.cursor.CursorService;
import com.spinyowl.legui.cursor.GLFWCursorServiceImpl;
import com.spinyowl.legui.event.FocusEvent;
import com.spinyowl.legui.listener.processor.EventProcessorProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector2f;
import org.joml.Vector2i;


public class GLFWContext extends NvgBasedContext {

  static {
    Configuration.getInstance();
  }

  private final long glfwWindow;
  private final Map<String, Object> contextData = new ConcurrentHashMap<>();
  private Vector2f windowPosition;
  private Vector2i windowSize;
  private Vector2i framebufferSize;
  private transient float pixelRatio;
  private boolean iconified;

  /**
   * Instantiates a new Context.
   *
   * @param glfwWindow the glfw window
   */
  public GLFWContext(long glfwWindow) {
    this.glfwWindow = glfwWindow;
  }

  /**
   * Gets context data.
   *
   * @return the context data
   */
  public Map<String, Object> getContextData() {
    return contextData;
  }

  /**
   * Update glfw window.
   */
  public void updateGlfwWindow() {
    int[] windowWidth = {0},
        windowHeight = {0};
    int[] frameBufferWidth = {0},
        frameBufferHeight = {0};
    int[] xpos = {0},
        ypos = {0};
    glfwGetWindowSize(glfwWindow, windowWidth, windowHeight);
    glfwGetFramebufferSize(glfwWindow, frameBufferWidth, frameBufferHeight);
    glfwGetWindowPos(glfwWindow, xpos, ypos);

    update(windowWidth[0], windowHeight[0],
        frameBufferWidth[0], frameBufferHeight[0],
        xpos[0], ypos[0],
        glfwGetWindowAttrib(glfwWindow, GLFW_ICONIFIED) == GLFW_TRUE
    );
  }

  /**
   * Update.
   *
   * @param targetWidth       the target width
   * @param targetHeight      the target height
   * @param framebufferWidth  the framebuffer width
   * @param framebufferHeight the framebuffer height
   * @param targetPosX        the target pos x
   * @param targetPosY        the target pos y
   * @param iconified         the iconified
   */
  public void update(int targetWidth, int targetHeight, int framebufferWidth, int framebufferHeight,
      int targetPosX, int targetPosY, boolean iconified) {
    setWindowSize(new Vector2i(targetWidth, targetHeight));
    setFramebufferSize(new Vector2i(framebufferWidth, framebufferHeight));
    setPixelRatio((float) framebufferWidth / (float) targetWidth);
    setWindowPosition(new Vector2f(targetPosX, targetPosY));
    setIconified(iconified);
  }

  /**
   * Gets pixel ratio.
   *
   * @return the pixel ratio
   */
  public float getPixelRatio() {
    return pixelRatio;
  }

  /**
   * Sets pixel ratio.
   *
   * @param pixelRatio the pixel ratio
   */
  public void setPixelRatio(float pixelRatio) {
    this.pixelRatio = pixelRatio;
  }

  /**
   * Gets glfw window.
   *
   * @return the glfw window
   */
  public long getGlfwWindow() {
    return glfwWindow;
  }

  /**
   * Gets window position.
   *
   * @return the window position
   */
  public Vector2f getWindowPosition() {
    return windowPosition;
  }

  /**
   * Sets window position.
   *
   * @param windowPosition the window position
   */
  public void setWindowPosition(Vector2f windowPosition) {
    this.windowPosition = windowPosition;
  }

  /**
   * Gets window size.
   *
   * @return the window size
   */
  public Vector2i getWindowSize() {
    return windowSize;
  }

  /**
   * Sets window size.
   *
   * @param windowSize the window size
   */
  public void setWindowSize(Vector2i windowSize) {
    this.windowSize = windowSize;
  }

  /**
   * Gets framebuffer size.
   *
   * @return the framebuffer size
   */
  public Vector2i getFramebufferSize() {
    return framebufferSize;
  }

  /**
   * Sets framebuffer size.
   *
   * @param framebufferSize the framebuffer size
   */
  public void setFramebufferSize(Vector2i framebufferSize) {
    this.framebufferSize = framebufferSize;
  }

  /**
   * Returns window iconified state.
   *
   * @return window iconified state.
   */
  @Override
  public boolean isIconified() {
    return iconified;
  }

  private static final GLFWCursorServiceImpl CURSOR_SERVICE = new GLFWCursorServiceImpl();

  @Override
  public CursorService getCursorService() {
    return CURSOR_SERVICE;
  }

  /**
   * Used to update state of window (in case of window iconified).
   *
   * @param iconified window state.
   */
  public void setIconified(boolean iconified) {
    this.iconified = iconified;
  }

  @Override
  public long getNanoVGContext() {
    return (long) contextData.get(NVG_CONTEXT);
  }

  @Override
  public void setNanoVGContext(long ctx) {
    contextData.put(NVG_CONTEXT, ctx);
  }
}
