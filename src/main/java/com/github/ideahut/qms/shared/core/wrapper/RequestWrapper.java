package com.github.ideahut.qms.shared.core.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

public class RequestWrapper extends HttpServletRequestWrapper {
	
	private Map<String, List<String>> parameters;
	private Map<String, List<String>> headers;
	private Map<String, Long> dateHeaders;
	private byte[] bytes;

	public RequestWrapper(HttpServletRequest request) {
		super(request);
		parameters = new HashMap<String, List<String>>();
		Enumeration<String> en = request.getParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			String[] values = request.getParameterValues(name);
			if (values != null) {
				List<String> list = new ArrayList<String>();
				for (String v : values) {
					list.add(v);
				}
				parameters.put(name, list);
			} else {
				parameters.put(name, null);
			}
		}
		dateHeaders = new HashMap<String, Long>();
		headers = new HashMap<String, List<String>>();
		en = request.getHeaderNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			Enumeration<String> values = request.getHeaders(name);
			if (values != null) {
				try {
					dateHeaders.put(name, request.getDateHeader(name));
				} catch (Exception e) {
					dateHeaders.put(name, -2L);
				}
				List<String> list = new ArrayList<String>();
				while (values.hasMoreElements()) {
					list.add(values.nextElement());
				}
				headers.put(name, list);
			} else {
				headers.put(name, null);
			}
		}
		try {
			//bytes = IOUtils.toByteArray(request.getReader(), "UTF-8");
			bytes = IOUtils.toByteArray(request.getInputStream());
		} catch (IOException e) {
			bytes = new byte[0];
		}
	}
	
	@Override
    public long getDateHeader(String name) {
        Long value = dateHeaders.get(name);
        if (value == null) {
        	return -1L;
        }
        if (value == -2L) {
        	throw new IllegalArgumentException();
        }
        return value;
    }

	@Override
	public String getHeader(String name) {
		List<String> values = headers.get(name);
		return values != null && values.size() != 0 ? values.get(0) : null;
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		List<String> values = headers.get(name);
		return values != null ? Collections.enumeration(values) : null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public int getIntHeader(String name) {
		String value = getHeader(name);
        if (value == null) {
            return -1;
        }
        return Integer.parseInt(value);
	}
	
	public void setHeader(String name, String[] values) {
		headers.put(name, values != null ? Arrays.asList(values) : null);
	}
	
	public void setHeader(String name, String value) {
		setHeader(name, new String[] { value });
	}
	
	public void addHeader(String name, String[] values) {
		List<String> list = headers.get(name);
		if (list == null) {
			list = new ArrayList<String>();
		}
		for (String v : values) {
			list.add(v);
		}
		headers.put(name, list);
	}
	
	public void addHeader(String name, String value) {
		addHeader(name, new String[] { value });
	}
	
	public void removeHeader(String name) {
		headers.remove(name);
	}
	
	
	@Override
	public String getParameter(String name) {
		List<String> values = parameters.get(name);
		return values != null && values.size() != 0 ? values.get(0) : null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (String key : parameters.keySet()) {
			List<String> values = parameters.get(key);
			map.put(key, values != null ? values.toArray(new String[0]) : null);
		}
		return Collections.unmodifiableMap(map);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		List<String> values = parameters.get(name);
		return values != null ? values.toArray(new String[0]) : null;
	}
	
	public void setParameter(String name, String[] values) {
		parameters.put(name, values != null ? Arrays.asList(values) : null);
	}
	
	public void setParameter(String name, String value) {
		setParameter(name, new String[] { value });
	}
	
	public void addParameter(String name, String[] values) {
		List<String> list = parameters.get(name);
		if (list == null) {
			list = new ArrayList<String>();
		}
		for (String v : values) {
			list.add(v);
		}
		parameters.put(name, list);
	}
	
	public void addParameter(String name, String value) {
		addParameter(name, new String[] { value });
	}
	
	public void removeParameter(String name) {
		parameters.remove(name);
	}
	

	@Override
	public ServletInputStream getInputStream() throws IOException {
		ServletInputStream stream = new ServletInputStream() {
        	private int lastIndexRetrieved = -1;
            private ReadListener readListener = null;            
            
            @Override
            public int read() throws IOException {
            	int i;
                if (!isFinished()) {
                    i = bytes[lastIndexRetrieved + 1];
                    lastIndexRetrieved++;
                    if (isFinished() && (readListener != null)) {
                        try {
                            readListener.onAllDataRead();
                        } catch (IOException ex) {
                            readListener.onError(ex);
                            throw ex;
                        }
                    }
                    return i;
                } else {
                    return -1;
                }
            }

			@Override
			public boolean isFinished() {
				return lastIndexRetrieved == (bytes.length - 1);
			}

			@Override
			public boolean isReady() {
				return isFinished();
			}

			@Override
			public void setReadListener(ReadListener listener) {
				this.readListener = listener;
	            if (!isFinished()) {
	                try {
	                    readListener.onDataAvailable();
	                } catch (IOException e) {
	                    readListener.onError(e);
	                }
	            } else {
	                try {
	                    readListener.onAllDataRead();
	                } catch (IOException e) {
	                    readListener.onError(e);
	                }
	            }
			}
        };
        return stream;        
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
	}

	public byte[] getBytes() {
		return bytes;
	}
	
}
