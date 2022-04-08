package com.spinyowl.legui.system.handler;

import static com.spinyowl.legui.event.MouseClickEvent.MouseClickAction.CLICK;
import static com.spinyowl.legui.event.MouseClickEvent.MouseClickAction.PRESS;
import static com.spinyowl.legui.event.MouseClickEvent.MouseClickAction.RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.Layer;
import com.spinyowl.legui.component.Widget;
import com.spinyowl.legui.event.FocusEvent;
import com.spinyowl.legui.event.MouseClickEvent;
import com.spinyowl.legui.input.Mouse;
import com.spinyowl.legui.listener.processor.EventProcessorProvider;
import com.spinyowl.legui.style.Style.DisplayType;
import com.spinyowl.legui.system.context.Context;
import com.spinyowl.legui.system.context.Context;
import com.spinyowl.legui.system.event.SystemMouseClickEvent;
import java.util.Collections;
import java.util.List;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;


public class MouseClickEventHandler implements SystemEventHandler<SystemMouseClickEvent> {

  @Override
  public void handle(SystemMouseClickEvent event, Frame frame, Context ctx) {
    Mouse.MouseButton btn = Mouse.MouseButton.getByCode(event.button);
    btn.setPressed(event.action != GLFW_RELEASE);
    Vector2f cursorPos = Mouse.getCursorPosition();
    btn.setPressPosition(cursorPos);

    List<Layer> layers = frame.getAllLayers();
    Collections.reverse(layers);

    Component focusedGui = ctx.getFocusedGui();
    Component target = null;
    for (Layer layer : layers) {
      if (layer.isEventReceivable()) {
        if (!layer.isVisible() || !layer.isEnabled()) {
          continue;
        }
        target = SehUtil.getTargetComponent(layer, cursorPos);
        if (target != null) {
          break;
        }
      }
      if (!layer.isEventPassable()) {
        break;
      }
    }
    int mods = event.mods;
    if (target == null) {
      if (event.action == GLFW_RELEASE) {
        if (focusedGui != null) {
          updateReleasePosAndFocusedGui(btn, cursorPos, focusedGui);
          EventProcessorProvider.getInstance()
              .pushEvent(new MouseClickEvent<>(focusedGui, ctx, frame, RELEASE, btn,
                  buttonCursorPosition(cursorPos, focusedGui), cursorPos, mods));
        }
      } else {
        ctx.setFocusedGui(null);
      }
    } else {
      if (event.action == GLFW.GLFW_PRESS) {
        btn.setPressPosition(cursorPos);
        removeFocus(target, frame, ctx);
        target.setPressed(true);

        if (focusedGui != target) {
          target.setFocused(true);
          ctx.setFocusedGui(target);
        }

        EventProcessorProvider.getInstance()
            .pushEvent(new MouseClickEvent<>(target, ctx, frame, PRESS, btn,
                buttonCursorPosition(cursorPos, target), cursorPos, mods));

        if (focusedGui != target) {
          EventProcessorProvider.getInstance()
              .pushEvent(new FocusEvent<>(target, ctx, frame, target, true));
        }
      } else {
        updateReleasePosAndFocusedGui(btn, cursorPos, focusedGui);

        if (focusedGui != null) {
          if (focusedGui == target) {
            EventProcessorProvider.getInstance()
                .pushEvent(new MouseClickEvent<>(target, ctx, frame, CLICK, btn,
                    buttonCursorPosition(cursorPos, target), cursorPos, mods));
          }
          EventProcessorProvider.getInstance()
              .pushEvent(new MouseClickEvent<>(focusedGui, ctx, frame, RELEASE, btn,
                  buttonCursorPosition(cursorPos, focusedGui), cursorPos, mods));
        }
      }
      pushWidgetsUp(target);
    }
  }

  private Vector2f buttonCursorPosition(Vector2f cursorPos, Component target) {
    return target.getAbsolutePosition().sub(cursorPos).negate();
  }

  private void updateReleasePosAndFocusedGui(Mouse.MouseButton button, Vector2f cursorPosition,
      Component focusedGui) {
    button.setReleasePosition(cursorPosition);
    if (focusedGui != null) {
      focusedGui.setPressed(false);
    }
  }

  private void removeFocus(Component targetComponent, Frame frame, Context context) {
    List<Layer> allLayers = frame.getAllLayers();
    for (Layer layer : allLayers) {
      List<Component> childComponents = layer.getChildComponents();
      for (Component child : childComponents) {
        removeFocus(targetComponent, child, context, frame);
      }
    }
  }

  private void removeFocus(Component focused, Component component, Context context, Frame frame) {
    if (component != focused && component.isVisible() && component.isFocused()) {
      component.setFocused(false);
      component.setPressed(false);
      EventProcessorProvider.getInstance()
          .pushEvent(new FocusEvent<>(component, context, frame, focused, false));
    }
    List<? extends Component> childComponents = component.getChildComponents();
    for (Component child : childComponents) {
      removeFocus(focused, child, context, frame);
    }
  }

  private void pushWidgetsUp(Component gui) {
    Component parent = gui.getParent();
    Component current = gui;
    if (parent != null) {
      boolean push = false;
      while (parent != null) {
        if (parent instanceof Widget) {
          Widget widget = (Widget) parent;
          push = widget.isAscendible() && (parent.getParent() != null) && (
              parent.getParent().getStyle().getDisplay() == DisplayType.MANUAL);
        }
        current = parent;
        parent = parent.getParent();
        if (push) {
          break;
        }
      }
      if (push) {
        parent.remove(current);
        parent.add(current);
      }
    }
  }
}
