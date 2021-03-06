package ch.arons.android.gps.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public class GpxReader extends DefaultHandler {
	
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    
    private List<GPXWayPoint> wayPointList = new ArrayList<GPXWayPoint>();
    
    private StringBuffer buf = new StringBuffer();
    private String name;
    private Double lat;
    private Double lon;
    private Double ele;
    private Date time;

    public static List<GPXWayPoint> readTrack(InputStream in) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            GpxReader reader = new GpxReader();
            parser.parse(in, reader);
            return reader.wayPointList;
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static List<GPXWayPoint> readTrack(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            return readTrack(in);
        } finally {
            in.close();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        buf.setLength(0);
        
        name = null;
        lat = null;
        lon = null;
        ele = null;
        time = null;
        
        if (qName.equals("wpt")) {
            lat = Double.parseDouble(attributes.getValue("lat"));
            lon = Double.parseDouble(attributes.getValue("lon"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("wpt")) {
        	
        	GPXWayPoint wp = new GPXWayPoint();
        	wp.name = name;
        	wp.lat = lat;
        	wp.lon = lon;
        	wp.ele = ele;
        	wp.time = time;
        	wayPointList.add(wp);
        	
        } else if (qName.equals("name")) {
            name = buf.toString();
        } else if (qName.equals("ele")) {
            ele = Double.parseDouble(buf.toString());
        } else if (qName.equals("time")) {
            try {
                time = TIME_FORMAT.parse(buf.toString());
            } catch (ParseException e) {
                throw new SAXException("Invalid time " + buf.toString());
            }
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        buf.append(chars, start, length);
    }

}
