package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import enums.LoginStatus;
import enums.RegistrationStatus;

public interface Authenticator extends Remote {

    LoginStatus login(String username, String password) throws RemoteException;

    boolean logout(String username) throws RemoteException;

    RegistrationStatus register(String username, String password) throws RemoteException;

}

