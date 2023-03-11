package theholyrailmod.theholyrail;

import java.util.Arrays;
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
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

public class PoweredRailObject extends MinecartTrackObject {
   private String[] railIds = new String[] {"minecarttrack", "poweredrail"};

   protected int objectId;

   public GameTexture unpoweredTexture;
   public GameTexture poweredTexture;

   public PoweredRailObject() { 
   this.setItemCategory(new String[]{"wiring"});
   this.stackSize = 50;
   this.showsWire = true;
   this.canReplaceRotation = false;
   this.canPlaceOnShore = false; // for now
   this.canPlaceOnLiquid = false; // for now
   this.overridesInLiquid = false; // for now
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
   public void onWireUpdate(Level level, int x, int y, int wireID, boolean active)
   {        
      level.lightManager.updateStaticLight(x, y);

      if (isPowered(level, x, y)) {
         this.roomProperties.add("lights");
      } else {
         this.roomProperties.remove("lights");         
      }

      if (active) {
         ObjectEntity ent = level.entityManager.getObjectEntity(x, y);       
         if (ent != null) {
            ((PoweredRailObjectEntity)ent).isPowered = true;
         }
      }
   }

   public boolean isPowered(Level level, int x, int y) {
      return level.wireManager.isWireActiveAny(x, y);
   }
   // rotation == 0 // up
   // rotation == 1 // right
   // rotation == 2 // down
   // rotation == 3 // left
     
   public PoweredRailObject.TrackSprite getPoweredRailSprite(Level level, int tileX, int tileY, int rotation) {
      boolean alternateSprite;
      synchronized(this.drawRandom) {
         alternateSprite = this.drawRandom.seeded(this.getTileSeed(tileX, tileY)).nextBoolean();
      }

      PoweredRailObject.TrackSprite out = new PoweredRailObject.TrackSprite();
      boolean adjTop = Arrays.stream(this.railIds).anyMatch(level.getObject(tileX, tileY - 1).getStringID()::equals);
      boolean adjRight = Arrays.stream(this.railIds).anyMatch(level.getObject(tileX + 1, tileY).getStringID()::equals);
      boolean adjBot = Arrays.stream(this.railIds).anyMatch(level.getObject(tileX, tileY + 1).getStringID()::equals);
      boolean adjLeft = Arrays.stream(this.railIds).anyMatch(level.getObject(tileX - 1, tileY).getStringID()::equals);
      if (rotation == 0) {
         if (adjLeft) {
            byte adjRotation = level.getObjectRotation(tileX - 1, tileY);
            adjLeft = adjRotation != 0 && adjRotation != 2;
         }

         if (adjRight) {
            byte adjRotation = level.getObjectRotation(tileX + 1, tileY);
            adjRight = adjRotation != 0 && adjRotation != 2;
         }

         if (adjLeft && adjRight) {
            out.goingUp();
            out.goingRight();
            out.goingDown();
            out.goingLeft();
            if (!adjTop) {
               out.connectedUp = false;
            }

            if (!adjBot) {
               out.connectedDown = false;
            }

            return out.sprite(4, 0);
         } else if (adjLeft) {
            if (adjBot) {
               out.goingUp();
               out.goingDown();
               out.goingLeft();
               if (!adjTop) {
                  out.connectedUp = false;
               }

               return out.sprite(3, 3);
            } else {
               out.goingUp();
               out.goingLeft();
               if (!adjTop) {
                  out.connectedUp = false;
               }

               return out.sprite(3, 1);
            }
         } else if (adjRight) {
            if (adjBot) {
               out.goingUp();
               out.goingRight();
               out.goingDown();
               if (!adjTop) {
                  out.connectedUp = false;
               }

               return out.sprite(2, 3);
            } else {
               out.goingUp();
               out.goingRight();
               if (!adjTop) {
                  out.connectedUp = false;
               }

               return out.sprite(2, 1);
            }
         } else {
            out.goingUp();
            out.goingDown();
            if (!adjTop) {
               out.connectedUp = false;
            }

            if (!adjBot) {
               out.connectedDown = false;
            }

            return out.sprite(1, alternateSprite ? 1 : 0);
         }
      } else if (rotation == 1) {
         if (adjTop) {
            byte adjRotation = level.getObjectRotation(tileX, tileY - 1);
            adjTop = adjRotation != 1 && adjRotation != 3;
         }

         if (adjBot) {
            byte adjRotation = level.getObjectRotation(tileX, tileY + 1);
            adjBot = adjRotation != 1 && adjRotation != 3;
         }

         if (adjTop && adjBot) {
            out.goingUp();
            out.goingRight();
            out.goingDown();
            out.goingLeft();
            if (!adjLeft) {
               out.connectedLeft = false;
            }

            if (!adjRight) {
               out.connectedRight = false;
            }

            return out.sprite(4, 1);
         } else if (adjTop) {
            if (adjLeft) {
               out.goingUp();
               out.goingRight();
               out.goingLeft();
               if (!adjRight) {
                  out.connectedRight = false;
               }

               return out.sprite(0, 3);
            } else {
               out.goingUp();
               out.goingRight();
               if (!adjRight) {
                  out.connectedRight = false;
               }

               return out.sprite(2, 1);
            }
         } else if (adjBot) {
            if (adjLeft) {
               out.goingRight();
               out.goingDown();
               out.goingLeft();
               if (!adjRight) {
                  out.connectedRight = false;
               }

               return out.sprite(0, 2);
            } else {
               out.goingRight();
               out.goingDown();
               if (!adjRight) {
                  out.connectedRight = false;
               }

               return out.sprite(2, 0);
            }
         } else {
            out.goingRight();
            out.goingLeft();
            if (!adjLeft) {
               out.connectedLeft = false;
            }

            if (!adjRight) {
               out.connectedRight = false;
            }

            return out.sprite(0, alternateSprite ? 1 : 0);
         }
      } else if (rotation == 2) {
         if (adjLeft) {
            byte adjRotation = level.getObjectRotation(tileX - 1, tileY);
            adjLeft = adjRotation != 0 && adjRotation != 2;
         }

         if (adjRight) {
            byte adjRotation = level.getObjectRotation(tileX + 1, tileY);
            adjRight = adjRotation != 0 && adjRotation != 2;
         }

         if (adjLeft && adjRight) {
            out.goingUp();
            out.goingRight();
            out.goingDown();
            out.goingLeft();
            if (!adjTop) {
               out.connectedUp = false;
            }

            if (!adjBot) {
               out.connectedDown = false;
            }

            return out.sprite(4, 2);
         } else if (adjLeft) {
            if (adjTop) {
               out.goingUp();
               out.goingDown();
               out.goingLeft();
               if (!adjBot) {
                  out.connectedDown = false;
               }

               return out.sprite(3,2);
            } else {
               out.goingDown();
               out.goingLeft();
               if (!adjBot) {
                  out.connectedDown = false;
               }

               return out.sprite(3, 0);
            }
         } else if (adjRight) {
            if (adjTop) {
               out.goingUp();
               out.goingRight();
               out.goingDown();
               if (!adjBot) {
                  out.connectedDown = false;
               }

               return out.sprite(2, 2);
            } else {
               out.goingRight();
               out.goingDown();
               if (!adjBot) {
                  out.connectedDown = false;
               }

               return out.sprite(2, 0);
            }
         } else {
            out.goingUp();
            out.goingDown();
            if (!adjTop) {
               out.connectedUp = false;
            }

            if (!adjBot) {
               out.connectedDown = false;
            }

            return out.sprite(1, alternateSprite ? 1 : 0);
         }
      } else {
         if (adjTop) {
            byte adjRotation = level.getObjectRotation(tileX, tileY - 1);
            adjTop = adjRotation != 1 && adjRotation != 3;
         }

         if (adjBot) {
            byte adjRotation = level.getObjectRotation(tileX, tileY + 1);
            adjBot = adjRotation != 1 && adjRotation != 3;
         }

         if (adjTop && adjBot) {
            out.goingUp();
            out.goingRight();
            out.goingDown();
            out.goingLeft();
            if (!adjLeft) {
               out.connectedLeft = false;
            }

            if (!adjRight) {
               out.connectedRight = false;
            }

            return out.sprite(4, 3);
         } else if (adjTop) {
            if (adjRight) {
               out.goingUp();
               out.goingRight();
               out.goingLeft();
               if (!adjLeft) {
                  out.connectedLeft = false;
               }

               return out.sprite(1, 3);
            } else {
               out.goingUp();
               out.goingLeft();
               if (!adjLeft) {
                  out.connectedLeft = false;
               }

               return out.sprite(3, 1);
            }
         } else if (adjBot) {
            if (adjRight) {
               out.goingRight();
               out.goingDown();
               out.goingLeft();
               if (!adjLeft) {
                  out.connectedLeft = false;
               }

               return out.sprite(1, 2);
            } else {
               out.goingDown();
               out.goingLeft();
               if (!adjLeft) {
                  out.connectedLeft = false;
               }

               return out.sprite(3, 0);
            }
         } else {
            out.goingRight();
            out.goingLeft();
            if (!adjLeft) {
               out.connectedLeft = false;
            }

            if (!adjRight) {
               out.connectedRight = false;
            }

            return out.sprite(1, alternateSprite ? 1 : 0);
         }
      }
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
      PlayerMob perspective
   ) {
      byte rotation = level.getObjectRotation(tileX, tileY);
      GameLight light = level.getLightLevel(tileX, tileY);
      int drawX = camera.getTileDrawX(tileX);
      int drawY = camera.getTileDrawY(tileY);
      DrawOptionsList options = new DrawOptionsList();
      PoweredRailObject.TrackSprite sprite = this.getPoweredRailSprite(level, tileX, tileY, rotation);
      if (level.isLiquidTile(tileX, tileY) || level.isShore(tileX, tileY)) {
         if ((level.isLiquidTile(tileX, tileY + 1) || level.isShore(tileX, tileY + 1))
            && (!sprite.connectedDown || sprite.connectedLeft || sprite.connectedRight)) {
            TextureDrawOptions bridgeOptions = this.bridgeTexture.initDraw().sprite(sprite.x, sprite.y, 32).light(light).pos(drawX, drawY + 8);
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
   public void drawPreview(Level level, int tileX, int tileY, int rotation, float alpha, PlayerMob player, GameCamera camera) {
      int drawX = camera.getTileDrawX(tileX);
      int drawY = camera.getTileDrawY(tileY);
      PoweredRailObject.TrackSprite sprite = this.getPoweredRailSprite(level, tileX, tileY, rotation);
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

   private static class TrackSprite {
      public int x;
      public int y;
      public boolean goingUp;
      public boolean goingRight;
      public boolean goingDown;
      public boolean goingLeft;
      public boolean connectedUp;
      public boolean connectedRight;
      public boolean connectedDown;
      public boolean connectedLeft;

      private TrackSprite() {
      }

      public void goingUp() {
         this.goingUp = true;
         this.connectedUp = true;
      }

      public void goingRight() {
         this.goingRight = true;
         this.connectedRight = true;
      }

      public void goingDown() {
         this.goingDown = true;
         this.connectedDown = true;
      }

      public void goingLeft() {
         this.goingLeft = true;
         this.connectedLeft = true;
      }

      public PoweredRailObject.TrackSprite sprite(int spriteX, int spriteY) {
         this.x = spriteX;
         this.y = spriteY;
         return this;
      }
   }
}
