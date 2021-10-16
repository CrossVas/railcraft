package mods.railcraft.world.level.block;

import mods.railcraft.world.level.block.entity.multiblock.CokeOvenBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

// todo: multiblock base block type (so that we have isparent property)
public class CokeOvenBricks extends Block {
  public static final BooleanProperty ISLIT = BooleanProperty.create("lit");
  public static final BooleanProperty ISPARENT = BooleanProperty.create("parent");

  public CokeOvenBricks(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any()
        .setValue(ISLIT, Boolean.valueOf(false))
        .setValue(ISPARENT, Boolean.valueOf(false)));
  }

  @Override
  protected void createBlockStateDefinition(
        StateContainer.Builder<Block, BlockState> stateContainer) {
    stateContainer.add(ISLIT, ISPARENT);
  }

  @Override
  public TileEntity createTileEntity(BlockState blockState, IBlockReader level) {
    return new CokeOvenBlockEntity();
  }

  @Override
  public boolean hasTileEntity(BlockState blockState) {
    return true;
  }

  @Override
  public ActionResultType use(BlockState blockState, World level,
      BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
    TileEntity blockEntity = level.getBlockEntity(pos);
    if (!(blockEntity instanceof CokeOvenBlockEntity)) {
      return ActionResultType.PASS;
    }
    CokeOvenBlockEntity recast = (CokeOvenBlockEntity) blockEntity;

    if (!recast.isFormed() && !recast.tryToMakeParent(rayTraceResult.getDirection())) {
      return ActionResultType.PASS;
    }

    recast = recast.getParent();
    if (recast == null) {
      return ActionResultType.PASS;
    }

    player.openMenu(recast);
    // player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
    // TODO: interaction stats

    return ActionResultType.sidedSuccess(level.isClientSide());
  }
}
