package mods.railcraft.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

class SimpleLineRenderer implements LineRenderer {

  private final VertexConsumer consumer;

  SimpleLineRenderer(MultiBufferSource bufferSource) {
    this.consumer = bufferSource.getBuffer(RenderType.lines());
  }

  @Override
  public void renderLine(PoseStack poseStack, int red, int green, int blue, int alpha,
      float x0, float y0, float z0, float x1, float y1, float z1) {
    poseStack.pushPose();
    var matrix = poseStack.last().pose();

    // Draw a copy with each UV value to make the line visible from all angles.
    for (int i = 0; i < 3; i++) {
      int nx = i == 0 ? 1 : 0;
      int ny = i == 1 ? 1 : 0;
      int nz = i == 2 ? 1 : 0;
      this.consumer
          .addVertex(matrix, x0, y0, z0)
          .setColor(red, green, blue, alpha)
          .setNormal(poseStack.last(), nx, ny, nz);
      this.consumer
          .addVertex(matrix, x1, y1, z1)
          .setColor(red, green, blue, alpha)
          .setNormal(poseStack.last(), nx, ny, nz);
    }
    poseStack.popPose();
  }
}
