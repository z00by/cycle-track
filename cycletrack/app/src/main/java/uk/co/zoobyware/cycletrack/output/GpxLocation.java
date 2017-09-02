package uk.co.zoobyware.cycletrack.output;

import android.location.Location;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GpxLocation {
    private final Location location;

    public GpxLocation (final Location location) {
        this.location = location;
    }

    public void write(final Document xml, final Element trackSegment) {

        final Element trackPoint = xml.createElement("trkpt");
        trackSegment.appendChild(trackPoint);

        final Element time = xml.createElement("time");
        trackPoint.appendChild(time);
        time.appendChild(xml.createTextNode(Instant.ofEpochMilli(location.getTime()).atZone(ZoneOffset.UTC).toString()));

        trackPoint.setAttribute("lat", Double.toString(location.getLatitude()));
        trackPoint.setAttribute("lon", Double.toString(location.getLongitude()));

        if (location.hasAltitude()) {
            final Element elevation = xml.createElement("ele");
            trackPoint.appendChild(elevation);
            elevation.appendChild(xml.createTextNode(Double.toString(location.getAltitude())));
        }
    }
}
