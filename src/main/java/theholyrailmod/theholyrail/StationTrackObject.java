package theholyrailmod.theholyrail;

import java.util.List;

import necesse.engine.localization.Localization;
import necesse.engine.network.server.ServerClient;
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
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import theholyrailmod.container.StationTrackContainer;

public class StationTrackObject extends CustomTrackObject {
   protected int objectId;
   public GameTexture unpoweredTexture;
   public GameTexture poweredTexture;

   public StationTrackObject() {
      this.setItemCategory(new String[] { "wiring" });
      this.stackSize = 50;
      this.showsWire = true;
   }

   @Override
   public void loadTextures() {
      super.loadTextures();
      this.unpoweredTexture = GameTexture.fromFile("objects/stationtrack_unpowered");
      this.poweredTexture = GameTexture.fromFile("objects/stationtrack");
   }

   @Override
   public ObjectEntity getNewObjectEntity(Level level, int x, int y) {
      return new StationTrackObjectEntity(level, x, y);
   }

   @Override
   public void onWireUpdate(Level level, int x, int y, int wireID, boolean active) {
      if (active) {
         ObjectEntity ent = level.entityManager.getObjectEntity(x, y);
         if (ent != null) {
            ((StationTrackObjectEntity) ent).isPowered = true;
         }
      }
   }

   public boolean isPowered(Level level, int x, int y) {
      ObjectEntity ent = level.entityManager.getObjectEntity(x, y);
      if (ent != null && ent instanceof StationTrackObjectEntity) {
         if (level.wireManager.isWireActiveAny(x, y)) {
            ((StationTrackObjectEntity) ent).isPowered = true;
            return true;
         } else {
            ((StationTrackObjectEntity) ent).isPowered = false;
            return false;
         }
      }

      return false;
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

   @Override
   public boolean canInteract(Level level, int x, int y, PlayerMob player) {
      return true;
   }

   @Override
   public String getInteractTip(Level level, int x, int y, PlayerMob perspective, boolean debug) {
      return Localization.translate("controls", "usetip");
   }

   @Override
   public ListGameTooltips getItemTooltips(InventoryItem item, PlayerMob perspective) {
      ListGameTooltips tooltips = new ListGameTooltips();
      tooltips.add(Localization.translate("itemtooltip", "stationtracktip"));
      return tooltips;
   }

   @Override
   public void interact(Level level, int x, int y, PlayerMob player) {
      if (level.isServerLevel() && player.isServerClient()) {
         ServerClient client = player.getServerClient();

         int STATION_TRACK_CONTAINER = StationTrackContainer.registryId;
         StationTrackObjectEntity oe = (StationTrackObjectEntity) level.entityManager.getObjectEntity(x, y);
         if (oe != null) {
            oe.openContainer(STATION_TRACK_CONTAINER, client);
         }
      }
   }

   public static int registerStationTrack() {
      StationTrackObject stObj = new StationTrackObject();

      stObj.objectId = ObjectRegistry.registerObject("stationtrack", stObj, 10, true);
      return stObj.objectId;
   }
}
