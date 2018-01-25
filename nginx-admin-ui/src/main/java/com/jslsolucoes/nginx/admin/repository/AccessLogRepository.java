package com.jslsolucoes.nginx.admin.repository;

import java.util.List;

import com.jslsolucoes.nginx.admin.model.AccessLog;
import com.jslsolucoes.nginx.admin.model.Nginx;

public interface AccessLogRepository {

	public void log(AccessLog accessLog);
	
	public List<AccessLog> listAllFor(Nginx nginx,Integer firstResult,Integer maxResults);

	public Long countFor(Nginx nginx);
}
