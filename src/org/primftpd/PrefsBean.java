package org.primftpd;

import java.io.Serializable;

public class PrefsBean implements Serializable{
	private static final long serialVersionUID = 1L;	//±âº» SerialVersonUID

	private final String userName;
	private final String password;
	private final String portStr;
	private final String directory;
	private final int port;

	public PrefsBean(String userName, String password, int port, String directory){
		super();
		this.userName = userName;
		this.password = password;
		this.port = port;
		this.portStr = String.valueOf(port);
		this.directory = directory;
	}

	public String getUserName() { return userName; }
	public String getDirectory(){ return directory; }
	public String getPassword() { return password; }

	public String getPortStr() { return portStr; }

	public int getPort() { return port; }

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + port;
		result = prime * result + ((portStr == null) ? 0 : portStr.hashCode());
		result = prime * result	+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PrefsBean other = (PrefsBean) obj;
		if (password == null) {
			if (other.password != null) return false;
		} else if (!password.equals(other.password)) return false;
		if (port != other.port)	return false;
		if (portStr == null) { 
			if (other.portStr != null) return false;
		} else if (!portStr.equals(other.portStr)) return false;
		if (userName == null) {
			if (other.userName != null) return false;
		} else if (!userName.equals(other.userName)) return false;
		
		return true;
	}
}
