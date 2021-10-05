package mods.railcraft.gui.button;

import java.util.List;
import javax.annotation.Nullable;
import mods.railcraft.client.gui.widget.button.TexturePosition;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface ButtonState extends IStringSerializable {

  ITextComponent getLabel();

  TexturePosition getTexturePosition();

  @Nullable
  default List<? extends ITextProperties> getTooltip() {
    return null;
  }
}
