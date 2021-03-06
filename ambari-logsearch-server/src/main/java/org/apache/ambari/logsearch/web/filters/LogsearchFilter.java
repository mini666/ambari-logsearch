/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ambari.logsearch.web.filters;

import static org.apache.ambari.logsearch.util.JSONUtil.toJson;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ambari.logsearch.common.StatusMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class LogsearchFilter implements Filter {

  private static final Logger logger = LogManager.getLogger(LogsearchFilter.class);

  private final RequestMatcher requestMatcher;
  private final StatusProvider statusProvider;

  public LogsearchFilter(RequestMatcher requestMatcher, StatusProvider statusProvider) {
    this(requestMatcher, statusProvider, true);
  }

  public LogsearchFilter(RequestMatcher requestMatcher, StatusProvider statusProvider, boolean enabled) {
    this.requestMatcher = requestMatcher;
    this.statusProvider = statusProvider;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    if (requestMatcher.matches(request)) {
      StatusMessage errorResponse = statusProvider.getStatusMessage(request.getRequestURI());
      if (errorResponse != null) {
        logger.info("{} request is filtered out: {}", request.getRequestURL(), errorResponse.getMessage());
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        resp.setStatus(errorResponse.getStatusCode());
        resp.setContentType("application/json");
        resp.getWriter().print(toJson(errorResponse));
        return;
      }
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
  }
}
