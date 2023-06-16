package mods.railcraft.world.level.material.fluid.steam;

/**
 * @author CovertJaguar <https://www.railcraft.info>
 */
public final class SteamConstants {

  public static final float COLD_TEMP = 20;
  public static final float BOILING_POINT = 100;
  public static final float SUPER_HEATED = 300;
  public static final float MAX_HEAT_LOW = 500F;
  public static final float MAX_HEAT_HIGH = 1000F;
  public static final float HEAT_STEP = 0.05f;
  public static final float FUEL_PER_BOILER_CYCLE = 8f;
  public static final float FUEL_HEAT_INEFFICIENCY = 0.8f;
  public static final float FUEL_PRESSURE_INEFFICIENCY = 4f;
  public static final int STEAM_PER_UNIT_WATER = 160;
  public static final int STEAM_PER_10RF = 5;
  public static final boolean BOILERS_EXPLODE = true; // TODO: config this

  private SteamConstants() {}
}
