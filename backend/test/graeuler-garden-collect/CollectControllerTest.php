<?php

namespace Graeuler\Garden\Collect;

use Slim\Http\Request;
use Slim\Http\Response;
use Interop\Container\ContainerInterface;

use PHPUnit\Framework\TestCase;

class CollectControllerTest extends TestCase
{
    
    protected $apiToken;
    protected $dataProcessor;
    protected $dataStore;
    protected $container;
    
    protected function setUp() {
        // mock the Controller Depencendy Injection
        $this->dataProcessor = $this->createMock(UplinkDataProcessor::class);
        $this->apiToken = $this->createMock(ApiToken::class);
        $this->dataStore = $this->createMock(DataFeedDataStore::class);
        
        $this->container = $this->createMock(ContainerInterface::class);
        $this->container->method('get')->will($this->returnValueMap([
            ['uplinkDataProcessor', $this->dataProcessor],
            ['apiToken', $this->apiToken],
            ['dataStore', $this->dataStore]
            ]));
            
        $this->request = $this->createMock(Request::class);
        $this->request->method('getHeaderLine')->will($this->returnValue(''));

        $this->response = $this->createMock(Response::class);
    }
    
    public function testCollectSuccess() {
        $this->response->method('withJson')->will($this->returnArgument(0));
        $this->request->method('getParsedBody')->willReturn(array());

        $this->apiToken->method('checkJsonData')->willReturn(true);
        $this->dataStore->method('selectTokenBySource')->willReturn(array());
        $this->dataStore->method('storeDataSet')->willReturn(true);
        $this->dataProcessor->method('buildDataSet')->willReturn(new GardenDataSet());

        $c = new CollectController($this->container);
        $response = $c->collect($this->request, $this->response, ['source' => 'unittest']);

        $this->assertTrue($response->success, "Success is expected to be true."); 
    }

    public function testCollectEmptyBody() {
        $this->response->method('withJson')->will($this->returnArgument(0));
        $this->request->method('getParsedBody')->willReturn("  ");

        $this->apiToken->method('checkJsonData')->willReturn(true);
        $this->dataStore->method('selectTokenBySource')->willReturn(array());
        $this->dataStore->method('storeDataSet')->willReturn(true);
        $this->dataProcessor->method('buildDataSet')->willReturn(new GardenDataSet());
        
        $c = new CollectController($this->container);
        $response = $c->collect($this->request, $this->response, ['source' => 'unittest']);
        
        $this->assertFalse($response->success, "Success is expected to be false."); 
    }        
    
    public function testCollectInvalidData() {
        $this->response->method('withJson')->will($this->returnArgument(0));
        $this->response->expects($this->once())->method('withJson')->with($this->anything(),$this->equalTo(400), $this->anything());
        $this->request->method('getParsedBody')->willReturn(array());

        $this->apiToken->method('checkJsonData')->willReturn(true);
        $this->dataStore->method('selectTokenBySource')->willReturn(array());
        $this->dataStore->method('storeDataSet')->willReturn(true);
        $this->dataProcessor->method('buildDataSet')->will($this->throwException(
                new InvalidDataException("Invalid Data!")));

        $c = new CollectController($this->container);
        $response = $c->collect($this->request, $this->response, ['source' => 'unittest']);
        
        $this->assertFalse($response->success, "Success is required to be false."); 
        $this->assertFalse(empty($response->message), "Message should inform about invalid token.");
    }
    
    public function testCollectInvalidToken() {
        $this->response->method('withJson')->will($this->returnArgument(0));
        $this->response->expects($this->once())->method('withJson')->with($this->anything(),$this->equalTo(403), $this->anything());
        $this->request->method('getParsedBody')->willReturn(array());

        $this->apiToken->method('checkJsonData')->will($this->throwException(
                new InvalidTokenException("Invalid Token!")));
        $this->dataStore->method('selectTokenBySource')->willReturn(array());
        $this->dataStore->method('storeDataSet')->willReturn(true);
        $this->dataProcessor->method('buildDataSet')->willReturn(new GardenDataSet());


        $c = new CollectController($this->container);
        $response = $c->collect($this->request, $this->response, ['source' => 'unittest']);
        
        $this->assertFalse($response->success, "Success is required to be false.");
        $this->assertFalse(empty($response->message), "Message should inform about invalid token.");
    }
    
    public function testCollectPdoError() {
        $this->response->method('withJson')->will($this->returnArgument(0));
        $this->request->method('getParsedBody')->willReturn(array());

        $this->apiToken->method('checkJsonData')->willReturn(true);
        $this->dataStore->method('selectTokenBySource')->willReturn(array());
        $this->dataStore->method('storeDataSet')->will($this->throwException(
                new \PDOException("Bang.")));
        $this->dataProcessor->method('buildDataSet')->willReturn(new GardenDataSet());

        $c = new CollectController($this->container);
        $response = $c->collect($this->request, $this->response, ['source' => 'unittest']);
        
        $this->assertFalse($response->success, "Success is required to be false.");
        $this->assertFalse(empty($response->message), "Message should inform about invalid token.");
    }
}