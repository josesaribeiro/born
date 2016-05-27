package de.tudresden.inf.lat.born.owlapi.processor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * An object of this class manages the download of the ProbLog ZIP file.
 * 
 * @author Julian Mendez
 *
 */
public class ProblogDownloadManager {

	final String MD5 = "MD5";
	final String SHA_1 = "SHA-1";
	final String SHA_256 = "SHA-256";

	static final String DEFAULT_PROBLOG_ZIP_FILE = "problog-2.1-SNAPSHOT.zip";
	static final String DEFAULT_VERIFICATION_FILE = DEFAULT_PROBLOG_ZIP_FILE + ".sha";
	static final URI DEFAULT_PROBLOG_DOWNLOAD_URI = URI.create("https://bitbucket.org/problog/problog/get/master.zip");

	/**
	 * Creates a new ProbLog download manager.
	 */
	public ProblogDownloadManager() {
	}

	/**
	 * Returns the downloaded ZIP file name.
	 * 
	 * @return
	 */
	public String getProblogZipFile() {
		return DEFAULT_PROBLOG_ZIP_FILE;
	}

	/**
	 * Checks if it is necessary to download the ProbLog ZIP file, and in that
	 * case, it downloads the file.
	 * 
	 * @throws IOException
	 *             if something went wrong with the input/output
	 */
	public void downloadIfNecessary() throws IOException {
		boolean downloadIsNecessary = checkIfDownloadIsNecessary(DEFAULT_PROBLOG_ZIP_FILE);
		if (downloadIsNecessary) {
			downloadProblog(DEFAULT_PROBLOG_ZIP_FILE);
			String verificationCode = computeVerificationCode();
			storeVerificationCode(verificationCode);
		}
	}

	/**
	 * Downloads ProbLog from the default download URI.
	 * 
	 * @param start
	 *            execution start
	 * @param problogZipFile
	 *            file name of the ProbLog ZIP file
	 * @throws IOException
	 *             if something goes wrong with I/O
	 * @throws URISyntaxException
	 *             if the default URI is not valid
	 */
	void downloadProblog(String problogZipFile) throws IOException {
		Objects.requireNonNull(problogZipFile);
		ReadableByteChannel channel = Channels.newChannel(DEFAULT_PROBLOG_DOWNLOAD_URI.toURL().openStream());
		FileOutputStream output = new FileOutputStream(problogZipFile);
		output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		output.close();
	}

	String readVerificationCode() {
		String ret = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(DEFAULT_VERIFICATION_FILE));
			StringBuilder sb = new StringBuilder();
			reader.lines().forEach(line -> {
				sb.append(line.trim());
			});
			reader.close();
			ret = sb.toString();
		} catch (IOException e) {
		}
		return ret;
	}

	void storeVerificationCode(String code) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(DEFAULT_VERIFICATION_FILE));
		writer.write(code.trim());
		writer.newLine();
		writer.flush();
		writer.close();
	}

	byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BufferedInputStream input = new BufferedInputStream(inputStream);
		for (int ch = input.read(); ch != -1; ch = input.read()) {
			output.write(ch);
		}
		return output.toByteArray();
	}

	String computeVerificationCode() {
		String ret = "";
		try {
			ret = asHexString(computeVerificationCode(getBytes(new FileInputStream(DEFAULT_PROBLOG_ZIP_FILE))));
		} catch (IOException e) {

		}
		return ret;
	}

	byte[] computeVerificationCode(byte[] content) throws IOException {
		Objects.requireNonNull(content);
		MessageDigest md;
		byte[] digest;
		try {
			md = MessageDigest.getInstance(SHA_256);
			md.update(content);
			digest = md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		return digest;
	}

	String asHexString(byte[] buffer) {
		StringBuilder sb = new StringBuilder();
		IntStream.range(0, buffer.length).forEach(index -> {
			int value = Byte.toUnsignedInt(buffer[index]);
			String valueStr = ((value < 0x10) ? "0" : "") + Integer.toHexString(value);
			sb.append("" + valueStr);
		});
		return sb.toString();
	}

	boolean checkIfDownloadIsNecessary(String problogZipFile) {
		Objects.requireNonNull(problogZipFile);
		String storedVerificationCode = readVerificationCode();
		String verificationCode = computeVerificationCode();
		return storedVerificationCode.isEmpty()
				|| verificationCode.isEmpty() && !verificationCode.equals(storedVerificationCode);
	}

}