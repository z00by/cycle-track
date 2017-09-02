package uk.co.zoobyware.cycletrack.output;

import android.location.Location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GpxBuilder {
    private final GpxDocument gpx = new GpxDocument();

    public GpxBuilder track() {
        gpx.addTrack();

        return this;
    }

    public GpxBuilder location(final Location location) {
        gpx.addLocation(new GpxLocation(location));

        return this;
    }

    public GpxDocument build() {
        return gpx;
    }

    public void write(final File outputDirectory) throws TransformerException, ParserConfigurationException, IOException {
        final File locationFile = new File (outputDirectory, Calendar.getInstance().getTime().getTime() + ".xml");

        if (!locationFile.createNewFile()) {
            throw new IOException("Failed to create output file");
        }

        TransformerFactory.newInstance().newTransformer().transform(
                new DOMSource(gpx.getXml()),
                new StreamResult(new FileOutputStream(locationFile))
        );
    }
}
