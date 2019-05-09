package fi.aalto;

import java.util.List;

import org.iota.jota.IotaAPI;
import org.iota.jota.error.ArgumentException;
import org.iota.jota.model.Transaction;
import org.iota.jota.model.Transfer;
import org.iota.jota.pow.pearldiver.PearlDiverLocalPoW;
import org.iota.jota.utils.InputValidator;
import org.iota.jota.utils.SeedRandomGenerator;
import org.iota.jota.utils.TrytesConverter;

public class IotaTests {

	public static void main(String[] args) {
		String generatedSeed = SeedRandomGenerator.generateNewSeed();
		System.out.println(generatedSeed.length());
		@SuppressWarnings("unused")
		String seed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9ABCDEFGHIJKLMNOPQRSTUVWXYZ9ABCDEFGHIJKLMNOPQRSTUVWXYZ9";
		String addr = "DO9OEM9IMNXSUITDZMUNVDZWHFLVVCJF9AFYKLTGLYYDJ9XQXHBJWJMODXWIOGQSSDYZMWKQSF9NGWFJWJQMXGWIT9";

		String test_message = "Hello Aalto!";
		String trytes_message = null;
		try {
			trytes_message = TrytesConverter.asciiToTrytes(test_message);
		} catch (ArgumentException e) {
			e.printStackTrace();
		}
		System.out.println("message in trytes: " + trytes_message);

		try {

			String message = TrytesConverter.trytesToAscii(trytes_message);
			System.out.println("message: " + message);
		} catch (ArgumentException e) {
			e.printStackTrace();
		}

		IotaAPI api = new IotaAPI.Builder().localPoW(new PearlDiverLocalPoW()).protocol("http")
				.host("node01.iotatoken.nl").port(14265).build();

		// GetNodeInfoResponse response = api.getNodeInfo();
		// System.out.println(response.toString());
		/*
		 * List<Transfer> transfers = new ArrayList<>(); String TEST_TAG =
		 * "IOTAJAVASPAM999999999999999";
		 * 
		 * //System.out.println("gen add:"+api.getAddressesUnchecked(seed, 2, true, 1,
		 * 2));
		 * 
		 * 
		 * transfers.add(new Transfer(addr, 0, trytes_message, TEST_TAG));
		 * 
		 * validateTransfers(transfers);
		 * 
		 * int DEPTH = 9; int MIN_WEIGHT_MAGNITUDE = 14; List<Transaction> tips = new
		 * ArrayList<>(); //List<String> trytes = api.prepareTransfers(seed, 2,
		 * transfers, null, null, null, false); SendTransferResponse str =
		 * api.sendTransfer(seed, 2, DEPTH, MIN_WEIGHT_MAGNITUDE, transfers, null, null,
		 * false, false, tips); System.out.println(str.getTransactions().size());
		 * System.out.println(str.getTransactions());
		 * System.out.println(str.getSuccessfully());
		 */

		System.out.println("Find:");
		String[] hashes = { addr };
		List<Transaction> transactions = api.findTransactionObjectsByAddresses(hashes);
		for (Transaction t : transactions) {
			try {

				String message = t.getSignatureFragments();

				if (message.length() % 2 != 0) {
					message += "9";
				}
				System.out.println("message: " + TrytesConverter.trytesToAscii(message).trim());
			} catch (ArgumentException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done");

	}

	@SuppressWarnings("unused")
	private static void validateTransfers(List<Transfer> transfers) {
		// Input validation of transfers object
		if (transfers == null || transfers.isEmpty()) {
			System.out.println("invalid trandfer");
			return;
		}

		for (final Transfer transfer : transfers) {
			if (transfer == null) {
				System.out.println("null!!");
				return;
			}

			if (!InputValidator.isAddress(transfer.getAddress())) {
				System.out.println("address!!");
				return;
			}

			// Check if message is correct trytes encoded of any length
			if (transfer.getMessage() == null
					|| !InputValidator.isTrytes(transfer.getMessage(), transfer.getMessage().length())) {
				System.out.println("not trytes!!");
				return;
			}

			// Check if tag is correct trytes encoded and not longer than 27 trytes
			if (!InputValidator.isTag(transfer.getTag())) {
				System.out.println("invalid tag!!");
				return;
			}
		}
	}

}
