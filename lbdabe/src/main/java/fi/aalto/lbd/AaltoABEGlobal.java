package fi.aalto.lbd;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.aalto.lbd.lib.AaltoIPFSConnection;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;

public class AaltoABEGlobal {
	// voi olla koneen parametri
	static public String global_parameters_hash = "QmezCbZRUjwHqThpcsyF7sjNy2sjvqtCtzreetSFRywHsw";

	private final AaltoIPFSConnection ipfs_connection = AaltoIPFSConnection.getInstance();
	private Optional<GlobalParameters> optional_gp = Optional.empty();

	static private Optional<AaltoABEGlobal> instance = Optional.empty();

	public static AaltoABEGlobal getInstance(String global_parameters_hash) {
		AaltoABEGlobal.global_parameters_hash =global_parameters_hash;
		if (!instance.isPresent())
			instance = Optional.of(new AaltoABEGlobal(global_parameters_hash));
		return instance.get();
	}

	private AaltoABEGlobal(String global_parameters_hash) {
		System.out.println("Global Parameters 1");
		Multihash filePointer = Multihash.fromBase58(global_parameters_hash);
		try {
			String contents = new String(this.ipfs_connection.cat(filePointer));
			optional_gp = Optional.ofNullable( new ObjectMapper().readerFor(GlobalParameters.class).readValue(contents));

			if(this.ipfs_connection.getIpfs().isPresent())
			   this.ipfs_connection.getIpfs().get().pin.add(filePointer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!optional_gp.isPresent()) {
			try {
				optional_gp = Optional.ofNullable(DCPABE.globalSetup(160));

				String json_string = new ObjectMapper().writeValueAsString(optional_gp.get());
				System.out.println("GP json string: "+json_string);
				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("gp", json_string.getBytes());
				List<MerkleNode> node = ipfs_connection.add(file);
				System.out.println("Global parameters hash: " + node.get(0).hash.toBase58());
				AaltoABEGlobal.global_parameters_hash = node.get(0).hash.toBase58();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public AaltoABEGlobal() {
		System.out.println("Global Parameters 2");
		if (!optional_gp.isPresent()) {
			try {
				optional_gp = Optional.ofNullable(DCPABE.globalSetup(160));

				String json_string = new ObjectMapper().writeValueAsString(optional_gp.get());
				System.out.println("GP json string: "+json_string);
				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("gp", json_string.getBytes());
				List<MerkleNode> node = ipfs_connection.add(file);
				System.out.println("Global parameters hash: " + node.get(0).hash.toBase58());
				AaltoABEGlobal.global_parameters_hash = node.get(0).hash.toBase58();
				System.out.println("Directory hash: " + node.get(0).hash.toBase58());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public Optional<GlobalParameters> getGp() {
		return optional_gp;
	}

	public static void main(String[] args) {
		new AaltoABEGlobal();
	}

}
