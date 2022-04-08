package com.spinyowl.legui.system.renderer.nvg;

import static com.spinyowl.legui.system.renderer.nvg.NvgRenderer.NVG_CONTEXT;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.style.border.SimpleLineBorder;
import com.spinyowl.legui.style.color.ColorConstants;
import com.spinyowl.legui.system.context.GLFWContext;
import com.spinyowl.legui.system.renderer.ComponentRenderer;
import com.spinyowl.legui.system.renderer.nvg.border.NvgSimpleLineBorderRenderer;
import com.spinyowl.legui.util.Utilites;

/**
 * The base NanoVG component renderer.
 *
 * @param <C> component type.
 */
public abstract class NvgComponentRenderer<C extends Component> extends ComponentRenderer<C> {

  private NvgSimpleLineBorderRenderer debugBorderRenderer = new NvgSimpleLineBorderRenderer();
  private SimpleLineBorder debugBorder = new SimpleLineBorder(ColorConstants.red(), 1);
  private SimpleLineBorder debugFocusBorder = new SimpleLineBorder(ColorConstants.blue(), 2);

  @Override
  public void initialize() {
    debugBorderRenderer.initialize();
  }

  /**
   * Used to render component.
   *
   * @param component component to render.
   * @param context   legui context.
   */
  @Override
  public void renderComponent(C component, GLFWContext context) {
    long nanovgContext = (long) context.getContextData().get(NVG_CONTEXT);
    if (component.isVisible() && (component.keepRendering() || Utilites.visibleInParents(
        component))) {
      renderComponent(component, context, nanovgContext);
      if (context.isDebugEnabled()) {
        if (component.isFocused()) {
          debugBorderRenderer.renderBorder(debugFocusBorder, component, context);
        } else {
          debugBorderRenderer.renderBorder(debugBorder, component, context);
        }
      }
    } else {
      return;
    }
  }

  /**
   * Used to render component.
   *
   * @param component component to render.
   * @param context   legui context.
   * @param nanovg    nanovg context pointer.
   */
  protected abstract void renderComponent(C component, GLFWContext context, long nanovg);

}
