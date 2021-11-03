package io.mosip.pms.policy.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RequestWrapper extends HttpServletRequestWrapper {
	private final String body;
	
	public RequestWrapper(HttpServletRequest request) throws IOException {
	   super(request);
	   StringBuilder stringBuilder = new StringBuilder();
	   BufferedReader bufferedReader = null;
	   try {
	     InputStream inputStream = request.getInputStream();
	     if (inputStream != null) {
	       bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	       char[] charBuffer = new char[128];
	       int bytesRead = -1;
	       while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	         stringBuilder.append(charBuffer, 0, bytesRead);
	       }
	     } else {
	       stringBuilder.append("");
	     }
	   } catch (IOException ex) {
	       throw ex;
	   } finally {
	     if (bufferedReader != null) {
	       try {
	         bufferedReader.close();
	       } catch (IOException ex) {
	         //throw ex;
	       }
	     }
	   }
	   body = stringBuilder.toString();
	 }
	
	 @Override
	 public ServletInputStream getInputStream() throws IOException {
	   final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
	   ServletInputStream servletInputStream = new ServletInputStream() {
	     public int read() throws IOException {
	       return byteArrayInputStream.read();
	     }

		@Override
		public boolean isFinished() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setReadListener(ReadListener listener) {
			// TODO Auto-generated method stub
			
		}
	   };
	   return servletInputStream;
	 }
	 
	 @Override
	 public BufferedReader getReader() throws IOException {
	   return new BufferedReader(new InputStreamReader(this.getInputStream()));
	 }

	 public String getBody() {
	   return this.body;
	 }
}
