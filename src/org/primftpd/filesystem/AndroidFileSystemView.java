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
		return new AndroidFtpFile(new File(user.getHomeDirectory()), user); // 경로 sdcard로 설정
	}
	
	public FtpFile getHomeDirectory() throws FtpException {
		return createHomeDirObj(user);
	}
	
	public FtpFile getWorkingDirectory() throws FtpException {
		return workingDir;
	}
	
	public boolean changeWorkingDirectory(String dir) throws FtpException {
		
		File dirObj = new File(dir); 
		if (dirObj.isFile())  // 파일이 존재하면 false;
			return false;
		String path = dir;
		
//		if(dir.equals("") || dir.equals("/") || dir==null){
//			path= "/sdcard";
//		}else
//			path="/sdcard"+dir;
		
		if (!dirObj.isAbsolute()) { // 파일이 절대경로명이 아니면, 경로를 절대 경로로 수정한다.
			path = workingDir.getAbsolutePath() + File.separator + dir;
		}
		workingDir = new AndroidFtpFile(new File(path), user); // 최종 디렉토리 수정
		return true;
	}

	@Override
	public FtpFile getFile(String file) throws FtpException { // 파일 정보
		File fileObj = new File(file); // 해당 파일 객체 생성
		if (fileObj.isAbsolute()) { // 파일이 절대경로명이면, 바로 파일로 반환
			return new AndroidFtpFile(fileObj, user);
		}

		// handle relative paths
		file = workingDir.getAbsolutePath() + File.separator + file; // 그렇지 않으면, 절대경로로 수정한 뒤에, 파일로 반환
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