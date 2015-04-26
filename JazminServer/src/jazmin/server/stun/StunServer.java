/*
 * This file is part of JSTUN. 
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 * 
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */

package jazmin.server.stun;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import jazmin.core.Server;
import jazmin.misc.InfoBuilder;

/*
 * http://www.h3c.com.cn/MiniSite/Technology_Circle/Net_Reptile/The_Five/Home/Catalog/201206/747038_97665_0.htm
 * This class implements a STUN server as described in RFC 3489.
 * The server requires a machine that is dual-homed to be functional. 
 */
public class StunServer extends Server{
	//
	List<DatagramSocket> sockets;
	private String primaryAddress;
	private int primaryPort;
	private String secondaryAddress;
	private int secondaryPort;
	//
	public StunServer() {
		primaryAddress="127.0.0.1";
		primaryPort=3478;
		secondaryAddress="127.0.0.1";
		secondaryPort=3479;
		sockets = new ArrayList<DatagramSocket>();
	}
	
	/**
	 * @return the primaryAddress
	 */
	public String getPrimaryAddress() {
		return primaryAddress;
	}

	/**
	 * @param primaryAddress the primaryAddress to set
	 */
	public void setPrimaryAddress(String primaryAddress) {
		if(isStarted()){
			throw new IllegalStateException("set before started.");
		}
		this.primaryAddress = primaryAddress;
	}

	/**
	 * @return the primaryPort
	 */
	public int getPrimaryPort() {
		return primaryPort;
	}

	/**
	 * @param primaryPort the primaryPort to set
	 */
	public void setPrimaryPort(int primaryPort) {
		if(isStarted()){
			throw new IllegalStateException("set before started.");
		}
		this.primaryPort = primaryPort;
	}

	/**
	 * @return the secondaryAddress
	 */
	public String getSecondaryAddress() {
		return secondaryAddress;
	}

	/**
	 * @param secondaryAddress the secondaryAddress to set
	 */
	public void setSecondaryAddress(String secondaryAddress) {
		if(isStarted()){
			throw new IllegalStateException("set before started.");
		}
		this.secondaryAddress = secondaryAddress;
	}

	/**
	 * @return the secondaryPort
	 */
	public int getSecondaryPort() {
		return secondaryPort;
	}

	/**
	 * @param secondaryPort the secondaryPort to set
	 */
	public void setSecondaryPort(int secondaryPort) {
		if(isStarted()){
			throw new IllegalStateException("set before started.");
		}
		this.secondaryPort = secondaryPort;
	}

	//
	//--------------------------------------------------------------------------
	//
	private void init(int primaryPort, 
			InetAddress primary,
			int secondaryPort, 
			InetAddress secondary) throws SocketException {
		if(primaryAddress.equals(secondaryAddress)){
			throw new IllegalArgumentException("primary address can not equals secondary address");
		}
		sockets.add(new DatagramSocket(primaryPort, primary));
		sockets.add(new DatagramSocket(secondaryPort, primary));
		sockets.add(new DatagramSocket(primaryPort, secondary));
		sockets.add(new DatagramSocket(secondaryPort, secondary));		
	}
	//
	@Override
	public void start() throws Exception {
		init(primaryPort,
			InetAddress.getByName(primaryAddress), 
			secondaryPort, 
			InetAddress.getByName(secondaryAddress));
		//
		for (DatagramSocket socket : sockets) {
			socket.setReceiveBufferSize(2000);
			StunServerReceiverThread ssrt = new StunServerReceiverThread(socket,sockets);
			ssrt.start();
		}
	}
	//
	@Override
	public void stop() throws Exception {
		for (DatagramSocket socket : sockets) {
			socket.close();
		}
	}
	//
    @Override
    public String info() {
    	InfoBuilder ib=InfoBuilder.create();
		ib.section("info")
		.format("%-30s:%-30s\n")
		.print("primaryAddress",primaryAddress)
		.print("primaryPort",primaryPort)
		.print("secondaryAddress",secondaryAddress)
		.print("secondaryPort",secondaryPort);
		return ib.toString();
    }
}