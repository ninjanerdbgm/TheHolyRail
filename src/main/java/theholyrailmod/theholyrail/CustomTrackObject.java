package theholyrailmod.theholyrail;

import java.util.Arrays;

import necesse.entity.mobs.summon.MinecartLine;
import necesse.entity.mobs.summon.MinecartLines;
import necesse.level.gameObject.MinecartTrackObject;
import necesse.level.maps.Level;

public class CustomTrackObject extends MinecartTrackObject {
    private String[] railIds = new String[] { "minecarttrack", "poweredrail", "stationtrack" };

    public CustomTrackObject() {
    }

    public CustomTrackObject.TrackSprite getCustomSprite(Level level, int tileX, int tileY, int rotation) {
        CustomTrackObject.TrackSprite out = new CustomTrackObject.TrackSprite();
        MinecartLines lines = this.getMinecartLines(level, tileX, tileY, rotation, 0.0F, 0.0F, false);
        if (lines.up != null) {
            out.goingUp();
            MinecartLine upLine = lines.up.nextNegative != null ? lines.up.nextNegative.get() : null;
            if (upLine == null) {
                out.connectedUp = false;
            }
        }

        if (lines.right != null) {
            out.goingRight();
            MinecartLine rightLine = lines.right.nextPositive != null ? lines.right.nextPositive.get() : null;
            if (rightLine == null) {
                out.connectedRight = false;
            }
        }

        if (lines.down != null) {
            out.goingDown();
            MinecartLine downLine = lines.down.nextPositive != null ? lines.down.nextPositive.get() : null;
            if (downLine == null) {
                out.connectedDown = false;
            }
        }

        if (lines.left != null) {
            out.goingLeft();
            MinecartLine leftLine = lines.left.nextNegative != null ? lines.left.nextNegative.get() : null;
            if (leftLine == null) {
                out.connectedLeft = false;
            }
        }

        switch (rotation) {
            case 0:
                if (out.connectedLeft && out.connectedRight) {
                    return out.sprite(4, 0);
                } else if (out.connectedLeft && out.connectedDown) {
                    return out.sprite(3, 3);
                } else if (out.connectedRight && out.connectedDown) {
                    return out.sprite(2, 3);
                } else if (out.connectedLeft) {
                    return out.sprite(3, 1);
                } else {
                    if (out.connectedRight) {
                        return out.sprite(2, 1);
                    }

                    return out.sprite(1, 0);
                }
            case 1:
                if (out.connectedUp && out.connectedDown) {
                    return out.sprite(4, 0);
                } else if (out.connectedUp && out.connectedLeft) {
                    return out.sprite(0, 3);
                } else if (out.connectedDown && out.connectedLeft) {
                    return out.sprite(0, 2);
                } else if (out.connectedUp) {
                    return out.sprite(2, 1);
                } else {
                    if (out.connectedDown) {
                        return out.sprite(2, 0);
                    }

                    return out.sprite(0, 0);
                }
            case 2:
                if (out.connectedLeft && out.connectedRight) {
                    return out.sprite(4, 0);
                } else if (out.connectedLeft && out.connectedUp) {
                    return out.sprite(3, 2);
                } else if (out.connectedRight && out.connectedUp) {
                    return out.sprite(2, 2);
                } else if (out.connectedLeft) {
                    return out.sprite(3, 0);
                } else {
                    if (out.connectedRight) {
                        return out.sprite(2, 0);
                    }

                    return out.sprite(1, 0);
                }
            case 3:
                if (out.connectedUp && out.connectedDown) {
                    return out.sprite(4, 0);
                } else if (out.connectedUp && out.connectedRight) {
                    return out.sprite(1, 3);
                } else if (out.connectedDown && out.connectedRight) {
                    return out.sprite(1, 2);
                } else if (out.connectedUp) {
                    return out.sprite(3, 1);
                } else {
                    if (out.connectedDown) {
                        return out.sprite(3, 0);
                    }

                    return out.sprite(0, 0);
                }
            default:
                return out.sprite(0, 0);
        }
    }

    @Override
    public MinecartLines getMinecartLines(Level level, int x, int y, int rotation, float entityDx, float entityDy,
            boolean ignoreEntityDirection) {
        MinecartLines lines = new MinecartLines(x, y);
        boolean hasUp = Arrays.stream(this.railIds).anyMatch(level.getObject(x, y - 1).getStringID()::equals);
        boolean hasRight = Arrays.stream(this.railIds).anyMatch(level.getObject(x + 1, y).getStringID()::equals);
        boolean hasDown = Arrays.stream(this.railIds).anyMatch(level.getObject(x, y + 1).getStringID()::equals);
        boolean hasLeft = Arrays.stream(this.railIds).anyMatch(level.getObject(x - 1, y).getStringID()::equals);
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
                        lines.up.nextNegative = () -> this.getMinecartLines(level, x, y - 1, entityDx, entityDy,
                                false).down;
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
                        lines.down.nextPositive = () -> this.getMinecartLines(level, x, y + 1, entityDx, entityDy,
                                false).up;
                        anyConnectionFromSides = true;
                    } else {
                        straightAcross = true;
                    }
                }
            }

            if (hasLeft) {
                lines.left = MinecartLine.left(x, y);
                lines.left.nextNegative = () -> this.getMinecartLines(level, x - 1, y, entityDx, entityDy, false).right;
            } else if (rotation == 3 || !anyConnectionFromSides) {
                lines.left = MinecartLine.leftEnd(x, y);
                lines.left.nextNegative = null;
            }

            if (hasRight) {
                lines.right = MinecartLine.right(x, y);
                lines.right.nextPositive = () -> this.getMinecartLines(level, x + 1, y, entityDx, entityDy, false).left;
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
                lines.up.nextNegative = () -> this.getMinecartLines(level, x, y - 1, entityDx, entityDy, false).down;
                lines.down = MinecartLine.down(x, y);
                lines.down.nextPositive = () -> this.getMinecartLines(level, x, y + 1, entityDx, entityDy, false).up;
                lines.up.nextPositive = () -> lines.down;
                lines.down.nextNegative = () -> lines.up;
            } else if (lines.down != null) {
                lines.down.nextPositive = () -> this.getMinecartLines(level, x, y + 1, entityDx, entityDy, false).up;
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
                lines.up.nextNegative = () -> this.getMinecartLines(level, x, y - 1, entityDx, entityDy, false).down;
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
                        lines.left.nextNegative = () -> this.getMinecartLines(level, x - 1, y, entityDx, entityDy,
                                false).right;
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
                        lines.right.nextPositive = () -> this.getMinecartLines(level, x + 1, y, entityDx, entityDy,
                                false).left;
                        anyConnectionFromSides = true;
                    } else {
                        straightAcross = true;
                    }
                }
            }

            if (hasUp) {
                lines.up = MinecartLine.up(x, y);
                lines.up.nextNegative = () -> this.getMinecartLines(level, x, y - 1, entityDx, entityDy, false).down;
            } else if (rotation == 0 || !anyConnectionFromSides) {
                lines.up = MinecartLine.upEnd(x, y);
                lines.up.nextNegative = null;
            }

            if (hasDown) {
                lines.down = MinecartLine.down(x, y);
                lines.down.nextPositive = () -> this.getMinecartLines(level, x, y + 1, entityDx, entityDy, false).up;
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
                lines.left.nextNegative = () -> this.getMinecartLines(level, x - 1, y, entityDx, entityDy, false).right;
                lines.right = MinecartLine.right(x, y);
                lines.right.nextPositive = () -> this.getMinecartLines(level, x + 1, y, entityDx, entityDy, false).left;
                lines.left.nextPositive = () -> lines.right;
                lines.right.nextNegative = () -> lines.left;
            } else if (lines.right != null) {
                lines.right.nextPositive = () -> this.getMinecartLines(level, x + 1, y, entityDx, entityDy, false).left;
                if (rotation == 2) {
                    if (lines.up == null || entityDx > turnThreshold && !ignoreEntityDirection) {
                        lines.down.nextNegative = () -> lines.right;
                    }

                    lines.right.nextNegative = () -> lines.down;
                } else {
                    if ((lines.down == null || entityDx > turnThreshold && !ignoreEntityDirection)
                            && lines.up != null) {
                        lines.up.nextPositive = () -> lines.right;
                    }

                    lines.right.nextNegative = () -> lines.up;
                }
            } else if (lines.left != null) {
                lines.left.nextNegative = () -> this.getMinecartLines(level, x - 1, y, entityDx, entityDy, false).right;
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

    protected static class TrackSprite {
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

        public CustomTrackObject.TrackSprite sprite(int spriteX, int spriteY) {
            this.x = spriteX;
            this.y = spriteY;
            return this;
        }
    }
}
