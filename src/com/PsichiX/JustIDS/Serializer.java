package com.PsichiX.JustIDS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
	public static byte[] serialize(Object obj) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o;
		try {
			o = new ObjectOutputStream(b);
			o.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return b.toByteArray();
	}

	public static Object deserialize(byte[] bytes){
		try {
			ByteArrayInputStream b = new ByteArrayInputStream(bytes);
			ObjectInputStream o = new ObjectInputStream(b);
			return o.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
