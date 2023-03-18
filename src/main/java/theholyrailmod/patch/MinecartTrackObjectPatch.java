package theholyrailmod.patch;

import java.util.Arrays;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.summon.MinecartLine;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;

public class MinecartTrackObjectPatch {
    @ModMethodPatch(target = MinecartTrackObject.class, name = "getMinecartLines", arguments = { Level.class, int.class,
            int.class, float.class, float.class, boolean.class })
    public static class AddHolyRailInteroperability {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static MinecartLines onEnter(@Advice.This MinecartTrackObject obj, @Advice.Argument(0) Level level,
                @Advice.Argument(1) int x, @Advice.Argument(2) int y, @Advice.Argument(3) float entityDx,
                @Advice.Argument(4) float entityDy, @Advice.Argument(5) boolean ignoreEntityDirection) {
            return getMinecartLines(obj, level, x, y, entityDx, entityDy, ignoreEntityDirection);
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false, typing = Typing.DYNAMIC) Object returned,
                @Advice.Enter Object enter) {
            if (enter != null) {
                returned = enter;
            }
        }

        public static MinecartLines getMinecartLines(MinecartTrackObject obj, Level level, int x, int y, float entityDx,
                float entityDy, boolean ignoreEntityDirection) {
            String[] railIds = new String[] { "minecarttrack", "poweredrail", "stationtrack" };

            MinecartLines lines = new MinecartLines(x, y);
            byte rotation = level.getObjectRotation(x, y);
            boolean hasUp = Arrays.stream(railIds).anyMatch(level.getObject(x, y - 1).getStringID()::equals);
            boolean hasDown = Arrays.stream(railIds).anyMatch(level.getObject(x, y + 1).getStringID()::equals);
            boolean hasLeft = Arrays.stream(railIds).anyMatch(level.getObject(x - 1, y).getStringID()::equals);
            boolean hasRight = Arrays.stream(railIds).anyMatch(level.getObject(x + 1, y).getStringID()::equals);

            float turnThreshold = 0.2F;
            if (rotation != 0 && rotation != 2) {
                boolean straightAcross = false;
                boolean anyConnectionFromSides = false;
                if (hasUp) {
                    byte upRotation = level.getObjectRotation(x, y - 1);
                    if (upRotation == 0 || upRotation == 2) {
                        boolean success = true;
                        if (hasDown) {
                            byte downRotation = level.getObjectRotation(x, y + 1);
                            if (downRotation == 0 || downRotation == 2) {
                                success = false;
                            }
                        }

                        if (success) {
                            lines.up = MinecartLine.up(x, y);
                            lines.up.nextNegative = () -> getMinecartLines(obj, level, x, y - 1, entityDx,
                                    entityDy, ignoreEntityDirection).down;
                            anyConnectionFromSides = true;
                        } else {
                            straightAcross = true;
                        }
                    }
                }

                if (hasDown) {
                    byte downRotation = level.getObjectRotation(x, y + 1);
                    if (downRotation == 0 || downRotation == 2) {
                        boolean success = true;
                        if (hasUp) {
                            byte upRotation = level.getObjectRotation(x, y - 1);
                            if (upRotation == 0 || upRotation == 2) {
                                success = false;
                            }
                        }

                        if (success) {
                            lines.down = MinecartLine.down(x, y);
                            lines.down.nextPositive = () -> getMinecartLines(obj, level, x, y + 1, entityDx,
                                    entityDy, ignoreEntityDirection).up;
                            anyConnectionFromSides = true;
                        } else {
                            straightAcross = true;
                        }
                    }
                }

                if (hasLeft) {
                    lines.left = MinecartLine.left(x, y);
                    lines.left.nextNegative = () -> getMinecartLines(obj, level, x - 1, y, entityDx, entityDy,
                            ignoreEntityDirection).right;
                } else if (rotation == 3 || !anyConnectionFromSides) {
                    lines.left = MinecartLine.leftEnd(x, y);
                    lines.left.nextNegative = null;
                }

                if (hasRight) {
                    lines.right = MinecartLine.right(x, y);
                    lines.right.nextPositive = () -> getMinecartLines(obj, level, x + 1, y, entityDx, entityDy,
                            ignoreEntityDirection).left;
                } else if (rotation == 1 || !anyConnectionFromSides) {
                    lines.right = MinecartLine.rightEnd(x, y);
                    lines.right.nextPositive = null;
                }

                if (lines.left != null && lines.right != null) {
                    lines.left.nextPositive = () -> lines.right;
                    lines.right.nextNegative = () -> lines.left;
                }

                if (straightAcross) {
                    lines.up = MinecartLine.up(x, y);
                    lines.up.nextNegative = () -> getMinecartLines(obj, level, x, y - 1, entityDx, entityDy,
                            ignoreEntityDirection).down;
                    lines.down = MinecartLine.down(x, y);
                    lines.down.nextPositive = () -> getMinecartLines(obj, level, x, y + 1, entityDx, entityDy,
                            ignoreEntityDirection).up;
                    lines.up.nextPositive = () -> lines.down;
                    lines.down.nextNegative = () -> lines.up;
                } else if (lines.down != null) {
                    lines.down.nextPositive = () -> getMinecartLines(obj, level, x, y + 1, entityDx, entityDy,
                            ignoreEntityDirection).up;
                    if (rotation == 1) {
                        if (lines.left == null || entityDy > turnThreshold) {
                            lines.right.nextNegative = () -> lines.down;
                        }

                        lines.down.nextNegative = () -> lines.right;
                    } else {
                        if ((lines.right == null || entityDy > turnThreshold) && lines.left != null) {
                            lines.left.nextPositive = () -> lines.down;
                        }

                        lines.down.nextNegative = () -> lines.left;
                    }
                } else if (lines.up != null) {
                    lines.up.nextNegative = () -> getMinecartLines(obj, level, x, y - 1, entityDx, entityDy,
                            ignoreEntityDirection).down;
                    if (rotation == 1) {
                        if (lines.left == null || entityDy < -turnThreshold) {
                            lines.right.nextNegative = () -> lines.up;
                        }

                        lines.up.nextPositive = () -> lines.right;
                    } else {
                        if ((lines.right == null || entityDy < -turnThreshold) && lines.left != null) {
                            lines.left.nextPositive = () -> lines.up;
                        }

                        lines.up.nextPositive = () -> lines.left;
                    }
                }
            } else {
                boolean straightAcross = false;
                boolean anyConnectionFromSides = false;
                if (hasLeft) {
                    byte leftRotation = level.getObjectRotation(x - 1, y);
                    if (leftRotation == 1 || leftRotation == 3) {
                        boolean success = true;
                        if (hasRight) {
                            byte rightRotation = level.getObjectRotation(x + 1, y);
                            if (rightRotation == 1 || rightRotation == 3) {
                                success = false;
                            }
                        }

                        if (success) {
                            lines.left = MinecartLine.left(x, y);
                            lines.left.nextNegative = () -> getMinecartLines(obj, level, x - 1, y, entityDx,
                                    entityDy, ignoreEntityDirection).right;
                            anyConnectionFromSides = true;
                        } else {
                            straightAcross = true;
                        }
                    }
                }

                if (hasRight) {
                    byte rightRotation = level.getObjectRotation(x + 1, y);
                    if (rightRotation == 1 || rightRotation == 3) {
                        boolean success = true;
                        if (hasLeft) {
                            byte leftRotation = level.getObjectRotation(x - 1, y);
                            if (leftRotation == 1 || leftRotation == 3) {
                                success = false;
                            }
                        }

                        if (success) {
                            lines.right = MinecartLine.right(x, y);
                            lines.right.nextPositive = () -> getMinecartLines(obj, level, x + 1, y, entityDx,
                                    entityDy, ignoreEntityDirection).left;
                            anyConnectionFromSides = true;
                        } else {
                            straightAcross = true;
                        }
                    }
                }

                if (hasUp) {
                    lines.up = MinecartLine.up(x, y);
                    lines.up.nextNegative = () -> getMinecartLines(obj, level, x, y - 1, entityDx, entityDy,
                            ignoreEntityDirection).down;
                } else if (rotation == 0 || !anyConnectionFromSides) {
                    lines.up = MinecartLine.upEnd(x, y);
                    lines.up.nextNegative = null;
                }

                if (hasDown) {
                    lines.down = MinecartLine.down(x, y);
                    lines.down.nextPositive = () -> getMinecartLines(obj, level, x, y + 1, entityDx, entityDy,
                            ignoreEntityDirection).up;
                } else if (rotation == 2 || !anyConnectionFromSides) {
                    lines.down = MinecartLine.downEnd(x, y);
                    lines.down.nextPositive = null;
                }

                if (lines.up != null && lines.down != null) {
                    lines.up.nextPositive = () -> lines.down;
                    lines.down.nextNegative = () -> lines.up;
                }

                if (straightAcross) {
                    lines.left = MinecartLine.left(x, y);
                    lines.left.nextNegative = () -> getMinecartLines(obj, level, x - 1, y, entityDx, entityDy,
                            ignoreEntityDirection).right;
                    lines.right = MinecartLine.right(x, y);
                    lines.right.nextPositive = () -> getMinecartLines(obj, level, x + 1, y, entityDx, entityDy,
                            ignoreEntityDirection).left;
                    lines.left.nextPositive = () -> lines.right;
                    lines.right.nextNegative = () -> lines.left;
                } else if (lines.right != null) {
                    lines.right.nextPositive = () -> getMinecartLines(obj, level, x + 1, y, entityDx, entityDy,
                            ignoreEntityDirection).left;
                    if (rotation == 2) {
                        if (lines.up == null || entityDx > turnThreshold) {
                            lines.down.nextNegative = () -> lines.right;
                        }

                        lines.right.nextNegative = () -> lines.down;
                    } else {
                        if ((lines.down == null || entityDx > turnThreshold) && lines.up != null) {
                            lines.up.nextPositive = () -> lines.right;
                        }

                        lines.right.nextNegative = () -> lines.up;
                    }
                } else if (lines.left != null) {
                    lines.left.nextNegative = () -> getMinecartLines(obj, level, x - 1, y, entityDx, entityDy,
                            ignoreEntityDirection).right;
                    if (rotation == 2) {
                        if (lines.up == null || entityDx < -turnThreshold) {
                            lines.down.nextNegative = () -> lines.left;
                        }

                        lines.left.nextPositive = () -> lines.down;
                    } else {
                        if ((lines.down == null || entityDx < -turnThreshold) && lines.up != null) {
                            lines.up.nextPositive = () -> lines.left;
                        }

                        lines.left.nextPositive = () -> lines.up;
                    }
                }
            }

            return lines;
        }
    }
}
