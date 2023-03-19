package theholyrailmod.container;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.registries.ContainerRegistry;
import necesse.inventory.container.Container;
import theholyrailmod.form.StationTrackContainerForm;
import theholyrailmod.theholyrail.StationTrackObjectEntity;

public class StationTrackContainer extends Container {
    public static int registryId;
    public NetworkClient client;
    public StationTrackObjectEntity stationTrackEntity;

    public StationTrackContainer(final NetworkClient client, int uniqueSeed, StationTrackObjectEntity obj,
            Packet content) {
        super(client, uniqueSeed);

        this.stationTrackEntity = obj;
    }

    public StationTrackContainer(final NetworkClient client, int uniqueSeed,
            Packet content, StationTrackObjectEntity obj, Object serverObject) {
        super(client, uniqueSeed);

        this.stationTrackEntity = obj;
    }

    public static void registerStationTrackContainer() {
        registryId = ContainerRegistry.registerOEContainer(
                (client, uniqueSeed, oe, content) -> new StationTrackContainerForm<>(client,
                        new StationTrackContainer(client.getClient(), uniqueSeed, (StationTrackObjectEntity) oe,
                                content)),
                (client, uniqueSeed, oe, content, serverObject) -> new StationTrackContainer(client,
                        uniqueSeed, content, (StationTrackObjectEntity) oe,
                        serverObject));
    }
}
