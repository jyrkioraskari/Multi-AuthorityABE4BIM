package fi.aalto.lbd.lib;

import java.util.ArrayList;
import java.util.List;

import fr.rhaz.ipfs.IPFSDaemon;

public class IPFSDaemonThread extends Thread {

	public IPFSDaemonThread() {
		this.start();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("init done");
	}

	public void run() {
		IPFSDaemon ipfsd = new IPFSDaemon();
		ipfsd.download();
		String[] args= {"--enable-pubsub-experiment"};
		ipfsd.setArgs(args);
		ipfsd.start();
	}

}
