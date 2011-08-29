package org.bndtools.runtime.controller.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bndtools.runtime.controller.IHandler;
import org.bndtools.runtime.controller.IResponse;

/**
 * Adapts an IHandler to a Servlet.
 * 
 * @author kgilmer
 *
 */
public class HandlerServletAdapter extends HttpServlet implements Servlet {
	private static final long serialVersionUID = -9223098343374995024L;
	private final IHandler handler;

	public HandlerServletAdapter(IHandler handler) {
		this.handler = handler;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Properties p = getProperties(req);
		String[] qs = getQueryString(req);

		respond(handler.handleGet(qs, p), resp);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Properties p = getProperties(req);
		String[] qs = getQueryString(req);

		//TODO: figure out what should be passed as 'upload' argument.
		respond(handler.handlePost(qs, p, null, req.getInputStream()), resp);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Properties p = getProperties(req);
		String[] qs = getQueryString(req);

		respond(handler.handleDelete(qs, p), resp);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Properties p = getProperties(req);
		String[] qs = getQueryString(req);

		//TODO: figure out what should be passed as 'upload' argument.
		respond(handler.handlePut(qs, p, null, req.getInputStream()), resp);
	}

	private String[] getQueryString(HttpServletRequest req) {
		String[] q = new String[] {};

		if (req.getQueryString() != null)
			q = req.getQueryString().split("/");

		return q;
	}

	/**
	 * @param req
	 * @return Properties for the request, or empty properties if null.
	 */
	private Properties getProperties(HttpServletRequest req) {
		Properties p = new Properties();

		if (req.getParameterMap() != null) {
			for (Iterator i = req.getParameterMap().entrySet().iterator(); i.hasNext();) {
				Entry e = (Entry) i.next();
				p.put(e.getKey(), e.getValue());
			}
		}

		return p;
	}

	/**
	 * Given an IResponse and a HttpServletResponse, load the latter with data from the former, and then flush the buffer.
	 * 
	 * @param handlerResponse
	 * @param servletResponse
	 * @throws IOException
	 */
	private void respond(IResponse handlerResponse, HttpServletResponse servletResponse) throws IOException {

		servletResponse.setContentType(handlerResponse.getMimeType());
		servletResponse.setContentLength((int) handlerResponse.getContentLength());

		InputStream input = handlerResponse.getData();
		ServletOutputStream output = servletResponse.getOutputStream();

		byte[] buf = new byte[8192];
		while (true) {
			int length = input.read(buf);
			if (length < 0)
				break;
			output.write(buf, 0, length);
		}

		try {
			input.close();
		} catch (IOException ignore) {
		}
		try {
			output.close();
		} catch (IOException ignore) {
		}
		servletResponse.flushBuffer();
	}
}
