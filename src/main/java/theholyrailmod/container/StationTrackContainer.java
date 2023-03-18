package theholyrailmod.container;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.inventory.container.Container;

import theholyrailmod.form.StationTrackContainerForm;
import theholyrailmod.theholyrail.StationTrackObject;

public class StationTrackContainer extends Container {
    public static int registryId;
    public StationTrackObject stationTrackObject;
    public NetworkClient client;

    public StationTrackContainer(final NetworkClient client, int uniqueSeed,
            Packet content) {
        super(client, uniqueSeed);

        PacketReader pr = new PacketReader(content);

        int x = pr.getNextShortUnsigned();
        int y = pr.getNextShortUnsigned();
        this.stationTrackObject = (StationTrackObject) client.playerMob.getLevel().getObject(x, y);
    }

    public StationTrackContainer(final NetworkClient client, int uniqueSeed,
            Packet content, Object serverObject) {
        super(client, uniqueSeed);

        PacketReader pr = new PacketReader(content);

        int x = pr.getNextShortUnsigned();
        int y = pr.getNextShortUnsigned();
        this.stationTrackObject = (StationTrackObject) client.playerMob.getLevel().getObject(x, y);
    }

    public static void registerStationTrackContainer() {
        registryId = ContainerRegistry.registerContainer(
                (client, uniqueSeed, content) -> new StationTrackContainerForm<>(client,
                        new StationTrackContainer(client.getClient(), uniqueSeed, content)),
                (client, uniqueSeed, content, serverObject) -> new StationTrackContainer(client, uniqueSeed, content,
                        serverObject));
    }
}
