package uk.co.zoobyware.cycletrack.output;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class GpxTrack {
    private final List<GpxLocation> locations = new ArrayList<>();

    public void addLocation(final GpxLocation location) {
        locations.add(location);
    }

    public Document write(final Document xml, final Element parent) {
        final Element track = xml.createElement("trk");
        parent.appendChild(track);

        final Element src = xml.createElement("src");
        src.appendChild(xml.createTextNode("Android device location data"));
        track.appendChild(src);

        final Element trackSegment = xml.createElement("trkseg");
        track.appendChild(trackSegment);

        for (final GpxLocation location : locations) {
            location.write(xml, trackSegment);
        }

        return xml;
    }
}
