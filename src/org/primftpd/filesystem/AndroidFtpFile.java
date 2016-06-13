package org.primftpd.filesystem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

public class AndroidFtpFile implements FtpFile {

	private final File file;
	private final User user;

	public AndroidFtpFile(File file, User user) { // FtpFile 클래스 : FTP 서버의 파일 표현할 때 사용되는 클래스. 파일명, 파일크기, 위치 등에 대한 정보를 제공
		super();
		this.file = file;
		this.user = user;
	}

	@Override
	public String getAbsolutePath() { // 절대경로
		return file.getAbsolutePath();
	}

	@Override
	public String getName() { // 파일 이름
		return file.getName();
	}

	@Override
	public boolean isHidden() { // 파일 숨김상태 여부
		return file.isHidden();
	}

	@Override
	public boolean isDirectory() { // 디렉토리 여부
		return file.isDirectory();
	}

	@Override
	public boolean isFile() { // 파일 여부
		return file.isFile();
	}

	@Override
	public boolean doesExist() { // 디렉토리 및 파일 존재 여부
		return file.exists();
	}

	@Override
	public boolean isReadable() { // 읽기 가능한 상태 여부
		return file.canRead();
	}

	@Override
	public boolean isWritable() { // 쓰기 가능한 상태 여부

		if (file.exists()) {
			return file.canWrite();
		}
		
		File parent = file.getParentFile(); // 상위 디렉토리 반환
		while (parent != null) {
			if (parent.exists()) {
				return parent.canWrite();
			}
			parent = parent.getParentFile(); // 상위 디렉토리가 null이면, false 반환
		}
		return false;
	}

	@Override
	public boolean isRemovable() { // 쓰기 가능 여부 : 삭제
		return file.canWrite();
	}

	@Override
	public String getOwnerName() {
		return user.getName(); // user 자신의 이름 반환
	}

	@Override
	public String getGroupName() {
		return user.getName(); // user 그룹의 이름 반환
	}

	@Override
	public int getLinkCount() {
		return 0;
	}

	@Override
	public long getLastModified() {
		return file.lastModified(); // 파일 마지막 변경날짜
	}

	@Override
	public boolean setLastModified(long time) {
		return file.setLastModified(time); // 파일 마지막 변경날짜 최신화
	}

	@Override
	public long getSize() {
		return file.length(); // 파일의 크기
	}

	@Override
	public boolean mkdir() {
		return file.mkdir(); // 하나의 디렉토리 생성
	}

	@Override
	public boolean delete() {
		return file.delete(); // 파일 삭제
	}

	@Override
	public boolean move(FtpFile destination) {
		file.renameTo(new File(destination.getAbsolutePath())); // 파일명 변경
		return true; // 파일 이동 완료
	}

	@Override
	public List<FtpFile> listFiles() {
		File[] filesArray = file.listFiles(); // 해당 파일에 들어있는 파일들의 이름을 배열에 할당
		if (filesArray != null) {
			List<FtpFile> files = new ArrayList<FtpFile>(filesArray.length); // 파일의 갯수만큼 ArrayList 생성
			for (File file : filesArray) {
				files.add(new AndroidFtpFile(file, user)); // 해당 하위 파일들을 리스트에 저장
			}
			return files;
		}
		return new ArrayList<FtpFile>(0);
	}

	public static final int BUFFER_SIZE = 1024 * 1024;

	@Override
	public OutputStream createOutputStream(long offset) throws IOException {

		// may be necessary to create dirs
		// see isWritable()
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}

		// now create out stream
		OutputStream os = null;
		if (offset == 0) {
			os = new FileOutputStream(file);
		} else if (offset == this.file.length()) {
			os = new FileOutputStream(file, true);
		} else {
			final RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
			raf.seek(offset);
			os = new OutputStream() {
				@Override
				public void write(int oneByte) throws IOException {
					raf.write(oneByte);
				}
				@Override
				public void close() throws IOException {
					raf.close();
				}
			};
		}

		BufferedOutputStream bos = new BufferedOutputStream(os, BUFFER_SIZE);
		return bos;
	}

	@Override
	public InputStream createInputStream(long offset) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		fis.skip(offset);
		BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
		return bis;
	}

	public File getFile() {
		return file;
	}

	public User getUser() {
		return user;
	}
}
