package com.spinyowl.legui.demo;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL.setCapabilities;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.spinyowl.legui.animation.AnimatorProvider;
import com.spinyowl.legui.component.Button;
import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.Label;
import com.spinyowl.legui.component.RadioButton;
import com.spinyowl.legui.component.RadioButtonGroup;
import com.spinyowl.legui.event.CursorEnterEvent;
import com.spinyowl.legui.event.MouseClickEvent;
import com.spinyowl.legui.listener.CursorEnterEventListener;
import com.spinyowl.legui.listener.MouseClickEventListener;
import com.spinyowl.legui.listener.processor.EventProcessorProvider;
import com.spinyowl.legui.style.border.SimpleLineBorder;
import com.spinyowl.legui.style.color.ColorConstants;
import com.spinyowl.legui.system.context.CallbackKeeper;
import com.spinyowl.legui.system.context.GLFWContext;
import com.spinyowl.legui.system.context.DefaultCallbackKeeper;
import com.spinyowl.legui.system.handler.processor.SystemEventProcessor;
import com.spinyowl.legui.system.handler.processor.SystemEventProcessorImpl;
import com.spinyowl.legui.system.layout.LayoutManager;
import com.spinyowl.legui.system.renderer.Renderer;
import com.spinyowl.legui.system.renderer.nvg.NvgRenderer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.opengl.GLCapabilities;

public class MultipleWindowsMultipleThreadsExample {

  public static final int WIDTH = 400;
  public static final int HEIGHT = 200;
  private static final int WINDOW_COUNT = 3;

  private static volatile boolean running = false;

  private static Thread mainThread;
  private static Thread rendererThread;
  private static Thread eventProcessorThread;
  private static Thread leguiEventProcessorThread;

  private static long[] windows = new long[WINDOW_COUNT];
  private static Renderer[] renderers = new NvgRenderer[WINDOW_COUNT];
  private static GLFWContext[] contexts = new GLFWContext[WINDOW_COUNT];
  private static Frame[] frames = new Frame[WINDOW_COUNT];
  private static CallbackKeeper[] keepers = new DefaultCallbackKeeper[WINDOW_COUNT];
  private static SystemEventProcessor[] systemEventProcessors = new SystemEventProcessor[WINDOW_COUNT];
  private static GLCapabilities[] glCapabilities = new GLCapabilities[WINDOW_COUNT];

  public static void main(String[] args) throws IOException {
    System.setProperty("joml.nounsafe", Boolean.TRUE.toString());
    System.setProperty("java.awt.headless", Boolean.TRUE.toString());

    mainThread = new Thread(() -> {
      initialize();
      sleep(1000000000);
      startRenderer();
      startSystemEventProcessor();
      startLeguiEventProcessor();
      handleSystemEvents();
      destroy();
    }, "LEGUI_EXAMPLE");

    mainThread.start();
    // wait while not initialized
    while (!running) {
      Thread.yield();
    }
  }

  private static void sleep(long sleepTime) {
    try {
      TimeUnit.NANOSECONDS.sleep(sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void startSystemEventProcessor() {
    eventProcessorThread = new Thread(() -> {
      while (running) {
        for (int i = 0; i < WINDOW_COUNT; i++) {
          systemEventProcessors[i].processEvents(frames[i], contexts[i]);
        }
      }
    }, "GUI_SYSTEM_EVENT_PROCESSOR");
    eventProcessorThread.start();
  }

  private static void handleSystemEvents() {
    while (running) {
      glfwWaitEvents();
    }
  }

  private static void destroy() {
    for (int i = 0; i < WINDOW_COUNT; i++) {
      glfwDestroyWindow(windows[i]);
    }
    glfwTerminate();
  }

  private static void startLeguiEventProcessor() {
    leguiEventProcessorThread = new Thread(() -> {
      while (running) {
        EventProcessorProvider.getInstance().processEvents();
      }
    }, "GUI_EVENT_PROCESSOR");
    leguiEventProcessorThread.start();
  }

  private static void render() {
    for (int i = 0; i < WINDOW_COUNT; i++) {
      System.out.println(i);
      glfwMakeContextCurrent(windows[i]);
      glCapabilities[i] = createCapabilities();
      glfwSwapInterval(0);

      renderers[i] = new NvgRenderer();
      renderers[i].initialize();
    }

    while (running) {
      for (int i = 0; i < WINDOW_COUNT; i++) {
        glfwMakeContextCurrent(windows[i]);
        setCapabilities(glCapabilities[i]);
        glfwSwapInterval(0);

        contexts[i].updateGlfwWindow();
        Vector2i windowSize = contexts[i].getFramebufferSize();

        glClearColor(1, 1, 1, 1);
        glViewport(0, 0, windowSize.x, windowSize.y);
        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        renderers[i].render(frames[i], contexts[i]);

        glfwSwapBuffers(windows[i]);

        // When everything done we need to relayout components.
        LayoutManager.getInstance().layout(frames[i]);

        // Run animations. Should be also called cause some components use animations for updating state.
        AnimatorProvider.getAnimator().runAnimations();
      }
    }

    for (int i = 0; i < WINDOW_COUNT; i++) {
      renderers[i].destroy();
    }
  }

  private static void createGuiElements(Frame frame) {
    // Set background color for frame
    frame.getContainer().getStyle().getBackground().setColor(ColorConstants.lightBlue());

    Button button = new Button("Add components", 20, 20, 160, 30);
    SimpleLineBorder border = new SimpleLineBorder(ColorConstants.black(), 1);
    button.getStyle().setBorder(border);

    boolean[] added = {false};
    button.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) event -> {
      if (!added[0]) {
        added[0] = true;
        for (Component c : generateOnFly()) {
          frame.getContainer().add(c);
        }
      }
    });

    button.getListenerMap()
        .addListener(CursorEnterEvent.class, (CursorEnterEventListener) System.out::println);

    frame.getContainer().add(button);
  }

  private static List<Component> generateOnFly() {
    List<Component> list = new ArrayList<>();

    Label label = new Label(20, 60, 200, 20);
    label.getTextState().setText("Generated on fly label");
    label.getStyle().setTextColor(ColorConstants.red());

    RadioButtonGroup group = new RadioButtonGroup();
    RadioButton radioButtonFirst = new RadioButton("First", 20, 90, 200, 20);
    RadioButton radioButtonSecond = new RadioButton("Second", 20, 110, 200, 20);

    radioButtonFirst.setRadioButtonGroup(group);
    radioButtonSecond.setRadioButtonGroup(group);

    list.add(label);
    list.add(radioButtonFirst);
    list.add(radioButtonSecond);

    return list;
  }

  private static void initialize() {
    if (!GLFW.glfwInit()) {
      throw new RuntimeException("Can't initialize GLFW");
    }
    glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

    GLFWKeyCallbackI glfwKeyCallbackI = (w1, key, code, action, mods) -> running = !(
        key == GLFW_KEY_ESCAPE && action != GLFW_RELEASE);
    GLFWWindowCloseCallbackI glfwWindowCloseCallbackI = w -> running = false;

    Frame frame = new Frame(WIDTH, HEIGHT);
    createGuiElements(frame);

    for (int i = 0; i < WINDOW_COUNT; i++) {
      windows[i] = glfwCreateWindow(WIDTH, HEIGHT, "Multiple Windows Multiple Threads Example " + i,
          NULL, NULL);
      glfwSetWindowPos(windows[i], 50, 50 + (HEIGHT + 50) * i);
      glfwShowWindow(windows[i]);

      frames[i] = frame;

      contexts[i] = new GLFWContext(windows[i]);
      keepers[i] = new DefaultCallbackKeeper();

      CallbackKeeper.registerCallbacks(windows[i], keepers[i]);
      keepers[i].getChainKeyCallback().add(glfwKeyCallbackI);
      keepers[i].getChainWindowCloseCallback().add(glfwWindowCloseCallbackI);

      systemEventProcessors[i] = new SystemEventProcessorImpl();
      SystemEventProcessor.addDefaultCallbacks(keepers[i], systemEventProcessors[i]);
    }

    running = true;
  }

  private static void startRenderer() {
    rendererThread = new Thread(MultipleWindowsMultipleThreadsExample::render, "GUI_RENDERER");
    rendererThread.start();
  }
}