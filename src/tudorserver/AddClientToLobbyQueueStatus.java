package tudorserver;

public enum AddClientToLobbyQueueStatus{

	SUCCESS, 
	OVERFLOW, 
	BANNED,
	LVL_DIFFERENCE_ERROR,
	INVALID_HOST,
	SOCKET_EXCEPTION
}
