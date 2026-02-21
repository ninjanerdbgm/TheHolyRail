package theholyrailmod.theholyrail;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.level.maps.Level;

public class PoweredRailObjectEntity extends ObjectEntity {
    public boolean isPowered;

    public PoweredRailObjectEntity(Level level, int x, int y) {
        super(level, "poweredrail", x, y);
        this.shouldSave = false;
        this.isPowered = false;
    }

    public void setIsPowered(boolean is_powered) {
        this.isPowered = is_powered;
    }

    @Override
    public boolean shouldRequestPacket() {
        return false;
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addBoolean("is_powered", this.isPowered);
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        this.setIsPowered(save.getBoolean("is_powered", this.isPowered));
    }

    @Override
    public void setupContentPacket(PacketWriter writer) {
        super.setupContentPacket(writer);
        writer.putNextBoolean(this.isPowered);
    }

    @Override
    public void applyContentPacket(PacketReader reader) {
        super.applyContentPacket(reader);
        this.setIsPowered(reader.getNextBoolean());
    }

}
