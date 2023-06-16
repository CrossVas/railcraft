package mods.railcraft.client;

import mods.railcraft.Railcraft;
import mods.railcraft.Translations;
import mods.railcraft.api.signal.SignalAspect;
import mods.railcraft.api.signal.SignalUtil;
import mods.railcraft.client.emblem.EmblemClientUtil;
import mods.railcraft.client.emblem.EmblemPackageManagerImpl;
import mods.railcraft.client.gui.screen.inventory.BlastFurnaceScreen;
import mods.railcraft.client.gui.screen.inventory.CartDispenserScreen;
import mods.railcraft.client.gui.screen.inventory.CokeOvenScreen;
import mods.railcraft.client.gui.screen.inventory.CreativeLocomotiveScreen;
import mods.railcraft.client.gui.screen.inventory.CrusherScreen;
import mods.railcraft.client.gui.screen.inventory.ElectricLocomotiveScreen;
import mods.railcraft.client.gui.screen.inventory.FeedStationScreen;
import mods.railcraft.client.gui.screen.inventory.FluidFueledSteamBoilerScreen;
import mods.railcraft.client.gui.screen.inventory.FluidManipulatorScreen;
import mods.railcraft.client.gui.screen.inventory.ItemManipulatorScreen;
import mods.railcraft.client.gui.screen.inventory.ManualRollingMachineScreen;
import mods.railcraft.client.gui.screen.inventory.PoweredRollingMachineScreen;
import mods.railcraft.client.gui.screen.inventory.RoutingTrackScreen;
import mods.railcraft.client.gui.screen.inventory.SolidFueledSteamBoilerScreen;
import mods.railcraft.client.gui.screen.inventory.SteamLocomotiveScreen;
import mods.railcraft.client.gui.screen.inventory.SteamOvenScreen;
import mods.railcraft.client.gui.screen.inventory.SteamTurbineScreen;
import mods.railcraft.client.gui.screen.inventory.SwitchTrackRouterScreen;
import mods.railcraft.client.gui.screen.inventory.TankMinecartScreen;
import mods.railcraft.client.gui.screen.inventory.TankScreen;
import mods.railcraft.client.gui.screen.inventory.TrackLayerScreen;
import mods.railcraft.client.gui.screen.inventory.TrainDispenserScreen;
import mods.railcraft.client.gui.screen.inventory.TunnelBoreScreen;
import mods.railcraft.client.gui.screen.inventory.WaterTankSidingScreen;
import mods.railcraft.client.model.RailcraftLayerDefinitions;
import mods.railcraft.client.particle.FireSparkParticle;
import mods.railcraft.client.particle.ForceSpawnParticle;
import mods.railcraft.client.particle.PumpkinParticle;
import mods.railcraft.client.particle.SparkParticle;
import mods.railcraft.client.particle.SteamParticle;
import mods.railcraft.client.particle.TuningAuraParticle;
import mods.railcraft.client.renderer.ShuntingAuraRenderer;
import mods.railcraft.client.renderer.blockentity.RailcraftBlockEntityRenderers;
import mods.railcraft.client.renderer.entity.RailcraftEntityRenderers;
import mods.railcraft.particle.RailcraftParticleTypes;
import mods.railcraft.world.inventory.ManualRollingMachineMenu;
import mods.railcraft.world.inventory.RailcraftMenuTypes;
import mods.railcraft.world.item.LocomotiveItem;
import mods.railcraft.world.item.RailcraftItems;
import mods.railcraft.world.level.block.ForceTrackEmitterBlock;
import mods.railcraft.world.level.block.RailcraftBlocks;
import mods.railcraft.world.level.block.track.ForceTrackBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GrassColor;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientManager {

  private static ClientManager instance;
  private final ShuntingAuraRenderer shuntingAuraRenderer;

  public ClientManager() {
    instance = this;

    SignalUtil._setTuningAuraHandler(new TuningAuraHandlerImpl());
    EmblemClientUtil._setPackageManager(new EmblemPackageManagerImpl());

    var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(this::handleClientSetup);
    modEventBus.addListener(this::handleItemColors);
    modEventBus.addListener(this::handleBlockColors);
    modEventBus.addListener(this::handleParticleRegistration);
    modEventBus.addListener(this::handleRegisterRenderers);
    modEventBus.addListener(this::handleRegisterLayerDefinitions);

    MinecraftForge.EVENT_BUS.register(this);

    this.shuntingAuraRenderer = new ShuntingAuraRenderer();
  }

  public ShuntingAuraRenderer getShuntingAuraRenderer() {
    return this.shuntingAuraRenderer;
  }

  // ================================================================================
  // Mod Events
  // ================================================================================

  private void handleClientSetup(FMLClientSetupEvent event) {
    // === Menu Screens ===
    MenuScreens.register(RailcraftMenuTypes.SOLID_FUELED_STEAM_BOILER.get(),
        SolidFueledSteamBoilerScreen::new);
    MenuScreens.register(RailcraftMenuTypes.FLUID_FUELED_STEAM_BOILER.get(),
        FluidFueledSteamBoilerScreen::new);
    MenuScreens.register(RailcraftMenuTypes.STEAM_TURBINE.get(),
        SteamTurbineScreen::new);
    MenuScreens.register(RailcraftMenuTypes.TANK.get(),
        TankScreen::new);
    MenuScreens.register(RailcraftMenuTypes.WATER_TANK_SIDING.get(),
        WaterTankSidingScreen::new);
    MenuScreens.register(RailcraftMenuTypes.TRACK_LAYER.get(),
        TrackLayerScreen::new);
    MenuScreens.register(RailcraftMenuTypes.BLAST_FURNACE.get(),
        BlastFurnaceScreen::new);
    MenuScreens.register(RailcraftMenuTypes.FEED_STATION.get(),
        FeedStationScreen::new);
    MenuScreens.register(RailcraftMenuTypes.CREATIVE_LOCOMOTIVE.get(),
        CreativeLocomotiveScreen::new);
    MenuScreens.register(RailcraftMenuTypes.ELECTRIC_LOCOMOTIVE.get(),
        ElectricLocomotiveScreen::new);
    MenuScreens.register(RailcraftMenuTypes.STEAM_LOCOMOTIVE.get(),
        SteamLocomotiveScreen::new);
    MenuScreens.register(RailcraftMenuTypes.MANUAL_ROLLING_MACHINE.get(),
        ManualRollingMachineScreen::new);
    MenuScreens.register(RailcraftMenuTypes.POWERED_ROLLING_MACHINE.get(),
        PoweredRollingMachineScreen::new);
    MenuScreens.register(RailcraftMenuTypes.COKE_OVEN.get(),
        CokeOvenScreen::new);
    MenuScreens.register(RailcraftMenuTypes.CRUSHER.get(),
        CrusherScreen::new);
    MenuScreens.register(RailcraftMenuTypes.STEAM_OVEN.get(),
        SteamOvenScreen::new);
    MenuScreens.register(RailcraftMenuTypes.ITEM_MANIPULATOR.get(),
        ItemManipulatorScreen::new);
    MenuScreens.register(RailcraftMenuTypes.FLUID_MANIPULATOR.get(),
        FluidManipulatorScreen::new);
    MenuScreens.register(RailcraftMenuTypes.CART_DISPENSER.get(),
        CartDispenserScreen::new);
    MenuScreens.register(RailcraftMenuTypes.TRAIN_DISPENSER.get(),
        TrainDispenserScreen::new);
    MenuScreens.register(RailcraftMenuTypes.TANK_MINECART.get(),
        TankMinecartScreen::new);
    MenuScreens.register(RailcraftMenuTypes.SWITCH_TRACK_ROUTER.get(),
        SwitchTrackRouterScreen::new);
    MenuScreens.register(RailcraftMenuTypes.TUNNEL_BORE.get(), TunnelBoreScreen::new);
    MenuScreens.register(RailcraftMenuTypes.ROUTING_TRACK.get(), RoutingTrackScreen::new);
  }

  private void handleItemColors(RegisterColorHandlersEvent.Item event) {
    event.register((stack, tintIndex) -> switch (tintIndex) {
          case 0 -> LocomotiveItem.getPrimaryColor(stack).getMaterialColor().col;
          case 1 -> LocomotiveItem.getSecondaryColor(stack).getMaterialColor().col;
          default -> 0xFFFFFFFF;
        },
        RailcraftItems.CREATIVE_LOCOMOTIVE.get(),
        RailcraftItems.STEAM_LOCOMOTIVE.get(),
        RailcraftItems.ELECTRIC_LOCOMOTIVE.get());
  }

  private void handleBlockColors(RegisterColorHandlersEvent.Block event) {
    event.register((state, level, pos, tintIndex) ->
            state.getValue(ForceTrackEmitterBlock.COLOR).getMaterialColor().col,
        RailcraftBlocks.FORCE_TRACK_EMITTER.get());

    event.register((state, level, pos, tintIndex) ->
            state.getValue(ForceTrackBlock.COLOR).getMaterialColor().col,
        RailcraftBlocks.FORCE_TRACK.get());

    event.register((state, level, pos, tintIndex) -> level != null && pos != null
            ? BiomeColors.getAverageGrassColor(level, pos)
            : GrassColor.get(0.5D, 1.0D),
        RailcraftBlocks.ABANDONED_TRACK.get());
  }

  private void handleParticleRegistration(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(RailcraftParticleTypes.STEAM.get(), SteamParticle.Provider::new);
    event.registerSpriteSet(RailcraftParticleTypes.SPARK.get(), SparkParticle.Provider::new);
    event.registerSpriteSet(RailcraftParticleTypes.PUMPKIN.get(), PumpkinParticle.Provider::new);
    event.registerSpriteSet(RailcraftParticleTypes.TUNING_AURA.get(),
        TuningAuraParticle.Provider::new);
    event.registerSpriteSet(RailcraftParticleTypes.FIRE_SPARK.get(),
        FireSparkParticle.Provider::new);
    event.registerSpriteSet(RailcraftParticleTypes.FORCE_SPAWN.get(),
        ForceSpawnParticle.Provider::new);
  }

  private void handleRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    RailcraftEntityRenderers.register(event);
    RailcraftBlockEntityRenderers.register(event);
  }

  private void handleRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    RailcraftLayerDefinitions.createRoots(event::registerLayerDefinition);
  }

  // ================================================================================
  // Forge Events
  // ================================================================================

  @SubscribeEvent
  public void handleClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.START
        && (Minecraft.getInstance().level != null && !Minecraft.getInstance().isPaused())) {
      SignalAspect.tickBlinkState();
    }
  }

  @SubscribeEvent
  public void handleRenderWorldLast(RenderLevelStageEvent event) {
    this.shuntingAuraRenderer.render(event.getPartialTick(), event.getPoseStack());
  }

  @SubscribeEvent
  public void handleClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
    this.shuntingAuraRenderer.clearCarts();
  }

  @SubscribeEvent
  public void handleClientLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
    if (!Railcraft.BETA) {
      return;
    }
    var message = CommonComponents.joinLines(
        Component.literal("You are using a development version of Railcraft.")
            .withStyle(ChatFormatting.RED),
        Component.literal("- World saves are not stable and may break between versions.")
            .withStyle(ChatFormatting.GRAY),
        Component.literal("- Features might be missing or only partially implemented.")
            .withStyle(ChatFormatting.GRAY),
        Component.literal("You have been warned.")
            .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
        Component.literal("Bug reports are welcome at our issue tracker.")
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://github.com/Sm0keySa1m0n/Railcraft/issues"))),
        Component.literal("- CovertJaguar, Sm0keySa1m0n, 3divad99")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    event.getPlayer().displayClientMessage(message, false);
  }

  @SubscribeEvent
  public void onItemTooltip(ItemTooltipEvent event) {
    var itemStack = event.getItemStack();
    var tag = itemStack.getTag();
    if (tag == null) {
      return;
    }
    if (tag.contains(ManualRollingMachineMenu.CLICK_TO_CRAFT_TAG) &&
        tag.getBoolean(ManualRollingMachineMenu.CLICK_TO_CRAFT_TAG)) {
      event.getToolTip().add(Component.translatable(Translations.Tips.CLICK_TO_CRAFT)
              .withStyle(ChatFormatting.YELLOW));
    }
  }

  // ================================================================================
  // Static Methods
  // ================================================================================

  public static ClientManager instance() {
    return instance;
  }
}
