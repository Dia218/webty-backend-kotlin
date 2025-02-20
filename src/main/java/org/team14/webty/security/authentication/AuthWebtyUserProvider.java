// package org.team14.webty.security.authentication;
//
// import org.springframework.stereotype.Component;
// import org.team14.webty.common.exception.BusinessException;
// import org.team14.webty.common.exception.ErrorCode;
// import org.team14.webty.user.entity.WebtyUser;
//
// @Component
// public class AuthWebtyUserProvider {
// 	public WebtyUser getAuthenticatedWebtyUser(WebtyUserDetails webtyUserDetails) {
// 		if (webtyUserDetails == null) {
// 			throw new BusinessException(ErrorCode.USER_LOGIN_REQUIRED);
// 		}
// 		WebtyUser webtyUser = webtyUserDetails.getWebtyUser();
// 		if (webtyUser == null) {
// 			throw new BusinessException(ErrorCode.USER_NOT_FOUND);
// 		}
// 		return webtyUser;
// 	}
// }
