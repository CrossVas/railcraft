package mods.railcraft.world.level.block.post;

import java.util.EnumMap;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mods.railcraft.tags.RailcraftTags;
import mods.railcraft.world.level.block.signal.AbstractSignalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LeadItem;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class PostBlock extends Block implements IWaterLoggable {

  public static final EnumProperty<Column> COLUMN = EnumProperty.create("column", Column.class);
  public static final EnumProperty<Connection> NORTH =
      EnumProperty.create("north", Connection.class);
  public static final EnumProperty<Connection> SOUTH =
      EnumProperty.create("south", Connection.class);
  public static final EnumProperty<Connection> EAST = EnumProperty.create("east", Connection.class);
  public static final EnumProperty<Connection> WEST = EnumProperty.create("west", Connection.class);
  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final Map<Direction, EnumProperty<Connection>> PROPERTY_BY_DIRECTION =
      Util.make(new EnumMap<>(Direction.class), (map) -> {
        map.put(Direction.NORTH, NORTH);
        map.put(Direction.EAST, EAST);
        map.put(Direction.SOUTH, SOUTH);
        map.put(Direction.WEST, WEST);
      });

  private final VoxelShape[] collisionShapeByIndex;
  private final VoxelShape[] shapeByIndex;
  private final VoxelShape[] occlusionByIndex;
  private final Object2IntMap<BlockState> stateToIndex = new Object2IntOpenHashMap<>();

  public PostBlock(Properties properties) {
    super(properties);
    properties.strength(3, 15);

    this.collisionShapeByIndex = this.makeShapes(2.0F, 2.0F, 16.0F, 0.0F, 16.0F);
    this.shapeByIndex = this.makeShapes(2.0F, 2.0F, 16.0F, 0.0F, 16.0F);
    this.occlusionByIndex = this.makeShapes(2.0F, 1.0F, 16.0F, 6.0F, 15.0F);

    for (BlockState blockstate : this.stateDefinition.getPossibleStates()) {
      this.getShapeIndex(blockstate);
    }

    this.registerDefaultState(this.stateDefinition.any()
        .setValue(COLUMN, Column.NORMAL)
        .setValue(NORTH, Connection.NONE)
        .setValue(SOUTH, Connection.NONE)
        .setValue(EAST, Connection.NONE)
        .setValue(WEST, Connection.NONE)
        .setValue(WATERLOGGED, false));
  }

  @Override
  protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(COLUMN, NORTH, SOUTH, EAST, WEST, WATERLOGGED);
  }

  protected VoxelShape[] makeShapes(float width, float extendedWidth,
      float extendedHeight, float minY, float maxY) {
    float minX = 8.0F - width;
    float maxX = 8.0F + width;
    float minExtendedX = 8.0F - extendedWidth;
    float maxExtendedX = 8.0F + extendedWidth;

    VoxelShape normalShape = Block.box(minX, 0.0D, minX, maxX, extendedHeight, maxX);

    VoxelShape westExtended = Block.box(
        minExtendedX, minY, 0.0D, maxExtendedX,
        maxY, maxExtendedX);
    VoxelShape northExtended = Block.box(minExtendedX, minY, minExtendedX, maxExtendedX,
        maxY, 16.0D);
    VoxelShape southExtended = Block.box(0.0D, minY, minExtendedX, maxExtendedX,
        maxY, maxExtendedX);
    VoxelShape voxelshape4 = Block.box(minExtendedX, minY, minExtendedX, 16.0D,
        maxY, maxExtendedX);

    VoxelShape voxelshape5 = VoxelShapes.or(westExtended, voxelshape4);
    VoxelShape voxelshape6 = VoxelShapes.or(northExtended, southExtended);
    VoxelShape[] extendedShapes = new VoxelShape[] {VoxelShapes.empty(),
        northExtended, southExtended,
        voxelshape6, westExtended, VoxelShapes.or(northExtended, westExtended),
        VoxelShapes.or(southExtended, westExtended), VoxelShapes.or(voxelshape6, westExtended),
        voxelshape4, VoxelShapes.or(northExtended, voxelshape4),
        VoxelShapes.or(southExtended, voxelshape4), VoxelShapes.or(voxelshape6, voxelshape4),
        voxelshape5, VoxelShapes.or(northExtended, voxelshape5),
        VoxelShapes.or(southExtended, voxelshape5), VoxelShapes.or(voxelshape6, voxelshape5)};

    for (int i = 0; i < 16; ++i) {
      extendedShapes[i] = VoxelShapes.or(normalShape, extendedShapes[i]);
    }

    return extendedShapes;
  }

  @Override
  public VoxelShape getShape(BlockState blockState, IBlockReader level, BlockPos blockPos,
      ISelectionContext context) {
    return this.shapeByIndex[this.getShapeIndex(blockState)];
  }

  @Override
  public VoxelShape getCollisionShape(BlockState blockState, IBlockReader level, BlockPos blockPos,
      ISelectionContext context) {
    return this.collisionShapeByIndex[this.getShapeIndex(blockState)];
  }

  @Override
  public VoxelShape getOcclusionShape(BlockState blockState, IBlockReader level,
      BlockPos blockPos) {
    return this.occlusionByIndex[this.getShapeIndex(blockState)];
  }

  @Override
  public VoxelShape getVisualShape(BlockState blockState, IBlockReader level,
      BlockPos blockPos, ISelectionContext context) {
    return this.getShape(blockState, level, blockPos, context);
  }

  private static int indexFor(Direction direction) {
    return 1 << direction.get2DDataValue();
  }

  protected int getShapeIndex(BlockState blockState) {
    return this.stateToIndex.computeIntIfAbsent(blockState, __ -> {
      int i = 0;
      if (blockState.getValue(NORTH) != Connection.NONE) {
        i |= indexFor(Direction.NORTH);
      }

      if (blockState.getValue(EAST) != Connection.NONE) {
        i |= indexFor(Direction.EAST);
      }

      if (blockState.getValue(SOUTH) != Connection.NONE) {
        i |= indexFor(Direction.SOUTH);
      }

      if (blockState.getValue(WEST) != Connection.NONE) {
        i |= indexFor(Direction.WEST);
      }

      return i;
    });
  }

  @Override
  public ActionResultType use(BlockState blockState, World level, BlockPos pos,
      PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
    if (level.isClientSide()) {
      ItemStack itemStack = player.getItemInHand(hand);
      return itemStack.getItem() == Items.LEAD ? ActionResultType.SUCCESS : ActionResultType.PASS;
    } else {
      return LeadItem.bindPlayerMobs(player, level, pos);
    }
  }

  @Override
  public BlockState getStateForPlacement(BlockItemUseContext context) {
    IBlockReader level = context.getLevel();
    BlockPos blockPos = context.getClickedPos();
    FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
    BlockPos northPos = blockPos.north();
    BlockPos eastPos = blockPos.east();
    BlockPos southPos = blockPos.south();
    BlockPos westPos = blockPos.west();
    BlockState northState = level.getBlockState(northPos);
    BlockState eastState = level.getBlockState(eastPos);
    BlockState southState = level.getBlockState(southPos);
    BlockState westState = level.getBlockState(westPos);
    return super.getStateForPlacement(context)
        .setValue(COLUMN, this.getColumn(level, blockPos))
        .setValue(NORTH, this.getConnection(northState,
            northState.isFaceSturdy(level, northPos, Direction.SOUTH), Direction.SOUTH))
        .setValue(EAST, this.getConnection(eastState,
            eastState.isFaceSturdy(level, eastPos, Direction.WEST), Direction.WEST))
        .setValue(SOUTH, this.getConnection(southState,
            southState.isFaceSturdy(level, southPos, Direction.NORTH), Direction.NORTH))
        .setValue(WEST, this.getConnection(westState,
            westState.isFaceSturdy(level, westPos, Direction.EAST), Direction.EAST))
        .setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
  }


  @Override
  public BlockState updateShape(BlockState blockState, Direction direction,
      BlockState neighborState, IWorld level, BlockPos blockPos, BlockPos neighborPos) {
    if (blockState.getValue(WATERLOGGED)) {
      level.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER,
          Fluids.WATER.getTickDelay(level));
    }

    return direction.getAxis().getPlane() == Direction.Plane.HORIZONTAL
        ? blockState.setValue(PROPERTY_BY_DIRECTION.get(direction),
            this.getConnection(neighborState,
                neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite()),
                direction.getOpposite()))
        : blockState.setValue(COLUMN, this.getColumn(level, blockPos));
  }

  public Connection getConnection(BlockState blockState, boolean sturdy, Direction direction) {
    Block block = blockState.getBlock();

    if (block instanceof AbstractSignalBlock) {
      return AbstractSignalBlock.connectsToDirection(blockState, direction)
          ? Connection.DOUBLE
          : Connection.NONE;
    }

    if (block.is(RailcraftTags.Blocks.POST) || !isExceptionForConnection(block) && sturdy) {
      return Connection.DOUBLE;
    }

    return Connection.NONE;
  }

  public Column getColumn(IBlockReader level, BlockPos blockPos) {
    BlockPos abovePos = blockPos.above();
    BlockState aboveState = level.getBlockState(abovePos);
    BlockPos belowPos = blockPos.below();
    BlockState belowState = level.getBlockState(belowPos);
    return aboveState.is(RailcraftTags.Blocks.POST)
        || belowState.is(RailcraftTags.Blocks.POST)
        || belowState.isFaceSturdy(level, belowPos, Direction.UP)
            ? Column.NORMAL
            : Column.SHORT;
  }

  public boolean isPlatform(BlockState state) {
    return false;
  }

  @Override
  public boolean propagatesSkylightDown(BlockState blockState,
      IBlockReader level, BlockPos pos) {
    return !blockState.getValue(WATERLOGGED);
  }

  @SuppressWarnings("deprecation")
  @Override
  public FluidState getFluidState(BlockState blockState) {
    return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false)
        : super.getFluidState(blockState);
  }


  @Override
  public boolean isPathfindable(BlockState state, IBlockReader level, BlockPos pos,
      PathType pathType) {
    return false;
  }

  @Override
  public BlockState rotate(BlockState blockState, Rotation rotation) {
    switch (rotation) {
      case CLOCKWISE_180:
        return blockState.setValue(NORTH, blockState.getValue(SOUTH))
            .setValue(EAST, blockState.getValue(WEST)).setValue(SOUTH, blockState.getValue(NORTH))
            .setValue(WEST, blockState.getValue(EAST));
      case COUNTERCLOCKWISE_90:
        return blockState.setValue(NORTH, blockState.getValue(EAST))
            .setValue(EAST, blockState.getValue(SOUTH)).setValue(SOUTH, blockState.getValue(WEST))
            .setValue(WEST, blockState.getValue(NORTH));
      case CLOCKWISE_90:
        return blockState.setValue(NORTH, blockState.getValue(WEST))
            .setValue(EAST, blockState.getValue(NORTH)).setValue(SOUTH, blockState.getValue(EAST))
            .setValue(WEST, blockState.getValue(SOUTH));
      default:
        return blockState;
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public BlockState mirror(BlockState blockState, Mirror mirror) {
    switch (mirror) {
      case LEFT_RIGHT:
        return blockState.setValue(NORTH, blockState.getValue(SOUTH)).setValue(SOUTH,
            blockState.getValue(NORTH));
      case FRONT_BACK:
        return blockState.setValue(EAST, blockState.getValue(WEST)).setValue(WEST,
            blockState.getValue(EAST));
      default:
        return super.mirror(blockState, mirror);
    }
  }

  @Override
  public boolean canCreatureSpawn(BlockState state, IBlockReader level, BlockPos pos,
      EntitySpawnPlacementRegistry.PlacementType placementType, EntityType<?> entityType) {
    return false;
  }
}
