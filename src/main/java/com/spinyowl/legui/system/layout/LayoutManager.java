package com.spinyowl.legui.system.layout;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.style.Style.DisplayType;
import com.spinyowl.legui.system.context.Context;

/**
 * Layout manager. Used to layout component and it's child components..
 */
public abstract class LayoutManager {

  /**
   * Instance of layout manager.
   */
  private static LayoutManager instance = new DefaultLayoutManager();

  /**
   * Returns layout manager instance.
   *
   * @return layout manager instance.
   */
  public static LayoutManager getInstance() {
    return instance;
  }

  /**
   * Used to set layout manager instance.
   *
   * @param instance layout manager instance to set.
   */
  public static void setInstance(LayoutManager instance) {
    LayoutManager.instance = instance;
  }

  /**
   * Used to register layout for specified display type.
   *
   * @param displayType display type.
   * @param layout      layout to register.
   */
  public abstract void registerLayout(DisplayType displayType, Layout layout);

  /**
   * Used to layout frame layers and all of their child components.
   *
   * @param frame frame to lay out.
   */
  public void layout(Frame frame) {
    layout(frame, null);
  }

  /**
   * Used to layout frame layers and all of their child components.
   *
   * @param frame   frame to lay out.
   * @param context context (used for event generation).
   */
  public abstract void layout(Frame frame, Context context);

  /**
   * Used to layout component and all of his child components.
   *
   * @param component component to lay out.
   */
  public void layout(Component component) {
    layout(component, null, null);
  }

  /**
   * Used to layout component and all of his child components.
   *
   * @param component component to lay out.
   * @param frame     component frame (for event generation if needed).
   * @param context   context (used for event generation).
   */
  public abstract void layout(Component component, Frame frame, Context context);
}
