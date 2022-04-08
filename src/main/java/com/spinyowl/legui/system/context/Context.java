package com.spinyowl.legui.system.context;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.cursor.CursorService;
import com.spinyowl.legui.event.FocusEvent;
import com.spinyowl.legui.listener.processor.EventProcessorProvider;

public abstract class Context {
    private boolean debugEnabled;
    private Component mouseTargetGui;
    private Component focusedGui;

    /**
     * Is debug enabled boolean.
     *
     * @return the boolean
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Sets debug enabled.
     *
     * @param debugEnabled the debug enabled
     */
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }



    /**
     * Gets focused gui.
     *
     * @return the focused gui
     */
    public Component getFocusedGui() {
        return focusedGui;
    }

    /**
     * Sets focused gui.
     *
     * @param focusedGui the focused gui
     */
    public void setFocusedGui(Component focusedGui) {
        this.focusedGui = focusedGui;
    }

    /**
     * Returns current mouse target component.
     *
     * @return current mouse target component.
     */
    public Component getMouseTargetGui() {
        return mouseTargetGui;
    }

    /**
     * Used to update current mouse target component.
     *
     * @param mouseTargetGui new mouse target component.
     */
    public void setMouseTargetGui(Component mouseTargetGui) {
        this.mouseTargetGui = mouseTargetGui;
    }

    public abstract boolean isIconified();

    public abstract CursorService getCursorService();

    public static void setFocusedGui(Component toGainFocus, Context context, Frame frame) {
        Component current = context == null ? null : context.focusedGui;
        if (current != null) {
            current.setFocused(false);
            EventProcessorProvider.getInstance()
                                  .pushEvent(new FocusEvent<>(current, context, frame, toGainFocus, false));
        }
        if (toGainFocus != null) {
            toGainFocus.setFocused(true);
            EventProcessorProvider.getInstance()
                                  .pushEvent(new FocusEvent<>(toGainFocus, context, frame, toGainFocus, true));
            if (context != null) {
                context.setFocusedGui(toGainFocus);
            }
        }
    }
}
