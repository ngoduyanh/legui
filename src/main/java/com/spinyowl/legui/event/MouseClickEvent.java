package com.spinyowl.legui.event;

import static org.lwjgl.glfw.GLFW.GLFW_MOD_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CAPS_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_NUM_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SUPER;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.input.Mouse;
import com.spinyowl.legui.input.Mouse.MouseButton;
import com.spinyowl.legui.system.context.GLFWContext;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joml.Vector2f;


public class MouseClickEvent<T extends Component> extends Event<T> {

  private final MouseClickAction action;
  private final Mouse.MouseButton button;
  private final Vector2f position;
  private final Vector2f absolutePosition;
  private final int mods;

  public MouseClickEvent(T component, GLFWContext context, Frame frame, MouseClickAction action,
                         MouseButton button, Vector2f position,
                         Vector2f absolutePosition, int mods) {
    super(component, context, frame);
    this.action = action;
    this.button = button;
    this.position = position;
    this.absolutePosition = absolutePosition;
    this.mods = mods;
  }

  public MouseClickAction getAction() {
    return action;
  }

  public Mouse.MouseButton getButton() {
    return button;
  }

  /**
   * Cursor position in button coordinates.
   *
   * @return cursor position in button coordinates.
   */
  public Vector2f getPosition() {
    return position;
  }

  /**
   * Cursor position in frame coordinates.
   *
   * @return cursor position in frame coordinates.
   */
  public Vector2f getAbsolutePosition() {
    return absolutePosition;
  }

  public int getMods() {
    return mods;
  }

  public boolean isModShift() {
    return (mods & GLFW_MOD_SHIFT) != 0;
  }

  public boolean isModControl() {
    return (mods & GLFW_MOD_CONTROL) != 0;
  }

  public boolean isModAlt() {
    return (mods & GLFW_MOD_ALT) != 0;
  }

  public boolean isModSuper() {
    return (mods & GLFW_MOD_SUPER) != 0;
  }

  public boolean isModCapsLock() {
    return (mods & GLFW_MOD_CAPS_LOCK) != 0;
  }

  public boolean isModNumLock() {
    return (mods & GLFW_MOD_NUM_LOCK) != 0;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("targetComponent", getTargetComponent().getClass().getSimpleName())
        .append("action", action)
        .append("button", button)
        .append("position", position)
        .append("absolutePosition", absolutePosition)
        .append("mods", mods)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MouseClickEvent<?> that = (MouseClickEvent<?>) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(mods, that.mods)
        .append(action, that.action)
        .append(button, that.button)
        .append(position, that.position)
        .append(absolutePosition, that.absolutePosition)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(action)
        .append(button)
        .append(position)
        .append(absolutePosition)
        .append(mods)
        .toHashCode();
  }

  public enum MouseClickAction {
    PRESS,
    CLICK,
    RELEASE,
    ;
  }
}
