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
import necesse.entity.mobs.MobDrawable;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.summon.MinecartMob;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.lootTable.LootTable;
import necesse.inventory.lootTable.lootItem.LootItem;
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

   public RailRunnerMob() { 
      setSpeed(260.0F);
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
   public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
      super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);

      if (texture == null) {         
         texture = GameTexture.fromFile("mobs/railrunner");  
      }

      GameLight light = level.getLightLevel(getTileX(), getTileY());
      int drawX = camera.getDrawX(x) - 32;
      int drawY = camera.getDrawY(y) - 51;
      Point sprite = getAnimSprite(x, y, dir);

      drawY += getBobbing(x, y);
      drawY += getLevel().getTile(getTileX(), getTileY()).getMobSinkingAmount(this);

      DrawOptions drawOptions = texture.initDraw()
               .sprite(sprite.x, sprite.y, 64)
               .light(light)
               .pos(drawX, drawY);

      list.add(new MobDrawable() {
         @Override
         public void draw(TickManager tickManager) {
               drawOptions.draw();
         }
      });

      addShadowDrawables(tileList, x, y, light, camera);
   }

   public static int registerRailRunnerMob() {
      texture = GameTexture.fromFile("mobs/railrunner");      
      return MobRegistry.registerMob("railrunnermob", RailRunnerMob.class, false);
   }
}
