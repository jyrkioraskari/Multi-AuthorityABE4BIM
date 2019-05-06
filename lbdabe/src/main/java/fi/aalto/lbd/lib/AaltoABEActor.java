package fi.aalto.lbd.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import fi.aalto.lbd.AaltoABEGlobal;
import io.ipfs.api.IPFS;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;

public class AaltoABEActor {
	protected final AaltoIPFSConnection ipfs = AaltoIPFSConnection.getInstance();
	protected Optional<GlobalParameters> gp=Optional.empty();
	public  String security_directory = "security/";

	public AaltoABEActor(String global_parameters_hash)
	{
		try {
			this.security_directory=(new File(security_directory)).getCanonicalFile().getAbsolutePath()+File.separator;
		} catch (IOException e) {
			e.printStackTrace();
		}

		gp=AaltoABEGlobal.getInstance(global_parameters_hash).getGp();
		Path fp = Paths.get("security"); // Relative
	    try {
			Files.createDirectories(fp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public IPFS getIpfs() {
		return ipfs.getIpfs().get();
	}
	
	

}
