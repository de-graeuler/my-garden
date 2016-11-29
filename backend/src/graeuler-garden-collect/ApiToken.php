<?php

namespace Graeuler\Garden\Collect;

class ApiToken 
{
    public function check($jsonData, $validTokens) {
        if ( ! isset ($jsonData["api-token"] ) ) throw new InvalidTokenException("api-token key in POST data is missing.");
        list($hash, $salt) = explode(':',$jsonData["api-token"]);
        foreach($validTokens as $token) {
            $checkToken = sha1($token.$salt);
            if ($hash === $checkToken) return;
        }
        throw new InvalidTokenException("Invalid token submitted.");
    }    
}
