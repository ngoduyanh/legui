package com.spinyowl.legui.system.renderer.nvg.image;


import static org.lwjgl.nanovg.NanoVG.nvgCreateImageRGBA;

import com.spinyowl.legui.image.BufferedImageRGBA;
import com.spinyowl.legui.system.context.Context;
import com.spinyowl.legui.system.context.NvgBasedContext;
import com.spinyowl.legui.system.renderer.nvg.NvgImageReferenceManager;
import com.spinyowl.legui.system.renderer.nvg.NvgImageRenderer;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.joml.Vector2fc;
import org.lwjgl.nanovg.NanoVG;

/**
 * Used to render image rectangle if no other renderers implemented.
 */
public class NvgBufferedImageRGBARenderer extends NvgImageRenderer<BufferedImageRGBA> {

  @Override
  public void initialize() {
    NvgImageReferenceManager manager = NvgImageReferenceManager.getInstance();
    manager.putImageReferenceProvider(BufferedImageRGBA.class, (image, context) -> {
      int imageRef = 0;
      Function<BufferedImageRGBA, String> getPath = i -> "TI::RGBA::" + i.hashCode();

      if (image != null) {
        String path = getPath.apply(image);
        try {
          imageRef = manager.getImageCache().get(path, () -> {
            int reference = nvgCreateImageRGBA(context, image.getWidth(), image.getHeight(), 0,
                image.getImageData());
            manager.getImageAssociationMap().put(getPath.apply(image), reference);
            return reference;
          });
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
      return imageRef;
    });
  }

  /**
   * Used to render specific Icon.
   *
   * @param image    image to render.
   * @param position image position.
   * @param size     image size.
   * @param context  context.
   * @param nanovg   nanoVG context.
   */
  @Override
  protected void renderImage(BufferedImageRGBA image, Vector2fc position, Vector2fc size,
                             Map<String, Object> properties, NvgBasedContext context, long nanovg) {

    NvgImageReferenceManager manager = NvgImageReferenceManager.getInstance();
    int imageRef = manager.getImageReference(image, nanovg);
    if (image.isUpdated()) {
      NanoVG.nvgUpdateImage(nanovg, imageRef, image.getImageData());
    }
    renderImage(imageRef, position, size, properties, nanovg);
  }
}
