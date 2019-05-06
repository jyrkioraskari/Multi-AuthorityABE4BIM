package fi.aalto.lbd.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

public class AaltoIPFSConnection {
	private Optional<IPFS> ipfs = Optional.empty();

	static private Optional<AaltoIPFSConnection> instance = Optional.empty();

	static public AaltoIPFSConnection getInstance() {
		if (!instance.isPresent())
			instance = Optional.of(new AaltoIPFSConnection());
		return instance.get();
	}

	private AaltoIPFSConnection() {
		while (!this.ipfs.isPresent()) {
			try {
				this.ipfs = Optional.of(new IPFS("/ip4/127.0.0.1/tcp/5001"));
			} catch (Exception e) {
				new IPFSDaemonThread();
				this.ipfs = Optional.of(new IPFS("/ip4/127.0.0.1/tcp/5001"));
			}
		}
	}

	private class Bytes {
		private byte[] data;

		public Bytes(byte[] data) {
			this.data = data;
		}

		public byte[] getData() {
			return data;
		}

	}

	// Idea from:
	// https://stackoverflow.com/questions/1164301/how-do-i-call-some-blocking-method-with-a-timeout-in-java
	public byte[] cat(final Multihash filePointer) throws TimeoutException {
		if(!ipfs.isPresent())
		{
			System.err.println("IPFS is not initialized.");
			return null;
		}
		ExecutorService executor = Executors.newCachedThreadPool();
		Callable<Bytes> task = new Callable<Bytes>() {
			public Bytes call() {
				try {
					if (ipfs.isPresent())
						return new Bytes(ipfs.get().cat(filePointer));
					else
						return null;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		Future<Bytes> future = executor.submit(task);
		try {
			Bytes result = future.get(15, TimeUnit.SECONDS);

			return result.getData();

		} catch (TimeoutException ex) {
			// handle the timeout
		} catch (InterruptedException e) {
			// handle the interrupts
		} catch (ExecutionException e) {
			// handle other exceptions
		} finally {
			future.cancel(true); // may or may not desire this
		}
		throw new TimeoutException();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<MerkleNode>  add(NamedStreamable.ByteArrayWrapper file)
	{
		if(!ipfs.isPresent())
		{
			System.err.println("IPFS is not initialized.");
			return new ArrayList();
		}
		try {
			return ipfs.get().add(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList();
	}
	
	public String getID() {
		if(!ipfs.isPresent())
		{
			System.err.println("IPFS is not initialized.");
			 return "unknown";
		}

		if (ipfs.isPresent())
			try {
				return ipfs.get().id().get("ID").toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return "unknown";
	}

	public Optional<IPFS> getIpfs() {
		return ipfs;
	}

	
	public static String publish(String topic, String content) {
		System.out.println("Client publish topic: " + topic + " content: " + content);
		String urlToRead = "http://127.0.0.1:5001/api/v0/pubsub/pub?arg=" + URLEncoder.encode(topic) + "&arg="
				+ URLEncoder.encode(content);
		StringBuilder result = new StringBuilder();
		URL url;
		try {
			url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
}
