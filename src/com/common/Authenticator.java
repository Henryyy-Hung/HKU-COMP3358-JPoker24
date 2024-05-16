package com.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.enums.LoginStatus;
import com.enums.RegistrationStatus;

public interface Authenticator extends Remote {

    LoginStatus login(String username, String password) throws RemoteException;

    boolean logout(String username) throws RemoteException;

    RegistrationStatus register(String username, String password) throws RemoteException;

}

