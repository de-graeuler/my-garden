<?php

namespace Graeuler\Garden\Collect;

class ApiToken 
{
    public function checkToken($saltedTokenHash, $validTokens) {
        list($hash, $salt) = explode(':',$saltedTokenHash);
        foreach($validTokens as $token) {
            $check= sha1($token.$salt);
            if ($hash === $check) {
                return true;
            }
        }
        throw new InvalidTokenException("Invalid token submitted: $saltedTokenHash. ". print_r($validTokens, true));
    }
    
    public function checkJsonData($jsonData, $validTokens) {
        if ( ! isset ($jsonData["api-token"] ) ) {
            throw new InvalidTokenException("api-token key in POST data is missing.");
        }
        return $this->checkToken($jsonData["api-token"], $validTokens);
    }    
}
