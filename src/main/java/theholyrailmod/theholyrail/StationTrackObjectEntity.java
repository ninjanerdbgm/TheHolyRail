package theholyrailmod.theholyrail;

import necesse.entity.objectEntity.ObjectEntity;
import necesse.level.maps.Level;

public class StationTrackObjectEntity extends ObjectEntity {
    public boolean isPowered;

    public StationTrackObjectEntity(Level level, int x, int y) {
        super(level, "stationtrack", x, y);
        this.shouldSave = false;
        this.isPowered = false;
    }

    @Override
    public boolean shouldRequestPacket() {
        return false;
    }

}
