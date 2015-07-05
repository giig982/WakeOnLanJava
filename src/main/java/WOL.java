package main.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class WOL {
	private static int WAKE_UP_PORT = 9;

	private String macAddress;
	private int port;

	public WOL(String macAddress, Integer port) {
		this.macAddress = macAddress;
		if (port != null)
			this.port = port;
		else
			this.port = WAKE_UP_PORT;
	}

	public void wakeUp() throws IOException {
		byte[] bytes = getMagicBytes(macAddress);
		InetAddress address = getMulticastAddress();
		send(bytes, address, port);
	}

	private byte[] getMagicBytes(String macAddress) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		for (int i = 0; i < 6; i++)
			bytes.write(0xff);

		// MAC address are often presented on AA:BB:CC:DD:EE:FF, not in
		// AABBCCDDEEFF form, so we strip ":"s out
		if (macAddress.indexOf(':') != -1) {
			macAddress = macAddress.replace(":", "");
		}
		// In Windows world, instead, MAC Address are in AA-BB-CC-DD-EE-FF form
		if (macAddress.indexOf('-') != -1) {
			macAddress = macAddress.replace("-", "");
		}

		byte[] macAddressBytes = parseHexString(macAddress);
		for (int i = 0; i < 16; i++)
			bytes.write(macAddressBytes);

		bytes.flush();

		return bytes.toByteArray();
	}

	/**
	 * Parse hex string
	 * 
	 * @param string
	 * @return
	 */
	private byte[] parseHexString(String string) {
		byte[] bytes = new byte[string.length() / 2];
		for (int i = 0, j = 0; i < string.length(); i += 2, j++)
			bytes[j] = (byte) Integer.parseInt(string.substring(i, i + 2), 16);
		return bytes;
	}

	private void send(byte[] bytes, InetAddress addr, int port) throws IOException {
		DatagramPacket p = new DatagramPacket(bytes, bytes.length, addr, port);
		try (DatagramSocket s = new DatagramSocket()) {
			System.out.println("Sending magic packet to " + this.macAddress);
			s.send(p);

		} catch (Error e) {
			System.err.println(e);
		}
	}

	private static InetAddress getMulticastAddress() throws UnknownHostException {
		return InetAddress.getByAddress(new byte[] { -1, -1, -1, -1 });
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * @param macAddress
	 *            the macAddress to set
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			printUsage();
			return;

		} else {
			System.out.println("Trying to wakeup " + args[0] + " on port " + WAKE_UP_PORT);
			WOL wol = new WOL(args[0], WAKE_UP_PORT);
			wol.wakeUp();
		}
	}

	/**
	 * 
	 */
	private static void printUsage() {
		StringBuffer usage = new StringBuffer();
		usage.append("Usage: \n");
		usage.append("\t WOL <mac_address_space_separated>");
		System.out.println(usage);
	}

}
