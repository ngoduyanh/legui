package com.spinyowl.legui.component.event.textinput;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.TextInput;
import com.spinyowl.legui.event.Event;
import com.spinyowl.legui.system.context.GLFWContext;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class TextInputContentChangeEvent<T extends TextInput> extends Event<T> {

  /**
   * Old value.
   */
  private final String oldValue;
  /**
   * New value.
   */
  private final String newValue;

  public TextInputContentChangeEvent(T component, GLFWContext context, Frame frame, String oldValue,
                                     String newValue) {
    super(component, context, frame);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /**
   * Returns old value.
   *
   * @return old value.
   */
  public String getOldValue() {
    return oldValue;
  }

  /**
   * Returns new value.
   *
   * @return new value.
   */
  public String getNewValue() {
    return newValue;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("oldValue", oldValue)
        .append("newValue", newValue)
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

    TextInputContentChangeEvent<?> that = (TextInputContentChangeEvent<?>) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(oldValue, that.oldValue)
        .append(newValue, that.newValue)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(oldValue)
        .append(newValue)
        .toHashCode();
  }
}
