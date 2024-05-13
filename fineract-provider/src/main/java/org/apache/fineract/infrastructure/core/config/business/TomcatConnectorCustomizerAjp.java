/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.config.business;

import org.springframework.context.annotation.Configuration;

//@Component
//@ConditionalOnClass(name = "org.apache.coyote.ajp.AjpNioProtocol")
@Configuration
public class TomcatConnectorCustomizerAjp //implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> 
{

//    @Override
//    public void customize(ConfigurableServletWebServerFactory factory) {
//        ((TomcatServletWebServerFactory) factory).setProtocol("org.apache.coyote.ajp.AjpNioProtocol");
//        ((TomcatServletWebServerFactory) factory).setPort(8009);
//    }
//    @Bean
//    public ConfigurableServletWebServerFactory webServerFactory() {
//        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
//
////        Connector connector = new Connector("AJP/1.3");
////        connector.setPort(8009);
////        connector.setRedirectPort(8443);
////        connector.setSecure(false);
////        connector.setAllowTrace(false);
////        connector.setProperty("address", "10.2.3.33");
////        connector.setProperty("allowedRequestAttributesPattern", ".*");
////        ((AbstractAjpProtocol) connector.getProtocolHandler()).setSecretRequired(false);
//        Connector connector = new Connector("org.apache.coyote.ajp.AjpNioProtocol");
//        ((AbstractAjpProtocol) connector.getProtocolHandler()).setSecretRequired(false);
//        //connector.setAttribute("maxThreads", 100);
//        connector.setPort(8009);
//        connector.setRedirectPort(8443);
//        connector.setURIEncoding("UTF-8");
//        connector.setScheme("https");
//        connector.setSecure(false);
////        connector.setProperty("address", "10.2.3.33");
//        connector.setProperty("allowedRequestAttributesPattern", ".*");
//        factory.addAdditionalTomcatConnectors(connector);
//        return factory;
//    }
}
