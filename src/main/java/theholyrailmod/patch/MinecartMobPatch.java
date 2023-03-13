package theholyrailmod.patch;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.summon.MinecartMob;
import necesse.level.gameObject.GameObject;
import net.bytebuddy.asm.Advice;
import theholyrailmod.theholyrail.ChestMinecartMob;
import theholyrailmod.theholyrail.PoweredRailObject;
import theholyrailmod.theholyrail.RailRunnerMob;

public class MinecartMobPatch {
    @ModMethodPatch(target = MinecartMob.class, name = "tickCollisionMovement", arguments = {float.class, Mob.class})
    public static class TickCollisionMovementPatch {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This MinecartMob obj, @Advice.Argument(0) float delta, @Advice.Argument(1) Mob rider) {            
            int tileX = obj.getTileX();
            int tileY = obj.getTileY();
            GameObject object = obj.getLevel().getObject(tileX, tileY);        

            if (!(obj instanceof RailRunnerMob) && !(obj instanceof ChestMinecartMob)) {
                if (object instanceof PoweredRailObject) {
                    if (((PoweredRailObject)object).isPowered(obj.getLevel(), tileX, tileY) && rider != null) {
                        obj.minecartSpeed = Math.min(200.0f, obj.minecartSpeed + 5.7f);
                    } else if (rider == null) {
                        obj.minecartSpeed = 0.0f;
                    } else {
                        obj.minecartSpeed = Math.min(4f, obj.minecartSpeed > 4f ? obj.minecartSpeed / 2.0f : obj.minecartSpeed - 0.01f);
                    }
                }
            } else {
                if (object instanceof PoweredRailObject) {
                    if (obj instanceof RailRunnerMob) {
                        if (((PoweredRailObject)object).isPowered(obj.getLevel(), tileX, tileY) && rider != null) {
                            obj.minecartSpeed = Math.min(262.0f, obj.minecartSpeed + 9.2f);
                        } else if (rider == null) {
                            obj.minecartSpeed = 0.0f;
                        } else {
                            obj.minecartSpeed = Math.min(12f, obj.minecartSpeed > 12f ? obj.minecartSpeed / 2.0f : obj.minecartSpeed - 0.01f);
                        }
                    } else if (obj instanceof ChestMinecartMob) {
                        if (((PoweredRailObject)object).isPowered(obj.getLevel(), tileX, tileY)) {
                            obj.minecartSpeed = Math.min(215.0f, obj.minecartSpeed + 5.85f);
                        } else {
                            obj.minecartSpeed = Math.min(8f, obj.minecartSpeed > 8f ? obj.minecartSpeed / 2.0f : obj.minecartSpeed - 0.01f);
                        }                        
                    }
                }
            }
                    
            obj.sendMovementPacket(false);  
        }        
    }
}
