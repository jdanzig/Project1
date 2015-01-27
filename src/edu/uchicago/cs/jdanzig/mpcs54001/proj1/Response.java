pckage edu.uchicago.cs.jdanzig.mpcs54001.proj1;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.io.File;
import java.util.Date;
import java.io.*;


public class Response {
	private String path;
	private Request req;
	private int httpStatus;
	private Hashtable<String,String> headers;
	String redirectPath;
	public Response(Request req) {
		this.req = req;
		this.headers = new Hashtable<String,String>();
		headers.put("Connection", "Close");
		headers.put("Date", new Date().toString());
	}
	
	public Response() {
		this.headers = new Hashtable<String,String>();
		headers.put("Connection", "Close");
		headers.put("Date", new Date().toString());
	}
	
	private boolean handleRedirect(PrintWriter out) throws HTTPErrorException {
		this.path = this.req.getPath();
		if (Redirects.isRedirect(this.path)) {
			headers.put("Location", Redirects.getRedirect(this.path));
			//TODO: Figure out a way to throw an exception here
      // (but still preserve the target location)
			//showError(out, new HTTPErrorException(301));
			return true;
		}
		return false;
	}
	
	private void addContentTypeHeader(File f) {
		try{
			headers.put("Content-Type", MimeTypeDetector.getContentType(f));	
		} catch (IOException x)	{ 
			headers.put("Content-Type", "application/unknown");
		}
	}
	
	private void writeHeaders(PrintWriter out) {
		for(String headerName : this.headers.keySet()) {	
			out.printf("%s: %s\r\n", headerName, this.headers.get(headerName));
		}	
		out.print("\r\n");
	}
	
	private void writeContent(PrintWriter out, File f) throws IOException {
		String lineOut;
		BufferedReader fileReader = new BufferedReader(new FileReader(f));
		while ((lineOut = fileReader.readLine()) != null){
			out.println(lineOut);
		}
	}
	
	public void show(PrintWriter out) throws HTTPErrorException {
    // This function is almost done.
    // The only thing that needs to be added is HTTP responses
    //   that are successful (This function only deals with successful responses)
    // return HTTP/1.1 200 OK as their first line
    // The problem we have is that we don't know if the status will be 200..
    //   until we go through all the steps below
    // So what we're going to need to do is, instead of writing data directly to the client with out, we need to use a java.lang.StringBuffer to hold the response, and only print the response to out at the very end of the show function.
    // This way, if an error gets thrown in the show function, we can abandon the string buffer that contain the partial output, and go directly to printing the error message without screwing up our response.
    // Note that when sending http/1.1 200 OK to the browser, you are saying that you are using http protocol 1.1. This version must match the http versison given by the client when they made their request.
    // Check the varibale req to get the http version number
    if(handleRedirect(out)) return;
		this.path = "www/" + this.path;
		File reqFile = new File(path);
		if (!reqFile.exists() || this.path == "www/redirect.defs") throw new HTTPErrorException(404);
		addContentTypeHeader(reqFile);
		writeHeaders(out);
		
		try {
			if (req.getRequestMethod() == "GET") writeContent(out, reqFile);
		} catch(IOException e) {
			throw new HTTPErrorException(500);
		}	
	}
	
	public void showError(PrintWriter out, HTTPErrorException exp) {
		out.println("HTTP/1.1: " + exp.getHttpStatusCode());
		// Return first line in this format HTTP/1.x 404 NOT FOUND
    // What words to include for which status code (IN ALL CAPS)
    // http://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml#http-status-codes-1
    // Not sure if we need to return content explaining the errors, I'll look at some sample requests --status codes in the 400s and 500s normally have some sort of webpage error message
	}
}
