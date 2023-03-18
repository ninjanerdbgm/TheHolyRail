package theholyrailmod.form;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.presets.containerComponent.ContainerFormSwitcher;
import theholyrailmod.container.StationTrackContainer;
import theholyrailmod.theholyrail.StationTrackObject;

public class StationTrackContainerForm<T extends StationTrackContainer>
        extends ContainerFormSwitcher<StationTrackContainer> {
    public StationTrackConfigureForm stationTrackForm;
    public StationTrackObject stationObject;

    public StationTrackContainerForm(Client client, StationTrackContainer container) {
        super(client, container);
        stationObject = container.stationTrackObject;

        GameMessage storageTitle = new LocalMessage("ui", "stationtrackheader");

        this.stationTrackForm = this.addComponent(
                new StationTrackConfigureForm(client, container, storageTitle.translate(),
                        e -> {
                        }) {
                    @Override
                    public StationTrackObject getStationTrack() {
                        return StationTrackContainerForm.this.container.stationTrackObject;
                    }
                });

        this.makeCurrent(this.stationTrackForm);
    }

    @Override
    public boolean shouldOpenInventory() {
        return true;
    }
}
