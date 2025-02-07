package mods.railcraft.world.level.block.signal;

import java.util.List;
import com.mojang.serialization.MapCodec;
import mods.railcraft.Translations;
import mods.railcraft.client.ScreenFactories;
import mods.railcraft.world.level.block.entity.RailcraftBlockEntityTypes;
import mods.railcraft.world.level.block.entity.signal.SignalReceiverBoxBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SignalReceiverBoxBlock extends SignalBoxBlock implements EntityBlock {

  private static final MapCodec<SignalReceiverBoxBlock> CODEC =
      simpleCodec(SignalReceiverBoxBlock::new);

  public SignalReceiverBoxBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends SignalBoxBlock> codec() {
    return CODEC;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos,
      Player player, BlockHitResult rayTraceResult) {
    if (level.isClientSide()) {
      level.getBlockEntity(pos, RailcraftBlockEntityTypes.SIGNAL_RECEIVER_BOX.get())
          .ifPresent(ScreenFactories::openActionSignalBoxScreen);
    }
    return InteractionResult.sidedSuccess(level.isClientSide());
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
    return new SignalReceiverBoxBlockEntity(blockPos, blockState);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context,
      List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable(Translations.Tips.SIGNAL_RECEIVER_BOX)
        .withStyle(ChatFormatting.GRAY));
  }
}
