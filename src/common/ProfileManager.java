package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ProfileManager extends Remote {

    User getUser(String username, String password) throws RemoteException;

    List<User> getTopUsers() throws RemoteException;

}

