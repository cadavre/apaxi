package pro.jazzy.paxi.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import pro.jazzy.paxi.PaxiUtility;

import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

public class Serialization {
	public static String PREF_NAME = ".paxi_ROUTE";

	public static String routeAsString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(out).writeObject(PaxiUtility.CurrentRoute);
			byte[] data = out.toByteArray();
			out.close();

			out = new ByteArrayOutputStream();
			Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
			b64.write(data);
			b64.close();
			out.close();

			return new String(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String membersAsString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(out).writeObject(PaxiUtility.members);
			byte[] data = out.toByteArray();
			out.close();

			out = new ByteArrayOutputStream();
			Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
			b64.write(data);
			b64.close();
			out.close();

			return new String(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Route stringToRoute(String route) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(route.getBytes());
			Base64InputStream b64 = new Base64InputStream(bais, Base64.DEFAULT);
	        return (Route)(new ObjectInputStream(b64).readObject());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public static Hashtable<String, Member> stringToMembers(String members) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(members.getBytes());
			Base64InputStream b64 = new Base64InputStream(bais, Base64.DEFAULT);
	        return (Hashtable<String, Member>)(new ObjectInputStream(b64).readObject());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
}
