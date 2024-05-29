package theholyrailmod.theholyrail;

import necesse.engine.Screen;
import necesse.engine.localization.Localization;
import necesse.engine.network.PacketReader;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.summon.MinecartLinePos;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.itemAttack.ItemAttackDrawOptions;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlaceableItemInterface;
import necesse.inventory.PlayerInventorySlot;
import necesse.inventory.item.mountItem.MountItem;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.gameObject.TrapTrackObject;
import necesse.level.maps.Level;

public class RailRunnerMountItem extends MountItem implements PlaceableItemInterface {
   public RailRunnerMountItem() {
      super("railrunnermob");
      this.setMounterPos = false;
   }

   @Override
   public ListGameTooltips getTooltips(InventoryItem item, PlayerMob perspective, GameBlackboard blackboard) {
      ListGameTooltips tooltips = this.getBaseTooltips(item, perspective, blackboard);
      tooltips.add(Localization.translate("itemtooltip", "railrunnertip"));
      return tooltips;
   }

   @Override
   public String canUseMount(InventoryItem item, PlayerMob player, Level level) {
      Mob lastMount = player.getMount();
      if (lastMount != null) {
         return null;
      } else {
         String superError = super.canUseMount(item, player, level);
         if (superError != null) {
            return superError;
         } else {
            int playerTileX = player.getTileX();
            int playerTileY = player.getTileY();

            for (int tileX = playerTileX - 1; tileX <= playerTileX + 1; ++tileX) {
               for (int tileY = playerTileY - 1; tileY <= playerTileY + 1; ++tileY) {
                  GameObject object = level.getObject(tileX, tileY);
                  if (object instanceof MinecartTrackObject && !(object instanceof TrapTrackObject)) {
                     return null;
                  }
               }
            }

            return Localization.translate("misc", "cannotusemounthere", "mount", this.getDisplayName(item));
         }
      }
   }

   @Override
   public java.awt.geom.Point2D.Float getMountSpawnPos(Mob mount, ServerClient client, float playerX, float playerY,
         InventoryItem item, Level level) {
      PlayerMob player = client.playerMob;
      float bestDist = java.lang.Float.MAX_VALUE;
      MinecartLinePos bestPos = null;
      int playerTileX = player.getTileX();
      int playerTileY = player.getTileY();

      for (int tileX = playerTileX - 1; tileX <= playerTileX + 1; ++tileX) {
         for (int tileY = playerTileY - 1; tileY <= playerTileY + 1; ++tileY) {
            GameObject object = level.getObject(tileX, tileY);
            if (object instanceof MinecartTrackObject && !(object instanceof TrapTrackObject)) {
               MinecartLines lines = ((MinecartTrackObject) object).getMinecartLines(level, tileX, tileY, 0.0F, 0.0F,
                     false);
               MinecartLinePos pos = lines.getMinecartPos(player.x, player.y, player.dir);
               if (pos != null) {
                  float distance = player.getDistance(pos.x, pos.y);
                  if (bestPos == null || distance < bestDist) {
                     bestPos = pos;
                     bestDist = distance;
                  }
               }
            }
         }
      }

      if (bestPos != null) {
         mount.dir = bestPos.dir;
         ((RailRunnerMob) mount).minecartDir = bestPos.dir;
         return new java.awt.geom.Point2D.Float(bestPos.x, bestPos.y);
      } else {
         return super.getMountSpawnPos(mount, client, playerX, playerY, item, level);
      }
   }

   @Override
   public void setDrawAttackRotation(InventoryItem item, ItemAttackDrawOptions drawOptions, float attackDirX,
         float attackDirY, float attackProgress) {
      drawOptions.swingRotation(attackProgress);
   }

   @Override
   public InventoryItem onAttack(
         Level level,
         int x,
         int y,
         PlayerMob player,
         int attackHeight,
         InventoryItem item,
         PlayerInventorySlot slot,
         int animAttack,
         int seed,
         PacketReader contentReader) {
      if (this.canPlace(level, x, y, player, item, contentReader) == null) {
         if (level.isServer()) {
            Mob mob = MobRegistry.getMob("railrunnermob", level);
            if (mob instanceof RailRunnerMob) {
               ((RailRunnerMob) mob).minecartDir = player.isAttacking ? player.beforeAttackDir : player.dir;
               mob.resetUniqueID();
               level.entityManager.addMob(mob, (float) x, (float) y);
            }
         }

         if (level.isClient()) {
            Screen.playSound(GameResources.cling, SoundEffect.effect((float) x, (float) y).volume(0.8F));
         }

         item.setAmount(item.getAmount() - 1);
         return item;
      } else {
         return item;
      }
   }

   @Override
   public String canAttack(Level level, int x, int y, PlayerMob player, InventoryItem item) {
      return null;
   }

   protected String canPlace(Level level, int x, int y, PlayerMob player, InventoryItem item,
         PacketReader contentReader) {
      if (player.getPositionPoint().distance((double) x, (double) y) > 100.0) {
         return "outofrange";
      } else {
         Mob mob = MobRegistry.getMob("railrunnermob", level);
         if (mob != null) {
            mob.setPos((float) x, (float) y, true);
            if (mob.collidesWith(level)) {
               return "collision";
            }

            GameObject object = level.getObject(mob.getTileX(), mob.getTileY());
            if (!(object instanceof MinecartTrackObject) || object instanceof TrapTrackObject) {
               return "nottracks";
            }
         }

         return null;
      }
   }

   @Override
   public void drawPlacePreview(Level level, int x, int y, GameCamera camera, PlayerMob player, InventoryItem item,
         PlayerInventorySlot slot) {
      String error = this.canPlace(level, x, y, player, item, null);
      if (error == null) {
         int placeDir = player.isAttacking ? player.beforeAttackDir : player.dir;
         RailRunnerMob.drawPlacePreview(level, x, y, placeDir, camera);
      }
   }

   public static int registerRailRunnerMountItem() {
      return ItemRegistry.registerItem("railrunner", new RailRunnerMountItem(), 90, true);
   }
}
