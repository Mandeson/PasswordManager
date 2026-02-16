package passwordmanager.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Storage {
	private Document document = null;
	private String password;
	
	public Storage(InputStream input, String password) {
		this.password = password;
		try {
			if (input == null) {
				// Create empty database
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				document = builder.newDocument();
				Element root = document.createElement("password_storage");
				document.appendChild(root);
			} else {
				load(input);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void save(OutputStream outputStream) {
		ByteArrayOutputStream cleartextOutput = new ByteArrayOutputStream();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(cleartextOutput));
			byte[] cleartext = cleartextOutput.toByteArray();
			
			byte[] encrypted = new Cipher.Encryptor(cleartext, password).getEncrypted();
			
			outputStream.write(encrypted);
		} catch (TransformerException | IOException e) {
			throw new RuntimeException("Save error", e);
		}
	}
	
	private void load(InputStream inputStream) {
		try {
			byte[] encrypted = inputStream.readAllBytes();
			byte[] cleartext = new Cipher.Decryptor(encrypted, password).getDecrypted();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.parse(new ByteArrayInputStream(cleartext));
		} catch (ParserConfigurationException | IOException | SAXException e) {
			throw new RuntimeException("Load error", e);
		}
	}
}
