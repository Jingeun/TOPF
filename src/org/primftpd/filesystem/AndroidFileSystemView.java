package org.primftpd.filesystem;

import java.io.File;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

public class AndroidFileSystemView implements FileSystemView {

	private final User user;
	private AndroidFtpFile workingDir; 
	
	public AndroidFileSystemView(User user) {
		this.user = user;
		workingDir = createHomeDirObj(user);
	}
	
	private AndroidFtpFile createHomeDirObj(User user) {
		return new AndroidFtpFile(new File(user.getHomeDirectory()), user); // ��� sdcard�� ����
	}
	
	public FtpFile getHomeDirectory() throws FtpException {
		return createHomeDirObj(user);
	}
	
	public FtpFile getWorkingDirectory() throws FtpException {
		return workingDir;
	}
	
	public boolean changeWorkingDirectory(String dir) throws FtpException {
		
		File dirObj = new File(dir); 
		if (dirObj.isFile())  // ������ �����ϸ� false;
			return false;
		String path = dir;
		
//		if(dir.equals("") || dir.equals("/") || dir==null){
//			path= "/sdcard";
//		}else
//			path="/sdcard"+dir;
		
		if (!dirObj.isAbsolute()) { // ������ �����θ��� �ƴϸ�, ��θ� ���� ��η� �����Ѵ�.
			path = workingDir.getAbsolutePath() + File.separator + dir;
		}
		workingDir = new AndroidFtpFile(new File(path), user); // ���� ���丮 ����
		return true;
	}

	@Override
	public FtpFile getFile(String file) throws FtpException { // ���� ����
		File fileObj = new File(file); // �ش� ���� ��ü ����
		if (fileObj.isAbsolute()) { // ������ �����θ��̸�, �ٷ� ���Ϸ� ��ȯ
			return new AndroidFtpFile(fileObj, user);
		}

		// handle relative paths
		file = workingDir.getAbsolutePath() + File.separator + file; // �׷��� ������, �����η� ������ �ڿ�, ���Ϸ� ��ȯ
		return new AndroidFtpFile(new File(file), user);
	}	

	@Override
	public boolean isRandomAccessible() throws FtpException {
		return true;
	}

	@Override
	public void dispose() {
	}
}