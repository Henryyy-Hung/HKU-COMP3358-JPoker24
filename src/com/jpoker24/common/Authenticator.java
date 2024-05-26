package com.jpoker24.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.jpoker24.enums.LoginStatus;
import com.jpoker24.enums.RegistrationStatus;

public interface Authenticator extends Remote {

    LoginStatus login(String username, String password) throws RemoteException;

    boolean logout(String username) throws RemoteException;

    RegistrationStatus register(String username, String password) throws RemoteException;

}

