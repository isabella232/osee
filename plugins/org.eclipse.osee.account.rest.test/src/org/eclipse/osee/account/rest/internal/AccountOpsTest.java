/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.account.rest.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.lang.math.RandomUtils;
import org.eclipse.osee.account.admin.AccessDetails;
import org.eclipse.osee.account.admin.Account;
import org.eclipse.osee.account.admin.AccountAdmin;
import org.eclipse.osee.account.admin.AccountLoginRequest;
import org.eclipse.osee.account.admin.AccountPreferences;
import org.eclipse.osee.account.admin.AccountSession;
import org.eclipse.osee.account.admin.CreateAccountRequest;
import org.eclipse.osee.account.rest.model.AccountActiveData;
import org.eclipse.osee.account.rest.model.AccountDetailsData;
import org.eclipse.osee.account.rest.model.AccountInfoData;
import org.eclipse.osee.account.rest.model.AccountInput;
import org.eclipse.osee.account.rest.model.AccountLoginData;
import org.eclipse.osee.account.rest.model.AccountPreferencesData;
import org.eclipse.osee.account.rest.model.AccountPreferencesInput;
import org.eclipse.osee.account.rest.model.AccountSessionData;
import org.eclipse.osee.account.rest.model.AccountSessionDetailsData;
import org.eclipse.osee.framework.jdk.core.type.Identifiable;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link AccountOps}
 * 
 * @author Roberto E. Escobar
 */
public class AccountOpsTest {

   private static final String ACCOUNT_UID = "hello@hello.com";

   //@formatter:off
   @Mock private AccountAdmin accountAdmin;
   //@formatter:on

   private AccountOps ops;

   @Before
   public void setUp() {
      MockitoAnnotations.initMocks(this);

      ops = new AccountOps(accountAdmin);
   }

   @Test
   public void testCreateAccount() {
      String guid = GUID.create();
      String username = "aUser";

      String email = "hello@hello.com";
      String name = "myName";
      boolean active = true;

      Map<String, String> map = new HashMap<String, String>();
      map.put("a", "1");
      map.put("b", "2");
      map.put("c", "3");

      AccountInput input = mock(AccountInput.class);
      when(input.getEmail()).thenReturn(email);
      when(input.getName()).thenReturn(name);
      when(input.isActive()).thenReturn(active);
      when(input.getPreferences()).thenReturn(map);

      ArgumentCaptor<CreateAccountRequest> captor = ArgumentCaptor.forClass(CreateAccountRequest.class);

      @SuppressWarnings("unchecked")
      Identifiable<String> id = mock(Identifiable.class);
      when(id.getGuid()).thenReturn(guid);

      when(accountAdmin.createAccount(any(CreateAccountRequest.class))).thenReturn(id);

      long accountId = 1982671L;
      Account account = mockAccount(accountId, guid, name, email, username, active);
      ResultSet<Account> result = ResultSets.singleton(account);
      when(accountAdmin.getAccountByUuid(guid)).thenReturn(result);

      AccountInfoData actual = ops.createAccount(username, input);

      verify(accountAdmin).createAccount(captor.capture());
      CreateAccountRequest request = captor.getValue();
      assertNotNull(request);
      assertAccount(actual, accountId, guid, name, email, username, active);
      assertEquals(name, request.getDisplayName());
      assertEquals(email, request.getEmail());
      assertEquals(username, request.getUserName());
      assertEquals(active, request.isActive());
      assertEquals(map, request.getPreferences());
   }

   @Test
   public void testDoLogin() {
      String ipAddress = "192.168.100.199";
      String userAgent = "my agent";

      String scheme = "myscheme";
      String username = "myuser";
      String password = "mypass";

      RequestInfo reqInfo = mock(RequestInfo.class);
      when(reqInfo.getDetails()).thenReturn(userAgent);
      when(reqInfo.getRemoteIpAddress()).thenReturn(ipAddress);

      AccountLoginData login = mock(AccountLoginData.class);
      when(login.getScheme()).thenReturn(scheme);
      when(login.getUsername()).thenReturn(username);
      when(login.getPassword()).thenReturn(password);

      Date d1 = newRandomDate();
      Date d2 = newRandomDate();
      AccountSession access = mockAccess(789L, "t3", d1, d2, ipAddress, userAgent);
      ArgumentCaptor<AccountLoginRequest> captor = ArgumentCaptor.forClass(AccountLoginRequest.class);

      when(accountAdmin.login(any(AccountLoginRequest.class))).thenReturn(access);

      AccountSessionData actual = ops.doLogin(reqInfo, login);

      verify(accountAdmin).login(captor.capture());
      AccountLoginRequest request = captor.getValue();
      AccessDetails details = request.getDetails();
      assertNotNull(details);
      assertEquals(789L, actual.getAccountId());
      assertEquals("t3", actual.getToken());

      assertEquals(scheme, request.getScheme());
      assertEquals(username, request.getUserName());
      assertEquals(password, request.getPassword());

      assertEquals(userAgent, details.getAccessDetails());
      assertEquals(ipAddress, details.getRemoteAddress());
   }

   @Test
   public void testDoLogout() {
      String token = "asdasa";

      when(accountAdmin.logout(token)).thenReturn(true);

      boolean actual = ops.doLogout(token);

      assertEquals(true, actual);
      verify(accountAdmin).logout(token);
   }

   @Test
   public void testDeleteAccount() {
      when(accountAdmin.deleteAccount(ACCOUNT_UID)).thenReturn(true);

      boolean actual = ops.deleteAccount(ACCOUNT_UID);

      assertEquals(true, actual);
      verify(accountAdmin).deleteAccount(ACCOUNT_UID);
   }

   @Test
   public void testAsRequestInfo() {
      String ipAddress = "192.168.100.199";
      String userAgent = "my agent";

      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(userAgent);
      when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(ipAddress);

      RequestInfo actual = ops.asRequestInfo(request);

      assertEquals(userAgent, actual.getDetails());
      assertEquals(ipAddress, actual.getRemoteIpAddress());
   }

   @Test
   public void testGetAccountAccessById() {
      Date d1 = newRandomDate();
      Date d2 = newRandomDate();
      Date d3 = newRandomDate();
      Date d4 = newRandomDate();
      Date d5 = newRandomDate();
      Date d6 = newRandomDate();

      AccountSession access1 = mockAccess(123L, "t1", d1, d2, "f1", "d1");
      AccountSession access2 = mockAccess(456L, "t2", d3, d4, "f2", "d2");
      AccountSession access3 = mockAccess(789L, "t3", d5, d6, "f3", "d3");

      ResultSet<AccountSession> result = ResultSets.newResultSet(access1, access2, access3);
      when(accountAdmin.getAccountSessionByUniqueField(ACCOUNT_UID)).thenReturn(result);

      List<AccountSessionDetailsData> actual = ops.getAccountSessionById(ACCOUNT_UID);

      assertEquals(3, actual.size());
      Iterator<AccountSessionDetailsData> iterator = actual.iterator();
      assertAccess(iterator.next(), 123L, d1, d2, "f1", "d1");
      assertAccess(iterator.next(), 456L, d3, d4, "f2", "d2");
      assertAccess(iterator.next(), 789L, d5, d6, "f3", "d3");

      verify(accountAdmin).getAccountSessionByUniqueField(ACCOUNT_UID);
   }

   @Test
   public void testGetAccountData() {
      Account account = mockAccount(456L, "DEF", "acc2", "acc2@email.com", "u2", true);
      ResultSet<Account> accounts = ResultSets.singleton(account);
      when(accountAdmin.getAccountByUniqueField(ACCOUNT_UID)).thenReturn(accounts);

      AccountInfoData actual = ops.getAccountData(ACCOUNT_UID);

      assertAccount(actual, 456L, "DEF", "acc2", "acc2@email.com", "u2", true);
      verify(accountAdmin).getAccountByUniqueField(ACCOUNT_UID);
   }

   @Test
   public void testGetAccountDetailsData() {
      Account account = mockAccount(789L, "GHI", "acc3", "acc3@email.com", "u3", true);
      Map<String, String> map = new HashMap<String, String>();
      map.put("a", "1");
      map.put("b", "2");
      map.put("c", "3");

      AccountPreferences preferences = mock(AccountPreferences.class);
      when(preferences.asMap()).thenReturn(map);
      when(preferences.getId()).thenReturn(789L);
      when(account.getPreferences()).thenReturn(preferences);

      ResultSet<Account> accounts = ResultSets.singleton(account);
      when(accountAdmin.getAccountByUniqueField(ACCOUNT_UID)).thenReturn(accounts);

      AccountDetailsData actual = ops.getAccountDetailsData(ACCOUNT_UID);

      assertAccount(actual, 789L, "GHI", "acc3", "acc3@email.com", "u3", true);
      AccountPreferencesData actualPrefs = actual.getPreferences();
      Map<String, String> actualMap = actualPrefs.getMap();

      assertEquals(789L, actualPrefs.getId());
      assertEquals(3, actualMap.size());
      assertEquals("1", actualMap.get("a"));
      assertEquals("2", actualMap.get("b"));
      assertEquals("3", actualMap.get("c"));

      verify(accountAdmin).getAccountByUniqueField(ACCOUNT_UID);
   }

   @Test
   public void testGetAccountPreferencesData() {
      Map<String, String> map = new HashMap<String, String>();
      map.put("a", "1");
      map.put("b", "2");
      map.put("c", "3");

      AccountPreferences preferences = mock(AccountPreferences.class);
      when(preferences.asMap()).thenReturn(map);
      when(preferences.getId()).thenReturn(123L);

      Account account = mock(Account.class);
      when(account.getPreferences()).thenReturn(preferences);

      ResultSet<Account> accounts = ResultSets.singleton(account);
      when(accountAdmin.getAccountByUniqueField(ACCOUNT_UID)).thenReturn(accounts);

      AccountPreferencesData actual = ops.getAccountPreferencesData(ACCOUNT_UID);
      assertEquals(123L, actual.getId());

      Map<String, String> actualPrefs = actual.getMap();
      assertEquals(3, actualPrefs.size());
      assertEquals("1", actualPrefs.get("a"));
      assertEquals("2", actualPrefs.get("b"));
      assertEquals("3", actualPrefs.get("c"));

      verify(accountAdmin).getAccountByUniqueField(ACCOUNT_UID);
   }

   @Test
   public void testGetAllAccounts() {
      Account account1 = mockAccount(123L, "ABC", "acc1", "acc1@email.com", "u1", true);
      Account account2 = mockAccount(456L, "DEF", "acc2", "acc2@email.com", "u2", false);
      Account account3 = mockAccount(789L, "GHI", "acc3", "acc3@email.com", "u3", true);

      ResultSet<Account> accounts = ResultSets.newResultSet(account1, account2, account3);

      when(accountAdmin.getAllAccounts()).thenReturn(accounts);

      List<AccountInfoData> actual = ops.getAllAccounts();

      assertEquals(3, actual.size());
      Iterator<AccountInfoData> iterator = actual.iterator();

      assertAccount(iterator.next(), 123L, "ABC", "acc1", "acc1@email.com", "u1", true);
      assertAccount(iterator.next(), 456L, "DEF", "acc2", "acc2@email.com", "u2", false);
      assertAccount(iterator.next(), 789L, "GHI", "acc3", "acc3@email.com", "u3", true);

      verify(accountAdmin).getAllAccounts();
   }

   @Test
   public void testIsActive() {
      String guid = GUID.create();
      long accountId = 23127916023214L;

      Account account = mock(Account.class);
      ResultSet<Account> result = ResultSets.singleton(account);

      when(account.getGuid()).thenReturn(guid);
      when(account.getId()).thenReturn(accountId);
      when(account.isActive()).thenReturn(true);
      when(accountAdmin.getAccountByUniqueField(ACCOUNT_UID)).thenReturn(result);

      AccountActiveData actual = ops.isActive(ACCOUNT_UID);

      assertEquals(accountId, actual.getAccountId());
      assertEquals(guid, actual.getGuid());
      assertEquals(true, actual.isActive());
      verify(accountAdmin).getAccountByUniqueField(ACCOUNT_UID);
   }

   @Test
   public void testSetAccountActive() {
      when(accountAdmin.setActive(ACCOUNT_UID, true)).thenReturn(true);

      boolean actual = ops.setAccountActive(ACCOUNT_UID, true);

      assertEquals(true, actual);
      verify(accountAdmin).setActive(ACCOUNT_UID, true);
   }

   @Test
   public void testSetAccountPreferences() {
      Map<String, String> map = new HashMap<String, String>();
      map.put("a", "1");
      map.put("b", "2");
      map.put("c", "3");

      AccountPreferencesInput input = mock(AccountPreferencesInput.class);
      when(input.getMap()).thenReturn(map);
      when(accountAdmin.setAccountPreferences(ACCOUNT_UID, map)).thenReturn(true);

      boolean actual = ops.setAccountPreferences(ACCOUNT_UID, input);

      assertEquals(true, actual);
      verify(accountAdmin).setAccountPreferences(ACCOUNT_UID, map);
   }

   private static Date newRandomDate() {
      return new Date(Math.abs(System.currentTimeMillis() - RandomUtils.nextLong()));
   }

   private static AccountSession mockAccess(long id, String token, Date created, Date accessed, String accessFrom, String accessDetails) {
      AccountSession access = Mockito.mock(AccountSession.class);
      when(access.getAccountId()).thenReturn(id);
      when(access.getSessionToken()).thenReturn(token);
      when(access.getCreatedOn()).thenReturn(created);
      when(access.getLastAccessedOn()).thenReturn(accessed);
      when(access.getAccessedFrom()).thenReturn(accessFrom);
      when(access.getAccessDetails()).thenReturn(accessDetails);
      return access;
   }

   private static void assertAccess(AccountSessionDetailsData actual, long id, Date created, Date accessed, String accessFrom, String accessDetails) {
      assertEquals(accessDetails, actual.getAccessDetails());
      assertEquals(accessFrom, actual.getAccessedFrom());
      assertEquals(id, actual.getAccountId());
      assertEquals(created, actual.getCreatedOn());
      assertEquals(accessed, actual.getLastAccessedOn());
   }

   private static Account mockAccount(long id, String uuid, String name, String email, String username, boolean active) {
      Account account = Mockito.mock(Account.class);
      when(account.getId()).thenReturn(id);
      when(account.getGuid()).thenReturn(uuid);
      when(account.getName()).thenReturn(name);
      when(account.getEmail()).thenReturn(email);
      when(account.getUserName()).thenReturn(username);
      when(account.isActive()).thenReturn(active);
      return account;
   }

   private static void assertAccount(AccountInfoData data, long id, String uuid, String name, String email, String username, boolean active) {
      assertEquals(id, data.getAccountId());
      assertEquals(uuid, data.getGuid());
      assertEquals(name, data.getName());
      assertEquals(email, data.getEmail());
      assertEquals(username, data.getUserName());
      assertEquals(active, data.isActive());
   }
}
