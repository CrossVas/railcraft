package mods.railcraft.util;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.authlib.GameProfile;
import mods.railcraft.api.core.RailcraftConstants;
import mods.railcraft.api.item.ActivationBlockingItem;
import mods.railcraft.api.track.TrackUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class PlayerUtil {

  @Nullable
  public static Player getPlayer(Level level, GameProfile gameProfile) {
    var playerId = gameProfile.getId();
    if (playerId != null) {
      return level.getPlayerByUUID(playerId);
    }
    return null;
  }

  public static Component getUsername(Level level, @NotNull GameProfile gameProfile) {
    var playerId = gameProfile.getId();
    if (playerId != null) {
      var player = level.getPlayerByUUID(playerId);
      if (player != null)
        return player.getDisplayName();
    }
    String username = gameProfile.getName();
    return Component.literal(
        StringUtils.isEmpty(username) ? RailcraftConstants.UNKNOWN_PLAYER : username);
  }

  public static Component getUsername(Level level, @Nullable UUID playerId) {
    if (playerId != null) {
      var player = level.getPlayerByUUID(playerId);
      if (player != null)
        return player.getDisplayName();
    }
    return Component.literal(RailcraftConstants.UNKNOWN_PLAYER);
  }

  public static boolean isOwnerOrOp(GameProfile owner, Player player) {
    return player.getGameProfile().equals(owner) || player.hasPermissions(2);
  }

  public static boolean isSamePlayer(GameProfile a, GameProfile b) {
    if (a.getId() != null && b.getId() != null)
      return a.getId().equals(b.getId());
    return a.getName() != null && a.getName().equals(b.getName());
  }

  public static boolean doesItemBlockActivation(Player player, InteractionHand hand) {
    if (player.isShiftKeyDown() || hand == InteractionHand.OFF_HAND)
      return true;
    var heldItem = player.getItemInHand(hand);
    if (!heldItem.isEmpty()) {
      return TrackUtil.isRail(heldItem)
          || ReflectionUtil.isAnnotatedDeepSearch(ActivationBlockingItem.class, heldItem.getItem());
    }
    return false;
  }
}
