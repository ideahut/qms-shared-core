package com.github.ideahut.qms.shared.core.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;

import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.mapper.DataMapper;

public abstract class BaseResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		try {
			Object entity = responseContext.getEntity();
			if (entity == null || byte[].class.equals(entity.getClass())) {
				return;
			}
			String accept = RequestContext.currentContext().getAccept();
			byte[] data = new byte[0];
			MediaType mediaType;
			if (accept != null && accept.indexOf("xml") != -1) {
				data = dataMapper().writeXmlAsBytes(entity);
				mediaType = MediaType.APPLICATION_XML_TYPE;
			} else {
				data = dataMapper().writeJsonAsBytes(entity);
				mediaType = MediaType.APPLICATION_JSON_TYPE;
			}
			responseContext.setEntity(data, null, mediaType);
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			RequestContext.destroy();
		}		
	}
	
	protected abstract DataMapper dataMapper(); 
	
}
