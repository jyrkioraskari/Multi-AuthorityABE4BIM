package fi.aalto.lbd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Optional;

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
			ByteArrayInputStream bis = new ByteArrayInputStream(this.ipfs_connection.cat(filePointer));
			ObjectInputStream in = new ObjectInputStream(bis);
			optional_gp = Optional.ofNullable((GlobalParameters) in.readObject());
			if(this.ipfs_connection.getIpfs().isPresent())
			   this.ipfs_connection.getIpfs().get().pin.add(filePointer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!optional_gp.isPresent()) {
			try {
				optional_gp = Optional.ofNullable(DCPABE.globalSetup(160));

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(optional_gp.get());
				out.flush();

				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("gp", bos.toByteArray());
				List<MerkleNode> node = ipfs_connection.add(file);
				System.out.println("Global parameters hash: " + node.get(0).hash.toBase58());
				AaltoABEGlobal.global_parameters_hash = node.get(0).hash.toBase58();
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public AaltoABEGlobal() {
		System.out.println("Global Parameters 2");
		if (!optional_gp.isPresent()) {
			try {
				optional_gp = Optional.ofNullable(DCPABE.globalSetup(160));

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(optional_gp.get());
				out.flush();

				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("gp", bos.toByteArray());
				List<MerkleNode> node = ipfs_connection.add(file);
				System.out.println("Directory hash: " + node.get(0).hash.toBase58());
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
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
