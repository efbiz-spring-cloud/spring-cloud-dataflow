/*
 * Copyright 2016 the original author or authors.
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
 */
package org.springframework.cloud.dataflow.server.local.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.cloud.dataflow.server.local.LocalDataflowResource;

import static org.springframework.cloud.dataflow.server.local.security.SecurityTestUtils.basicAuthorizationHeader;
/**
 * @author Marius Bogoevici
 * @author Gunnar Hillert
 */
public class LocalServerSecurityWithLdapSearchAndBindSslTests {

	private final static LocalDataflowResource localDataflowResource =
		new LocalDataflowResource("classpath:org/springframework/cloud/dataflow/server/local/security/ldapSslSearchAndBind.yml");

	@ClassRule
	public static TestRule springDataflowAndLdapServer = RuleChain
			.outerRule(new LdapServerResource(true))
			.around(localDataflowResource);

	@Test
	public void testUnauthenticatedAccessToModulesEndpointFails() throws Exception {
		localDataflowResource.getMockMvc()
				.perform(get("/apps"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void testUnauthenticatedAccessToManagementEndpointFails() throws Exception {
		localDataflowResource.getMockMvc()
				.perform(get("/management/metrics"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void testAuthenticatedAccessToModulesEndpointSucceeds() throws Exception {
		localDataflowResource.getMockMvc()
				.perform(get("/apps").header("Authorization", basicAuthorizationHeader("joe", "joespassword")))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void testUserExistsButNotFoundBySearch() throws Exception {
		localDataflowResource.getMockMvc()
				.perform(get("/apps").header("Authorization", basicAuthorizationHeader("bob", "bobspassword")))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void testAuthenticatedAccessToManagementEndpointSucceeds() throws Exception {
		localDataflowResource.getMockMvc()
				.perform(get("/management/metrics").header("Authorization", basicAuthorizationHeader("joe", "joespassword")))
				.andDo(print())
				.andExpect(status().isOk());
	}
}
