package theholyrailmod.patch;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.summon.MinecartMob;
import necesse.level.gameObject.GameObject;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import theholyrailmod.theholyrail.ChestMinecartMob;
import theholyrailmod.theholyrail.PoweredRailObject;
import theholyrailmod.theholyrail.RailRunnerMob;
import theholyrailmod.theholyrail.StationTrackObject;

public class MinecartMobPatch {
    @ModMethodPatch(target = MinecartMob.class, name = "tickCollisionMovement", arguments = { float.class, Mob.class })
    public static class TickCollisionMovementPatch {
        public static long stationedTime = -1L;
        public static long lastStationLeft = -2L;
        public static boolean isBeingStationed = false;

        public static long MAX_STATION_WAIT_TIME = 5200L;
        public static long STATION_COOLDOWN_TIME = 350L;

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This MinecartMob obj,
                @Advice.Argument(value = 0, typing = Typing.DYNAMIC, readOnly = false) float delta,
                @Advice.Argument(1) Mob rider) {
            int tileX = obj.getTileX();
            int tileY = obj.getTileY();
            GameObject object = obj.getLevel().getObject(tileX, tileY);

            if (!(obj instanceof RailRunnerMob) && !(obj instanceof ChestMinecartMob)) {
                if (object instanceof PoweredRailObject) {
                    if (((PoweredRailObject) object).isPowered(obj.getLevel(), tileX, tileY) && rider != null) {
                        obj.minecartSpeed = Math.min(200.0f, obj.minecartSpeed + 5.7f);
                    } else if (rider == null) {
                        obj.minecartSpeed = 0.0f;
                    } else {
                        obj.minecartSpeed = Math.min(0f,
                                obj.minecartSpeed > 4f ? obj.minecartSpeed / 2.0f
                                        : obj.minecartSpeed > 0f ? obj.minecartSpeed - 0.01f : 0f);
                    }
                }
            } else {
                // Powered Track
                if (object instanceof PoweredRailObject) {
                    if (obj instanceof RailRunnerMob) {
                        if (((PoweredRailObject) object).isPowered(obj.getLevel(), tileX, tileY) && rider != null) {
                            obj.minecartSpeed = Math.min(262.0f, obj.minecartSpeed + 9.2f);
                        } else if (rider == null) {
                            obj.minecartSpeed = 0.0f;
                        } else {
                            obj.minecartSpeed = Math.min(0f,
                                    obj.minecartSpeed > 12f ? obj.minecartSpeed / 2.0f
                                            : obj.minecartSpeed > 0f ? obj.minecartSpeed - 0.01f : 0f);
                        }
                    } else if (obj instanceof ChestMinecartMob) {
                        isBeingStationed = false;
                        if (((PoweredRailObject) object).isPowered(obj.getLevel(), tileX, tileY)) {
                            obj.minecartSpeed = Math.min(215.0f, obj.minecartSpeed + 5.85f);
                        } else {
                            obj.minecartSpeed = Math.min(0f,
                                    obj.minecartSpeed > 8f ? obj.minecartSpeed / 2.0f
                                            : obj.minecartSpeed > 0f ? obj.minecartSpeed - 0.01f : 0f);
                        }
                    }
                }

                // Station Track
                if (object instanceof StationTrackObject) {
                    if (obj instanceof ChestMinecartMob) {
                        int invSize = ((ChestMinecartMob) obj).getInventory().getSize();
                        int totalFilledSlots = 0;
                        for (int i = 0; i < invSize; ++i) {
                            if (((ChestMinecartMob) obj).getInventory().isSlotClear(i)) {
                                continue;
                            }
                            ++totalFilledSlots;
                        }

                        if (getTimeSinceLeftLastStation((ChestMinecartMob) obj) > STATION_COOLDOWN_TIME
                                || lastStationLeft == -2L) {
                            obj.setSpeed(0);
                            obj.minecartSpeed = 0.0f;
                            delta = 0.0f;
                            // if (obj.dir == 3) {
                            // obj.moveX = 100.0f;
                            // obj.moveY = 0.0f;
                            // obj.colDx = -100.0f;
                            // obj.colDy = 0.0f;
                            // } else if (obj.dir == 2) {
                            // obj.moveX = 0.0f;
                            // obj.moveY = -100.0f;
                            // obj.colDx = 0.0f;
                            // obj.colDy = 100.0f;
                            // } else if (obj.dir == 1) {
                            // obj.moveX = -100.0f;
                            // obj.moveY = 0.0f;
                            // obj.colDx = 100.0f;
                            // obj.colDy = 0.0f;
                            // } else {
                            // obj.moveX = 0.0f;
                            // obj.moveY = 100.0f;
                            // obj.colDx = 0.0f;
                            // obj.colDy = -100.0f;
                            // }
                            obj.sendMovementPacket(true);
                        } else if (!((ChestMinecartMob) obj).getIsOpened() && !isBeingStationed
                                && getTimeSinceLeftLastStation((ChestMinecartMob) obj) <= STATION_COOLDOWN_TIME
                                && lastStationLeft != -2) {
                            obj.minecartSpeed = Math.min(210.0f,
                                    obj.minecartSpeed + (15.85f * Math.min(10, (totalFilledSlots + 1))));
                        }

                        if (!((ChestMinecartMob) obj).getIsOpened()) {
                            if (obj.minecartSpeed <= 0.0F && !isBeingStationed
                                    && (getTimeSinceLeftLastStation((ChestMinecartMob) obj) > STATION_COOLDOWN_TIME
                                            || lastStationLeft == -2L)) {
                                isBeingStationed = true;
                                stationedTime = obj.getWorldEntity().getTime();
                                ((ChestMinecartMob) obj).setIsMakingStop(false);
                            }

                            if (isBeingStationed
                                    && getTimeSinceStationed((ChestMinecartMob) obj) >= MAX_STATION_WAIT_TIME) {
                                isBeingStationed = false;
                                lastStationLeft = obj.getWorldEntity().getTime();
                                obj.minecartSpeed = Math.min(210.0f,
                                        obj.minecartSpeed + (15.85f * Math.min(10, (totalFilledSlots + 1))));

                                if (((StationTrackObject) object).isPowered(obj.getLevel(), tileX, tileY)) {
                                    if (obj.dir == 3) {
                                        // Set it so the minecart is facing right (dir = 1) and is moving that way
                                        obj.setFacingDir(1.0F, 0.0F);
                                        obj.minecartDir = obj.dir;
                                    } else if (obj.dir == 2) {
                                        // Set it so the minecart is facing up (dir = 0) and is moving that way
                                        obj.setFacingDir(0.0F, -1.0F);
                                        obj.minecartDir = obj.dir;
                                    } else if (obj.dir == 1) {
                                        // Set it so the minecart is facing left (dir = 3) and is moving that way
                                        obj.setFacingDir(-1.0F, 0.0F);
                                        obj.minecartDir = obj.dir;
                                    } else {
                                        // Set it so the minecart is facing down (dir = 2) and is moving that way
                                        obj.setFacingDir(0.0F, 1.0F);
                                        obj.minecartDir = obj.dir;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            obj.sendMovementPacket(false);
        }

        public static long getTimeSinceStationed(ChestMinecartMob mob) {
            if (stationedTime < 0L) {
                return stationedTime;
            } else {
                return isBeingStationed ? mob.getWorldEntity().getTime() - stationedTime : -1L;
            }
        }

        public static long getTimeSinceLeftLastStation(ChestMinecartMob mob) {
            if (lastStationLeft < 0L) {
                return lastStationLeft;
            } else {
                return isBeingStationed ? -1
                        : mob.getWorldEntity().getTime() - lastStationLeft;
            }
        }
    }
}
