/*******************************************************************************
 * Copyright 2016 JSL Solucoes LTDA - https://jslsolucoes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.jslsolucoes.nginx.admin.repository.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import com.jslsolucoes.nginx.admin.model.Nginx;
import com.jslsolucoes.nginx.admin.model.SslCertificate;
import com.jslsolucoes.nginx.admin.repository.NginxRepository;
import com.jslsolucoes.nginx.admin.repository.ResourceIdentifierRepository;
import com.jslsolucoes.nginx.admin.repository.SslCertificateRepository;

@RequestScoped
public class SslCertificateRepositoryImpl extends RepositoryImpl<SslCertificate> implements SslCertificateRepository {

	private NginxRepository nginxRepository;
	private ResourceIdentifierRepository resourceIdentifierRepository;

	public SslCertificateRepositoryImpl() {

	}

	@Inject
	public SslCertificateRepositoryImpl(Session session, NginxRepository nginxRepository,
			ResourceIdentifierRepository resourceIdentifierRepository) {
		super(session);
		this.nginxRepository = nginxRepository;
		this.resourceIdentifierRepository = resourceIdentifierRepository;
	}
	
	
	@Override
	public OperationType delete(SslCertificate sslCertificate) {
		try {
			File ssl = nginxRepository.configuration().ssl();
			sslCertificate = load(sslCertificate);
			String sslCertificateHash = sslCertificate.getCertificate();
			String sslCertificatePrivateKeyHash = sslCertificate.getCertificatePrivateKey();
			FileUtils.forceDelete(new File(ssl,sslCertificateHash + ".conf"));
			FileUtils.forceDelete(new File(ssl,sslCertificatePrivateKeyHash + ".conf"));
			super.delete(sslCertificate);
			resourceIdentifierRepository.delete(sslCertificateHash);
			resourceIdentifierRepository.delete(sslCertificatePrivateKeyHash); 
			return OperationType.DELETE;
		} catch(Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public OperationResult saveOrUpdate(SslCertificate sslCertificate, InputStream certificateFile,
			InputStream certificatePrivateKeyFile) throws Exception {
		Nginx nginx = nginxRepository.configuration();
		if (certificateFile != null) {
			if (StringUtils.isEmpty(sslCertificate.getCertificate())) {
				sslCertificate.setCertificate(UUID.randomUUID().toString() + ".cer");
			}
			IOUtils.copy(certificateFile, new FileOutputStream(new File(nginx.ssl(), sslCertificate.getCertificate())));
		}
		if (certificatePrivateKeyFile != null) {
			if (StringUtils.isEmpty(sslCertificate.getCertificatePrivateKey())) {
				sslCertificate.setCertificatePrivateKey(UUID.randomUUID().toString() + ".key");
			}
			IOUtils.copy(certificatePrivateKeyFile,
					new FileOutputStream(new File(nginx.ssl(), sslCertificate.getCertificatePrivateKey())));
		}
		return super.saveOrUpdate(sslCertificate);
	}

	@Override
	public InputStream download(String hash) throws FileNotFoundException {
		Nginx nginx = nginxRepository.configuration();
		return new FileInputStream(new File(nginx.ssl(), hash));
	}
}
