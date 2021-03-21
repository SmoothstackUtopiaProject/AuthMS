package com.utopia.auth.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utopia.auth.exceptions.ExpiredTokenExpception;
import com.utopia.auth.exceptions.TokenNotFoundExpection;
import com.utopia.auth.models.UserToken;
import com.utopia.auth.repositories.UserTokenRepository;

@Service
public class UserTokenService {
	
	@Autowired
	UserTokenRepository userTokenRepository;
	
	public boolean verifyIfTokenBeenIssuedin15Min(Integer userId, Date before) {
		List<UserToken> userTokes = userTokenRepository.findTokenIssuedInLast15Min(userId, before);
		if(userTokes.isEmpty()) {
			return true;
		} else return false; 
	}
	
	// verify if token is exists and if not expired. 
	public UserToken verifyToken(String tokenId) throws ExpiredTokenExpception, TokenNotFoundExpection {
		
		UserToken uToken = null;
		Optional<UserToken> userToken = userTokenRepository.findById(tokenId);
		if(userToken.isPresent()) {
			uToken = userToken.get();
		}
		if(!userToken.isPresent()) {
			throw new TokenNotFoundExpection("Unexpected error occured");
		}
		//date when token was issued
		Date tokenDate = uToken.getDate();
		// +15 minutes to determine id token is expired 
		tokenDate = DateUtils.addMinutes(tokenDate, 15);
		// current dateTime
		Date dateNow = new Date();
		// if current date is after token's expiration date 
		if(dateNow.after(tokenDate)) {
			throw new ExpiredTokenExpception("Link is expired");
		}
		
		return uToken;	
	}
	
	public void delete(String tokenId) {
		userTokenRepository.deleteById(tokenId);
	}	
}
