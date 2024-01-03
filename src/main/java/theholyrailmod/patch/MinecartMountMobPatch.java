package theholyrailmod.patch;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.summon.summonFollowingMob.mountFollowingMob.MinecartMountMob;
import necesse.level.gameObject.GameObject;
import net.bytebuddy.asm.Advice;
import theholyrailmod.theholyrail.PoweredRailObject;
import theholyrailmod.theholyrail.RailRunnerMob;
import theholyrailmod.theholyrail.StationTrackObject;
import theholyrailmod.theholyrail.StationTrackObjectEntity;

public class MinecartMountMobPatch {
    @ModMethodPatch(target = MinecartMountMob.class, name = "tickCollisionMovement", arguments = { float.class,
            Mob.class })
    public static class TickCollisionMovementPatch {
        public static StationTrackObjectEntity stationTrackEntity;

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This MinecartMountMob mobObject,
                @Advice.Argument(0) float delta,
                @Advice.Argument(1) Mob rider) {
            int tileX = mobObject.getTileX();
            int tileY = mobObject.getTileY();
            GameObject trackObject = mobObject.getLevel().getObject(tileX, tileY);

            if (trackObject instanceof StationTrackObject) {
                // set station track-specific variables
                stationTrackEntity = (StationTrackObjectEntity) mobObject.getLevel().entityManager
                        .getObjectEntity(tileX, tileY);
            }

            RailRunnerMob rrMob = mobObject instanceof RailRunnerMob ? (RailRunnerMob) mobObject : null;

            if (!(mobObject instanceof RailRunnerMob)) {
                if (trackObject instanceof PoweredRailObject) {
                    if (((PoweredRailObject) trackObject).isPowered(mobObject.getLevel(), tileX, tileY)
                            && rider != null) {
                        float MAX_SPEED = 200.0f;
                        float BOOST_SPEED = MAX_SPEED * 4.5f;
                        mobObject.minecartSpeed = Math.min(MAX_SPEED,
                                mobObject.minecartSpeed
                                        + (BOOST_SPEED * delta / 150.0f
                                                * mobObject.getAccelerationModifier()));
                    } else if (rider == null) {
                        mobObject.minecartSpeed = 0.0f;
                    } else {
                        mobObject.minecartSpeed = Math.min(0f,
                                mobObject.minecartSpeed > 4f ? (mobObject.minecartSpeed / 2.0f) * delta / 250.0f
                                        : mobObject.minecartSpeed > 0f
                                                ? (mobObject.minecartSpeed - 0.01f) * delta / 250.0f
                                                : 0f);
                    }
                }
            } else {
                // Powered Track
                if (trackObject instanceof PoweredRailObject) {
                    if (mobObject instanceof RailRunnerMob) {
                        if (((PoweredRailObject) trackObject).isPowered(mobObject.getLevel(), tileX, tileY)
                                && rider != null) {
                            mobObject.minecartSpeed = Math.min(rrMob.MAX_SPEED,
                                    mobObject.minecartSpeed
                                            + (rrMob.BOOST_SPEED * delta / 150.0f
                                                    * mobObject.getAccelerationModifier()));
                        } else if (rider == null) {
                            mobObject.minecartSpeed = 0.0f;
                        } else {
                            mobObject.minecartSpeed = Math.max(0f,
                                    mobObject.minecartSpeed > 12f ? (mobObject.minecartSpeed / 2.0f) * delta / 250.0f
                                            : (mobObject.minecartSpeed - 0.1f) * delta / 250.0f);
                        }
                    }
                }
            }
        }
    }
}
