package theholyrailmod.theholyrail;

import java.awt.Point;
import java.util.List;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.MobRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.sound.SoundPlayer;
import necesse.engine.tickManager.TickManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.summon.MinecartLinePos;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.entity.mobs.summon.MinecartMob;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.lootTable.LootTable;
import necesse.inventory.lootTable.lootItem.LootItem;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

public class RailRunnerMob extends MinecartMob {
   public static LootTable lootTable = new LootTable(new LootItem("railrunner"));
   public static GameTexture texture;
   public float collisionMovementBuffer;
   public Point collisionMovementLastPos;
   public float railRunnerSpeed;
   public int railRunnerDir;
   protected SoundPlayer movingSound;
   protected SoundPlayer breakingSound;
   protected float breakParticleBuffer;
   protected boolean breakParticleAlternate;

   // vars used by the MinecartMobPatch
   public final float MAX_SPEED = 265.0f;
   public final float BOOST_SPEED = MAX_SPEED * 5;
   // ----------------------------------------------

   public RailRunnerMob() {
      setSpeed(MAX_SPEED);
      setFriction(2.65F);
      this.accelerationMod = 0.4F;
   }

   @Override
   public void addSaveData(SaveData save) {
      super.addSaveData(save);
      save.addInt("railRunnerDir", this.railRunnerDir);
      save.addFloat("railRunnerSpeed", this.railRunnerSpeed);
   }

   @Override
   public void applyLoadData(LoadData save) {
      super.applyLoadData(save);
      this.railRunnerDir = save.getInt("railRunnerDir", this.railRunnerDir);
      this.railRunnerSpeed = save.getFloat("railRunnerSpeed", this.railRunnerSpeed);
   }

   @Override
   public void setupMovementPacket(PacketWriter writer) {
      super.setupMovementPacket(writer);
      writer.putNextFloat(this.railRunnerSpeed);
      writer.putNextMaxValue(this.railRunnerDir, 3);
   }

   @Override
   public void applyMovementPacket(PacketReader reader, boolean isDirect) {
      super.applyMovementPacket(reader, isDirect);
      this.railRunnerSpeed = reader.getNextFloat();
      this.railRunnerDir = reader.getNextMaxValue(3);
   }

   @Override
   public LootTable getLootTable() {
      return lootTable;
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

      DrawOptions drawOptions = texture.initDraw()
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

   public static void drawPlacePreview(Level level, int levelX, int levelY, int dir, GameCamera camera) {
      Mob mob = MobRegistry.getMob("railrunnermob", level);
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

            MinecartLines lines = trackObject.getMinecartLines(level, tileX, tileY, moveX, moveY, false);
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

   public static int registerRailRunnerMob() {
      return MobRegistry.registerMob("railrunnermob", RailRunnerMob.class, false);
   }
}
