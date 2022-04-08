package com.spinyowl.legui.demo;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgRotate;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;
import static org.lwjgl.nanovg.NanoVG.nvgTranslate;
import static org.lwjgl.opengl.GL30.GL_BLEND;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL30.GL_MINOR_VERSION;
import static org.lwjgl.opengl.GL30.GL_NEAREST;
import static org.lwjgl.opengl.GL30.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.GL_RGBA;
import static org.lwjgl.opengl.GL30.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL30.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glBindTexture;
import static org.lwjgl.opengl.GL30.glBlendFunc;
import static org.lwjgl.opengl.GL30.glClear;
import static org.lwjgl.opengl.GL30.glClearColor;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL30.glDeleteTextures;
import static org.lwjgl.opengl.GL30.glDisable;
import static org.lwjgl.opengl.GL30.glDrawBuffer;
import static org.lwjgl.opengl.GL30.glEnable;
import static org.lwjgl.opengl.GL30.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30.glGenTextures;
import static org.lwjgl.opengl.GL30.glGetInteger;
import static org.lwjgl.opengl.GL30.glRenderbufferStorage;
import static org.lwjgl.opengl.GL30.glTexImage2D;
import static org.lwjgl.opengl.GL30.glTexParameteri;
import static org.lwjgl.opengl.GL30.glViewport;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.spinyowl.legui.animation.AnimatorProvider;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.ImageView;
import com.spinyowl.legui.component.Widget;
import com.spinyowl.legui.image.FBOImage;
import com.spinyowl.legui.listener.processor.EventProcessorProvider;
import com.spinyowl.legui.style.Style.DisplayType;
import com.spinyowl.legui.style.Style.PositionType;
import com.spinyowl.legui.system.context.CallbackKeeper;
import com.spinyowl.legui.system.context.GLFWContext;
import com.spinyowl.legui.system.context.DefaultCallbackKeeper;
import com.spinyowl.legui.system.handler.processor.SystemEventProcessor;
import com.spinyowl.legui.system.handler.processor.SystemEventProcessorImpl;
import com.spinyowl.legui.system.layout.LayoutManager;
import com.spinyowl.legui.system.renderer.Renderer;
import com.spinyowl.legui.system.renderer.nvg.NvgRenderer;
import java.io.IOException;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

public class FBOImageExample {

  public static final int WIDTH = 400;
  public static final int HEIGHT = 200;
  private static volatile boolean running = false;

  private static int textureWidth = 200;
  private static int textureHeight = 200;
  private static int frameBufferID;
  private static int textureID;
  private static int renderBufferID;
  private static long lastTime = System.nanoTime();

  public static void main(String[] args) throws IOException {
    System.setProperty("joml.nounsafe", Boolean.TRUE.toString());
    System.setProperty("java.awt.headless", Boolean.TRUE.toString());
    if (!GLFW.glfwInit()) {
      throw new RuntimeException("Can't initialize GLFW");
    }
    long window = glfwCreateWindow(WIDTH, HEIGHT, "FBO Image example", NULL, NULL);
    glfwShowWindow(window);

    glfwMakeContextCurrent(window);
    GL.createCapabilities();
    glfwSwapInterval(0);

    // Firstly we need to create frame component for window.
    Frame frame = new Frame(WIDTH, HEIGHT);

    // We need to create legui context which shared by renderer and event processor.
    // Also we need to pass event processor for ui events such as click on component, key typing and etc.
    GLFWContext context = new GLFWContext(window);

    // We need to create callback keeper which will hold all of callbacks.
    // These callbacks will be used in initialization of system event processor
    // (will be added callbacks which will push system events to event queue and after that processed by SystemEventProcessor)
    CallbackKeeper keeper = new DefaultCallbackKeeper();

    // register callbacks for window. Note: all previously binded callbacks will be unbinded.
    CallbackKeeper.registerCallbacks(window, keeper);

    GLFWKeyCallbackI glfwKeyCallbackI = (w1, key, code, action, mods) -> running = !(
        key == GLFW_KEY_ESCAPE && action != GLFW_RELEASE);
    GLFWWindowCloseCallbackI glfwWindowCloseCallbackI = w -> running = false;

    // if we want to create some callbacks for system events you should create and put them to keeper
    //
    // Wrong:
    // glfwSetKeyCallback(window, glfwKeyCallbackI);
    // glfwSetWindowCloseCallback(window, glfwWindowCloseCallbackI);
    //
    // Right:
    keeper.getChainKeyCallback().add(glfwKeyCallbackI);
    keeper.getChainWindowCloseCallback().add(glfwWindowCloseCallbackI);

    // Event processor for system events. System events should be processed and translated to gui events.
    SystemEventProcessor systemEventProcessor = new SystemEventProcessorImpl();
    SystemEventProcessor.addDefaultCallbacks(keeper, systemEventProcessor);

    // Also we need to create renderer provider
    // and create renderer which will render our ui components.
    Renderer renderer = new NvgRenderer();

    // Initialization finished, so we can start render loop.
    running = true;

    // Everything can be done in one thread as well as in separated threads.
    // Here is one-thread example.

    // before render loop we need to initialize renderer
    renderer.initialize();

    ////// rendering to texture and use this texture as image
    long nvgContext = 0;
    FBOImage fboTexture = null;
    boolean isVersionNew =
        (glGetInteger(GL_MAJOR_VERSION) > 3) || (glGetInteger(GL_MAJOR_VERSION) == 3
            && glGetInteger(GL_MINOR_VERSION) >= 2);
    if (isVersionNew) {
      int flags = NanoVGGL3.NVG_STENCIL_STROKES | NanoVGGL3.NVG_ANTIALIAS;
      nvgContext = NanoVGGL3.nvgCreate(flags);
    } else {
      int flags = NanoVGGL2.NVG_STENCIL_STROKES | NanoVGGL2.NVG_ANTIALIAS;
      nvgContext = NanoVGGL2.nvgCreate(flags);
    }
    if (nvgContext != 0) {
      fboTexture = createFBOTexture(textureWidth, textureHeight);

      Widget widget = new Widget(10, 10, 100, 100);
      widget.setCloseable(false);
      widget.setMinimizable(false);
      widget.setResizable(true);
      widget.getContainer().getStyle().setDisplay(DisplayType.FLEX);

      ImageView imageView = new ImageView(fboTexture);
      imageView.setPosition(10, 10);
      imageView.getStyle().setPosition(PositionType.RELATIVE);
      imageView.getStyle().getFlexStyle().setFlexGrow(1);
      imageView.getStyle().setMargin(10f);
      imageView.getStyle().setMinimumSize(50, 50);

      widget.getContainer().add(imageView);

      frame.getContainer().add(widget);
    }

    while (running) {

      if (fboTexture != null) {
        renderToFBO(nvgContext);
      }

      // Before rendering we need to update context with window size and window framebuffer size
      //{
      //    int[] windowWidth = {0}, windowHeight = {0};
      //    GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
      //    int[] frameBufferWidth = {0}, frameBufferHeight = {0};
      //    GLFW.glfwGetFramebufferSize(window, frameBufferWidth, frameBufferHeight);
      //    int[] xpos = {0}, ypos = {0};
      //    GLFW.glfwGetWindowPos(window, xpos, ypos);
      //    double[] mx = {0}, my = {0};
      //    GLFW.glfwGetCursorPos(window, mx, my);
      //
      //    context.update(windowWidth[0], windowHeight[0],
      //            frameBufferWidth[0], frameBufferHeight[0],
      //            xpos[0], ypos[0],
      //            mx[0], my[0]
      //    );
      //}

      // Also we can do it in one line
      context.updateGlfwWindow();
      Vector2i windowSize = context.getFramebufferSize();

      glClearColor(1, 1, 1, 1);
      // Set viewport size
      glViewport(0, 0, windowSize.x, windowSize.y);
      // Clear screen
      glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

      // render frame
      renderer.render(frame, context);

      // poll events to callbacks
      glfwPollEvents();
      glfwSwapBuffers(window);

      // Now we need to process events. Firstly we need to process system events.
      systemEventProcessor.processEvents(frame, context);

      // When system events are translated to GUI events we need to process them.
      // This event processor calls listeners added to ui components
      EventProcessorProvider.getInstance().processEvents();

      // When everything done we need to relayout components.
      LayoutManager.getInstance().layout(frame);

      // Run animations. Should be also called cause some components use animations for updating state.
      AnimatorProvider.getAnimator().runAnimations();
    }

    if (nvgContext != 0) {
      glDeleteRenderbuffers(renderBufferID);
      glDeleteTextures(textureID);
      glDeleteFramebuffers(frameBufferID);
      if (isVersionNew) {
        NanoVGGL3.nnvgDelete(nvgContext);
      } else {
        NanoVGGL2.nnvgDelete(nvgContext);
      }
    }

    // And when rendering is ended we need to destroy renderer
    renderer.destroy();

    glfwDestroyWindow(window);
    glfwTerminate();
  }

  public static FBOImage createFBOTexture(int textureWidth, int textureHeight) {

    frameBufferID = GL30.glGenFramebuffers();
    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);

    // The texture we're going to render to
    textureID = glGenTextures();
    // "Bind" the newly created texture : all future texture functions will modify this texture
    glBindTexture(GL_TEXTURE_2D, textureID);

    // Give an empty image to OpenGL ( the last "0" )
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA,
        GL_UNSIGNED_BYTE, 0);

    // Poor filtering. Needed !
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

    // The depth buffer
    renderBufferID = glGenRenderbuffers();
    glBindRenderbuffer(GL_RENDERBUFFER, renderBufferID);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, textureWidth, textureHeight);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBufferID);

    // Set "textureID" as our colour attachment #0
    glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureID, 0);

    // Set the list of draw buffers.
    glDrawBuffer(GL_COLOR_ATTACHMENT0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    return new FBOImage(textureID, textureWidth, textureHeight);
  }

  public static void renderToFBO(long nvgContext) {
    // bind fbo

    long thisTime = System.nanoTime();
    float angle = 5 * (lastTime - thisTime) / 1E10f;

    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);
    glViewport(0, 0, textureWidth, textureHeight);

    glClearColor(0.3f, 0.5f, 0.7f, 0.0f);

    // Clear screen
    glClear(GL_COLOR_BUFFER_BIT);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    nvgBeginFrame(nvgContext, textureWidth, textureHeight, 1);

    try (
        NVGColor nvgColorOne = NVGColor.calloc();
        NVGColor nvgColorTwo = NVGColor.calloc()
    ) {
      nvgColorOne.r(0);
      nvgColorOne.g(1);
      nvgColorOne.b(0);
      nvgColorOne.a(1);

      nvgColorTwo.r(0);
      nvgColorTwo.g(0);
      nvgColorTwo.b(0);
      nvgColorTwo.a(1);

      nvgTranslate(nvgContext, textureWidth / 2f, textureHeight / 2f);
      nvgRotate(nvgContext, angle);

      nvgBeginPath(nvgContext);
      nvgRect(nvgContext, -textureWidth / 4f, -textureHeight / 4f, textureWidth / 2f,
          textureHeight / 2f);
      nvgStrokeColor(nvgContext, nvgColorTwo);
      nvgStroke(nvgContext);

      nvgBeginPath(nvgContext);
      nvgRect(nvgContext, -textureWidth / 4f, -textureHeight / 4f, textureWidth / 2f,
          textureHeight / 2f);
      nvgFillColor(nvgContext, nvgColorOne);
      nvgFill(nvgContext);
    }

    nvgEndFrame(nvgContext);
    glDisable(GL_BLEND);

    // unbind fbo
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
  }
}