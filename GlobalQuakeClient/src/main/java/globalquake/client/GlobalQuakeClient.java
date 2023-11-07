package globalquake.client;

import globalquake.core.SeedlinkNetworksReader;
import globalquake.main.Main;
import globalquake.ui.globalquake.GlobalQuakeFrame;
import gqserver.api.Packet;
import org.tinylog.Logger;

import java.awt.*;
import java.io.IOException;

public class GlobalQuakeClient extends GlobalQuakeLocal {
    public GlobalQuakeClient() {
        instance = this;

        super.globalStationManager = new GlobalStationManagerClient();
        super.earthquakeAnalysis = new EarthquakeAnalysisClient();
        super.clusterAnalysis = new ClusterAnalysisClient();
        super.archive = new EarthquakeArchiveClient();
        super.seedlinkNetworksReader = new SeedlinkNetworksReaderClient();
    }

    public void processPacket(ClientSocket socket, Packet packet) throws IOException {
        ((EarthquakeAnalysisClient)getEarthquakeAnalysis()).processPacket(socket, packet);
        ((EarthquakeArchiveClient)getArchive()).processPacket(socket, packet);
        ((GlobalStationManagerClient)getStationManager()).processPacket(socket, packet);
    }

    @Override
    public GlobalQuakeLocal createFrame() {
        EventQueue.invokeLater(() -> {
            try {
                globalQuakeFrame = new GlobalQuakeFrame();
                globalQuakeFrame.setVisible(true);

                Main.getErrorHandler().setParent(globalQuakeFrame);
            }catch (Exception e){
                Logger.error(e);
                System.exit(0);
            }
        });
        return this;

    }
}