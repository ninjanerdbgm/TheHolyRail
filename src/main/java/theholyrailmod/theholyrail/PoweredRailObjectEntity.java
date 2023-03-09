package theholyrailmod.theholyrail;

import necesse.entity.objectEntity.ObjectEntity;
import necesse.level.maps.Level;

public class PoweredRailObjectEntity extends ObjectEntity {
    public boolean isPowered;

    public PoweredRailObjectEntity(Level level, int x, int y) {
        super(level, "poweredrail", x, y);
        this.shouldSave = false;
        this.isPowered = false;
    }

    @Override
    public boolean shouldRequestPacket() {
        return false;
    }

}
