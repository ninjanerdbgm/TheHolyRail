package theholyrailmod.patch;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.summon.MinecartMob;
import necesse.level.gameObject.GameObject;
import net.bytebuddy.asm.Advice;
import theholyrailmod.theholyrail.ChestMinecartMob;
import theholyrailmod.theholyrail.PoweredRailObject;
import theholyrailmod.theholyrail.StationTrackObject;
import theholyrailmod.theholyrail.StationTrackObjectEntity;

public class MinecartMobPatch {
    @ModMethodPatch(target = MinecartMob.class, name = "tickCollisionMovement", arguments = { float.class, Mob.class })
    public static class TickCollisionMovementPatch {
        public static StationTrackObjectEntity stationTrackEntity;

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This MinecartMob mobObject,
                @Advice.Argument(0) float delta,
                @Advice.Argument(1) Mob rider) {
            int tileX = mobObject.getTileX();
            int tileY = mobObject.getTileY();
            boolean waitSeconds = false;
            boolean waitEmpty = false;
            boolean waitFull = false;
            boolean roleLoad = false;
            boolean roleUnload = false;
            GameObject trackObject = mobObject.getLevel().getObject(tileX, tileY);

            if (trackObject instanceof StationTrackObject) {
                // set station track-specific variables
                stationTrackEntity = (StationTrackObjectEntity) mobObject.getLevel().entityManager
                        .getObjectEntity(tileX, tileY);
                waitSeconds = stationTrackEntity.getWaitSeconds();
                waitEmpty = stationTrackEntity.getWaitEmpty();
                waitFull = stationTrackEntity.getWaitFull();
                roleLoad = stationTrackEntity.getRoleLoad();
                roleUnload = stationTrackEntity.getRoleUnload();
            }

            ChestMinecartMob cmMob = mobObject instanceof ChestMinecartMob ? (ChestMinecartMob) mobObject : null;

            if (!(mobObject instanceof ChestMinecartMob)) {
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
                        // mobObject.minecartSpeed = 0.0f;
                        // Originally, if the minecart that hit the powered rail was a standard cart /
                        // railrunner and it
                        // was empty, this would just halt the movement of the carts. As of v0.6.712 of
                        // this mod, the behavior
                        // is now to give the cart a boost, but a much smaller one than a ridden cart or
                        // a chest minecart.
                        float MAX_SPEED = 120.0f;
                        float BOOST_SPEED = MAX_SPEED * 4.15f;
                        mobObject.minecartSpeed = Math.min(MAX_SPEED,
                                mobObject.minecartSpeed
                                        + (BOOST_SPEED * delta / 160.0f
                                                * mobObject.getAccelerationModifier()));
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
                    if (mobObject instanceof ChestMinecartMob) {
                        cmMob.setIsBeingStationed(false);
                        if (((PoweredRailObject) trackObject).isPowered(mobObject.getLevel(), tileX, tileY)) {
                            mobObject.minecartSpeed = Math.min(cmMob.MAX_SPEED,
                                    (mobObject.minecartSpeed
                                            + (cmMob.BOOST_SPEED)// * inventoryAccelerationMod)
                                                    * delta
                                                    / 150.0f * mobObject.getAccelerationModifier()));
                        } else {
                            mobObject.minecartSpeed = Math.max(0f,
                                    mobObject.minecartSpeed > 8f ? (mobObject.minecartSpeed / 2.0f) * delta / 250.0f
                                            : (mobObject.minecartSpeed - 0.1f) * delta / 250.0f);
                        }
                    }
                }

                // Station Track
                if (trackObject instanceof StationTrackObject) {
                    // We only care about ChestMinecarts. Station Tracks act as normal tracks to
                    // other carts.
                    if (mobObject instanceof ChestMinecartMob) {
                        // The inventory acceleration mod is a number between 0.7 and 1.25 affected by
                        // how many slots in the cart are full.
                        // The emptier, the faster.
                        float inventoryAccelerationMod = Math.max(0.7f,
                                Math.min(1.25f, (4 / Math.min(10, (cmMob.getFilledInventorySlots(false) + 1))) * 0.5f));

                        if ((cmMob.getTimeSinceLeftLastStation(
                                (ChestMinecartMob) mobObject) > cmMob.STATION_COOLDOWN_TIME
                                || cmMob.getLastStationLeft() == -2L)
                                && !(waitSeconds && stationTrackEntity
                                        .getMaxStationWaitTime() == 0L)) {
                            // If it's time to make a stop at a station (the cart is entering the station
                            // track from a non-station track), slow the cart to a stop.
                            mobObject.minecartSpeed = Math.max(0.0f,
                                    (mobObject.minecartSpeed
                                            - ((cmMob.minecartSpeed + cmMob.BOOST_SPEED) * delta
                                                    / 28.0f
                                                    * mobObject.getAccelerationModifier())));
                            cmMob.sendMovementPacket(true);

                        } else if ((!cmMob.getIsOpened() && !cmMob.getIsBeingStationed()
                                && cmMob.getTimeSinceLeftLastStation(
                                        (ChestMinecartMob) mobObject) <= cmMob.STATION_COOLDOWN_TIME
                                && cmMob.getLastStationLeft() != -2)
                                || (waitSeconds && stationTrackEntity
                                        .getMaxStationWaitTime() == 0L)) {
                            // Not enough time has elapsed since the last time the cart was stationed, so
                            // have this instance of a station track act like a normal track if unpowered,
                            // or a powered track if powered.
                            // NOTE: this code will also trigger if the station track is set to wait for 0
                            // seconds.
                            if (!(waitSeconds && stationTrackEntity
                                    .getMaxStationWaitTime() == 0L)) {
                                mobObject.minecartSpeed = Math.min(cmMob.MAX_SPEED,
                                        (mobObject.minecartSpeed
                                                + (cmMob.MAX_SPEED * inventoryAccelerationMod)
                                                        * delta
                                                        / 150.0f * mobObject.getAccelerationModifier()));
                            }

                        }

                        if (cmMob.getIsBeingStationed()) {
                            if (roleLoad) {
                                // Pull from nearby chests
                                stationTrackEntity.transferItemsToMinecart(cmMob);
                            } else if (roleUnload) {
                                // Deposit to nearby chests
                                stationTrackEntity.transferItemsFromMinecart(cmMob);
                            }
                        }

                        // Only process the next block if the user isn't actively changing the chest
                        // minecart's inventory.
                        if (!cmMob.getIsOpened()) {
                            if (mobObject.minecartSpeed <= 0.0F && !cmMob.getIsBeingStationed()
                                    && (cmMob.getTimeSinceLeftLastStation(
                                            (ChestMinecartMob) mobObject) > cmMob.STATION_COOLDOWN_TIME
                                            || cmMob.getLastStationLeft() == -2L)) {
                                // The cart is fully stopped on the station track. Tell the game that it's being
                                // stationed and it's ready to start counting the seconds until it leaves.
                                cmMob.setIsBeingStationed(true);
                                cmMob.setStationedTime(mobObject.getWorldEntity().getTime());
                                cmMob.setIsMakingStop(false);
                                cmMob.sendMovementPacket(true);
                            }

                            if (cmMob.getIsBeingStationed()
                                    && ((waitSeconds && cmMob.getTimeSinceStationed(
                                            (ChestMinecartMob) mobObject) >= stationTrackEntity
                                                    .getMaxStationWaitTime())
                                            || (waitEmpty && cmMob.getIsInventoryEmpty())
                                            || (waitFull && cmMob.getIsInventoryFull()))) {
                                // The cart has been stationed for at least cmMob.MAX_STATION_WAIT_TIME ms, so
                                // it's time to send it on its way with a little extra oomph.
                                cmMob.setIsBeingStationed(false);
                                cmMob.setLastStationLeft(mobObject.getWorldEntity().getTime());
                                mobObject.minecartSpeed = Math.min(cmMob.MAX_SPEED,
                                        (mobObject.minecartSpeed
                                                + (cmMob.BOOST_SPEED * inventoryAccelerationMod)
                                                        * delta
                                                        / 30.0f * mobObject.getAccelerationModifier()));
                                cmMob.sendMovementPacket(true);

                                // If this station track is powered, have the cart make a u-turn. Powered
                                // station tracks are designed to be placed at the end of a hauling track line.
                                if (((StationTrackObject) trackObject).isPowered(mobObject.getLevel(), tileX, tileY)) {
                                    if (mobObject.getDir() == 3) {
                                        // Set it so the minecart is facing right (dir = 1) and is moving that way
                                        mobObject.setFacingDir(1.0F, 0.0F);
                                        mobObject.minecartDir = mobObject.getDir();
                                    } else if (mobObject.getDir() == 2) {
                                        // Set it so the minecart is facing up (dir = 0) and is moving that way
                                        mobObject.setFacingDir(0.0F, -1.0F);
                                        mobObject.minecartDir = mobObject.getDir();
                                    } else if (mobObject.getDir() == 1) {
                                        // Set it so the minecart is facing left (dir = 3) and is moving that way
                                        mobObject.setFacingDir(-1.0F, 0.0F);
                                        mobObject.minecartDir = mobObject.getDir();
                                    } else {
                                        // Set it so the minecart is facing down (dir = 2) and is moving that way
                                        mobObject.setFacingDir(0.0F, 1.0F);
                                        mobObject.minecartDir = mobObject.getDir();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
