package mods.railcraft.world.entity.vehicle;

import mods.railcraft.api.track.RailShapeUtil;
import mods.railcraft.util.container.ContainerTools;
import mods.railcraft.world.entity.RailcraftEntityTypes;
import mods.railcraft.world.inventory.TrackLayerMenu;
import mods.railcraft.world.item.RailcraftItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.IPlantable;

public class TrackLayer extends MaintenancePatternMinecart {

  public static final int SLOT_STOCK = 0;
  public static final int SLOT_REPLACE = 0;
  public static final int[] SLOTS = ContainerTools.buildSlotArray(0, 1);

  public TrackLayer(EntityType<?> type, Level level) {
    super(type, level);
  }

  public TrackLayer(ItemStack itemStack, double x, double y, double z, ServerLevel level) {
    super(RailcraftEntityTypes.TRACK_LAYER.get(), x, y, z, level);
  }

  @Override
  public ItemStack getPickResult() {
    return RailcraftItems.TRACK_LAYER.get().getDefaultInstance();
  }

  @Override
  public Item getDropItem() {
    return RailcraftItems.TRACK_LAYER.get();
  }

  @Override
  protected void moveAlongTrack(BlockPos pos, BlockState state) {
    super.moveAlongTrack(pos, state);
    if (this.level().isClientSide())
      return;

    stockItems(SLOT_REPLACE, SLOT_STOCK);
    updateTravelDirection(pos, state);
    if (travelDirection != null)
      placeTrack(pos);
  }

  private void placeTrack(BlockPos pos) {
    if (getMode() == Mode.TRANSPORT)
      return;
    pos = pos.relative(travelDirection);

    RailShape trackShape = RailShape.NORTH_SOUTH;
    if (travelDirection == Direction.EAST || travelDirection == Direction.WEST)
      trackShape = RailShape.EAST_WEST;
    if (!isValidReplacementBlock(pos) && isValidReplacementBlock(pos.above())
        && RailShapeUtil.isStraight(trackShape))
      pos = pos.above();
    if (isValidReplacementBlock(pos) && isValidReplacementBlock(pos.below())) {
      pos = pos.below();
      if (travelDirection == Direction.NORTH)
        trackShape = RailShape.ASCENDING_SOUTH;
      if (travelDirection == Direction.SOUTH)
        trackShape = RailShape.ASCENDING_NORTH;
      if (travelDirection == Direction.WEST)
        trackShape = RailShape.ASCENDING_WEST;
      if (travelDirection == Direction.EAST)
        trackShape = RailShape.ASCENDING_EAST;
    }

    if (isValidNewTrackPosition(pos)) {
      BlockState targetState = this.level().getBlockState(pos);
      if (placeNewTrack(pos, SLOT_STOCK, trackShape)) {
        Block.dropResources(targetState, this.level(), pos);
      }
    }
  }

  private boolean isValidNewTrackPosition(BlockPos pos) {
    return isValidReplacementBlock(pos) && Block.canSupportRigidBlock(this.level(), pos.below());
  }

  private boolean isValidReplacementBlock(BlockPos pos) {
    BlockState state = this.level().getBlockState(pos);
    Block block = state.getBlock();
    return (state.isAir() ||
        block instanceof IPlantable ||
        block instanceof IForgeShearable ||
        TunnelBore.REPLACEABLE_TAGS.stream().anyMatch(state::is) ||
        TunnelBore.REPLACEABLE_BLOCKS.contains(block));
  }

  @Override
  public int[] getSlotsForFace(Direction side) {
    return SLOTS;
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    ItemStack trackReplace = patternContainer.getItem(SLOT_REPLACE);
    return ContainerTools.isItemEqual(stack, trackReplace);
  }

  @Override
  protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
    return new TrackLayerMenu(id, inventory, this);
  }
}
