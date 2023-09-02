package globalquake.core.earthquake;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import globalquake.core.station.AbstractStation;
import globalquake.geo.GeoUtils;
import globalquake.sounds.SoundsInfo;

public class Cluster {

	private final int id;
	private final List<Event> assignedEvents;
	private double rootLat;
	private double rootLon;
	private double size;
	public int updateCount;
	private long lastUpdate;

	private Earthquake earthquake;
	@SuppressWarnings("unused")
	public final double bestAngle;
	private Hypocenter previousHypocenter;

	private int level;

	public boolean active;
	public int lastEpicenterUpdate;

	public final SoundsInfo soundsInfo;
	private double anchorLon;
	private double anchorLat;
	public int revisionID;

	public static final double NONE = -999;
	public final Object selectedEventsLock;

	public Color color = randomColor();

	private Color randomColor() {
		Random random = new Random();

		// Generate random values for the red, green, and blue components
		int red = random.nextInt(256); // 0-255
		int blue = random.nextInt(256); // 0-255

		// Create a new Color object with the random values
		return new Color(red, 255, blue);
	}

	// 20 selected
	private List<PickedEvent> selected = new ArrayList<>();

	public Cluster(int id) {
		this.id = id;
		this.assignedEvents = new CopyOnWriteArrayList<>();
		this.selectedEventsLock = new Object();
		this.updateCount = 0;
		this.earthquake = null;
		this.bestAngle = -1;
		this.rootLat = NONE;
		this.rootLon = NONE;
		this.lastUpdate = System.currentTimeMillis();
		this.soundsInfo = new SoundsInfo();
	}

	public Hypocenter getPreviousHypocenter() {
		return previousHypocenter;
	}

	public void setPreviousHypocenter(Hypocenter previousHypocenter) {
		this.previousHypocenter = previousHypocenter;
	}

	public int getId() {
		return id;
	}

	public void addEvent(Event ev) {
		this.assignedEvents.add(ev);
		lastUpdate = System.currentTimeMillis();
	}

	/**
	 * 
	 * @return all events that were added to this cluster
	 */
	public List<Event> getAssignedEvents() {
		return assignedEvents;
	}

	public void tick() {
		if (checkForUpdates()) {
			if (rootLat == NONE)
				calculateRoot();
			calculateSize();
			lastUpdate = System.currentTimeMillis();
		}
	}

	

	private boolean checkForUpdates() {
		int upd = 0;
		for (Event e : getAssignedEvents()) {
			upd += e.getUpdatesCount();
		}
		boolean b = (upd != updateCount);
		updateCount = upd;
		return b;
	}

	private void calculateSize() {
		double _size = 0;
		int r128 = 0;
		int r1024 = 0;
		int r8192 = 0;
		int r32K = 0;
		for (Event e : assignedEvents) {
			double dist = GeoUtils.greatCircleDistance(rootLat, rootLon, e.getAnalysis().getStation().getLatitude(),
					e.getAnalysis().getStation().getLongitude());
			if (dist > _size) {
				_size = dist;
			}
			if (e.getMaxRatio() >= 128) {
				r128++;
			}
			if (e.getMaxRatio() >= 1024) {
				r1024++;
			}
			if (e.getMaxRatio() >= 8192) {
				r8192++;
			}
			if (e.getMaxRatio() >= 32768) {
				r32K++;
			}
		}

		int _level = 0;
		if (r128 > 8 || r1024 > 2) {
			_level = 1;
		}
		if (r1024 > 6 || r8192 > 2) {
			_level = 2;
		}
		if (r8192 > 4 || r32K >= 2) {
			_level = 3;
		}
		if (r32K > 3) {
			_level = 4;
		}
		level = _level;
		this.size = _size;
	}

	private void calculateRoot() {
		int n = 0;
		double sumLat = 0;
		double sumLon = 0;
		for (Event e : assignedEvents) {
			sumLat += e.getAnalysis().getStation().getLatitude();
			sumLon += e.getAnalysis().getStation().getLongitude();
			n++;
		}
		if (n > 0) {
			rootLat = sumLat / n;
			rootLon = sumLon / n;
			anchorLat = rootLat;
			anchorLon = rootLon;
		}
	}

	public double getRootLat() {
		return rootLat;
	}

	public double getRootLon() {
		return rootLon;
	}

	@SuppressWarnings("unused")
	public double getSize() {
		return size;
	}

	protected boolean containsStation(AbstractStation station) {
		for (Event e : getAssignedEvents()) {
			if (e.getAnalysis().getStation().getId() == station.getId()) {
				return true;
			}
		}

		return false;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public Earthquake getEarthquake() {
		return earthquake;
	}

	public void setEarthquake(Earthquake earthquake) {
		this.earthquake = earthquake;
	}

	public int getActualLevel() {
		return level;
	}

	public boolean isActive() {
		return active;
	}

	public void updateAnchor(Hypocenter bestHypocenter) {
		this.anchorLat = bestHypocenter.lat;
		this.anchorLon = bestHypocenter.lon;
	}

	public double getAnchorLat() {
		return anchorLat;
	}

	public double getAnchorLon() {
		return anchorLon;
	}

	/**
	 * 
	 * @return list of events that were selected for hypocenter search last time
	 */
	public List<PickedEvent> getSelected() {
		return selected;
	}

	public void setSelected(List<PickedEvent> selected) {
		this.selected = selected;
	}

}
