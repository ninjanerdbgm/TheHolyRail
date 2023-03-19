package theholyrailmod.form;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.presets.containerComponent.ContainerFormSwitcher;
import theholyrailmod.container.StationTrackContainer;
import theholyrailmod.theholyrail.StationTrackObjectEntity;

public class StationTrackContainerForm<T extends StationTrackContainer>
        extends ContainerFormSwitcher<StationTrackContainer> {
    public StationTrackConfigureForm stationTrackForm;
    public StationTrackObjectEntity stationObjectEntity;

    public StationTrackContainerForm(Client client, StationTrackContainer container) {
        super(client, container);

        this.stationObjectEntity = container.stationTrackEntity;
        GameMessage storageTitle = new LocalMessage("ui", "stationtrackheader");

        this.stationTrackForm = this.addComponent(
                new StationTrackConfigureForm(client, container, storageTitle.translate(),
                        e -> {
                        }) {

                });

        this.makeCurrent(this.stationTrackForm);
    }

    @Override
    public boolean shouldOpenInventory() {
        return true;
    }
}
