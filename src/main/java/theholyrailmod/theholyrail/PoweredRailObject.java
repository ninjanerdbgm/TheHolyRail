package theholyrailmod.theholyrail;

import java.util.List;

import necesse.engine.registries.ObjectRegistry;
import necesse.engine.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

public class PoweredRailObject extends CustomTrackObject {
   protected int objectId;

   public GameTexture unpoweredTexture;
   public GameTexture poweredTexture;

   public PoweredRailObject() {
      this.setItemCategory(new String[] { "wiring" });
      this.stackSize = 50;
      this.showsWire = true;
      this.isLightTransparent = false;
      this.roomProperties.add("lights");
   }

   @Override
   public void loadTextures() {
      super.loadTextures();
      this.unpoweredTexture = GameTexture.fromFile("objects/poweredrail_unpowered");
      this.poweredTexture = GameTexture.fromFile("objects/poweredrail");
   }

   @Override
   public ObjectEntity getNewObjectEntity(Level level, int x, int y) {
      return new PoweredRailObjectEntity(level, x, y);
   }

   @Override
   public int getLightLevel(Level level, int x, int y) {
      return this.isPowered(level, x, y) ? 50 : 0;
   }

   @Override
   public void onWireUpdate(Level level, int x, int y, int wireID, boolean active) {
      level.lightManager.updateStaticLight(x, y);

      if (isPowered(level, x, y)) {
         this.roomProperties.add("lights");
      } else {
         this.roomProperties.remove("lights");
      }

      if (active) {
         ObjectEntity ent = level.entityManager.getObjectEntity(x, y);
         if (ent != null) {
            ((PoweredRailObjectEntity) ent).isPowered = true;
         }
      }
   }

   public boolean isPowered(Level level, int x, int y) {
      return level.wireManager.isWireActiveAny(x, y);
   }

   @Override
   public void addDrawables(
         List<LevelSortedDrawable> list,
         OrderableDrawables tileList,
         Level level,
         int tileX,
         int tileY,
         TickManager tickManager,
         GameCamera camera,
         PlayerMob perspective) {
      byte rotation = level.getObjectRotation(tileX, tileY);
      GameLight light = level.getLightLevel(tileX, tileY);
      int drawX = camera.getTileDrawX(tileX);
      int drawY = camera.getTileDrawY(tileY);
      DrawOptionsList options = new DrawOptionsList();
      CustomTrackObject.TrackSprite sprite = this.getCustomSprite(level, tileX, tileY, rotation);
      if (level.isLiquidTile(tileX, tileY) || level.isShore(tileX, tileY)) {
         if ((level.isLiquidTile(tileX, tileY + 1) || level.isShore(tileX, tileY + 1))
               && (!sprite.connectedDown || sprite.connectedLeft || sprite.connectedRight)) {
            TextureDrawOptions bridgeOptions = this.bridgeTexture.initDraw().sprite(sprite.x, sprite.y, 32).light(light)
                  .pos(drawX, drawY + 8);
            tileList.add(-100, tm -> bridgeOptions.draw());
         }

         options.add(this.supportTexture.initDraw().sprite(sprite.x, sprite.y, 32).light(light).pos(drawX, drawY));
      }

      if (this.isPowered(level, tileX, tileY)) {
         options.add(this.poweredTexture.initDraw().sprite(sprite.x, sprite.y, 32).light(light).pos(drawX, drawY));
      } else {
         options.add(this.unpoweredTexture.initDraw().sprite(sprite.x, sprite.y, 32).light(light).pos(drawX, drawY));
      }
      if (sprite.goingUp && !sprite.connectedUp) {
         options.add(this.endingTexture.initDraw().sprite(0, 0, 32).light(light).pos(drawX, drawY));
      }

      if (sprite.goingRight && !sprite.connectedRight) {
         options.add(this.endingTexture.initDraw().sprite(0, 1, 32).light(light).pos(drawX, drawY));
      }

      if (sprite.goingDown && !sprite.connectedDown) {
         options.add(this.endingTexture.initDraw().sprite(0, 2, 32).light(light).pos(drawX, drawY));
      }

      if (sprite.goingLeft && !sprite.connectedLeft) {
         options.add(this.endingTexture.initDraw().sprite(0, 3, 32).light(light).pos(drawX, drawY));
      }

      tileList.add(tm -> options.draw());
   }

   @Override
   public void drawPreview(Level level, int tileX, int tileY, int rotation, float alpha, PlayerMob player,
         GameCamera camera) {
      int drawX = camera.getTileDrawX(tileX);
      int drawY = camera.getTileDrawY(tileY);
      CustomTrackObject.TrackSprite sprite = this.getCustomSprite(level, tileX, tileY, rotation);
      if (level.isLiquidTile(tileX, tileY) || level.isShore(tileX, tileY)) {
         if ((level.isLiquidTile(tileX, tileY + 1) || level.isShore(tileX, tileY + 1))
               && (!sprite.connectedDown || sprite.connectedLeft || sprite.connectedRight)) {
            this.bridgeTexture.initDraw().sprite(sprite.x, sprite.y, 32).alpha(alpha).draw(drawX, drawY + 8);
         }

         this.supportTexture.initDraw().sprite(sprite.x, sprite.y, 32).alpha(alpha).draw(drawX, drawY);
      }

      if (this.isPowered(level, tileX, tileY)) {
         this.poweredTexture.initDraw().sprite(sprite.x, sprite.y, 32).alpha(alpha).draw(drawX, drawY);
      } else {
         this.unpoweredTexture.initDraw().sprite(sprite.x, sprite.y, 32).alpha(alpha).draw(drawX, drawY);
      }

      if (sprite.goingUp && !sprite.connectedUp) {
         this.endingTexture.initDraw().sprite(0, 0, 32).alpha(alpha).draw(drawX, drawY);
      }

      if (sprite.goingRight && !sprite.connectedRight) {
         this.endingTexture.initDraw().sprite(0, 1, 32).alpha(alpha).draw(drawX, drawY);
      }

      if (sprite.goingDown && !sprite.connectedDown) {
         this.endingTexture.initDraw().sprite(0, 2, 32).alpha(alpha).draw(drawX, drawY);
      }

      if (sprite.goingLeft && !sprite.connectedLeft) {
         this.endingTexture.initDraw().sprite(0, 3, 32).alpha(alpha).draw(drawX, drawY);
      }
   }

   public static int registerPoweredRail() {
      PoweredRailObject prObj = new PoweredRailObject();

      prObj.objectId = ObjectRegistry.registerObject("poweredrail", prObj, 10, true);
      return prObj.objectId;
   }
}
