package uk.co.zoobyware.cycletrack.output;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class GpxDocument {
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    // tracks are for recorded journeys
    private final List<GpxTrack> tracks = new ArrayList<>();

    private GpxTrack currentTrack;

    public void addLocation(final GpxLocation location) {
        if (currentTrack != null) {
            currentTrack.addLocation(location);
        }
    }

    public void addTrack() {
        currentTrack = new GpxTrack();
        tracks.add(currentTrack);
    }

    final Document getXml() throws ParserConfigurationException {
        factory.setNamespaceAware(true);

        final DocumentBuilder xmlBuilder = factory.newDocumentBuilder();
        final Document xml = xmlBuilder.newDocument();

        xml.setXmlVersion("1.0");

        final Element rootNode = xml.createElementNS("http://www.topografix.com/GPX/1/1", "gpx");
        xml.appendChild(rootNode);

        rootNode.setAttribute("creator", "co.uk.zoobyware.cycletrack");

        if (!tracks.isEmpty()) {
            for (final GpxTrack track : tracks) {
                track.write(xml, rootNode);
            }
        }

        return xml;
    }
}
