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

	public AndroidFtpFile(File file, User user) { // FtpFile Ŭ���� : FTP ������ ���� ǥ���� �� ���Ǵ� Ŭ����. ���ϸ�, ����ũ��, ��ġ � ���� ������ ����
		super();
		this.file = file;
		this.user = user;
	}

	@Override
	public String getAbsolutePath() { // ������
		return file.getAbsolutePath();
	}

	@Override
	public String getName() { // ���� �̸�
		return file.getName();
	}

	@Override
	public boolean isHidden() { // ���� ������� ����
		return file.isHidden();
	}

	@Override
	public boolean isDirectory() { // ���丮 ����
		return file.isDirectory();
	}

	@Override
	public boolean isFile() { // ���� ����
		return file.isFile();
	}

	@Override
	public boolean doesExist() { // ���丮 �� ���� ���� ����
		return file.exists();
	}

	@Override
	public boolean isReadable() { // �б� ������ ���� ����
		return file.canRead();
	}

	@Override
	public boolean isWritable() { // ���� ������ ���� ����

		if (file.exists()) {
			return file.canWrite();
		}
		
		File parent = file.getParentFile(); // ���� ���丮 ��ȯ
		while (parent != null) {
			if (parent.exists()) {
				return parent.canWrite();
			}
			parent = parent.getParentFile(); // ���� ���丮�� null�̸�, false ��ȯ
		}
		return false;
	}

	@Override
	public boolean isRemovable() { // ���� ���� ���� : ����
		return file.canWrite();
	}

	@Override
	public String getOwnerName() {
		return user.getName(); // user �ڽ��� �̸� ��ȯ
	}

	@Override
	public String getGroupName() {
		return user.getName(); // user �׷��� �̸� ��ȯ
	}

	@Override
	public int getLinkCount() {
		return 0;
	}

	@Override
	public long getLastModified() {
		return file.lastModified(); // ���� ������ ���泯¥
	}

	@Override
	public boolean setLastModified(long time) {
		return file.setLastModified(time); // ���� ������ ���泯¥ �ֽ�ȭ
	}

	@Override
	public long getSize() {
		return file.length(); // ������ ũ��
	}

	@Override
	public boolean mkdir() {
		return file.mkdir(); // �ϳ��� ���丮 ����
	}

	@Override
	public boolean delete() {
		return file.delete(); // ���� ����
	}

	@Override
	public boolean move(FtpFile destination) {
		file.renameTo(new File(destination.getAbsolutePath())); // ���ϸ� ����
		return true; // ���� �̵� �Ϸ�
	}

	@Override
	public List<FtpFile> listFiles() {
		File[] filesArray = file.listFiles(); // �ش� ���Ͽ� ����ִ� ���ϵ��� �̸��� �迭�� �Ҵ�
		if (filesArray != null) {
			List<FtpFile> files = new ArrayList<FtpFile>(filesArray.length); // ������ ������ŭ ArrayList ����
			for (File file : filesArray) {
				files.add(new AndroidFtpFile(file, user)); // �ش� ���� ���ϵ��� ����Ʈ�� ����
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
