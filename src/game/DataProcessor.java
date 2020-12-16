package game;

import tudorserver.PacketIntention;

public class DataProcessor {

	public PacketIntention packetIntention = PacketIntention.RESIDUAL;

	public DataProcessor(byte[] data) {

		int packetIntention = getIntentionFromPacket(data);

		switch (packetIntention) {

		case 0:

			this.packetIntention = PacketIntention.RECEIVE_ACCOUNT_UPON_LOG_IN;
			break;

		case 1:
			this.packetIntention = PacketIntention.JOIN_LOBBY;
			break;

		default:
			this.packetIntention = PacketIntention.RESIDUAL;
			break;
		}
	}

	private int getIntentionFromPacket(byte[] data) {
		//TODO - get integer from the first 4 bytes
		return 1;
	}

}
