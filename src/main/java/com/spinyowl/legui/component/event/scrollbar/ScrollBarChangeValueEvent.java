package com.spinyowl.legui.component.event.scrollbar;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.ScrollBar;
import com.spinyowl.legui.event.Event;
import com.spinyowl.legui.system.context.GLFWContext;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Event generated by default event listeners which shows that scrollbar value was changed.
 */
public class ScrollBarChangeValueEvent<T extends ScrollBar> extends Event<T> {

  private final float oldValue;
  private final float newValue;

  public ScrollBarChangeValueEvent(T component, GLFWContext context, Frame frame, float oldValue,
                                   float newValue) {
    super(component, context, frame);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /**
   * Returns new value of scrollbar.
   *
   * @return new value of scrollbar.
   */
  public float getNewValue() {
    return newValue;
  }

  /**
   * Returns old value of scrollbar.
   *
   * @return old value of scrollbar.
   */
  public float getOldValue() {
    return oldValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ScrollBarChangeValueEvent that = (ScrollBarChangeValueEvent) o;

    return new EqualsBuilder()
        .append(getOldValue(), that.getOldValue())
        .append(getNewValue(), that.getNewValue())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getOldValue())
        .append(getNewValue())
        .toHashCode();
  }
}
