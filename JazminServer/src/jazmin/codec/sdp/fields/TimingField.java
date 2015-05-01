/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package jazmin.codec.sdp.fields;

import jazmin.codec.sdp.SdpField;

/**
 * t=<start-time> <stop-time>
 * <p>
 * The "t=" lines specify the start and stop times for a session. <br>
 * Multiple "t=" lines MAY be used if a session is active at multiple
 * irregularly spaced times; each additional "t=" line specifies an additional
 * period of time for which the session will be active. If the session is active
 * at regular times, an "r=" line (see below) should be used in addition to, and
 * following, a "t=" line -- in which case the "t=" line specifies the start and
 * stop times of the repeat sequence.
 * </p>
 * <p>
 * The first and second sub-fields give the start and stop times, respectively,
 * for the session. These values are the decimal representation of Network Time
 * Protocol (NTP) time values in seconds since 1900. To convert these
 * values to UNIX time, subtract decimal 2208988800.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class TimingField implements SdpField {

	// text parsing
	public static final char FIELD_TYPE = 't';
	private static final String BEGIN = "t=";
	private static final int BEGIN_LEN = BEGIN.length();

	// default values
	private static final int DEFAULT_START = 0;
	private static final int DEFAULT_STOP = 0;
	
	private final StringBuilder builder;
	
	private int startTime;
	private int stopTime;
	
	public TimingField() {
		this(DEFAULT_START, DEFAULT_STOP);
	}
	
	public TimingField(int startTime, int stopTime) {
		this.builder = new StringBuilder(BEGIN);
		this.startTime = startTime;
		this.stopTime = stopTime;
	}
	
	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getStopTime() {
		return stopTime;
	}

	public void setStopTime(int stopTime) {
		this.stopTime = stopTime;
	}

	@Override
	public char getFieldType() {
		return FIELD_TYPE;
	}
	
	@Override
	public String toString() {
		// clear builder
		this.builder.setLength(BEGIN_LEN);
		this.builder.append(this.startTime).append(" ").append(this.stopTime);
		return this.builder.toString();
	}

}
