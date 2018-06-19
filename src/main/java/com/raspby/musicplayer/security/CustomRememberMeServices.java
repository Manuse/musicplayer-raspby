package com.raspby.musicplayer.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.raspby.musicplayer.persistence.dto.TokenDTO;
import com.raspby.musicplayer.persistence.dto.UsersDTO;
import com.raspby.musicplayer.persistence.repository.TokenRepository;
import com.raspby.musicplayer.persistence.repository.UsersRepository;

import org.apache.commons.lang3.time.DateUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Arrays;
//import java.util.Base64;
import java.util.Date;


import static com.raspby.musicplayer.config.WebSecurityConfig.REMEMBER_ME_KEY;

@Service
public class CustomRememberMeServices implements RememberMeServices, InitializingBean, LogoutHandler {

	// ~ Static fields/initializers
	// =====================================================================================

	public static final String SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY = "remember-me";
	public static final String DEFAULT_PARAMETER = "rememberme";
	public static final int TWO_WEEKS_S = 1209600;

	private static final String DELIMITER = ":";

	// ~ Instance fields
	// ================================================================================================

	// Token is valid for one month
	
	private static final int TOKEN_VALIDITY_DAYS = 12;

	private static final int TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS;

	private static final int DEFAULT_SERIES_LENGTH = 16;

	private static final int DEFAULT_TOKEN_LENGTH = 16;

	protected final MessageSourceAccessor messages = SpringSecurityMessageSource
			.getAccessor();

	private UserDetailsService userDetailsService;
	private static UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();
	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

	private String cookieName = SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;
	private String cookieDomain;
	@Value(DEFAULT_PARAMETER)
	private String parameter;
	private boolean alwaysRemember;
	private String key;
	private int tokenValiditySeconds = TWO_WEEKS_S;
	private Boolean useSecureCookie = null;
	private Method setHttpOnlyMethod;
	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	
	private SecureRandom random;

	@Autowired
	private TokenRepository tokenRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	public CustomRememberMeServices(Environment env, UserDetailsService userDetailsService) {
		this.userDetailsService=userDetailsService;
		this.key=REMEMBER_ME_KEY;
		this.parameter=DEFAULT_PARAMETER;
		random = new SecureRandom();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(key, "key cannot be empty or null");
		Assert.notNull(userDetailsService, "A UserDetailsService is required");
	}
	
	/**
	 * Template implementation which locates the Spring Security cookie, decodes it into a
	 * delimited array of tokens and submits it to subclasses for processing via the
	 * <tt>processAutoLoginCookie</tt> method.
	 * <p>
	 * The returned username is then used to load the UserDetails object for the user,
	 * which in turn is used to create a valid authentication token.
	 */
	public final Authentication autoLogin(HttpServletRequest request,
			HttpServletResponse response) {
		String rememberMeCookie = extractRememberMeCookie(request);

		if (rememberMeCookie == null) {
			return null;
		}

		//logger.debug("Remember-me cookie detected");

		if (rememberMeCookie.length() == 0) {
		//	logger.debug("Cookie was empty");
			cancelCookie(request, response);
			return null;
		}

		UserDetails user = null;

		try {
			String[] cookieTokens = decodeCookie(rememberMeCookie);
			user = processAutoLoginCookie(cookieTokens, request, response);
			userDetailsChecker.check(user);
			
		//	logger.debug("Remember-me cookie accepted");

			return createSuccessfulAuthentication(request, user);
		}
		catch (CookieTheftException cte) {
			cancelCookie(request, response);
			throw cte;
		}
		catch (UsernameNotFoundException noUser) {
		//	logger.debug("Remember-me login was valid but corresponding user not found.",noUser);
		}
		catch (InvalidCookieException invalidCookie) {
		//	logger.debug("Invalid remember-me cookie: " + invalidCookie.getMessage());
		}
		catch (AccountStatusException statusInvalid) {
		//	logger.debug("Invalid UserDetails: " + statusInvalid.getMessage());
		}
		catch (RememberMeAuthenticationException e) {
		//	logger.debug(e.getMessage());
		}

		cancelCookie(request, response);
		return null;
	}


	/**
	 * Locates the Spring Security remember me cookie in the request and returns its
	 * value. The cookie is searched for by name and also by matching the context path to
	 * the cookie path.
	 *
	 * @param request the submitted request which is to be authenticated
	 * @return the cookie value (if present), null otherwise.
	 */
	protected String extractRememberMeCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();

		if ((cookies == null) || (cookies.length == 0)) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}

		return null;
	}

	/**
	 * Creates the final <tt>Authentication</tt> object returned from the
	 * <tt>autoLogin</tt> method.
	 * <p>
	 * By default it will create a <tt>RememberMeAuthenticationToken</tt> instance.
	 *
	 * @param request the original request. The configured
	 * <tt>AuthenticationDetailsSource</tt> will use this to build the details property of
	 * the returned object.
	 * @param user the <tt>UserDetails</tt> loaded from the <tt>UserDetailsService</tt>.
	 * This will be stored as the principal.
	 *
	 * @return the <tt>Authentication</tt> for the remember-me authenticated user
	 */
	protected Authentication createSuccessfulAuthentication(HttpServletRequest request,
			UserDetails user) {
		RememberMeAuthenticationToken auth = new RememberMeAuthenticationToken(key, user,
				authoritiesMapper.mapAuthorities(user.getAuthorities()));
		auth.setDetails(authenticationDetailsSource.buildDetails(request));
		return auth;
	}
	
	/**
	 * Decodes the cookie and splits it into a set of token strings using the ":"
	 * delimiter.
	 *
	 * @param cookieValue the value obtained from the submitted cookie
	 * @return the array of tokens.
	 * @throws InvalidCookieException if the cookie was not base64 encoded.
	 */
	protected String[] decodeCookie(String cookieValue) throws InvalidCookieException {
		for (int j = 0; j < cookieValue.length() % 4; j++) {
			cookieValue = cookieValue + "=";
		}

		try {
			Base64Utils.decode(cookieValue.getBytes());
		}
		catch (IllegalArgumentException e) {
			throw new InvalidCookieException(
					"Cookie token was not Base64 encoded; value was '" + cookieValue
							+ "'");
		}

		String cookieAsPlainText = new String(Base64Utils.decode(cookieValue.getBytes()));

		String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText,
				DELIMITER);

		if ((tokens[0].equalsIgnoreCase("http") || tokens[0].equalsIgnoreCase("https"))
				&& tokens[1].startsWith("//")) {
			// Assume we've accidentally split a URL (OpenID identifier)
			String[] newTokens = new String[tokens.length - 1];
			newTokens[0] = tokens[0] + ":" + tokens[1];
			System.arraycopy(tokens, 2, newTokens, 1, newTokens.length - 1);
			tokens = newTokens;
		}

		return tokens;
	}
	
	/**
	 * Inverse operation of decodeCookie.
	 *
	 * @param cookieTokens the tokens to be encoded.
	 * @return base64 encoding of the tokens concatenated with the ":" delimiter.
	 */
	protected String encodeCookie(String[] cookieTokens) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cookieTokens.length; i++) {
			sb.append(cookieTokens[i]);

			if (i < cookieTokens.length - 1) {
				sb.append(DELIMITER);
			}
		}

		String value = sb.toString();

		sb = new StringBuilder(new String(Base64Utils.encode(value.getBytes())));

		while (sb.charAt(sb.length() - 1) == '=') {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}
	
	/**
	 * Allows customization of whether a remember-me login has been requested. The default
	 * is to return true if <tt>alwaysRemember</tt> is set or the configured parameter
	 * name has been included in the request and is set to the value "true".
	 *
	 * @param request the request submitted from an interactive login, which may include
	 * additional information indicating that a persistent login is desired.
	 * @param parameter the configured remember-me parameter name.
	 *
	 * @return true if the request includes information indicating that a persistent login
	 * has been requested.
	 */
	protected boolean rememberMeRequested(HttpServletRequest request) {
		if (alwaysRemember) {
			return true;
		}

		String paramValue = request.getParameter(parameter);
		if (paramValue != null) {
			if (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on")
					|| paramValue.equalsIgnoreCase("yes") || paramValue.equals("1")) {
				return true;
			}
		}

//		if (logger.isDebugEnabled()) {
//			logger.debug("Did not send remember-me cookie (principal did not set parameter '"
//					+ parameter + "')");
//		}

		return false;
	}

	/**
	 * Sets a "cancel cookie" (with maxAge = 0) on the response to disable persistent
	 * logins.
	 */
	protected void cancelCookie(HttpServletRequest request, HttpServletResponse response) {
	//	logger.debug("Cancelling cookie");
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		cookie.setPath(getCookiePath(request));
		if (cookieDomain != null) {
			cookie.setDomain(cookieDomain);
		}
		response.addCookie(cookie);
	}

	/**
	 * Sets the cookie on the response.
	 *
	 * By default a secure cookie will be used if the connection is secure. You can set
	 * the {@code useSecureCookie} property to {@code false} to override this. If you set
	 * it to {@code true}, the cookie will always be flagged as secure. If Servlet 3.0 is
	 * used, the cookie will be marked as HttpOnly.
	 *
	 * @param tokens the tokens which will be encoded to make the cookie value.
	 * @param maxAge the value passed to {@link Cookie#setMaxAge(int)}
	 * @param request the request
	 * @param response the response to add the cookie to.
	 */
	protected void setCookie(String[] tokens, int maxAge, HttpServletRequest request,
			HttpServletResponse response) {
		String cookieValue = encodeCookie(tokens);
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(maxAge);
		cookie.setPath(getCookiePath(request));
		if (cookieDomain != null) {
			cookie.setDomain(cookieDomain);
		}
		if (maxAge < 1) {
			cookie.setVersion(1);
		}

		if (useSecureCookie == null) {
			cookie.setSecure(request.isSecure());
		}
		else {
			cookie.setSecure(useSecureCookie);
		}

		if (setHttpOnlyMethod != null) {
			ReflectionUtils.invokeMethod(setHttpOnlyMethod, cookie, Boolean.TRUE);
		}
//		else if (logger.isDebugEnabled()) {
//			logger.debug("Note: Cookie will not be marked as HttpOnly because you are not using Servlet 3.0 (Cookie#setHttpOnly(boolean) was not found).");
//		}

		response.addCookie(cookie);
	}

	public void setCookieName(String cookieName) {
		Assert.hasLength(cookieName, "Cookie name cannot be empty or null");
		this.cookieName = cookieName;
	}

	public void setCookieDomain(String cookieDomain) {
		Assert.hasLength(cookieDomain, "Cookie domain cannot be empty or null");
		this.cookieDomain = cookieDomain;
	}

	protected String getCookieName() {
		return cookieName;
	}

	public void setAlwaysRemember(boolean alwaysRemember) {
		this.alwaysRemember = alwaysRemember;
	}

	/**
	 * Sets the name of the parameter which should be checked for to see if a remember-me
	 * has been requested during a login request. This should be the same name you assign
	 * to the checkbox in your login form.
	 *
	 * @param parameter the HTTP request parameter
	 */
	public void setParameter(String parameter) {
		Assert.hasText(parameter, "Parameter name cannot be empty or null");
		this.parameter = parameter;
	}

	public String getParameter() {
		return parameter;
	}

	protected UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public String getKey() {
		return key;
	}

	public void setTokenValiditySeconds(int tokenValiditySeconds) {
		this.tokenValiditySeconds = tokenValiditySeconds;
	}

	protected int getTokenValiditySeconds() {
		return tokenValiditySeconds;
	}

	/**
	 * Whether the cookie should be flagged as secure or not. Secure cookies can only be
	 * sent over an HTTPS connection and thus cannot be accidentally submitted over HTTP
	 * where they could be intercepted.
	 * <p>
	 * By default the cookie will be secure if the request is secure. If you only want to
	 * use remember-me over HTTPS (recommended) you should set this property to
	 * {@code true}.
	 *
	 * @param useSecureCookie set to {@code true} to always user secure cookies,
	 * {@code false} to disable their use.
	 */
	public void setUseSecureCookie(boolean useSecureCookie) {
		this.useSecureCookie = useSecureCookie;
	}

	protected AuthenticationDetailsSource<HttpServletRequest, ?> getAuthenticationDetailsSource() {
		return authenticationDetailsSource;
	}

	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
		Assert.notNull(authenticationDetailsSource,
				"AuthenticationDetailsSource cannot be null");
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	/**
	 * Sets the strategy to be used to validate the {@code UserDetails} object obtained
	 * for the user when processing a remember-me cookie to automatically log in a user.
	 *
	 * @param userDetailsChecker the strategy which will be passed the user object to
	 * allow it to be rejected if account should not be allowed to authenticate (if it is
	 * locked, for example). Defaults to a {@code AccountStatusUserDetailsChecker}
	 * instance.
	 *
	 */
	public static void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
		CustomRememberMeServices.userDetailsChecker = userDetailsChecker;
	}

	public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
		this.authoritiesMapper = authoritiesMapper;
	}
	private String getCookiePath(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		return contextPath.length() > 0 ? contextPath : "/";
	}
	
	@Transactional
	protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
			HttpServletResponse response) {

		TokenDTO token = getPersistentToken(cookieTokens);
		String login = token.getUserLogin();

		// Token also matches, so login is valid. Update the token value, keeping the
		// *same* series number.
		// log.debug("Refreshing persistent login token for user '{}', series '{}'",
		// login, token.getSeries());
		token.setDate(new Date());
		token.setValue(generateTokenData());
		token.setIpAddress(request.getRemoteAddr());
		token.setUserAgent(request.getHeader("User-Agent"));
		try {
			tokenRepository.save(token);
			addCookie(token, request, response);
		} catch (DataAccessException e) {
			// log.error("Failed to update token: ", e);
			throw new RememberMeAuthenticationException("Autologin failed due to data access problem", e);
		}
		return getUserDetailsService().loadUserByUsername(login);
	}


	protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication successfulAuthentication) {
		String username = successfulAuthentication.getName();

		// log.debug("Creating new persistent login for user {}", login);
		UsersDTO user = usersRepository.findByUsername(username);
		TokenDTO token = new TokenDTO();
		token.setSeries(generateSeriesData());
		token.setUserLogin(user.getUsername());
		token.setValue(generateTokenData());
		token.setDate(new Date());
		token.setIpAddress(request.getRemoteAddr());
		token.setUserAgent(request.getHeader("User-Agent"));
		try {
			tokenRepository.save(token);
			addCookie(token, request, response);
		} catch (DataAccessException e) {
			// log.error("Failed to save persistent token ", e);
			System.err.println("error");
			e.printStackTrace();
		}
	}

	/**
	 * When logout occurs, only invalidate the current token, and not all user
	 * sessions.
	 * <p/>
	 * The standard Spring Security implementations are too basic: they invalidate
	 * all tokens for the current user, so when he logs out from one browser, all
	 * his other sessions are destroyed.
	 */
	@Override
	@Transactional
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		String rememberMeCookie = extractRememberMeCookie(request);
		if (rememberMeCookie != null && rememberMeCookie.length() != 0) {
			try {
				String[] cookieTokens = decodeCookie(rememberMeCookie);
				TokenDTO token = getPersistentToken(cookieTokens);
				tokenRepository.deleteById(token.getSeries());
			} catch (InvalidCookieException ice) {
				// log.info("Invalid cookie, no persistent token could be deleted");
				System.err.println("Invalid cookie, no persistent token could be deleted");
			} catch (RememberMeAuthenticationException rmae) {
				// log.debug("No persistent token found, so no token could be deleted");
				System.err.println("No persistent token found, so no token could be deleted");
			}
		}
//		if (logger.isDebugEnabled()) {
//			logger.debug("Logout of user "
//					+ (authentication == null ? "Unknown" : authentication.getName()));
//		}
		cancelCookie(request, response);
	}

	/**
	 * Validate the token and return it.
	 */
	private TokenDTO getPersistentToken(String[] cookieTokens) {
		if (cookieTokens.length != 2) {
			throw new InvalidCookieException("Cookie token did not contain " + 2 + " tokens, but contained '"
					+ Arrays.asList(cookieTokens) + "'");
		}

		final String presentedSeries = cookieTokens[0];
		final String presentedToken = cookieTokens[1];

		TokenDTO token = null;
		try {
			token = tokenRepository.findById(presentedSeries).get();
		} catch (DataAccessException e) {
			// log.error("Error to access database", e );
			System.err.println("Error to access database");
		}

		if (token == null) {
			// No series match, so we can't authenticate using this cookie
			throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
		}

		// We have a match for this user/series combination
		// log.info("presentedToken={} / tokenValue={}", presentedToken,
		// token.getValue());
		if (!presentedToken.equals(token.getValue())) {
			// Token doesn't match series value. Delete this session and throw an exception.
			tokenRepository.deleteById(token.getSeries());
			throw new CookieTheftException(
					"Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.");
		}

		if (DateUtils.addDays(token.getDate(), TOKEN_VALIDITY_DAYS).before(new Date())) {
			tokenRepository.deleteById(token.getSeries());
			throw new RememberMeAuthenticationException("Remember-me login has expired");
		}
		return token;
	}

	private String generateSeriesData() {
		byte[] newSeries = new byte[DEFAULT_SERIES_LENGTH];
		random.nextBytes(newSeries);
		return new String(Base64Utils.encode(newSeries));
	}

	private String generateTokenData() {
		byte[] newToken = new byte[DEFAULT_TOKEN_LENGTH];
		random.nextBytes(newToken);
		return new String(Base64Utils.encode(newToken));
	}

	private void addCookie(TokenDTO token, HttpServletRequest request, HttpServletResponse response) {
		setCookie(new String[] { token.getSeries(), token.getValue() }, TOKEN_VALIDITY_SECONDS, request, response);
	}


	@Override
	public void loginFail(HttpServletRequest request, HttpServletResponse response) {
		cancelCookie(request, response);
		onLoginFail(request, response);
	}

	protected void onLoginFail(HttpServletRequest request, HttpServletResponse response) {
	
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Examines the incoming request and checks for the presence of the configured
	 * "remember me" parameter. If it's present, or if <tt>alwaysRemember</tt> is set to
	 * true, calls <tt>onLoginSucces</tt>.
	 * </p>
	 */
	public final void loginSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication successfulAuthentication) {
		if (!rememberMeRequested(request)) {
//			logger.debug("Remember-me login not requested.");
			return;
		}

		onLoginSuccess(request, response, successfulAuthentication);
	}

}
