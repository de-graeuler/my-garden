<?php

namespace Graeuler\Garden\Collect;

use PHPUnit\Framework\TestCase;

class ApiTokenTest extends TestCase
{
    private $apiToken;
    private $salt  = '123456';
    private $goodtoken = 'unit-test-token';
    private $badtoken  = 'bad-test-token';
    
    
    public function setUp() {
        $this->apiToken = new ApiToken();
        $this->goodhash = sha1($this->goodtoken . $this->salt).':'.$this->salt;
        $this->badhash  = sha1($this->badtoken . $this->salt).':'.$this->salt;
    }
    
    
    public function testCheckTokenGoodHash() {
        $validTokens = array ('sample-token', 'another-sample-token', 'unit-test-token', 'yet-another-token');
        $this->assertTrue($this->apiToken->checkToken($this->goodhash, $validTokens));
    }
    
    /**
     * @expectedException Graeuler\Garden\Collect\InvalidTokenException
     **/ 
    public function testCheckTokenBadHash() {
        $validTokens = array ('sample-token', 'another-sample-token', 'unit-test-token', 'yet-another-token');
        $failed = $this->apiToken->checkToken($this->badhash, $validTokens); // should not return true in this case.
        $this->assertFalse($failed, "checkToken should throw an exception in this case."); // should not be reached.
    }
    
    /**
     * @expectedException Graeuler\Garden\Collect\InvalidTokenException
     **/ 
    public function testCheckTokenNotInList() {
        $invalidTokens = array ('sample-token', 'another-sample-token', 'yet-another-token');
        $failed = $this->apiToken->checkToken($this->goodhash, $invalidTokens); // should not return true in this case.
        $this->assertFalse($failed, "checkToken should throw an exception in this case."); // should not be reached.
    }
        
        

    public function testCheckJsonData() {
        $validTokens = array ('sample-token', 'another-sample-token', 'unit-test-token', 'yet-another-token');
        $this->assertTrue($this->apiToken->checkJsonData(array("api-token"=>$this->goodhash), $validTokens ));
    }
    
    public function checkJsonData($jsonData, $validTokens) {
        if ( ! isset ($jsonData["api-token"] ) ) throw new InvalidTokenException("api-token key in POST data is missing.");
        $this->checkToken($jsonData["api-token"], $validTokens);
    }    
}
