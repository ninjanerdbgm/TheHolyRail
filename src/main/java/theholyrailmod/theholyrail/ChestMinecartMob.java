package theholyrailmod.theholyrail;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import necesse.engine.GameEvents;
import necesse.engine.events.loot.MobLootTableDropsEvent;
import necesse.engine.localization.Localization;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.save.levelData.InventorySave;
import necesse.engine.sound.SoundPlayer;
import necesse.engine.tickManager.TickManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.MobInventory;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.summon.MinecartLinePos;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.entity.mobs.summon.MinecartMob;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.Inventory;
import necesse.inventory.InventoryItem;
import necesse.inventory.lootTable.LootTable;
import necesse.inventory.lootTable.lootItem.LootItem;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import theholyrailmod.container.ChestMinecartContainer;

public class ChestMinecartMob extends MinecartMob implements MobInventory {
   public static LootTable lootTable = new LootTable(new LootItem("chestminecart"));
   public static GameTexture texture;
   public float collisionMovementBuffer;
   public Point collisionMovementLastPos;
   public float chestMinecartSpeed;
   public int chestMinecartDir;
   public boolean isOpened;
   public boolean isMakingStop;
   public Inventory itemInventory = new Inventory(20);
   public int filledInventorySlots;
   protected SoundPlayer movingSound;
   protected SoundPlayer breakingSound;
   protected float breakParticleBuffer;
   protected boolean breakParticleAlternate;

   // vars used by the MinecartMobPatch
   public long stationedTime = -1L;
   public long lastStationLeft = -2L;
   public boolean isBeingStationed = false;
   public final long STATION_COOLDOWN_TIME = 350L;
   public final float MAX_SPEED = 190.0f;
   public final float BOOST_SPEED = MAX_SPEED * 4.04f;
   // ----------------------------------------------

   public ChestMinecartMob() {
      setSpeed(MAX_SPEED);
      setFriction(3.2F);
      this.accelerationMod = 0.08F;
      this.filledInventorySlots = 0;
   }

   @Override
   public void addSaveData(SaveData save) {
      super.addSaveData(save);
      save.addInt("chestMinecartDir", this.chestMinecartDir);
      save.addFloat("chestMinecartSpeed", this.chestMinecartSpeed);
      save.addLong("lastStationLeft", this.lastStationLeft);
      save.addLong("stationedTime", this.stationedTime);
      save.addBoolean("isBeingStationed", this.isBeingStationed);
      save.addBoolean("isOpened", this.isOpened);
      save.addSaveData(InventorySave.getSave(this.itemInventory, "items"));
   }

   @Override
   public void applyLoadData(LoadData save) {
      super.applyLoadData(save);
      this.chestMinecartDir = save.getInt("chestMinecartDir", this.chestMinecartDir);
      this.chestMinecartSpeed = save.getFloat("chestMinecartSpeed", this.chestMinecartSpeed);
      this.lastStationLeft = save.getLong("lastStationLeft", this.lastStationLeft);
      this.stationedTime = save.getLong("stationedTime", this.stationedTime);
      this.isBeingStationed = save.getBoolean("isBeingStationed", this.isBeingStationed);
      this.isOpened = save.getBoolean("isOpened", this.isOpened);

      LoadData itemSave = save.getFirstLoadDataByName("items");
      if (itemSave != null) {
         this.itemInventory.override(InventorySave.loadSave(itemSave));
      }
      getFilledInventorySlots(true);
   }

   @Override
   public void setupMovementPacket(PacketWriter writer) {
      super.setupMovementPacket(writer);
      writer.putNextBoolean(this.isBeingStationed);
      writer.putNextLong(this.lastStationLeft);
      writer.putNextLong(this.stationedTime);
      writer.putNextFloat(this.chestMinecartSpeed);
      writer.putNextMaxValue(this.chestMinecartDir, 3);
   }

   @Override
   public void applyMovementPacket(PacketReader reader, boolean isDirect) {
      super.applyMovementPacket(reader, isDirect);
      this.isBeingStationed = reader.getNextBoolean();
      this.lastStationLeft = reader.getNextLong();
      this.stationedTime = reader.getNextLong();
      this.chestMinecartSpeed = reader.getNextFloat();
      this.chestMinecartDir = reader.getNextMaxValue(3);
   }

   @Override
   public LootTable getLootTable() {
      return lootTable;
   }

   @Override
   public void serverTick() {
      super.serverTick();
      this.serverTickInventorySync(this.getLevel().getServer(), (Mob) this);
   }

   @Override
   public void setupSpawnPacket(PacketWriter writer) {
      super.setupSpawnPacket(writer);
      this.itemInventory.writeContent(writer);
   }

   @Override
   public void applySpawnPacket(PacketReader reader) {
      super.applySpawnPacket(reader);
      this.itemInventory.override(Inventory.getInventory(reader), hasArrivedAtTarget, hasArrivedAtTarget);
   }

   @Override
   public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList,
         Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
      super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);

      GameLight light = level.getLightLevel(getTileX(), getTileY());
      int drawX = camera.getDrawX(x) - 32;
      int drawY = camera.getDrawY(y) - 51;
      Point sprite = getAnimSprite(x, y, dir);

      drawY += getBobbing(x, y);
      drawY += getLevel().getTile(getTileX(), getTileY()).getMobSinkingAmount(this);
      final DrawOptions behind = texture.initDraw().sprite(sprite.x, sprite.y, 64).light(light).pos(drawX, drawY);

      DrawOptions drawOptions = this.isOpened ? MobRegistry.Textures.fromFile("chestminecart_open").initDraw()
            .sprite(sprite.x, sprite.y, 64)
            .light(light)
            .pos(drawX, drawY)
            : texture.initDraw()
                  .sprite(sprite.x, sprite.y, 64)
                  .light(light)
                  .pos(drawX, drawY);

      list.add(new MobDrawable() {
         @Override
         public void draw(TickManager tickManager) {
            drawOptions.draw();
         }

         @Override
         public void drawBehindRider(TickManager tickManager) {
            behind.draw();
         }
      });

      addShadowDrawables(tileList, x, y, light, camera);
   }

   @Override
   public void onDeath(Attacker attacker, HashSet<Attacker> attackers) {
      super.onDeath(attacker, attackers);

      ArrayList<InventoryItem> containedItems = new ArrayList<InventoryItem>();
      for (int i = 0; i < this.itemInventory.getSize(); ++i) {
         containedItems.add(this.itemInventory.getItem(i));
      }

      MobLootTableDropsEvent dropEvent;
      ArrayList<InventoryItem> drops = this.getLootTable().getNewList(GameRandom.globalRandom, this);
      Point publicLootPosition = this.getLootDropsPosition(null);
      GameEvents.triggerEvent(dropEvent = new MobLootTableDropsEvent(this, publicLootPosition, drops));
      if (dropEvent.dropPos != null && dropEvent.drops != null) {
         for (InventoryItem item : containedItems) {
            if (item != null) {
               ItemPickupEntity entity = item.getPickupEntity(this.getLevel(), (float) dropEvent.dropPos.x,
                     (float) dropEvent.dropPos.y);
               this.getLevel().entityManager.pickups.add(entity);
            }
         }
         this.itemInventory.clearInventory();
      }

   }

   public static void drawPlacePreview(Level level, int levelX, int levelY, int dir, GameCamera camera) {
      Mob mob = MobRegistry.getMob("chestminecartmob", level);
      if (mob != null) {
         mob.setPos((float) levelX, (float) levelY, true);
         int tileX = mob.getTileX();
         int tileY = mob.getTileY();
         GameObject object = level.getObject(tileX, tileY);
         if (object instanceof MinecartTrackObject) {
            MinecartTrackObject trackObject = (MinecartTrackObject) object;
            float moveX = 0.0F;
            float moveY = 0.0F;
            if (dir == 0) {
               moveY = -1.0F;
            } else if (dir == 1) {
               moveX = 1.0F;
            } else if (dir == 2) {
               moveY = 1.0F;
            } else {
               moveX = -1.0F;
            }

            MinecartLines lines = trackObject.getMinecartLines(level, tileX, tileY, moveX, moveY);
            MinecartLinePos pos = lines.getMinecartPos((float) levelX, (float) levelY, dir);
            if (pos != null) {
               int drawX = camera.getDrawX(pos.x) - 32;
               int drawY = camera.getDrawY(pos.y) - 47;
               Point sprite = mob.getAnimSprite((int) pos.x, (int) pos.y, pos.dir);
               drawY += mob.getBobbing((int) pos.x, (int) pos.y);
               drawY += level.getTile((int) pos.x / 32, (int) pos.y / 32).getMobSinkingAmount(mob);
               texture.initDraw().sprite(sprite.x, sprite.y, 64).alpha(0.5F).draw(drawX, drawY);
               return;
            }
         }
      }

      int drawX = camera.getDrawX(levelX) - 32;
      int drawY = camera.getDrawY(levelY) - 47;
      drawY += level.getLevelTile(levelX / 32, levelY / 32).getLiquidBobbing();
      texture.initDraw().sprite(0, dir, 64).alpha(0.5F).draw(drawX, drawY);
   }

   @Override
   public boolean canInteract(Mob mob) {
      return true;
   }

   @Override
   public String getInteractTip(PlayerMob perspective, boolean debug) {
      return this.isOpened ? null : Localization.translate("controls", "opentip");
   }

   @Override
   public void interact(PlayerMob player) {
      Level level = player.getLevel();

      if (level.isServerLevel() && player.isServerClient()) {
         ServerClient client = player.getServerClient();
         int CHEST_MINECART_CONTAINER = ChestMinecartContainer.registryId;
         PacketOpenContainer p = PacketOpenContainer.Mob(CHEST_MINECART_CONTAINER, this,
               ChestMinecartContainer.getChestMinecartContent(this, client.getServer(), client));
         ContainerRegistry.openAndSendContainer(client, p);
      }
   }

   @Override
   public Inventory getInventory() {
      return this.itemInventory;
   }

   public Inventory getInventory(Packet contentPacket) {
      return getInventory(new PacketReader(contentPacket));
   }

   public Inventory getInventory(PacketReader reader) {
      int size = reader.getNextShortUnsigned();
      Inventory out = new Inventory(size) {
         @Override
         public boolean canLockItem(int slot) {
            return true;
         }
      };

      for (int i = 0; i < out.getSize(); ++i) {
         if (reader.getNextBoolean()) {
            out.setItem(i, InventoryItem.fromContentPacket(reader));
         }
      }

      return out;
   }

   public int getFilledInventorySlots(boolean recalc) {
      if (recalc) {
         int invSize = this.getInventory().getSize();
         int totalFilledSlots = 0;
         for (int i = 0; i < invSize; ++i) {
            if (this.getInventory().isSlotClear(i)) {
               continue;
            }
            ++totalFilledSlots;
         }

         return totalFilledSlots;
      } else {
         return this.filledInventorySlots;
      }
   }

   public void setFilledInventorySlots(int filledSlots) {
      this.filledInventorySlots = filledSlots;
   }

   public void getAndSetFilledInventorySlots() {
      int invSize = this.getInventory().getSize();
      int totalFilledSlots = 0;
      for (int i = 0; i < invSize; ++i) {
         if (this.getInventory().isSlotClear(i)) {
            continue;
         }
         ++totalFilledSlots;
      }

      this.filledInventorySlots = totalFilledSlots;
   }

   public void sortItems() {
      this.itemInventory.sortItems(0, this.itemInventory.getSize() - 1);
   }

   public ChestMinecartMob getMob() {
      return this;
   }

   public boolean getIsOpened() {
      return this.isOpened;
   }

   public void setIsOpened(boolean opened) {
      this.isOpened = opened;
   }

   public boolean getIsMakingStop() {
      return this.isMakingStop;
   }

   public void setIsMakingStop(boolean stopping) {
      this.isMakingStop = stopping;
   }

   public long getStationedTime() {
      return this.stationedTime;
   }

   public void setStationedTime(long stationedDur) {
      this.stationedTime = stationedDur;
   }

   public long getLastStationLeft() {
      return this.lastStationLeft;
   }

   public void setLastStationLeft(long stationedLeft) {
      this.lastStationLeft = stationedLeft;
   }

   public boolean getIsBeingStationed() {
      return this.isBeingStationed;
   }

   public void setIsBeingStationed(boolean beingStationed) {
      this.isBeingStationed = beingStationed;
   }

   public long getTimeSinceStationed(ChestMinecartMob mob) {
      if (stationedTime < 0L) {
         return stationedTime;
      } else {
         return isBeingStationed ? mob.getWorldEntity().getTime() - stationedTime : -1L;
      }
   }

   public long getTimeSinceLeftLastStation(ChestMinecartMob mob) {
      if (lastStationLeft < 0L) {
         return lastStationLeft;
      } else {
         return isBeingStationed ? -1
               : mob.getWorldEntity().getTime() - lastStationLeft;
      }
   }

   public static int registerChestMinecartMob() {
      return MobRegistry.registerMob("chestminecartmob", ChestMinecartMob.class, false);
   }

}
